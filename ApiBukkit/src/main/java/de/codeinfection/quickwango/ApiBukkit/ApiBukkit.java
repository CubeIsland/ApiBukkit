package de.codeinfection.quickwango.ApiBukkit;

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import org.bukkit.plugin.Plugin;

public class ApiBukkit extends JavaPlugin
{
    protected static final Logger logger = Logger.getLogger("Minecraft");
    protected Server server;
    protected PluginManager pm;
    protected PluginDescriptionFile pdf;
    protected File dataFolder;
    protected ApiBukkitServer webserver;
    protected Configuration config;
    protected boolean zombie = false;
    
    protected boolean initiated = false;
    protected final String defaultPassword;

    // Configuration
    protected int port;
    protected String password;
    protected int maxSessions;
    public static boolean debug = false;
    public static boolean verbose = true;

    public ApiBukkit()
    {
        this.defaultPassword = "changeMeToASuperSecurePassword";
        this.password = this.defaultPassword;
        this.port = 6561;
        this.maxSessions = 30;
    }

    public void onEnable()
    {
        this.dataFolder = this.getDataFolder().getAbsoluteFile();
        this.config = new Configuration(new File(this.dataFolder, "config.yml"));
        if (!this.dataFolder.exists())
        {
            this.dataFolder.mkdirs();
        }

        this.config.load();
        if (this.config.getNode("Configuration") == null)
        {
            this.config.setProperty("Configuration.port", this.port);
            this.config.setProperty("Configuration.password", this.password);
            this.config.setProperty("Configuration.maxSessions", this.maxSessions);
            this.config.setProperty("Configuration.verbose", verbose);
            this.config.setProperty("Configuration.debug", debug);
            this.config.save();
        }

        verbose = this.config.getBoolean("Configuration.verbose", verbose);
        debug = this.config.getBoolean("Configuration.debug", debug);
        this.port = this.config.getInt("Configuration.port", this.port);
        this.maxSessions = this.config.getInt("Configuration.maxSessions", this.maxSessions);
        String cfgPassword = this.config.getString("Configuration.password", this.password);

        this.init();
        
        if (!cfgPassword.equals(this.password))
        {
            this.password = cfgPassword;
        }
        else
        {
            error("####################################");
            error("It is not allowed to use ApiBukkit with the default password!");
            error("");
            error("Staying in a zombie state...");
            error("####################################");
            return;
        }

        this.getCommand("apibukkit").setExecutor(new ApibukkitCommand(this));
        
        try
        {
            log(String.format("Starting the web server on port %s!", this.port));
            this.webserver.start(this.port, this.password, this.maxSessions);
            log("Web server started!");
        }
        catch (Throwable t)
        {
            error("Failed to start the web server!");
            logException(t);
            error("Staying in a zombie state...");
            this.zombie = true;
            return;
        }
        
        log(String.format("Version %s is now enabled!", this.pdf.getVersion()), true);
    }

    public void onDisable()
    {
        this.onDisable(true);
    }

    public void onDisable(boolean complete)
    {
        log("Stopping Web Server...");
        if (this.webserver != null)
        {
            if (complete)
            {
                this.webserver.clearRequestControllers();
            }
            this.webserver.stop();
        }

        log(String.format("Version %s is now disabled!", this.pdf.getVersion()), true);
    }
    
    protected void init()
    {
        if (!this.initiated)
        {
            try
            {
                this.pdf = this.getDescription();
                this.server = this.getServer();
                this.pm = this.server.getPluginManager();
                this.webserver = new ApiBukkitServer(this);

                this.initiated = true;
            }
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
            }
        }
    }

    public boolean isZombie()
    {
        return this.zombie;
    }

    public int getApiPort()
    {
        return this.port;
    }

    public String getApiPassword()
    {
        return this.password;
    }
    
    /**
     * Sets an RequestController for the given name
     * 
     * @param name the name of the controller
     * @param controller an controller instance
     */
    public void setRequestController(String name, AbstractRequestController controller)
    {
        if (this.webserver != null)
        {
            debug(String.format("Registered %s on %s", controller.getClass().getName().replaceFirst(controller.getClass().getPackage().getName() + ".", ""), name));
            this.webserver.setRequestController(name, controller);
        }
    }
    
    /**
     * Sets an alias for a controller
     * 
     * @param alias the alias name
     * @param controller the original name (may be a alias as well)
     */
    public void setControllerAlias(String alias, String controller)
    {
        if (this.webserver != null)
        {
            ApiBukkit.debug(String.format("Set alias '%s' for '%s'", alias, controller));
            this.webserver.setControllerAlias(alias, controller);
        }
    }
    
    /**
     * Removes the RequestController for the given name
     * 
     * @param name the name of the controller
     */
    public void removeRequestController(String name)
    {
        if (this.webserver != null)
        {
            this.webserver.removeRequestController(name);
            ApiBukkit.debug("Removed " + name);
        }
    }
    
    public File getApiDataFolder(Plugin plugin)
    {
        return new File(this.dataFolder, plugin.getDescription().getName());
        
    }

    public static void log(String message)
    {
        log(message, false);
    }
    
    public static void log(String message, boolean force)
    {
        if (force || verbose)
        {
            logger.log(Level.INFO, "[ApiBukkit] " + message);
        }
    }
    
    public static void error(String message)
    {
        logger.log(Level.SEVERE, "[ApiBukkit] " + message);
    }
    
    public static void debug(String message)
    {
        if (debug)
        {
            log("[DEBUG] " + message);
        }
    }
    
    public static void logException(Throwable t)
    {
        System.err.println("ApiBukkit: " + t.getClass().getName());
        System.err.println("Message: " + t.getMessage());
        t.printStackTrace(System.err);
    }
}
