package server;

import proto.*;
import server.state.ClientState;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class Server {
    private final static Logger LOGGER = Logger.getGlobal();
    private int port;
    private byte[] readBuffer = new byte[512];
    private byte[] recvBuffer = new byte[516];
    private byte[] sendBuffer;

    DatagramPacket incoming = new DatagramPacket(recvBuffer, recvBuffer.length);
    DatagramPacket outgoing = new DatagramPacket(recvBuffer, recvBuffer.length);

    private ClientDB clients;
    private Map<SocketAddress, ClientState> states = new HashMap<>();

    public Server(int port, ClientDB clients) {
        this.clients = clients;
        this.port = port;
    }

    public void listen() throws IOException {
        LOGGER.info("Listening on port " + port);

        DatagramSocket sock = new DatagramSocket(port);
        PacketParser packetParser = new PacketParser();

        while (true) {
            sock.receive(incoming);

            byte[] inData = incoming.getData();
            String inAddr = incoming.getAddress().getHostAddress();
            int    inPort = incoming.getPort();

            Packet packet = packetParser.parse(incoming);

            LOGGER.finest("Packet from " + inAddr + ":" + inPort + ": " + packet.toString());

            ClientState state = states.getOrDefault(incoming.getSocketAddress(), new ClientState());
            if(!states.containsKey(incoming.getSocketAddress())) {
                states.put(incoming.getSocketAddress(), state);
            }

            sendBuffer = null;
            handleClient(state, packet);

            if(sendBuffer != null) {
                outgoing.setData(sendBuffer);
                outgoing.setAddress(incoming.getAddress());
                outgoing.setPort(incoming.getPort());
                sock.send(outgoing);
            }
        }
    }

    private void handleClient(ClientState state, Packet packet) {
        if(packet instanceof ReadRequestPacket) {
            handleReadRequest(state, (ReadRequestPacket) packet);
        } else if(packet instanceof WriteRequestPacket) {
            handleWriteRequest(state, (WriteRequestPacket) packet);
        } else if(packet instanceof DataPacket) {
            handleData(state, (DataPacket) packet);
        } else if(packet instanceof AckPacket) {
            handleAck(state, (AckPacket) packet);
        } else {
            throw new NotImplementedException();
        }
    }

    private void handleReadRequest(ClientState state, ReadRequestPacket packet) {
        if(!authorize(packet.getLogin(), packet.getPasswd())) {
            return;
        }

        LOGGER.info("Read request: " + packet.toString());

        state.filename = packet.getFilename();

        // Check file existence
        Path path = Paths.get(state.filename);
        if(!Files.exists(path)) {
            ErrorPacket errorPacket = new ErrorPacket(1, "File not found");
            sendBuffer = errorPacket.serialize();
            return;
        }

        if(Files.isDirectory(path)) {
            ErrorPacket errorPacket = new ErrorPacket(0, state.filename + " is a directory");
            sendBuffer = errorPacket.serialize();
            return;
        }

        try {
            state.file = new RandomAccessFile(state.filename, "r");
        } catch (FileNotFoundException e) {
            ErrorPacket errorPacket = new ErrorPacket(0, e.getMessage());
            sendBuffer = errorPacket.serialize();
            return;
        }

        sendBlock(state, 1);
    }

    private void handleWriteRequest(ClientState state, WriteRequestPacket packet) {
        if(!authorize(packet.getLogin(), packet.getPasswd())) {
            return;
        }

        LOGGER.info("Write request: " + packet.toString());

        state.filename = packet.getFilename();

        try {
            state.file = new RandomAccessFile(state.filename, "rw");
        } catch (FileNotFoundException e) {
            ErrorPacket errorPacket = new ErrorPacket(0, e.getMessage());
            sendBuffer = errorPacket.serialize();
            return;
        }

        state.state = ClientState.State.WaitingData;
        AckPacket ack = new AckPacket(0);
        sendBuffer = ack.serialize();
    }

    private boolean authorize(String login, String passwd) {
        boolean authorized = true;
        if(clients != null) {
            if(login == null || passwd == null) {
                authorized = false;
            } else {
                authorized = clients.isAuthorized(login, passwd);
            }
        }

        if(!authorized) {
            ErrorPacket errorPacket = new ErrorPacket(7, "No such user");
            sendBuffer = errorPacket.serialize();
        }

        return authorized;
    }

    private void handleAck(ClientState state, AckPacket packet) {
        if(state.state != ClientState.State.WaitingAck) {
            ErrorPacket errorPacket = new ErrorPacket(4, "Illegal TFTP operation");
            sendBuffer = errorPacket.serialize();
            return;
        }

        if(state.lastBlock != packet.getBlockNumber()) {
            ErrorPacket errorPacket = new ErrorPacket(4, "Wrong ack block. Expected " + state.lastBlock + ", got: " + packet.getBlockNumber());
            sendBuffer = errorPacket.serialize();
            return;
        }

        if(!state.isLast) {
            sendBlock(state, state.lastBlock + 1);
        } else {
            state.state = ClientState.State.None;
            state.closeConnection = true;
        }
    }

    private void handleData(ClientState state, DataPacket packet) {
        if(state.state != ClientState.State.WaitingData) {
            ErrorPacket errorPacket = new ErrorPacket(4, "Illegal TFTP operation");
            sendBuffer = errorPacket.serialize();
            return;
        }

        try {
            writeBlock(state, packet.getBlockNumber(), packet.getData());
        } catch (IOException e) {
            ErrorPacket errorPacket = new ErrorPacket(0, e.getMessage());
            sendBuffer = errorPacket.serialize();
            return;
        }

        if(packet.getData().length < 512) {
            state.isLast = true;
            state.state = ClientState.State.None;
        }


        AckPacket ack = new AckPacket(packet.getBlockNumber());
        sendBuffer = ack.serialize();
    }

    private void sendBlock(ClientState state, int blockNumber) {
        int bytesRead = 0;

        try {
            state.file.seek((blockNumber - 1) * 512);
            bytesRead = state.file.read(readBuffer);
        } catch (IOException e) {
            ErrorPacket errorPacket = new ErrorPacket(0, e.getMessage());
            sendBuffer = errorPacket.serialize();

        }

        DataPacket packet = new DataPacket(blockNumber, readBuffer, bytesRead);
        sendBuffer = packet.serialize();

        if(bytesRead < 512) {
            state.isLast = true;
        } else {
            state.isLast = false;
        }

        state.lastBlock = blockNumber;
        state.state = ClientState.State.WaitingAck;
    }

    private void writeBlock(ClientState state, int blockNumber, byte[] block) throws IOException {
        state.file.seek((blockNumber - 1) * 512);
        state.file.write(block);
    }

}
