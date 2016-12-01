package proto;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import server.state.TransferMode;
import sun.nio.cs.US_ASCII;

import static org.junit.Assert.*;


public class PacketParserTest {
    @Test
    public void parse() throws Exception {

    }

    @Test
    public void parsePacket() throws Exception {

    }

    @Test
    public void parseReadRequest() throws Exception {
        parseRWRequestPacket(1);
    }

    @Test
    public void parseWriteRequest() throws Exception {
        parseRWRequestPacket(2);
    }

    public void parseRWRequestPacket(int type) {
        String filename = "test";
        String mode = "octet";
        String login = "login";
        String passwd = "passwd";

        byte[] buffer = new byte[2];
        buffer[0] = (byte) ((type >> 8) & 0xFF);
        buffer[1] = (byte) (type & 0xFF);

        buffer = ArrayUtils.addAll(buffer, filename.getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);
        buffer = ArrayUtils.addAll(buffer, mode.getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);

        buffer = ArrayUtils.addAll(buffer, "login".getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);
        buffer = ArrayUtils.addAll(buffer, login.getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);

        buffer = ArrayUtils.addAll(buffer, "passwd".getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);
        buffer = ArrayUtils.addAll(buffer, passwd.getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);

        PacketParser parser = new PacketParser();

        Packet packet = parser.parse(buffer);

        assertTrue(packet instanceof RequestPacket);
        RequestPacket req = (RequestPacket) packet;

        if(type == 1) {
            assertEquals(req.getType(), RequestPacket.Type.Read);
        } else {
            assertEquals(req.getType(), RequestPacket.Type.Write);
        }

        assertEquals(req.getFilename(), filename);
        assertEquals(req.getMode(), TransferMode.valueOf(mode.toUpperCase()));
        assertEquals(req.getLogin(), login);
        assertEquals(req.getPasswd(), passwd);
    }

    @Test
    public void parseData() throws Exception {
        byte[] buffer = new byte[4];
        buffer[1] = (byte) 3;

        int blockNumber = 12345;
        buffer[2] = (byte) ((blockNumber >> 8) & 0xFF);
        buffer[3] = (byte) (blockNumber & 0xFF);

        byte[] data = new byte[512];
        for(int i = 0; i < 512; i++) {
            data[i] = (byte) (i & 0xFF);
        }

        buffer = ArrayUtils.addAll(buffer, data);

        PacketParser parser = new PacketParser();
        Packet packet = parser.parse(buffer);

        assertTrue(packet instanceof DataPacket);
        DataPacket dataPacket = (DataPacket) packet;

        assertEquals(dataPacket.getBlockNumber(), blockNumber);

        byte[] packetData = dataPacket.getData();
        for(int i = 0; i < 512; i++) {
            assertEquals(data[i], packetData[i]);
        }
    }

    @Test
    public void parseAck() throws Exception {
        byte[] buffer = new byte[4];
        buffer[1] = (byte) 4;

        int blockNumber = 12345;
        buffer[2] = (byte) ((blockNumber >> 8) & 0xFF);
        buffer[3] = (byte) (blockNumber & 0xFF);

        PacketParser parser = new PacketParser();
        Packet packet = parser.parse(buffer);

        assertTrue(packet instanceof AckPacket);
        AckPacket ack = (AckPacket) packet;

        assertEquals(ack.getBlockNumber(), blockNumber);
    }

    @Test
    public  void parseError() throws Exception {
        byte[] buffer = new byte[4];
        buffer[1] = (byte) 5;

        int errorNumber = 12345;
        buffer[2] = (byte) ((errorNumber >> 8) & 0xFF);
        buffer[3] = (byte) (errorNumber & 0xFF);

        String errorMsg = "test error";

        buffer = ArrayUtils.addAll(buffer,  errorMsg.getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);

        PacketParser parser = new PacketParser();
        Packet packet = parser.parse(buffer);

        assertTrue(packet instanceof ErrorPacket);
        ErrorPacket error = (ErrorPacket) packet;

        assertEquals(error.getErrorNumber(), errorNumber);
        assertEquals(error.getErrorMessage(), errorMsg);
    }
}