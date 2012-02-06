package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Server.Action;
import de.codeinfection.quickwango.ApiBukkit.Server.Controller;
import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import java.util.ArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "whitelist")
public class WhitelistController extends ApiController
{
    public WhitelistController(Plugin plugin)
    {
        super(plugin);
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server)
    {
        return this.getActions().keySet();
    }

    @Action
    public Object add(Parameters params, Server server)
    {
        String playerName = params.getString("player");
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

    @Action
    public Object remove(Parameters params, Server server)
    {
        String playerName = params.getString("player");
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

    @Action
    public Object is(Parameters params, Server server)
    {
        String playerName = params.getString("player");
        if (playerName != null)
        {
            return server.getOfflinePlayer(playerName).isWhitelisted();
        }
        else
        {
            throw new ApiRequestException("No player given!", 1);
        }
    }

    @Action
    public Object get(Parameters params, Server server)
    {
        ArrayList<String> whitelist = new ArrayList<String>();
        for (OfflinePlayer offlinePlayer : server.getWhitelistedPlayers())
        {
            whitelist.add(offlinePlayer.getName());
        }
        return whitelist;
    }
}
