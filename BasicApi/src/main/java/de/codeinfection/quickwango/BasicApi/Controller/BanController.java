package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class BanController extends ApiRequestController
{
    public BanController(Plugin plugin)
    {
        super(plugin, true);

        this.setAction("add", new AddAction());
        this.setAction("remove", new RemoveAction());
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
            String playerName = params.getString("player");
            String IP = params.getString("ip");
            if (playerName != null)
            {
                OfflinePlayer player = server.getOfflinePlayer(playerName);
                if (!player.isBanned())
                {
                    player.setBanned(true);
                    ApiBukkit.log("banned player " + playerName);
                    if (player instanceof Player)
                    {
                        ((Player)player).kickPlayer(params.getString("reason", "You got banned from this server!"));
                    }
                }
                else
                {
                    throw new ApiRequestException("The given player is already banned!", 3);
                }
            }
            else if (IP != null)
            {
                if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
                {
                    server.banIP(IP);
                    ApiBukkit.log("banned ip " + IP);
                }
                else
                {
                    throw new ApiRequestException("The given IP is invalid!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player or IP given!", 1);
            }
            return null;
        }
    }

    private class RemoveAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String playerName = params.getString("player");
            String IP = params.getString("ip");
            if (playerName != null)
            {
                OfflinePlayer player = server.getOfflinePlayer(playerName);
                if (player.isBanned())
                {
                    player.setBanned(false);
                    ApiBukkit.log("unbanned player " + playerName);
                }
                else
                {
                    throw new ApiRequestException("The given player is not banned!", 3);
                }
            }
            else if (IP != null)
            {
                if (server.getIPBans().contains(IP))
                {
                    server.unbanIP(IP);
                    ApiBukkit.log("unbanned ip " + IP);
                }
                else
                {
                    throw new ApiRequestException("The given IP is not banned!", 2);
                }
            }
            else
            {
                throw new ApiRequestException("No player or IP given!", 1);
            }
            return null;
        }
    }

    private class GetAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            Map<String, Object> data = new HashMap<String, Object>();
            List<String> bannedPlayers = new ArrayList<String>();
            for (OfflinePlayer offlinePlayer : server.getBannedPlayers())
            {
                bannedPlayers.add(offlinePlayer.getName());
            }
            data.put("player", bannedPlayers);
            data.put("ip", server.getIPBans());
            return data;
        }
    }
}
