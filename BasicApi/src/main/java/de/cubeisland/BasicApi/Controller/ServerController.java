package de.cubeisland.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import de.cubeisland.BasicApi.BasicApi;
import de.cubeisland.BasicApi.Utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "server")
public class ServerController extends ApiController
{
    private static Long timeStamp = null;

    public ServerController(Plugin plugin)
    {
        super(plugin);

        if (timeStamp == null)
        {
            timeStamp = System.currentTimeMillis() / 1000;
        }
    }

    @Action(serializer = "json", authenticate = false)
    public Object maxplayers(ApiRequest request, ApiResponse response)
    {
        return getServer().getMaxPlayers();
    }

    @Action(serializer = "json")
    public void info(ApiRequest request, ApiResponse response)
    {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> data = new HashMap<String, Object>();
        Server server = getServer();

        data.put("name", server.getName());
        data.put("ip", server.getIp());
        data.put("port", server.getPort());
        data.put("players", server.getOnlinePlayers().length);
        data.put("maxplayers", server.getMaxPlayers());
        data.put("worlds", server.getWorlds().size());
        data.put("plugins", getPluginManager().getPlugins().length);
        data.put("uptime", (System.currentTimeMillis() / 1000) - timeStamp);
        data.put("onlinemode", server.getOnlineMode());
        data.put("whitelisted", server.hasWhitelist());
        data.put("spawnRadius", server.getSpawnRadius());
        data.put("viewDistance", server.getViewDistance());
        data.put("defaultGamemode", server.getDefaultGameMode().getValue());
        data.put("allowEnd", server.getAllowEnd());
        data.put("allowNether", server.getAllowNether());
        data.put("allowFlight", server.getAllowFlight());
        data.put("worldContainer", server.getWorldContainer().toString());
        data.put("updateFolder", server.getUpdateFolder().toString());

        data.put("maxmemory", runtime.maxMemory());
        data.put("freememory", runtime.freeMemory());

        Map<String, Object> versions = new HashMap<String, Object>();
        versions.put("server", server.getVersion());
        versions.put("apibukkit", ApiBukkit.getInstance().getDescription().getVersion());
        versions.put("basicapi", getPlugin().getDescription().getVersion());

        data.put("versions", versions);
        data.put("os", Utils.getPropertiesByPrefix("os."));

        response.setContent(data);
    }

    @Action(serializer = "json", authenticate = false)
    public void online(ApiRequest request, ApiResponse response)
    {
        response.setContent(getServer().getOnlinePlayers().length);
    }

    @Action(serializer = "json", authenticate = false)
    public void version(ApiRequest request, ApiResponse response)
    {
        response.setContent(getServer().getVersion());
    }

    @Action
    public void garbagecollect(ApiRequest request, ApiResponse response)
    {
        getServer().getScheduler().scheduleAsyncDelayedTask(getPlugin(), new Runnable()
        {
            public void run()
            {
                Runtime runtime = Runtime.getRuntime();
                long free = runtime.freeMemory();
                runtime.gc();
                int freed = (int)((runtime.freeMemory() - free) / 1024 / 1024);
                BasicApi.log("executed the garbage collector (Freed " + freed + "mb)");
            }
        });
    }

    @Action
    public Object kill(ApiRequest request, ApiResponse response)
    {
        BasicApi.log("killing java runtime");
        System.exit(0);
        return null;
    }

    @Action
    public void stop(ApiRequest request, ApiResponse response)
    {
        for (World world : getServer().getWorlds())
        {
            world.save();
        }
        getServer().shutdown();
    }

    @Action(parameters = {"message"})
    public void broadcast(ApiRequest request, ApiResponse response)
    {
        String message = request.params.getString("message");
        String permission = ChatColor.translateAlternateColorCodes('&', request.params.getString("permission"));
        if (permission != null)
        {
            getServer().broadcast(message, permission);
        }
        else
        {
            getServer().broadcastMessage(message);
        }
        BasicApi.log("broadcasted message '" + message + "'");
    }

    @Action
    public Object reload(ApiRequest request, ApiResponse response)
    {
        getServer().reload();
        return null;
    }

    @Action(serializer = "json")
    public void console(ApiRequest request, ApiResponse response)
    {
        try
        {
            long lineCount = 100L;
            String linesParam = request.params.getString("lines");
            if (linesParam != null)
            {
                try
                {
                    lineCount = Long.valueOf(linesParam);
                }
                catch (NumberFormatException e)
                {
                }
            }
            File serverLog = new File("server.log");
            RandomAccessFile file = new RandomAccessFile(serverLog, "r");

            lineCount = Math.abs(lineCount);
            long startPosition = file.length() - (lineCount * 100);
            if (startPosition < 0)
            {
                startPosition = 0;
            }

            String line;
            List<String> lines = new ArrayList<String>();
            file.seek(startPosition);
            file.readLine(); // ignore first line
            while ((line = file.readLine()) != null)
            {
                lines.add(Utils.reverseChatColors(line));
            }
            file.close();

            response.setContent(lines);
        }
        catch (IOException e)
        {
            throw new ApiRequestException("Could not find the server log!", 1);
        }
    }

    @Action(serializer = "json")
    public void offlineplayers(ApiRequest request, ApiResponse response)
    {
        OfflinePlayer[] players = getServer().getOfflinePlayers();
        List<String> data;
        if (request.params.containsKey("minlastplayed"))
        {
            try
            {
                long minLastPlayed = Long.parseLong(request.params.getString("minlastplayed"));
                data = new ArrayList<String>();
                for (OfflinePlayer player : players)
                {
                    if (player.getLastPlayed() >= minLastPlayed)
                    {
                        data.add(player.getName());
                    }
                }
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("No valid number given for minlastplayed", 1);
            }
        }
        else
        {
            data = new ArrayList<String>(players.length);
            for (OfflinePlayer player : players)
            {
                data.add(player.getName());
            }
        }
        response.setContent(data);
    }
}
