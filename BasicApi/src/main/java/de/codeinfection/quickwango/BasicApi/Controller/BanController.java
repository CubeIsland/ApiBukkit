package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.minecraft.server.ServerConfigurationManager;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class BanController extends AbstractRequestController
{
    protected ServerConfigurationManager cserver;

    public BanController(Plugin plugin)
    {
        super(plugin, true);
        this.cserver = ((CraftServer)plugin.getServer()).getHandle();

        this.registerAction("add", new AddAction());
        this.registerAction("remove", new RemoveAction());
        this.registerAction("get", new GetAction());
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
            String IP = params.getProperty("ip");
            if (playerName != null)
            {
                cserver.a(playerName);
                ApiBukkit.log("banned player " + playerName);
                Player player = server.getPlayer(playerName);
                if (player != null)
                {
                    player.kickPlayer(params.getProperty("reason", "You got banned from this server!"));
                }
            }
            else if (IP != null)
            {
                if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
                {
                    cserver.c(IP);
                    ApiBukkit.log("banned ip " + IP);
                }
                else
                {
                    throw new RequestException("The given IP is invalid!", 2);
                }
            }
            else
            {
                throw new RequestException("No player or IP given!", 1);
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
            String IP = params.getProperty("ip");
            if (playerName != null)
            {
                cserver.b(playerName);
                ApiBukkit.log("banned player " + playerName);
            }
            else if (IP != null)
            {
                if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
                {
                    cserver.d(IP);
                    ApiBukkit.log("unbanned ip " + IP);
                }
                else
                {
                    throw new RequestException("The given IP is invalid!", 2);
                }
            }
            else
            {
                throw new RequestException("No player or IP given!", 1);
            }
            return null;
        }
    }

    private class GetAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            Map<String, List<String>> data = new HashMap<String, List<String>>();
            data.put("player", (List<String>)cserver.banByName);
            data.put("ip", (List<String>)cserver.banByIP);
            return data;
        }
    }
}
