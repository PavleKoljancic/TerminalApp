package test.designe.app.terminalapp.sigeltons;

import test.designe.app.terminalapp.models.Route;
import test.designe.app.terminalapp.models.Terminal;

public class RouteSingelton {

    private static Route r;



    public static Route getRoute()
    {
        return r;

    }

    public static  void setRoute(Route r1)
    {
        r = r1;
    }
}
