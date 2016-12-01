package proto;

import server.state.TransferMode;

public class ReadRequestPacket extends RequestPacket {
    public ReadRequestPacket(String filename, TransferMode mode) {
        super(Type.Read, filename, mode);
    }

    public ReadRequestPacket(String filename, TransferMode mode, String login, String passwd) {
        super(Type.Read, filename, mode, login, passwd);
    }

    public ReadRequestPacket(RequestPacket requestPacket) {
        super(requestPacket);
    }


}
