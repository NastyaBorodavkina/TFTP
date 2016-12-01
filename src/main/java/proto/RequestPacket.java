package proto;

import org.apache.commons.lang.ArrayUtils;
import server.state.TransferMode;
import sun.nio.cs.US_ASCII;

public class RequestPacket extends Packet {
    enum Type {
        Read,
        Write
    }

    private Type type;
    private String filename;
    private TransferMode mode;
    private String login;
    private String passwd;

    public RequestPacket(Type type, String filename, TransferMode mode, String login, String passwd) {
        this.type = type;
        this.filename = filename;
        this.mode = mode;
        this.login = login;
        this.passwd = passwd;
    }

    public RequestPacket(Type type, String filename, TransferMode mode) {
        this.type = type;
        this.filename = filename;
        this.mode = mode;
    }

    public RequestPacket(RequestPacket requestPacket) {
        this.type = requestPacket.getType();
        this.filename = requestPacket.getFilename();
        this.mode = requestPacket.getMode();
        this.login = requestPacket.getLogin();
        this.passwd = requestPacket.getPasswd();
    }

    public Type getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public TransferMode getMode() {
        return mode;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswd() {
        return passwd;
    }

    @Override
    public byte[] serialize() {
        int type = this.type.equals(Type.Read) ? 1 : 2;

        byte[] buffer = new byte[2];
        buffer[0] = (byte) ((type >>> 8) & 0xFF);
        buffer[1] = (byte) (type & 0xFF);

        buffer = ArrayUtils.addAll(buffer, filename.getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);
        buffer = ArrayUtils.addAll(buffer, mode.toString().toLowerCase().getBytes(US_ASCII.defaultCharset()));
        buffer = ArrayUtils.add(buffer, (byte) 0x0);

        if(login != null) {
            buffer = ArrayUtils.addAll(buffer, "login".getBytes(US_ASCII.defaultCharset()));
            buffer = ArrayUtils.add(buffer, (byte) 0x0);
            buffer = ArrayUtils.addAll(buffer, login.getBytes(US_ASCII.defaultCharset()));
            buffer = ArrayUtils.add(buffer, (byte) 0x0);
        }

        if(passwd != null) {
            buffer = ArrayUtils.addAll(buffer, "passwd".getBytes(US_ASCII.defaultCharset()));
            buffer = ArrayUtils.add(buffer, (byte) 0x0);
            buffer = ArrayUtils.addAll(buffer, passwd.getBytes(US_ASCII.defaultCharset()));
            buffer = ArrayUtils.add(buffer, (byte) 0x0);
        }

        return buffer;
    }

    @Override
    public String toString() {
        return "RequestPacket { type: " + type + ", filename: '" + filename + "', mode: " + mode + ", login:'" + login + "', passwd: '" + passwd + "' }";
    }
}
