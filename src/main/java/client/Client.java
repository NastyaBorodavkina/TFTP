package client;

import proto.*;
import server.state.TransferMode;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

public class Client {
    private Logger LOGGER = Logger.getGlobal();
    private InetAddress addr;
    private int port;
    private String login;
    private String passwd;
    private DatagramSocket sock;

    public Client(InetAddress addr, int port, String login, String passwd) throws SocketException {
        this.addr = addr;
        this.login = login;
        this.passwd = passwd;
        this.port = port;

        sock = new DatagramSocket();
    }

    public long writeFile(String localFilename, String remoteFilemname) throws IOException {
        byte[] recvBuffer = new byte[516];
        byte[] block = new byte[512];
        byte[] buffer;

        RandomAccessFile file = new RandomAccessFile(localFilename, "r");

        DatagramPacket outDatagram;
        DatagramPacket inDatagram = new DatagramPacket(recvBuffer, recvBuffer.length);

        PacketParser parser = new PacketParser();
        Packet packet;

        // Sending write request
        WriteRequestPacket writeRequest = new WriteRequestPacket(remoteFilemname, TransferMode.OCTET, login, passwd);
        buffer = writeRequest.serialize();
        outDatagram = new DatagramPacket(buffer, buffer.length, addr, port);
        sock.send(outDatagram);

        LOGGER.info("Sending file " + localFilename);
        LOGGER.finest("--> " + writeRequest.toString());

        long transferSize = 0;
        int lastBlock = 0;
        boolean done = false;
        while(true) {
            // Waiting for answer
            sock.receive(inDatagram);
            packet = parser.parse(inDatagram);

            // Check packet type
            if(packet instanceof ErrorPacket){
                throw new RuntimeException(((ErrorPacket) packet).getErrorMessage());
            } else if(!(packet instanceof AckPacket)) {
                throw new RuntimeException("Unexpected packet " + packet.toString());
            }

            AckPacket ackPacket = (AckPacket) packet;
            LOGGER.finest("<-- " + ackPacket.toString());
            int ackBlockNum = ackPacket.getBlockNumber();

            if(lastBlock != ackBlockNum) {
                throw new RuntimeException("Unexpected ack " + ackPacket.toString());
            }

            // Breaking here because we must read last ACK from socket.
            if(done) {
                break;
            }

            file.seek(0);
            file.seek((lastBlock++) * 512);
            int blockLen = file.read(block);

            // If file size is multiplication of block size, last read will fail with -1 return code
            if(blockLen < 0) blockLen = 0;
            transferSize += blockLen;

            DataPacket dataPacket = new DataPacket(lastBlock, block, blockLen);
            buffer = dataPacket.serialize();
            outDatagram = new DatagramPacket(buffer, buffer.length, addr, port);
            sock.send(outDatagram);

            LOGGER.finest("--> " + dataPacket.toString());

            if(blockLen < 512) {
                done = true;
            }
        }

        return transferSize;
    }

    public long readFile(String remoteFilemname, String localFilename) throws IOException {
        byte[] recvBuffer = new byte[516];
        byte[] buffer;
        RandomAccessFile file = new RandomAccessFile(localFilename, "rw");

        DatagramPacket outDatagram;
        DatagramPacket inDatagram = new DatagramPacket(recvBuffer, recvBuffer.length);

        PacketParser parser = new PacketParser();
        Packet packet;

        // Sending write request
        ReadRequestPacket readRequest = new ReadRequestPacket(remoteFilemname, TransferMode.OCTET, login, passwd);
        buffer = readRequest.serialize();
        outDatagram = new DatagramPacket(buffer, buffer.length, addr, port);
        sock.send(outDatagram);

        LOGGER.info("Receiving file " + remoteFilemname);
        LOGGER.finest("--> " + readRequest.toString());

        long transferSize = 0;
        while(true) {
            // Waiting for answer
            sock.receive(inDatagram);
            packet = parser.parse(inDatagram);

            // Check packet type
            if(packet instanceof ErrorPacket){
                throw new RuntimeException(((ErrorPacket) packet).getErrorMessage());
            } else if(!(packet instanceof DataPacket)) {
                throw new RuntimeException("Unexpected packet " + packet.toString());
            }

            DataPacket dataPacket = (DataPacket) packet;

            LOGGER.finest("<-- " + dataPacket.toString());

            int blockNum = dataPacket.getBlockNumber();
            byte[] data = dataPacket.getData();

            transferSize += data.length;

            file.seek(0);
            file.seek((blockNum-1) * 512);
            file.write(data);

            AckPacket ackPacket = new AckPacket(blockNum);
            buffer = ackPacket.serialize();
            outDatagram = new DatagramPacket(buffer, buffer.length, addr, port);
            sock.send(outDatagram);

            LOGGER.finest("--> " + ackPacket.toString());

            if(data.length < 512) {
                break;
            }
        }

        return transferSize;
    }
}
