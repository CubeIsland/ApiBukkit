package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.ArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class WhitelistController extends ApiRequestController
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
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }

    private class AddAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
                    throw new ApiRequestException("Player already whitelisted!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }

    private class RemoveAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
                    throw new ApiRequestException("Player not whitelisted!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
            return null;
        }
    }

    private class IsAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                return server.getOfflinePlayer(playerName).isWhitelisted();
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
        }
    }

    private class GetAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
