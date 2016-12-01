package proto;

import java.io.OutputStreamWriter;
import java.net.DatagramPacket;

public abstract class Packet {
    public abstract byte[] serialize();
}


