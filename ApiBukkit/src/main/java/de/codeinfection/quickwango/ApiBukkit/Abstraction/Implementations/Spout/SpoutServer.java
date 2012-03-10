package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Command;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
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

    public void registerCommand(Plugin plugin, Command command)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
