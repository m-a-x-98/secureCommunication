import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import com.securechat.dbconnection.ConnectionProvider;
import com.securechat.dbconnection.SingleConnectionProvider;

public class KeysDB implements AuthService{
    private ConnectionProvider dbProvider;

    private final String url;
    private final String user;
    private final String password;

    public KeysDB(String configPath) throws IOException {
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
            throw new IOException("Critical error, could not connect to database! Check the config.");
        }
    }

    public void createUser(String username, String password) throws UserAlreadyExistsException{
        String sql = "INSERT INTO keys (usrName, pwd_hash) VALUES (?, ?)";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));

            stmt.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new UserAlreadyExistsException(username);
            }
            throw new RuntimeException("Failed to create user", e); // or your AuthServiceException
        }
    }

    public boolean verifyLogin(String username, String password){
        String pwd_hash;
        String sql = "SELECT pwd_hash FROM keys WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    pwd_hash = result.getString("pwd_hash");
                } else { // no such user, verification failed
                    return false;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify user! ", e);
        }

        return passwordMatches(password, pwd_hash);
    }

    public boolean userExists(String username){
        String sql = "SELECT usrID FROM keys WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()){
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify user! ", e);
        }
    }

    public void deleteUser(String username) throws UserDoesntExistException{
        String sql = "DELETE FROM keys WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
             PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);

            int retMsg = stmt.executeUpdate();
            if (retMsg == 0) {
                throw new UserDoesntExistException(username);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e); // or your AuthServiceException
        }
    }

    public void storeEncryptedKeyMaterial(String username,  byte[] encryptedKey) throws UserDoesntExistException{
        String sql = "UPDATE keys SET key_material  = ? WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setBytes(1, encryptedKey);
            stmt.setString(2, username);

            int retMsg = stmt.executeUpdate();
            if (retMsg == 0) {
                throw new UserDoesntExistException(username);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e); // or your AuthServiceException
        }
    }
    public byte[] getEncryptedKeyMaterial(String username) throws UserDoesntExistException{
        String sql = "SELECT key_material FROM keys WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    byte[] key_chiper = result.getBytes("key_material");
                    return key_chiper;
                } else { // no such user, verification failed
                    throw new UserDoesntExistException(username);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify user! ", e);
        }
    }

    public void updatePadPosition(String username, long position) throws UserDoesntExistException{
        String sql = "UPDATE keys SET pad_position = ? WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setLong(1, position);
            stmt.setString(2, username);

            int retMsg = stmt.executeUpdate();
            if (retMsg == 0) {
                throw new UserDoesntExistException(username);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e); // or your AuthServiceException
        }
    }

    public long getPadState(String username) throws UserDoesntExistException{
        String sql = "SELECT pad_position FROM keys WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getLong("pad_position");
                } else { // no such user, verification failed
                    throw new UserDoesntExistException(username);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update pad state ", e);
        }
    }

    public void updatePadSize(String username, long size) throws UserDoesntExistException{
        String sql = "UPDATE keys SET pad_size = ? WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setLong(1, size);
            stmt.setString(2, username);

            int retMsg = stmt.executeUpdate();
            if (retMsg == 0) {
                throw new UserDoesntExistException(username);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update pad size ", e); // or your AuthServiceException
        }
    }

    public long getPadSize(String username) throws UserDoesntExistException{
        String sql = "SELECT pad_size FROM keys WHERE usrName = ?";

        try (Connection usrDB = dbProvider.getConnection();
            PreparedStatement stmt = usrDB.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getLong("pad_size");
                } else { // no such user, verification failed
                    throw new UserDoesntExistException(username);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify user! ", e);
        }
    }



    private String hashPassword(String password){
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        return encoder.encode(password);
    }
    private boolean passwordMatches(String password, String pwd_hash){
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        return encoder.matches(password, pwd_hash);
    }
}

class UserAlreadyExistsException extends Exception{
    public UserAlreadyExistsException() {}

    public UserAlreadyExistsException(String usr)
    {
        super("User already exists: " + usr);
    }
}

class UserDoesntExistException extends Exception{
    public UserDoesntExistException() {}

    public UserDoesntExistException(String usr)
    {
        super("User does not exist: " + usr);
    }
}