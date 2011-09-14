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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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

    // Configuration
    protected int port;
    protected String authKey;
    protected int maxSessions;
    public boolean whitelistEnabled = false;
    public boolean blacklistEnabled = false;
    public final List<String> whitelist;
    public final List<String> blacklist;
    public static boolean debug = false;
    public static boolean quiet = false;

    public ApiBukkit()
    {
        this.authKey = null;
        this.port = 6561;
        this.maxSessions = 30;
        this.whitelistEnabled = false;
        this.whitelist = new ArrayList<String>();
        this.blacklistEnabled = false;
        this.blacklist = new ArrayList<String>();
    }

    public void onEnable()
    {
        this.dataFolder = this.getDataFolder().getAbsoluteFile();
        this.config = new Configuration(new File(this.dataFolder, "config.yml"));
        if (!this.dataFolder.exists())
        {
            this.dataFolder.mkdirs();
        }

        this.init();

        this.config.load();
        if (this.config.getNode("Configuration") == null)
        {
            if (this.authKey == null)
            {
                try
                {
                    this.authKey = this.generateAuthKey();
                }
                catch (NoSuchAlgorithmException e)
                {
                    error("#################################");
                    error("Failed to generate an auth key!", e);
                    error("The auth key will not be persistent...");
                    error("#################################");
                    return;
                }
            }

            this.config.setProperty("Configuration.port", this.port);
            this.config.setProperty("Configuration.authKey", this.authKey);
            this.config.setProperty("Configuration.maxSessions", this.maxSessions);
            this.config.setProperty("Configuration.quiet", quiet);
            this.config.setProperty("Configuration.debug", debug);
            this.config.setProperty("Configuration.whitelistEnabled", this.whitelistEnabled);
            this.config.setProperty("Configuration.whitelist", this.whitelist);
            this.config.setProperty("Configuration.blacklistEnabled", this.blacklistEnabled);
            this.config.setProperty("Configuration.blacklist", this.blacklist);
            this.config.save();
        }

        quiet = this.config.getBoolean("Configuration.quiet", quiet);
        debug = this.config.getBoolean("Configuration.debug", debug);
        this.port = this.config.getInt("Configuration.port", this.port);
        this.maxSessions = this.config.getInt("Configuration.maxSessions", this.maxSessions);
        this.authKey = this.config.getString("Configuration.authKey", this.authKey);
        this.whitelistEnabled = this.config.getBoolean("Configuration.whitelistEnabled", this.whitelistEnabled);
        this.whitelist.clear();
        this.whitelist.addAll(this.config.getStringList("Configuration.whitelist", this.whitelist));
        this.blacklistEnabled = this.config.getBoolean("Configuration.blacklistEnabled", this.blacklistEnabled);
        this.blacklist.clear();
        this.blacklist.addAll(this.config.getStringList("Configuration.blacklist", this.blacklist));
        if (this.authKey == null)
        {
            try
            {
                this.authKey = this.generateAuthKey();
            }
            catch (NoSuchAlgorithmException e)
            {
                error("#################################");
                error("Failed to generate an auth key!", e);
                error("Staying in a zombie state...");
                error("#################################");
                this.zombie = true;
                return;
            }
        }

        this.getCommand("apibukkit").setExecutor(new ApibukkitCommand(this));
        
        try
        {
            log(String.format("Starting the web server on port %s!", this.port));
            log(String.format("Using %s as the auth key", this.authKey));
            log(String.format("with a maximum of %s parallel sessions!", this.maxSessions));
            this.webserver.start(this.port, this.authKey, this.maxSessions);
            log("Web server started!");
        }
        catch (Throwable t)
        {
            error("Failed to start the web server!", t);
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

    protected String generateAuthKey() throws NoSuchAlgorithmException
    {
        MessageDigest hasher = MessageDigest.getInstance("SHA1");
        hasher.reset();
        byte[] byteBuffer = (this.server.getServerName() + this.server.getServerId() + System.currentTimeMillis()).getBytes();
        hasher.update(byteBuffer);
        byteBuffer = hasher.digest();
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < byteBuffer.length; ++i)
        {
            hash.append(Integer.toString((byteBuffer[i] & 0xff) + 0x100, 16).substring(1));
        }
        return hash.toString();
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
        return this.authKey;
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
        if (!quiet || force)
        {
            logger.log(Level.INFO, "[ApiBukkit] " + message);
        }
    }
    
    public static void error(String message)
    {
        logger.log(Level.SEVERE, "[ApiBukkit] " + message);
    }

    public static void error(String msg, Throwable t)
    {
        logger.log(Level.SEVERE, "[DropLimit] " + msg, t);
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
