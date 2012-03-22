package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.Abstraction.Abstraction;
import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import de.codeinfection.quickwango.BasicApi.Utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

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
        return request.server.getMaxPlayers();
    }

    @Action(serializer = "json")
    public void info(ApiRequest request, ApiResponse response)
    {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("id", request.server.getServerId());
        data.put("name", request.server.getServerName());
        data.put("ip", request.server.getIp());
        data.put("port", request.server.getPort());
        data.put("players", request.server.getOnlinePlayers().length);
        data.put("maxplayers", request.server.getMaxPlayers());
        data.put("worlds", request.server.getWorlds().size());
        data.put("plugins", request.server.getPluginManager().getPlugins().length);
        data.put("uptime", (System.currentTimeMillis() / 1000) - timeStamp);
        data.put("onlinemode", request.server.getOnlineMode());
        data.put("whitelisted", request.server.hasWhitelist());
        data.put("spawnRadius", request.server.getSpawnRadius());
        data.put("viewDistance", request.server.getViewDistance());
        data.put("defaultGamemode", request.server.getDefaultGameMode().getValue());
        data.put("allowEnd", request.server.getAllowEnd());
        data.put("allowNether", request.server.getAllowNether());
        data.put("allowFlight", request.server.getAllowFlight());
        data.put("allowNether", request.server.getAllowNether());
        data.put("worldContainer", request.server.getWorldContainer().toString());
        data.put("updateFolder", request.server.getUpdateFolderFile().toString());

        data.put("maxmemory", runtime.maxMemory());
        data.put("freememory", runtime.freeMemory());

        Map<String, Object> versions = new HashMap<String, Object>();
        versions.put("bukkit", request.server.getBukkitVersion());
        versions.put("server", request.server.getVersion());
        versions.put("apibukkit", ApiBukkit.getInstance().getDescription().getVersion());
        versions.put("basicapi", getPlugin().getVersion());

        data.put("versions", versions);
        data.put("os", Utils.getPropertiesByPrefix("os."));

        response.setContent(data);
    }

    @Action(serializer = "json", authenticate = false)
    public void online(ApiRequest request, ApiResponse response)
    {
        response.setContent(request.server.getOnlinePlayers().length);
    }

    @Action(serializer = "json", authenticate = false)
    public void version(ApiRequest request, ApiResponse response)
    {
        response.setContent(request.server.getVersion());
    }

    @Action
    public void garbagecollect(ApiRequest request, ApiResponse response)
    {
        Abstraction.getScheduler().scheduleAsyncDelayedTask(getPlugin(), new Runnable()
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
        for (World world : request.server.getWorlds())
        {
            world.save();
        }
        request.server.shutdown();
    }

    @Action(parameters = {"message"})
    public void broadcast(ApiRequest request, ApiResponse response)
    {
        String message = request.params.getString("message");
        request.server.broadcastMessage(message.replaceAll("&([0-9a-f])", "§$1"));
        BasicApi.log("broadcasted message '" + message + "'");
    }

    @Action
    public Object reload(ApiRequest request, ApiResponse response)
    {
        request.server.reload();
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
        OfflinePlayer[] players = request.server.getOfflinePlayers();
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
