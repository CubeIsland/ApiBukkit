package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.UnknownDependencyException;

/**
 *
 * @author CodeInfection
 */
public class PluginController extends AbstractRequestController
{
    public PluginController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("list",         new ListAction());
        //this.setAction("load",         new LoadAction());
        //this.setAction("reload",       new ReloadAction());
        this.setAction("reloadall",    new ReloadallAction());
        //this.setAction("enable",       new EnableAction());
        //this.setAction("disable",      new DisableAction());
        this.setAction("info",         new InfoAction());
        this.setAction("available",    new AvailableAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }
    
    private class ListAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            ArrayList<String> data = new ArrayList<String>();
            Plugin[] plugins = server.getPluginManager().getPlugins();
            for (Plugin currentPlugin : plugins)
            {
                data.add(currentPlugin.getDescription().getName());
            }
            return data;
        }
    }
    
    private class LoadAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            try
            {
                String pluginName = params.getProperty("plugin");
                if (pluginName == null)
                {
                    throw new RequestException("No plugin file given!", 1);
                }
                
                File pluginDir = (File)((CraftServer)server).getHandle().server.options.valueOf("plugins");
                Plugin targetPlugin = server.getPluginManager().loadPlugin(new File(pluginDir, pluginName + ".jar"));
                if (targetPlugin == null)
                {
                    throw new RequestException("Could not load plugin " + pluginName + "!", 2);
                }
                server.getPluginManager().enablePlugin(targetPlugin);
                ApiBukkit.log("loaded and enabled plugin " + pluginName);
                return targetPlugin.getDescription().getName();
            }
            catch (InvalidPluginException e)
            {
                ApiBukkit.error("Failed to load the plugin: Invalid plugin!");
                throw new RequestException("Could not load plugin: Invalid plugin!", 3);
            }
            catch (InvalidDescriptionException e)
            {
                ApiBukkit.error("Failed to load the plugin: Invalid description!");
                throw new RequestException("Could not load plugin: Invalid description!", 4);
            }
            catch (UnknownDependencyException e)
            {
                ApiBukkit.error("Failed to load the plugin: Unknown dependency!");
                throw new RequestException("Could not load plugin: Unknown dependency!", 5);
            }
        }
    }
    
    private class ReloadAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String pluginName = params.getProperty("plugin");
            if (pluginName == null)
            {
                throw new RequestException("No plugin name given!", 1);
            }

            Plugin targetPlugin = server.getPluginManager().getPlugin(pluginName);
            if (targetPlugin == null)
            {
                throw new RequestException("The given plugin is not loaded!", 2);
            }
            server.getPluginManager().disablePlugin(targetPlugin);
            server.getPluginManager().enablePlugin(targetPlugin);
            ApiBukkit.log("reloaded plugin " + pluginName);
            return null;
        }
    }
    
    private class ReloadallAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            server.reload();
            return null;
        }
    }
    
    private class EnableAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String pluginName = params.getProperty("plugin");
            if (pluginName == null)
            {
                throw new RequestException("No plugin name given!", 1);
            }

            Plugin targetPlugin = server.getPluginManager().getPlugin(pluginName);
            if (targetPlugin == null)
            {
                throw new RequestException("The given plugin is not loaded!", 2);
            }
            server.getPluginManager().enablePlugin(targetPlugin);
            ApiBukkit.log("enabled plugin " + pluginName);
            return null;
        }
    }
    
    private class DisableAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String pluginName = params.getProperty("plugin");
            if (pluginName == null)
            {
                throw new RequestException("No plugin name given!", 1);
            }
            Plugin targetPlugin = server.getPluginManager().getPlugin(pluginName);
            if (targetPlugin == null)
            {
                throw new RequestException("The given plugin is not loaded!", 2);
            }
            server.getPluginManager().disablePlugin(targetPlugin);
            ApiBukkit.log("enabled plugin " + pluginName);
            return null;
        }
    }
    
    private class InfoAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String pluginName = params.getProperty("plugin");
            if (pluginName != null)
            {
                Plugin targetPlugin = server.getPluginManager().getPlugin(pluginName);
                if (targetPlugin != null)
                {
                    Map<String, Object> data = new HashMap<String, Object>();
                    PluginDescriptionFile description = targetPlugin.getDescription();
                    data.put("name",        description.getName());
                    data.put("fullName",    description.getFullName());
                    data.put("version",     description.getVersion());
                    data.put("description", description.getDescription());
                    data.put("website",     description.getWebsite());
                    data.put("authors",     description.getAuthors());
                    data.put("depend",      description.getDepend());
                    data.put("commands",    description.getCommands());
                    data.put("enabled",     targetPlugin.isEnabled());
                    data.put("dataFolder",  targetPlugin.getDataFolder().getAbsolutePath());
                    return data;
                }
                else
                {
                    throw new RequestException("Plugin not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No plugin given!", 1);
            }
        }
    }

    private class AvailableAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String pluginName = params.getProperty("plugin");
            if (pluginName != null)
            {
                return (server.getPluginManager().getPlugin(pluginName) != null);
            }
            else
            {
                throw new RequestException("No plugin given!", 1);
            }
        }
    }
    
    private class InstallAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
