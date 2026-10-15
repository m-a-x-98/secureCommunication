import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.Types;
import java.time.LocalDateTime;

import com.securechat.dbconnection.ConnectionProvider;
import com.securechat.dbconnection.SingleConnectionProvider;
import com.securechat.serverhelpers.MessageCodec;
import com.securechat.serverhelpers.UserMessage;

public class MessageDB{
    private ConnectionProvider dbProvider;
    private final String url;
    private final String user;
    private final String password;

    public MessageDB(String configPath) throws IOException {
        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream(configPath)) {
            config.load(in);
        }
        this.url = config.getProperty("db.url");
        this.user = config.getProperty("db.user");
        this.password = config.getProperty("db.password");

        try {
            dbProvider = new SingleConnectionProvider(url, user, password);
        } catch (Exception e) {
            throw new IOException("Critical error, could not connect to database! Check the config.", e);
        }
    }

    public List<UserMessage> getNMessages(String username, int count){
        String sql = "SELECT * FROM messages WHERE usrname = ? OR recipient = ? " +
                     "ORDER BY send_at DESC, id DESC LIMIT ?";
        List<UserMessage> messages = new ArrayList<UserMessage>();

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, count);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()){
                    messages.add(new UserMessage(result.getBytes("msg"),
                                                 result.getLong("pad_start_offset"),
                                                 result.getString("usrname"),
                                                 result.getString("recipient"),
                                                 MessageCodec.fromUtc(result.getObject("send_at", LocalDateTime.class))));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get messages", e); // or your AuthServiceException
        }
        return messages;
    }
    public List<UserMessage> getMessagesTime(String username, Timestamp startDate, Timestamp endDate, int limit){
        String sql = "SELECT * FROM messages WHERE (usrname = ? OR recipient = ?) " +
             "AND send_at >= ? AND send_at <= ? ORDER BY send_at DESC, id DESC" +
             (limit != 0 ? " LIMIT ?" : "");
        List<UserMessage> messages = new ArrayList<UserMessage>();

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setObject(3, MessageCodec.toUtc(startDate));
            stmt.setObject(4, MessageCodec.toUtc(endDate));
            if (limit != 0) stmt.setInt(5, limit);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()){
                    messages.add(new UserMessage(result.getBytes("msg"),
                                                 result.getLong("pad_start_offset"),
                                                 result.getString("usrname"),
                                                 result.getString("recipient"),
                                                 MessageCodec.fromUtc(result.getObject("send_at", LocalDateTime.class))));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get time messages", e); // or your AuthServiceException
        }
        return messages;
    }

    public void addMessage(UserMessage msg){
        String sql = "INSERT INTO messages (msg, pad_start_offset, usrname, recipient, send_at) " +
                     "VALUES (?, ?, ?, ?, COALESCE(?, now() AT TIME ZONE 'utc'))";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setBytes(1, msg.message);
            stmt.setLong(2, msg.padStartOffset);
            stmt.setString(3, msg.user);
            stmt.setString(4, msg.recipient);
            stmt.setObject(5, msg.time == null ? null : MessageCodec.toUtc(msg.time), Types.TIMESTAMP);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add message", e);
        }
    }
}
