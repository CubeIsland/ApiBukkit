package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.ArrayList;
import java.util.Properties;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class WhitelistController extends AbstractRequestController
{

    public WhitelistController(Plugin plugin)
    {
        super(plugin, true);

        this.setAction("add", new AddAction());
        this.setAction("remove", new RemoveAction());
        this.setAction("is", new IsAction());
        this.setAction("get", new GetAction());
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
                OfflinePlayer player = server.getOfflinePlayer(playerName);
                if (!player.isWhitelisted())
                {
                    player.setWhitelisted(true);
                }
                else
                {
                    throw new RequestException("Player already whitelisted!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
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
                OfflinePlayer player = server.getOfflinePlayer(playerName);
                if (player.isWhitelisted())
                {
                    player.setWhitelisted(false);
                }
                else
                {
                    throw new RequestException("Player not whitelisted!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
            return null;
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
                return server.getOfflinePlayer(playerName).isWhitelisted();
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
            ArrayList<String> whitelist = new ArrayList<String>();
            for (OfflinePlayer offlinePlayer : server.getWhitelistedPlayers())
            {
                whitelist.add(offlinePlayer.getName());
            }
            return whitelist;
        }
    }
}
