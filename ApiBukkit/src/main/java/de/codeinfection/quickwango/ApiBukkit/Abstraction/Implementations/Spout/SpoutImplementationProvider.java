package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configuration;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.ImplementationProvider;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.PluginManager;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Server;
import java.io.File;
import org.spout.api.Spout;

/**
 *
 * @author CodeInfection
 */
public class SpoutImplementationProvider implements ImplementationProvider
{
    public String getImplementationName()
    {
        return "Spout";
    }

    public Server getServer()
    {
        return (Server)Spout.getGame();
    }

    public PluginManager getPluginManager()
    {
        return new SpoutPluginManager(Spout.getGame().getPluginManager());
    }

    public Configuration loadConfiguration(File file)
    {
        return new SpoutConfiguration(new org.spout.api.util.config.Configuration(file));
    }
}
