package di;

public class ManualMainScopeImpl extends MainScope {

    private DashboardController dashboardController;
    private NetworkClient networkClient;

    @Override
    DashboardController getDashboardController() {
        return dashboardController();
    }

    private DashboardController dashboardController() {
        if (dashboardController == null) {
            dashboardController = createDashboardController(networkClient());
        }
        return dashboardController;
    }

    private NetworkClient networkClient() {
        if (networkClient == null) {
            networkClient = createNetworkClient();
        }
        return networkClient;
    }
}
