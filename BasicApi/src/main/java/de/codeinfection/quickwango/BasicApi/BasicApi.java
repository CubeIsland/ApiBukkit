package de.codeinfection.quickwango.BasicApi;

import de.codeinfection.quickwango.BasicApi.Controller.*;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiLogLevel;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author CodeInfection
 */
public class BasicApi extends JavaPlugin
{
    protected static final Logger logger = Logger.getLogger("Minecraft");
    protected static boolean initiated = false;
    protected Server server;
    protected PluginManager pm;
    protected ApiBukkit api;
    protected PluginDescriptionFile pdf;
    protected File apiDataFolder;
    protected Configuration config;

    public static boolean debugMode = false;
    
    public static String implode(String delim, Iterable<String> array)
    {
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
                sb.append(iter.next());
            }
            return sb.toString();
        }
    }

    public void onEnable()
    {
        this.init();
        
        this.api = (ApiBukkit)this.pm.getPlugin("ApiBukkit");
        if (this.api == null)
        {
            ApiBukkit.error("Could not hook into ApiBukkit! Staying inactive...");
            return;
        }
        if (this.api.isZombie())
        {
            ApiBukkit.error("ApiBukkit seems to be a zombie...");
            ApiBukkit.error("I think it infected me.");
            return;
        }

        debugMode = (this.api.logLevel.level >= ApiLogLevel.DEBUG.level);

        this.apiDataFolder = this.api.getApiDataFolder(this);
        this.apiDataFolder.mkdirs();
        File configFile = new File(this.apiDataFolder, "config.yml");
        this.config = new Configuration(configFile);
        if (!configFile.exists())
        {
            config.setProperty("configfiles", new ArrayList<String>());
            config.save();
        }
        config.load();
        
        this.api.setRequestController("command", new CommandController(this));
        this.api.setRequestControllerAlias("cmd", "command");
        
        this.api.setRequestController("serverinfos", new CompatController(this));
        
        this.api.setRequestController("plugin", new PluginController(this));
        this.api.setRequestControllerAlias("pluginmanager", "plugin");
        
        this.api.setRequestController("server", new ServerController(this));
        
        this.api.setRequestController("player", new PlayerController(this));

        this.api.setRequestController("world", new WorldController(this));

        this.api.setRequestController("ban", new BanController(this));

        this.api.setRequestController("whitelist", new WhitelistController(this));

        this.api.setRequestController("operator", new OperatorController(this));

        this.api.setRequestController("configuration", new ConfigurationController(this, this.config.getStringList("configfiles", new ArrayList<String>())));
        
        log("Version " + this.pdf.getVersion() + " enabled!");
    }

    public void onDisable()
    {
        log("Version " + this.pdf.getVersion() + " disabled!");
    }

    private void init()
    {
        if (!initiated)
        {
            this.server = this.getServer();
            this.pm = this.server.getPluginManager();
            this.pdf = this.getDescription();
            
            initiated = true;
        }
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
