package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ServerController extends ApiRequestController
{
    protected static Long timeStamp = null;

    protected final HashMap<String, String> colorReplacements;
    
    public ServerController(Plugin plugin)
    {
        super(plugin, true);
        
        if (timeStamp == null)
        {
            timeStamp = System.currentTimeMillis() / 1000;
        }
        
        this.setAction("info",             new InfoAction());
        this.setAction("maxplayers",       new MaxplayersAction());
        this.setAction("online",           new OnlineAction());
        this.setAction("version",          new VersionAction());
        this.setAction("stats",            new StatsAction());
        this.setAction("garbagecollect",   new GarbagecollectAction());
        this.setAction("kill",             new KillAction());
        this.setAction("stop",             new StopAction());
        this.setAction("broadcast",        new BroadcastAction());
        this.setAction("reload",           new ReloadAction());
        this.setAction("console",          new ConsoleAction());
        
        this.setActionAlias("playerlimit",      "maxplayers");
        this.setActionAlias("gc",               "garbagecollect");

        this.colorReplacements = new HashMap<String, String>();
        this.colorReplacements.put("\033\\[0m", "&0");
        this.colorReplacements.put("\033\\[31m", "&c");
        this.colorReplacements.put("\033\\[32m", "&a");
        this.colorReplacements.put("\033\\[33m", "&e");
        this.colorReplacements.put("\033\\[34m", "&9");
        this.colorReplacements.put("\033\\[35m", "&d");
        this.colorReplacements.put("\033\\[36m", "&b");
        this.colorReplacements.put("\033\\[37m", "&f");
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }
    
    private class MaxplayersAction extends ApiRequestAction
    {
        public MaxplayersAction()
        {
            super(false);
        }
        
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            return server.getMaxPlayers();
        }
    }
    
    private class InfoAction extends ApiRequestAction
    {
        
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            Map<String, Object> data = new HashMap<String, Object>();
            
            data.put("id", server.getServerId());
            data.put("name", server.getServerName());
            data.put("ip", server.getIp());
            data.put("port", server.getPort());
            data.put("players", server.getOnlinePlayers().length);
            data.put("maxplayers", server.getMaxPlayers());
            data.put("worlds", server.getWorlds().size());
            data.put("version", server.getVersion());
            data.put("plugins", server.getPluginManager().getPlugins().length);
            data.put("uptime", (System.currentTimeMillis() / 1000) - timeStamp);
            data.put("onlinemode", server.getOnlineMode());
            data.put("whitelisted", server.hasWhitelist());
            data.put("flying", server.getAllowFlight());
            data.put("nether", server.getAllowNether());
            data.put("spawnradius", server.getSpawnRadius());
            data.put("viewdistance", server.getViewDistance());
            
            return data;
        }
    }
    
    private class OnlineAction extends ApiRequestAction
    {
        public OnlineAction()
        {
            super(false);
        }
        
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            return server.getOnlinePlayers().length;
        }
    }
    
    private class VersionAction extends ApiRequestAction
    {
        public VersionAction()
        {
            super(false);
        }
        
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            return server.getVersion();
        }
    }
    
    private class StatsAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            Map<String, Object> data = new HashMap<String, Object>();
            Runtime runtime = Runtime.getRuntime();
            data.put("maxmemory", runtime.maxMemory());
            data.put("freememory", runtime.freeMemory());
            return data;
        }
    }
    
    private class GarbagecollectAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            server.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
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
    }
    
    private class KillAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            ApiBukkit.log("killing java runtime");
            System.exit(0);
            return null;
        }
    }
    
    private class StopAction extends ApiRequestAction
    {
        /**
         * Stops the Server by dispatching the commands "save-all" and "stop"
         */
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            for (World world : server.getWorlds())
            {
                world.save();
            }
            server.shutdown();
            return null;
        }
    }
    
    private class BroadcastAction extends ApiRequestAction
    {
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String msg = params.getProperty("message");
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
    }
    
    private class ReloadAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            server.reload();
            return null;
        }
    }

    private class ConsoleAction extends ReloadAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            try
            {
                long lineCount = 100L;
                String linesParam = params.getProperty("lines");
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

                String line = "";
                List<String> lines = new ArrayList<String>();
                file.seek(startPosition);
                file.readLine(); // ignore first line
                while ((line = file.readLine()) != null)
                {
                    for (Map.Entry<String, String> entry : colorReplacements.entrySet())
                    {
                        line.replaceAll(entry.getKey(), entry.getValue());
                    }
                    lines.add(line);
                }
                file.close();

                return lines;
            }
            catch (IOException e)
            {
                throw new ApiRequestException("Could not find the server log!", 1);
            }
        }
    }
}
