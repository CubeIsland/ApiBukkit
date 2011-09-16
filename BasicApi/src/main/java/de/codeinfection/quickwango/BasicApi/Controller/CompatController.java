package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class CompatController extends AbstractRequestController
{
    public CompatController(Plugin plugin)
    {
        super(plugin, false);
        
        this.setAction("online",           new OnlineAction());
        this.setAction("players-online",   new PlayersonlineAction());
        this.setAction("whois",            new WhoisAction());
        this.setAction("max-players",      new MaxplayersAction());
    }
    
    private class OnlineAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return server.getOnlinePlayers().length;
        }
    }
    
    private class PlayersonlineAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
    
    private class WhoisAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
            Player player = server.getPlayer(pathParts[2]);
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
    
    private class MaxplayersAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return server.getMaxPlayers();
        }
    }
    

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }
}
