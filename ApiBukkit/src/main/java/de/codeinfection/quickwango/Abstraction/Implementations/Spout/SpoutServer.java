package de.codeinfection.quickwango.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.Abstraction.CommandExecutor;
import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.Abstraction.PluginManager;
import de.codeinfection.quickwango.Abstraction.Scheduler;
import org.spout.api.Game;
import org.spout.api.Server;

/**
 *
 * @author CodeInfection
 */
public class SpoutServer implements de.codeinfection.quickwango.Abstraction.Server
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
