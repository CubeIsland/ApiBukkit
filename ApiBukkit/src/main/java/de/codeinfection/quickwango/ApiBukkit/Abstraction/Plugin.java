package de.codeinfection.quickwango.ApiBukkit.Abstraction;

import java.io.File;

/**
 *
 * @author CodeInfection
 */
public interface Plugin
{
    public String getName();
    public String getVersion();
    public File getDataFolder();
    public void enable();
    public void disable();
    public void relead();
    public Configration getConfig();
}
