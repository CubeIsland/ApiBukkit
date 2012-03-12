package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.CommandExecutor;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.PluginManager;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Scheduler;
import org.spout.api.Game;
import org.spout.api.Server;

/**
 *
 * @author CodeInfection
 */
public class SpoutServer implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Server
{
    private final Server server;

    public SpoutServer(Server server)
    {
        this.server = server;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PluginManager getPluginManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Scheduler getScheduler()
    {
        return new SpoutScheduler(this.server.getScheduler());
    }
}
