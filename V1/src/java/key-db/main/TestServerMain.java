public class TestServerMain {
    public static void main(String[] args) throws Exception {
        KeysDB keysDB = new KeysDB("../../config.properties");
        if (!keysDB.userExists("testing")) {
            keysDB.createUser("testing", "pwd123");
        }
        KeyMaterialServer server = new KeyMaterialServer(keysDB, 5000);

        System.out.println("KeyMaterialServer starting on port 5000...");
        server.start(); // blocks forever, handling connections one at a time
    }
}