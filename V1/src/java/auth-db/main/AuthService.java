
public interface AuthService {
    public void createUser(String username, String password) throws UserAlreadyExistsException;
    public boolean verifyLogin(String username, String password);
    public boolean userExists(String username);
    public void deleteUser(String username) throws UserDoesntExistException;
    public void storeEncryptedKeyMaterial(String username, byte[] encryptedKey) throws UserDoesntExistException;
    public byte[] getEncryptedKeyMaterial(String username) throws UserDoesntExistException;
    public void updatePadPosition(String username, long position) throws UserDoesntExistException;
    public long getPadState(String username) throws UserDoesntExistException;
    public void updatePadSize(String username, long size) throws UserDoesntExistException;
    public long getPadSize(String username) throws UserDoesntExistException;
}
