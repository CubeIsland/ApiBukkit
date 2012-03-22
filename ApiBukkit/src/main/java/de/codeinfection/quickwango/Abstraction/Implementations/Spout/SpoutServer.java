package de.codeinfection.quickwango.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.CommandExecutor;
import de.codeinfection.quickwango.Abstraction.GameMode;
import de.codeinfection.quickwango.Abstraction.Player;
import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.Abstraction.PluginManager;
import de.codeinfection.quickwango.Abstraction.Scheduler;
import de.codeinfection.quickwango.Abstraction.World;
import java.io.File;
import java.util.Set;
import org.spout.api.Game;
import org.spout.api.Server;

/**
 *
 * @author CodeInfection
 */
public class SpoutServer implements de.codeinfection.quickwango.Abstraction.Server
{
    private final Server server;
    private final PluginManager pm;

    public SpoutServer(Server server)
    {
        this.server = server;
        this.pm = new SpoutPluginManager(this.server.getPluginManager());
    }

    public SpoutServer(Game game)
    {
        this((Server)game);
    }

    public void registerCommand(Plugin plugin, String name, CommandExecutor commandExecutor)
    {
        this.server.getRootCommand().addSubCommand(((SpoutPlugin)plugin).getHandle(), name).setExecutor(new SpoutCommandExecutor(commandExecutor));
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
        return new SpoutScheduler(this.server.getScheduler());
    }

    public String getName()
    {
        return this.server.getName();
    }

    public String getIp()
    {
        final String address = this.server.getAddress();
        return address.substring(0, address.indexOf(":"));
    }

    public int getPort()
    {
        final String address = this.server.getAddress();
        return Integer.parseInt(address.substring(address.indexOf(":") + 1));
    }

    public Player[] getOnlinePlayer()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxPlayers()
    {
        return this.server.getMaxPlayers();
    }

    public World getWorlds()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Plugin> getPlugins()
    {
        return this.pm.getPlugins();
    }

    public boolean getOnlineMode()
    {
        return true;
    }

    public boolean isWhitelisted()
    {
        return this.server.isWhitelist();
    }

    public int getSpawnRadius()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getViewDistance()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GameMode getDefaultGameMode()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEndAllwed()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isNetherAllowed()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFlyingAllowed()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getWorldContainer()
    {
        return this.server.getWorldFolder();
    }
}
