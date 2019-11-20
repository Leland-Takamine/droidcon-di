package di;

@Scope
public abstract class MainScope {

    abstract DashboardController getDashboardController();

    DashboardController createDashboardController(NetworkClient networkClient) {
        return new DashboardController(networkClient);
    }

    NetworkClient createNetworkClient() {
        return new NetworkClient();
    }
}
