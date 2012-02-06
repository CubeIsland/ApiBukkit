package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiController;
import de.codeinfection.quickwango.ApiBukkit.Server.Action;
import de.codeinfection.quickwango.ApiBukkit.Server.Controller;
import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
    
    @Override
    public Object defaultAction(String action, Parameters params, Server server)
    {
        return this.getActions().keySet();
    }

    @Action
    public Object online(Parameters params, Server server)
    {
        return server.getOnlinePlayers().length;
    }

    @Action
    public Object playersonline(Parameters params, Server server)
    {
        Player[] players = server.getOnlinePlayers();
        List<String> data = new ArrayList<String>();
        for (Player player : players)
        {
            data.add(player.getName());
        }
        return data;
    }

    @Action
    public Object whois(Parameters params, Server server)
    {
        String requestPath = params.getString("__REQUEST_PATH__");
        if (requestPath == null)
        {
            return null;
        }
        String[] pathParts = requestPath.replaceFirst("^/", "").split("/");
        if (pathParts.length < 3)
        {
            return null;
        }
        Player player = server.getPlayerExact(pathParts[2]);
        if (player == null)
        {
            return null;
        }

        Map<String, Object> data = new HashMap<String, Object>();

        data.put("pseudo", player.getName());
        data.put("displayName", player.getDisplayName());
        data.put("health", player.getHealth());

        return data;
    }
    
    @Action
    public Object maxplayers(Parameters params, Server server)
    {
        return server.getMaxPlayers();
    }
}
