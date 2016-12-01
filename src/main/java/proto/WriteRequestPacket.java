package proto;

import server.state.TransferMode;


public class WriteRequestPacket extends RequestPacket {
    public WriteRequestPacket(String filename, TransferMode mode) {
        super(Type.Write, filename, mode);
    }

    public WriteRequestPacket(String filename, TransferMode mode, String login, String passwd) {
        super(Type.Write, filename, mode, login, passwd);
    }

    public WriteRequestPacket(RequestPacket requestPacket) {
        super(requestPacket);
    }
}
