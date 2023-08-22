package test.designe.app.terminalapp.sigeltons;

import test.designe.app.terminalapp.models.Terminal;

public class TerminalSingelton {

    private static Terminal terminal;



    public static Terminal getTerminal()
    {
        return terminal;

    }

    public static  void setTerminal(Terminal t)
    {
        terminal = t;
    }
}
