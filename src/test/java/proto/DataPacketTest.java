package proto;

import org.junit.Test;

import static org.junit.Assert.*;

// This test do rely on PacketParser correctness.
// Should be analyzed after running PacketParserTest.
public class DataPacketTest {
    @Test
    public void serialize() throws Exception {
        int blockNum = 12345;
        byte[] data = "Much data! Such tftp! Wow!".getBytes();

        DataPacket packet = new DataPacket(blockNum, data);
        byte[] buffer = packet.serialize();

        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(buffer);

        assertTrue(parsedPacket instanceof DataPacket);
        DataPacket ack = (DataPacket) parsedPacket;

        assertEquals(ack.getBlockNumber(), blockNum);

        byte[] packetData = packet.getData();
        for(int i = 0; i < data.length; i++) {
            assertEquals(data[i], packetData[i]);
        }
    }
}