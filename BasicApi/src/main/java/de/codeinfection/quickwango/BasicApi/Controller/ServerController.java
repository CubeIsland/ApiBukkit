package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Net.Parameters;
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
public class ServerController extends ApiRequestController
{
    protected static Long timeStamp = null;
    
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
        this.setAction("garbagecollect",   new GarbagecollectAction());
        this.setAction("kill",             new KillAction());
        this.setAction("stop",             new StopAction());
        this.setAction("broadcast",        new BroadcastAction());
        this.setAction("reload",           new ReloadAction());
        this.setAction("console",          new ConsoleAction());
        this.setAction("offlineplayers",   new OfflinePlayersAction());
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server) throws ApiRequestException
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
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            return server.getMaxPlayers();
        }
    }
    
    private class InfoAction extends ApiRequestAction
    {
        
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
            versions.put("basicapi", plugin.getDescription().getVersion());

            data.put("versions",            versions);
            data.put("os",                  this.getPropertiesByPrefix("os."));
            
            return data;
        }

        private Map<String, String> getPropertiesByPrefix(String prefix)
        {
            Map<String, String> properties = new HashMap<String, String>();
            int prefixLen = prefix.length();

            for (Map.Entry entry : System.getProperties().entrySet())
            {
                String key = String.valueOf(entry.getKey());
                if (key.startsWith(prefix))
                {
                    String value = String.valueOf(entry.getValue());

                    properties.put(key.substring(prefixLen), value);
                }
            }

            return properties;
        }
    }
    
    private class OnlineAction extends ApiRequestAction
    {
        public OnlineAction()
        {
            super(false);
        }
        
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            return server.getVersion();
        }
    }
    
    private class GarbagecollectAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
    }
    
    private class ReloadAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            server.reload();
            return null;
        }
    }

    private class ConsoleAction extends ReloadAction
    {
        private final HashMap<String, String> colorReplacements;

        public ConsoleAction()
        {
            this.colorReplacements = new HashMap<String, String>();
            this.colorReplacements.put("\033[0m",  "&0");
            this.colorReplacements.put("\033[34m", "&1");
            this.colorReplacements.put("\033[32m", "&2");
            this.colorReplacements.put("\033[36m", "&3");
            this.colorReplacements.put("\033[31m", "&4");
            this.colorReplacements.put("\033[35m", "&5");
            this.colorReplacements.put("\033[33m", "&6");
            this.colorReplacements.put("\033[37m", "&7");
        }

        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
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
                    for (Map.Entry<String, String> entry : this.colorReplacements.entrySet())
                    {
                        line = line.replace(entry.getKey(), entry.getValue());
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

    private class OfflinePlayersAction extends ApiRequestAction
    {
        @Override
        public Object execute(Parameters params, Server server) throws ApiRequestException
        {
            return server.getOfflinePlayers();
        }
    }
}
