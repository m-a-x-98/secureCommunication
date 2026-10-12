import java.sql.Timestamp;

public class UserMessage {
    public final byte[] message;
    public final long padStartOffset;
    public final String user;
    public final String recipient;
    public final Timestamp time;
    public UserMessage(byte[] message, long padStartOffset, String user, String recipient, Timestamp time){
        this.message = message;
        this.padStartOffset = padStartOffset;
        this.user = user;
        this.recipient = recipient;
        this.time = time;
    }
}
