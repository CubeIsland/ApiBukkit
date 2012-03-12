package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configuration;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementation;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.PluginManager;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Scheduler;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Server;
import java.io.File;
import org.spout.api.Spout;

/**
 *
 * @author CodeInfection
 */
public class SpoutImplementation implements Implementation
{
    private final Server server;
    private final PluginManager pm;
    private final Scheduler scheduler;

    public SpoutImplementation()
    {
        this.server = new SpoutServer((org.spout.api.Server)Spout.getGame());
        this.pm = this.server.getPluginManager();
        this.scheduler = this.server.getScheduler();
    }

    public String getImplementationName()
    {
        return "Spout";
    }

    public Server getServer()
    {
        return this.server;
    }

    public PluginManager getPluginManager()
    {
        return this.pm;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public Configuration loadConfiguration(File file)
    {
        return new SpoutConfiguration(new org.spout.api.util.config.Configuration(file));
    }
}
