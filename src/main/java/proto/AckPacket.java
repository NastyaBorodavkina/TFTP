package proto;

import java.io.OutputStreamWriter;
import java.net.DatagramPacket;

public class AckPacket extends Packet {
    private int blockNumber;

    public AckPacket(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    @Override
    public byte[] serialize() {
        byte[] buffer = new byte[4];
        buffer[1] = (byte) 4;

        buffer[2] = (byte) ((blockNumber >>> 8) & 0xFF);
        buffer[3] = (byte) (blockNumber & 0xFF);

        return buffer;
    }

    @Override
    public String toString() {
        return "AckPacket { blockNumber: " + blockNumber + " }";
    }
}
