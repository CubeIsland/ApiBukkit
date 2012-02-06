package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Server.Action;
import de.codeinfection.quickwango.ApiBukkit.Server.Controller;
import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import de.codeinfection.quickwango.BasicApi.Utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    protected static Long timeStamp = null;
    
    public ServerController(Plugin plugin)
    {
        super(plugin);
        
        if (timeStamp == null)
        {
            timeStamp = System.currentTimeMillis() / 1000;
        }
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
    
    @Action(authenticate = false)
    public Object maxplayers(Parameters params, Server server)
    {
        return server.getMaxPlayers();
    }
    
    @Action
    public Object info(Parameters params, Server server)
    {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("id",                  server.getServerId());
        data.put("name",                server.getServerName());
        data.put("ip",                  server.getIp());
        data.put("port",                server.getPort());
        data.put("players",             server.getOnlinePlayers().length);
        data.put("maxplayers",          server.getMaxPlayers());
        data.put("worlds",              server.getWorlds().size());
        data.put("plugins",             server.getPluginManager().getPlugins().length);
        data.put("uptime",              (System.currentTimeMillis() / 1000) - timeStamp);
        data.put("onlinemode",          server.getOnlineMode());
        data.put("whitelisted",         server.hasWhitelist());
        data.put("spawnRadius",         server.getSpawnRadius());
        data.put("viewDistance",        server.getViewDistance());
        data.put("defaultGamemode",     server.getDefaultGameMode().getValue());
        data.put("allowEnd",            server.getAllowEnd());
        data.put("allowNether",         server.getAllowNether());
        data.put("allowFlight",         server.getAllowFlight());
        data.put("allowNether",         server.getAllowNether());
        data.put("worldContainer",      server.getWorldContainer().toString());
        data.put("updateFolder",        server.getUpdateFolderFile().toString());

        data.put("maxmemory",           runtime.maxMemory());
        data.put("freememory",          runtime.freeMemory());

        Map<String, Object> versions = new HashMap<String, Object>();
        versions.put("bukkit", server.getBukkitVersion());
        versions.put("server", server.getVersion());
        versions.put("apibukkit", ApiBukkit.getInstance().getDescription().getVersion());
        versions.put("basicapi", getPlugin().getDescription().getVersion());

        data.put("versions",            versions);
        data.put("os",                  Utils.getPropertiesByPrefix("os."));

        return data;
    }

    @Action(authenticate = false)
    public Object online(Parameters params, Server server)
    {
        return server.getOnlinePlayers().length;
    }

    @Action(authenticate = false)
    public Object version(Parameters params, Server server)
    {
        return server.getVersion();
    }
    
    @Action
    public Object garbagecollect(Parameters params, Server server)
    {
        server.getScheduler().scheduleAsyncDelayedTask(getPlugin(), new Runnable() {
            public void run() {
                Runtime runtime = Runtime.getRuntime();
                long free = runtime.freeMemory();
                runtime.gc();
                int freed = (int)((runtime.freeMemory() - free) / 1024 / 1024);
                ApiBukkit.log("executed the garbage collector (Freed " + freed + "mb)");
            }
        });
        return null;
    }
    
    @Action
    public Object kill(Parameters params, Server server)
    {
        ApiBukkit.log("killing java runtime");
        System.exit(0);
        return null;
    }
    
    @Action
    public Object stop(Parameters params, Server server)
    {
        for (World world : server.getWorlds())
        {
            world.save();
        }
        server.shutdown();
        return null;
    }
    
    @Action
    public Object broadcast(Parameters params, Server server)
    {
        String msg = params.getString("message");
        if (msg != null)
        {
            server.broadcastMessage(msg.replaceAll("&([0-9a-f])", "§$1"));
            ApiBukkit.log("broadcasted message '" + msg + "'");
        }
        else
        {
            throw new ApiRequestException("No message given!", 1);
        }
        return null;
    }
    
    @Action
    public Object reload(Parameters params, Server server)
    {
        server.reload();
        return null;
    }
    
    @Action
    public Object console(Parameters params, Server server)
    {
        try
        {
            long lineCount = 100L;
            String linesParam = params.getString("lines");
            if (linesParam != null)
            try
            {
                lineCount = Long.valueOf(linesParam);
            }
            catch (NumberFormatException e)
            {}
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

            return lines;
        }
        catch (IOException e)
        {
            throw new ApiRequestException("Could not find the server log!", 1);
        }
    }

    @Action
    public Object offlineplayers(Parameters params, Server server)
    {
        return server.getOfflinePlayers();
    }
}
