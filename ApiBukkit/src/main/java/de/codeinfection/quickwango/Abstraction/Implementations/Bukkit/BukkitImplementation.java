package de.codeinfection.quickwango.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.Abstraction.Configuration;
import de.codeinfection.quickwango.Abstraction.Implementation;
import de.codeinfection.quickwango.Abstraction.PluginManager;
import de.codeinfection.quickwango.Abstraction.Scheduler;
import de.codeinfection.quickwango.Abstraction.Server;
import java.io.File;
import org.bukkit.Bukkit;

/**
 *
 * @author CodeInfection
 */
public class BukkitImplementation implements Implementation
{
    private final Server server;
    private final PluginManager pm;
    private final Scheduler scheduler;

    public BukkitImplementation()
    {
        this.server = new BukkitServer(Bukkit.getServer());
        this.pm = this.server.getPluginManager();
        this.scheduler = this.server.getScheduler();
    }

    public String getImplementationName()
    {
        return "Bukkit";
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
        return new BukkitConfigration(file);
    }
}
