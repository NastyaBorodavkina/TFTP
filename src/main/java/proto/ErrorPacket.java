package proto;

import org.apache.commons.lang.ArrayUtils;
import sun.nio.cs.US_ASCII;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.OutputStreamWriter;
import java.net.DatagramPacket;

public class ErrorPacket extends Packet {
    private int errorNumber;
    private String errorMessage;

    public ErrorPacket(int errorNumber, String errorMessage) {
        this.errorNumber = errorNumber;
        this.errorMessage = errorMessage;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public byte[] serialize() {
        byte[] buffer = new byte[4];
        buffer[1] = (byte) 5;

        buffer[2] = (byte) ((errorNumber >>> 8) & 0xFF);
        buffer[3] = (byte) (errorNumber & 0xFF);

        buffer = ArrayUtils.addAll(buffer,  errorMessage.getBytes(US_ASCII.defaultCharset()));
        return ArrayUtils.add(buffer, (byte) 0x0);
    }

    @Override
    public String toString() {
        return "ErrorPacket { errorNumber: " + errorNumber + ", errorMessage: '" + errorMessage + "' }";
    }
}
