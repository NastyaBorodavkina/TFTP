package proto;

import org.junit.Test;
import server.state.TransferMode;

import static org.junit.Assert.*;

// This test do rely on PacketParser correctness.
// Should be analyzed after running PacketParserTest.
public class RequestPacketTest {
    @Test
    public void serialize() throws Exception {
        testWithData(RequestPacket.Type.Read, "test/file", TransferMode.OCTET, "login", "passwd");
        testWithData(RequestPacket.Type.Write, "test/file", TransferMode.OCTET, "login", "passwd");
        testWithData(RequestPacket.Type.Read, "test/file", TransferMode.NETASCII, "login", "passwd");
        testWithData(RequestPacket.Type.Write, "test/file", TransferMode.OCTET, null, null);
    }

    private void testWithData(RequestPacket.Type type, String filename, TransferMode mode, String login, String passwd) {
        RequestPacket packet = new RequestPacket(type, filename, mode, login, passwd);
        byte[] buffer = packet.serialize();

        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(buffer);

        assertTrue(parsedPacket instanceof RequestPacket);
        RequestPacket request = (RequestPacket) parsedPacket;

        assertEquals(request.getType(), type);
        assertEquals(request.getFilename(), filename);
        assertEquals(request.getMode(), mode);
        assertEquals(request.getLogin(), login);
        assertEquals(request.getPasswd(), passwd);
    }
}