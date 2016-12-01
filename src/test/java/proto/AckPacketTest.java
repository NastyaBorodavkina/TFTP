package proto;

import org.junit.Test;

import java.net.DatagramPacket;

import static org.junit.Assert.*;

// This test do rely on PacketParser correctness.
// Should be analyzed after running PacketParserTest.
public class AckPacketTest {
    @Test
    public void serialize() throws Exception {
        AckPacket packet = new AckPacket(12345);
        byte[] buffer = packet.serialize();

        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(buffer);

        assertTrue(parsedPacket instanceof AckPacket);
        AckPacket ack = (AckPacket) parsedPacket;

        assertEquals(ack.getBlockNumber(), 12345);
    }
}