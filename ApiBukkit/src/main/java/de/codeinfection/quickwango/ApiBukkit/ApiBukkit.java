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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public boolean whitelistEnabled;
    public boolean blacklistEnabled;
    public final List<String> whitelist;
    public final List<String> blacklist;
    public static LogLevel logLevel = LogLevel.DEFAULT;

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
        this.config = this.getConfiguration();

        if (!(new File(this.dataFolder, "config.yml")).exists())
        {
            this.defaultConfig();
        }
        this.loadConfig();
        
        this.init();

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
        
        log(String.format("Version %s is now enabled!", this.pdf.getVersion()), LogLevel.QUIET);
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

        log(String.format("Version %s is now disabled!", this.pdf.getVersion()), LogLevel.QUIET);
    }
    
    protected void init()
    {
        if (!this.initiated)
        {
            try
            {
                if (!this.dataFolder.exists())
                {
                    this.dataFolder.mkdirs();
                }
                this.pdf = this.getDescription();
                this.server = this.getServer();
                this.pm = this.server.getPluginManager();
                this.webserver = new ApiBukkitServer(this);

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

                this.initiated = true;
            }
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
            }
        }
    }

    private void loadConfig()
    {
        this.config.load();

        try
        {
            logLevel = LogLevel.getLogLevel(this.config.getString("General.logLevel", logLevel.name()));
        }
        catch (Exception e)
        {
            logLevel = LogLevel.DEFAULT;
            logException(e);
        }
        this.port = this.config.getInt("Network.port", this.port);
        this.maxSessions = this.config.getInt("Network.maxSessions", this.maxSessions);
        this.authKey = this.config.getString("Network.authKey", this.authKey);

        this.whitelistEnabled = this.config.getBoolean("Whitelist.enabled", this.whitelistEnabled);
        this.whitelist.clear();
        this.whitelist.addAll(this.config.getStringList("Whitelist.IPs", this.whitelist));

        this.blacklistEnabled = this.config.getBoolean("Blacklist.enabled", this.blacklistEnabled);
        this.blacklist.clear();
        this.blacklist.addAll(this.config.getStringList("Blacklist.IPs", this.blacklist));

        this.defaultConfig();
    }

    private void defaultConfig()
    {
        this.config.setProperty("General.logLevel",         logLevel.name());
        this.config.setProperty("Network.port",             this.port);
        this.config.setProperty("Network.authKey",          this.authKey);
        this.config.setProperty("Network.maxSessions",      this.maxSessions);
        this.config.setProperty("Whitelist.enabled",        this.whitelistEnabled);
        this.config.setProperty("Whitelist.IPs",            this.whitelist);
        this.config.setProperty("Blacklist.enabled",        this.blacklistEnabled);
        this.config.setProperty("Blacklist.IPs",            this.blacklist);

        this.config.save();
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
        log(message, LogLevel.DEFAULT);
    }
    
    public static void log(String message, Throwable t, LogLevel requiredLogLevel)
    {
        if (requiredLogLevel.level >= logLevel.level)
        {
            String prefix = (requiredLogLevel.prefix == null ? "" : "[" + requiredLogLevel.prefix + "] ");
            message = "[ApiBukkit] " + prefix + message;
            logger.log(requiredLogLevel.logLevel, message);
        }
    }
    
    public static void log(String message, LogLevel requiredLogLevel)
    {
        log(message, null, requiredLogLevel);
    }
    
    public static void error(String message)
    {
        log(message, LogLevel.ERROR);
    }

    public static void error(String msg, Throwable t)
    {
        log(msg, t, LogLevel.ERROR);
    }
    
    public static void debug(String message)
    {
        log(message, LogLevel.DEBUG);
    }
    
    public static void logException(Throwable t)
    {
        log(t.getLocalizedMessage(), t, LogLevel.ERROR);
    }

    public enum LogLevel
    {
        QUIET(0),
        ERROR(1, "ERROR", Level.SEVERE),
        DEFAULT(2),
        INFO(3),
        DEBUG(4, "DEBUG");

        private final static Map<Integer, LogLevel> levelIdMap = new HashMap<Integer, LogLevel>();
        private final static Map<String, LogLevel> levelNameMap = new HashMap<String, LogLevel>();
        public final int level;
        public final String prefix;
        public final Level logLevel;

        LogLevel(int level, String prefix, Level logLevel)
        {
            this.level = level;
            this.prefix = prefix.toUpperCase();
            this.logLevel = logLevel;
        }

        LogLevel(int level, String prefix)
        {
            this.level = level;
            this.prefix = prefix.toUpperCase();
            this.logLevel = Level.INFO;
        }

        LogLevel(int level)
        {
            this.level = level;
            this.prefix = null;
            this.logLevel = Level.INFO;
        }

        public static LogLevel getLogLevel(int level) throws Exception
        {
            LogLevel logLevel = levelIdMap.get(level);
            if (logLevel == null)
            {
                throw new Exception("unknown LogLevel " + level);
            }
            return logLevel;
        }

        public static LogLevel getLogLevel(String level) throws Exception
        {
            level = level.trim();
            try
            {
                return getLogLevel(Integer.valueOf(level));
            }
            catch (NumberFormatException e)
            {}

            LogLevel logLevel = levelNameMap.get(level.toUpperCase());
            if (logLevel == null)
            {
                throw new Exception("unknown LogLevel " + level);
            }
            return logLevel;
        }

        static
        {
            for (LogLevel logLevel : values())
            {
                levelIdMap.put(logLevel.level, logLevel);
                levelNameMap.put(logLevel.name(), logLevel);
            }
        }
    }
}
