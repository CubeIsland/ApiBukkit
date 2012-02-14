package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import java.util.ArrayList;
import org.bukkit.OfflinePlayer;
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

    @Action(parameters = {"player"})
    public void add(ApiRequest request, ApiResponse response)
    {
        String playerName = request.REQUEST.getString("player");
        OfflinePlayer player = request.server.getOfflinePlayer(playerName);
        if (!player.isWhitelisted())
        {
            player.setWhitelisted(true);
        }
        else
        {
            throw new ApiRequestException("Player already whitelisted!", 1);
        }
    }

    @Action(parameters = {"player"})
    public void remove(ApiRequest request, ApiResponse response)
    {
        String playerName = request.REQUEST.getString("player");
        OfflinePlayer player = request.server.getOfflinePlayer(playerName);
        if (player.isWhitelisted())
        {
            player.setWhitelisted(false);
        }
        else
        {
            throw new ApiRequestException("Player not whitelisted!", 1);
        }
    }

    @Action(parameters = {"player"})
    public void is(ApiRequest request, ApiResponse response)
    {
        String playerName = request.REQUEST.getString("player");
        
        response.setContent(request.server.getOfflinePlayer(playerName).isWhitelisted());
    }

    @Action(serializer = "json")
    public void get(ApiRequest request, ApiResponse response)
    {
        ArrayList<String> whitelist = new ArrayList<String>();
        for (OfflinePlayer offlinePlayer : request.server.getWhitelistedPlayers())
        {
            whitelist.add(offlinePlayer.getName());
        }
        response.setContent(whitelist);
    }
}
