package server.state;

import java.io.RandomAccessFile;

public class ClientState {
    public enum State {
        None,
        WaitingData,
        WaitingAck,
    }

    public State state;
    public String filename;
    public RandomAccessFile file;
    public int lastBlock;
    public boolean isLast;
    public TransferMode mode;
    public boolean closeConnection;
}
