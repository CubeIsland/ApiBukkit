package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.CommandExecutor;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.PluginManager;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Scheduler;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author CodeInfection
 */
class BukkitServer implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Server
{
    private Server server;
    private PluginManager pm;
    private Scheduler scheduler;

    public BukkitServer(Server server)
    {
        this.server = server;

        this.pm = new BukkitPluginManager(server.getPluginManager());
        this.scheduler = new BukkitScheduler(server.getScheduler());
    }

    public void registerCommand(Plugin plugin, String name, CommandExecutor commandExecutor)
    {
        org.bukkit.plugin.Plugin bukkitPlugin = ((BukkitPlugin)plugin).getHandle();
        if (bukkitPlugin instanceof JavaPlugin)
        {
            PluginCommand command = ((JavaPlugin)bukkitPlugin).getCommand(name);
            if (command != null)
            {
                command.setExecutor(new BukkitCommandExecutor(commandExecutor));
            }
        }
    }

    public String getVersion()
    {
        return this.server.getVersion();
    }

    public PluginManager getPluginManager()
    {
        return this.pm;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }
}
