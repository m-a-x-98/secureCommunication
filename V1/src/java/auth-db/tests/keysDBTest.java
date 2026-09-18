import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class keysDBTest {

    @Test
    void createExistVerifyDeleteUsr() throws Exception {
        KeysDB db = new KeysDB("../../../config.properties");

        // Creating a user for the first time should succeed
        assertDoesNotThrow(() -> db.createUser("A", "B"));
        // Now we should get a "user already exists" error
        assertThrowsExactly(UserAlreadyExistsException.class, () -> db.createUser("A", "B"));


        // Check that existing user exist
        assertEquals(db.userExists("A"), true);
        // Check that non-existing user doesn't exist
        assertEquals(db.userExists("aosdoefjoi"), false);


        // Check whether we can login as the user 
        assertEquals(db.verifyLogin("A", "B"), true);
        // Try to login with the wrong password 
        assertEquals(db.verifyLogin("A", "oskadij"), false);
        // Try to login with the wrong username 
        assertEquals(db.verifyLogin("sadokoajfs", "B"),false);
        // Try to login with both wrong password and username 
        assertEquals(db.verifyLogin("asdfeeda", "adfdgo"), false);


        // Check storeEncryptedKeyMaterial doesnt throw an error when user exist
        byte[] key = new byte[] {97, 98, 99};
        assertDoesNotThrow(() -> db.storeEncryptedKeyMaterial("A", key));
        // Check user doesnt exist throws correct error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.storeEncryptedKeyMaterial("odkeojf", key));

        // Ensure encrypted key is retrived correctly 
        byte[] retrivedKey = db.getEncryptedKeyMaterial("A");
        assertArrayEquals(key, retrivedKey);
        // Check user doesnt exist throws correct error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.getEncryptedKeyMaterial("odkeojf"));

        // Check updatePadPosition doesn't throw when user exists
        assertDoesNotThrow(() -> db.updatePadPosition("A", 5L));
        // Check user doesn't exist throws correct error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.updatePadPosition("odkeojf", 5L));

        // Ensure pad position is retrieved correctly
        assertEquals(db.getPadState("A"), 5L);
        // Check user doesn't exist throws correct error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.getPadState("odkeojf"));

        // Updating pad position again should overwrite the previous value
        assertDoesNotThrow(() -> db.updatePadPosition("A", 12L));
        assertEquals(db.getPadState("A"), 12L);


        // Check updatePadSize doesn't throw when user exists
        assertDoesNotThrow(() -> db.updatePadSize("A", 1024L));
        // Check user doesn't exist throws correct error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.updatePadSize("odkeojf", 1024L));

        // Ensure pad size is retrieved correctly
        assertEquals(db.getPadSize("A"), 1024L);
        // Check user doesn't exist throws correct error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.getPadSize("odkeojf"));

        // Updating pad size again should overwrite the previous value
        assertDoesNotThrow(() -> db.updatePadSize("A", 2048L));
        assertEquals(db.getPadSize("A"), 2048L);

        
        // Deleting an existing user should succeed
        assertDoesNotThrow(() -> db.deleteUser("A"));
        // Now we should get a "user doesn't exist" error
        assertThrowsExactly(UserDoesntExistException.class, () -> db.deleteUser("A"));
    }
}