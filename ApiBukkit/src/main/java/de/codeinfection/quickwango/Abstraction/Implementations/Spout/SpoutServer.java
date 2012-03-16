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
}
