package de.codeinfection.quickwango.BasicApi;

import de.codeinfection.quickwango.Abstraction.Abstraction;
import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiLogLevel;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiManager;
import de.codeinfection.quickwango.BasicApi.Controller.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author CodeInfection
 */
public class BasicApi extends JavaPlugin
{
    protected static final Logger logger = Logger.getLogger("Minecraft");
    protected Server server;
    protected PluginManager pm;
    protected ApiBukkit api;
    protected PluginDescriptionFile pdf;
    protected File dataFolder;
    protected BasicApiConfiguration config;
    public static boolean debugMode = false;

    public static String implode(String delim, Object[] array)
    {
        return implode(delim, Arrays.asList(array));
    }

    public static String implode(String delim, Iterable<? extends Object> array)
    {
        if (array == null)
        {
            return "null";
        }
        Iterator iter = array.iterator();
        if (!iter.hasNext())
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(iter.next());
            while (iter.hasNext())
            {
                sb.append(delim);
                sb.append(iter.next().toString());
            }
            return sb.toString();
        }
    }

    @Override
    public void onEnable()
    {
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.pdf = this.getDescription();
        this.dataFolder = this.getDataFolder();
        if (!this.dataFolder.exists())
        {
            this.dataFolder.mkdirs();
        }

        this.api = (ApiBukkit)this.pm.getPlugin("ApiBukkit");
        if (this.api == null)
        {
            error("Could not hook into ApiBukkit! Disabling myself...");
            this.pm.disablePlugin(this);
            return;
        }

        debugMode = (this.api.getApiConfig().logLevel.level >= ApiLogLevel.DEBUG.level);

        this.reloadConfig();
        Configuration configFile = this.getConfig();
        configFile.options().copyDefaults(true);

        this.config = new BasicApiConfiguration(configFile);
        this.saveConfig();

        Plugin wrappedThis = Abstraction.getPluginManager().getPlugin(this.getName());
        ApiManager.getInstance()
            .registerController(new CommandController(wrappedThis))
            .registerController(new PluginController(wrappedThis))
            .registerController(new ServerController(wrappedThis))
            .registerController(new PlayerController(wrappedThis))
            .registerController(new WorldController(wrappedThis))
            .registerController(new BanController(wrappedThis))
            .registerController(new WhitelistController(wrappedThis))
            .registerController(new OperatorController(wrappedThis))
            .registerController(new ConfigurationController(wrappedThis, this.config.configFiles))
            .registerController(new PermissionController(wrappedThis));

        log("Version " + this.pdf.getVersion() + " enabled!");
    }

    @Override
    public void onDisable()
    {
        log("Version " + this.pdf.getVersion() + " disabled!");
    }

    public BasicApiConfiguration getBasicApiConfig()
    {
        return this.config;
    }

    public static boolean classMethodExists(String methodName, Class classObj)
    {
        try
        {
            Method method = classObj.getDeclaredMethod(methodName);
            return method.isAccessible();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean classFieldExists(String fieldName, Class classObj)
    {
        try
        {
            Field field = classObj.getDeclaredField(fieldName);
            return field.isAccessible();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static void log(Level logLevel, String msg, Throwable t)
    {
        logger.log(logLevel, "[BasicApi] " + msg, t);
    }

    public static void log(Level logLevel, String msg)
    {
        log(logLevel, msg, null);
    }

    public static void log(String msg)
    {
        log(Level.INFO, msg);
    }

    public static void error(String msg)
    {
        log(Level.SEVERE, msg);
    }

    public static void error(String msg, Throwable t)
    {
        log(Level.SEVERE, msg, t);
    }

    public static void debug(String msg)
    {
        if (debugMode)
        {
            log("[debug] " + msg);
        }
    }
}
