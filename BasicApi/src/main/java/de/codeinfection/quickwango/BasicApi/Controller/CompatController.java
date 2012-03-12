package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "serverinfos")
public class CompatController extends ApiController
{
    public CompatController(Plugin plugin)
    {
        super(plugin);
    }

    @Action
    public void online(ApiRequest request, ApiResponse response)
    {
        response.setContent(request.server.getOnlinePlayers().length);
    }

    @Action
    public void playersonline(ApiRequest request, ApiResponse response)
    {
        Player[] players = request.server.getOnlinePlayers();
        List<String> data = new ArrayList<String>();
        for (Player player : players)
        {
            data.add(player.getName());
        }
        response.setContent(data);
    }

    @Action
    public void whois(ApiRequest request, ApiResponse response)
    {
        String requestPath = (String)request.SERVER.get("REQUEST_PATH");
        if (requestPath == null)
        {
            return;
        }
        String[] pathParts = requestPath.replaceFirst("^/", "").split("/");
        if (pathParts.length < 3)
        {
            return;
        }
        Player player = request.server.getPlayerExact(pathParts[2]);
        if (player == null)
        {
            return;
        }

        Map<String, Object> data = new HashMap<String, Object>();

        data.put("pseudo", player.getName());
        data.put("displayName", player.getDisplayName());
        data.put("health", player.getHealth());

        response.setContent(data);
    }

    @Action
    public void maxplayers(ApiRequest request, ApiResponse response)
    {
        response.setContent(request.server.getMaxPlayers());
    }
}
