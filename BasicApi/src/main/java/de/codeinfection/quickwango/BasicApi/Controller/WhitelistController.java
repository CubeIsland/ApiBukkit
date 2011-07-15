package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.Properties;
import net.minecraft.server.ServerConfigurationManager;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class WhitelistController extends AbstractRequestController
{
    protected ServerConfigurationManager cserver;

    public WhitelistController(Plugin plugin)
    {
        super(plugin, true);
        this.cserver = ((CraftServer)plugin.getServer()).getHandle();

        this.registerAction("add", new AddAction());
        this.registerAction("remove", new RemoveAction());
        this.registerAction("is", new IsAction());
        this.registerAction("get", new GetAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }

    private class AddAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                cserver.k(playerName);
                return null;
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }
    }

    private class RemoveAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                cserver.l(playerName);
                return null;
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }
    }

    private class IsAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                return cserver.isWhitelisted(playerName);
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }
    }

    private class GetAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return cserver.e();
        }
    }
}
