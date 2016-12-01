package proto;

import org.junit.Test;

import static org.junit.Assert.*;

// This test do rely on PacketParser correctness.
// Should be analyzed after running PacketParserTest.
public class ErrorPacketTest {
    @Test
    public void serialize() throws Exception {
        int errorNumber = 12345;
        String errorMsg = "Fatal error :C";
        ErrorPacket packet = new ErrorPacket(errorNumber, errorMsg);
        byte[] buffer = packet.serialize();

        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(buffer);

        assertTrue(parsedPacket instanceof ErrorPacket);
        ErrorPacket error = (ErrorPacket) parsedPacket;

        assertEquals(error.getErrorNumber(), errorNumber);
        assertEquals(error.getErrorMessage(), errorMsg);
    }

}