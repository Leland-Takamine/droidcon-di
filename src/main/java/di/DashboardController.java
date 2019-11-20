package di;

public class DashboardController {

    private final NetworkClient networkClient;

    public DashboardController(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }
}
