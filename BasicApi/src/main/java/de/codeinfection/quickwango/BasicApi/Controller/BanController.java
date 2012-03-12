package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "ban")
public class BanController extends ApiController
{
    public BanController(Plugin plugin)
    {
        super(plugin);
    }

    @Action(parameters =
    {
        "ip"
    })
    public void addip(ApiRequest request, ApiResponse response)
    {
        String ip = request.params.getString("ip");
        request.server.banIP(ip);
        BasicApi.log("banned ip " + ip);
    }

    @Action(parameters =
    {
        "player"
    })
    public void addplayer(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        OfflinePlayer offlinePlayer = request.server.getOfflinePlayer(playerName);
        if (!offlinePlayer.isBanned())
        {
            offlinePlayer.setBanned(true);
            BasicApi.log("banned player " + playerName);
            Player player = offlinePlayer.getPlayer();
            if (player != null)
            {
                player.kickPlayer(request.params.getString("reason", "You got banned from this server!"));
            }
        }
        else
        {
            throw new ApiRequestException("The given player is already banned!", 1);
        }
    }

    @Action(parameters =
    {
        "ip"
    })
    public void removeip(ApiRequest request, ApiResponse response)
    {
        String IP = request.params.getString("ip");
        if (request.server.getIPBans().contains(IP))
        {
            request.server.unbanIP(IP);
            BasicApi.log("unbanned ip " + IP);
        }
        else
        {
            throw new ApiRequestException("The given IP is not banned!", 1);
        }
    }

    @Action(parameters =
    {
        "player"
    })
    public void removeplayer(ApiRequest request, ApiResponse response)
    {
        String playerName = request.params.getString("player");
        OfflinePlayer player = request.server.getOfflinePlayer(playerName);
        if (player.isBanned())
        {
            player.setBanned(false);
            BasicApi.log("unbanned player " + playerName);
        }
        else
        {
            throw new ApiRequestException("The given player is not banned!", 3);
        }
    }

    @Action(serializer = "json")
    public void get(ApiRequest request, ApiResponse response)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> bannedPlayers = new ArrayList<String>();
        for (OfflinePlayer offlinePlayer : request.server.getBannedPlayers())
        {
            bannedPlayers.add(offlinePlayer.getName());
        }
        data.put("player", bannedPlayers);
        data.put("ip", request.server.getIPBans());
        response.setContent(data);
    }
}
