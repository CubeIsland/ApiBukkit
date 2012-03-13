package de.codeinfection.quickwango.Abstraction;

import java.io.File;

/**
 *
 * @author CodeInfection
 */
public class Abstraction
{
    private static Implementation implementation = null;

    public static void initialize(Implementation impl)
    {
        if (implementation == null && impl != null)
        {
            implementation = impl;
        }
    }

    public static String getImplementationName()
    {
        return implementation.getImplementationName();
    }

    public static Server getServer()
    {
        return implementation.getServer();
    }

    public static PluginManager getPluginManager()
    {
        return implementation.getPluginManager();
    }

    public static Scheduler getScheduler()
    {
        return implementation.getScheduler();
    }

    public static Configuration loadConfiguration(File file)
    {
        return implementation.loadConfiguration(file);
    }
}
