package de.codeinfection.quickwango.ApiBukkit.Abstraction;

import java.io.File;

/**
 *
 * @author CodeInfection
 */
public interface ImplementationProvider
{
    public String getImplementationName();
    public Server getServer();
    public PluginManager getPluginManager();
    public Configuration loadConfiguration(File file);
}
