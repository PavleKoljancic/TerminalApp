package test.designe.app.terminalapp.sigeltons;

import test.designe.app.terminalapp.models.Driver;

public class DriverSingleton {

    private static Driver driver;

    public static void setDriver(Driver d) { driver= d;}
    public static Driver getDriver() { return  driver;}
}
