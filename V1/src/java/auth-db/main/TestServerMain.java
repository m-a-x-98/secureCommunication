public class TestServerMain {
    public static void main(String[] args) throws Exception {
        AuthService authService = new KeysDB("../../config.properties");
        if (!authService.userExists("testing")) {
            authService.createUser("testing", "pwd123");
        }
        KeyMaterialServer server = new KeyMaterialServer(authService, 5000);

        System.out.println("KeyMaterialServer starting on port 5000...");
        server.start(); // blocks forever, handling connections one at a time
    }
}