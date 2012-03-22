package de.codeinfection.quickwango.Abstraction.Implementations.Bukkit;

import de.codeinfection.quickwango.Abstraction.CommandExecutor;
import de.codeinfection.quickwango.Abstraction.GameMode;
import de.codeinfection.quickwango.Abstraction.Player;
import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.Abstraction.PluginManager;
import de.codeinfection.quickwango.Abstraction.Scheduler;
import de.codeinfection.quickwango.Abstraction.World;
import java.io.File;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author CodeInfection
 */
public class BukkitServer implements de.codeinfection.quickwango.Abstraction.Server
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

    public Server getHandle()
    {
        return this.server;
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

    public String getName()
    {
        return this.server.getServerName();
    }

    public String getIp()
    {
        return this.server.getIp();
    }

    public int getPort()
    {
        return this.server.getPort();
    }

    public Player[] getOnlinePlayers()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxPlayers()
    {
        return this.server.getMaxPlayers();
    }

    public World[] getWorlds()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getOnlineMode()
    {
        return this.server.getOnlineMode();
    }

    public boolean isWhitelisted()
    {
        return this.server.hasWhitelist();
    }

    public int getSpawnRadius()
    {
        return this.server.getSpawnRadius();
    }

    public int getViewDistance()
    {
        return this.server.getViewDistance();
    }

    public GameMode getDefaultGameMode()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEndAllwed()
    {
        return this.server.getAllowEnd();
    }

    public boolean isNetherAllowed()
    {
        return this.server.getAllowNether();
    }

    public boolean isFlyingAllowed()
    {
        return this.server.getAllowFlight();
    }

    public File getWorldContainer()
    {
        return this.server.getWorldContainer();
    }

    public File getUpdateFolder()
    {
        return this.server.getUpdateFolderFile();
    }

    public void shutdown()
    {
        this.server.shutdown();
    }

    public void broadcast(String message)
    {
        this.server.broadcastMessage(message);
    }
}
