package di;

public class Main {

    private static MainScope scope = new MainScopeImpl();

    public static void main(String[] args) {
        System.out.println(scope.getDashboardController());
        System.out.println(scope.getDashboardController());
    }
}