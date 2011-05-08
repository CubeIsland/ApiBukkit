package de.codeinfection.quickwango.ApiBukkit;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
    public static final Logger logger = Logger.getLogger("Minecraft.ApiCraft");
    public static boolean debug = true;
    protected Server server;
    protected PluginManager pm;
    protected PluginDescriptionFile pdf;
    protected File dataFolder;
    protected ApiBukkitServer webserver;
    protected Configuration config;
    
    protected boolean initiated = false;

    public void onDisable()
    {
        ApiBukkit.log("Disabling dependent plugins...");
        Iterator<AbstractRequestController> controllerIter = this.webserver.getAllRequestControllers().iterator();
        while (controllerIter.hasNext())
        {
            this.pm.disablePlugin(controllerIter.next().getPlugin());
        }
        
        ApiBukkit.log("Stopping Web Server...");
        this.webserver.stop();
        this.webserver = null;
        
        ApiBukkit.log(String.format("%s Version %s is now disabled!", this.pdf.getName(), this.pdf.getVersion()));
    }

    public void onEnable()
    {
        this.init();
        
        try
        {
            this.webserver = new ApiBukkitServer(this, config);
            ApiBukkit.log(String.format("Web server started on port %s!", config.getString("Configuration.webServerPort", "6561")));
        }
        catch (IOException e)
        {
            ApiBukkit.error("Failed to start the web server!");
            this.pm.disablePlugin(this);
            return;
        }
        
        ApiBukkit.log(String.format("%s Version %s is now enabled!", this.pdf.getName(), this.pdf.getVersion()));
    }
    
    public void init()
    {
        try
        {
            this.dataFolder = this.getDataFolder().getAbsoluteFile();
            this.config = new Configuration(new File(this.dataFolder, "config.yml"));
            this.config.load();
            if (!this.initiated)
            {
                this.pdf = this.getDescription();
                this.server = this.getServer();
                this.pm = this.server.getPluginManager();

                if (!this.dataFolder.exists())
                {
                    this.dataFolder.mkdirs();
                }

                if (this.config.getNode("Configuration") == null)
                {
                    this.config.setProperty("Configuration.webServerPort", 6561);
                    this.config.setProperty("Configuration.APIPassword", "changeMe");
                    this.config.save();
                }

                this.initiated = true;
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
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
            ApiBukkit.debug(String.format("Registered %s on %s", controller.getClass().getName(), name));
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
    
    public File apiDataFolder(Plugin plugin)
    {
        return new File(this.dataFolder, plugin.getDescription().getName());
        
    }
    
    public static void log(String message)
    {
        logger.log(Level.INFO, "ApiBukkit: " + message);
    }
    
    public static void error(String message)
    {
        logger.log(Level.SEVERE, "ApiBukkit: " + message);
    }
    
    public static void debug(String message)
    {
        if (debug)
        {
            logger.log(Level.INFO, "[DEBUG] ApiBukkit: " + message);
        }
    }
    
    public static void logException(Throwable t)
    {
        System.err.println("ApiBukkit: " + t.getClass().getName());
        System.err.println("Message: " + t.getMessage());
        t.printStackTrace(System.err);
    }
}
