package de.codeinfection.quickwango.Abstraction;

import java.io.File;

/**
 *
 * @author CodeInfection
 */
public interface Implementation
{
    public String getImplementationName();

    public Server getServer();

    public PluginManager getPluginManager();

    public Scheduler getScheduler();

    public Configuration loadConfiguration(File file);
}
