package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ServerController extends AbstractRequestController
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
        
        this.registerAction("info",             new InfoAction());
        this.registerAction("maxplayers",       new MaxplayersAction());
        this.registerAction("online",           new OnlineAction());
        this.registerAction("version",          new VersionAction());
        this.registerAction("stats",            new StatsAction());
        this.registerAction("garbagecollect",   new GarbagecollectAction());
        this.registerAction("kill",             new KillAction());
        this.registerAction("stop",             new StopAction());
        this.registerAction("broadcast",        new BroadcastAction());
        this.registerAction("reload",           new ReloadAction());
        this.registerAction("console",          new ConsoleAction());
        
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
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }
    
    private class MaxplayersAction extends RequestAction
    {
        public MaxplayersAction()
        {
            super(false);
        }
        
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return server.getMaxPlayers();
        }
    }
    
    private class InfoAction extends RequestAction
    {
        
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
    
    private class OnlineAction extends RequestAction
    {
        public OnlineAction()
        {
            super(false);
        }
        
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return server.getOnlinePlayers().length;
        }
    }
    
    private class VersionAction extends RequestAction
    {
        public VersionAction()
        {
            super(false);
        }
        
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return server.getVersion();
        }
    }
    
    private class StatsAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            Map<String, Object> data = new HashMap<String, Object>();
            Runtime runtime = Runtime.getRuntime();
            data.put("maxmemory", runtime.maxMemory());
            data.put("freememory", runtime.freeMemory());
            return data;
        }
    }
    
    private class GarbagecollectAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
    
    private class KillAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            ApiBukkit.log("killing java runtime");
            System.exit(0);
            return null;
        }
    }
    
    private class StopAction extends RequestAction
    {
        /**
         * Stops the Server by dispatching the commands "save-all" and "stop"
         */
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            MinecraftServer mcServer = ((CraftServer)server).getHandle().server;
            CommandSender sender = new ConsoleCommandSender(server);
            ApiBukkit.log("save-all:");
            server.dispatchCommand(sender, "save-all");
            ApiBukkit.log("stop:");
            mcServer.a();
            return null;
        }
    }
    
    private class BroadcastAction extends RequestAction
    {
        public Object run(Properties params, Server server) throws RequestException
        {
            String msg = params.getProperty("message");
            if (msg != null)
            {
                server.broadcastMessage(msg.replaceAll("&([0-9a-f])", "§$1"));
                ApiBukkit.log("broadcasted message '" + msg + "'");
            }
            else
            {
                throw new RequestException("No message given!", 1);
            }
            return null;
        }
    }
    
    private class ReloadAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            server.reload();
            return null;
        }
    }

    private class ConsoleAction extends ReloadAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
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
                throw new RequestException("Could not find the server log!", 1);
            }
        }
    }
}
