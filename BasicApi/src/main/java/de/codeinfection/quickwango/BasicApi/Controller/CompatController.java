package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
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
public class CompatController extends ApiRequestController
{
    public CompatController(Plugin plugin)
    {
        super(plugin, false);
        
        this.setAction("online",           new OnlineAction());
        this.setAction("players-online",   new PlayersonlineAction());
        this.setAction("whois",            new WhoisAction());
        this.setAction("max-players",      new MaxplayersAction());
    }
    
    private class OnlineAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            return server.getOnlinePlayers().length;
        }
    }
    
    private class PlayersonlineAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            Player[] players = server.getOnlinePlayers();
            List<String> data = new ArrayList<String>();
            for (Player player : players)
            {
                data.add(player.getName());
            }
            return data;
        }
    }
    
    private class WhoisAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            String requestPath = params.getProperty("__REQUEST_PATH__");
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
    }
    
    private class MaxplayersAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            return server.getMaxPlayers();
        }
    }
    

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
}
