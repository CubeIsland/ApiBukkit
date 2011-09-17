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
import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.ApiResponseFormat;
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
    private static ApiBukkit instance = null;

    protected Server server;
    protected PluginManager pm;
    protected PluginDescriptionFile pdf;
    protected File dataFolder;
    protected ApiBukkitServer webserver;
    protected Configuration config;
    protected boolean zombie = false;

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
        instance = this;
    }

    public void onEnable()
    {
        this.pdf = this.getDescription();
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder().getAbsoluteFile();
        if (!this.dataFolder.exists())
        {
            this.dataFolder.mkdirs();
        }
        this.config = this.getConfiguration();

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

        if (!(new File(this.dataFolder, "config.yml")).exists())
        {
            this.defaultConfig();
        }
        this.loadConfig();

        this.getCommand("apibukkit").setExecutor(new ApibukkitCommand(this));
        
        try
        {
            if (webserver == null)
            {
                this.webserver = new ApiBukkitServer(this);
            }
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
            this.webserver = null;
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


    /*
     * Public API
     */

    /**
     * Returns the instance of ApiBukkit.
     *
     * @return the instance of ApiBukkit or null
     */
    public static ApiBukkit getInstance()
    {
        return instance;
    }

    /**
     * Returns whether the ApiBukkit is a zombie or not
     *
     * @return the state of ApiBukkit
     */
    public boolean isZombie()
    {
        return this.zombie;
    }

    /**
     * Returns the port the API server has bound to.
     *
     * @return the API port
     */
    public int getApiPort()
    {
        return this.port;
    }

    /**
     * Returns the authkey.
     *
     * @return the authkey
     */
    public String getApiAuthKey()
    {
        return this.authKey;
    }

    /**
     * Returns the API data folder for the given plugin
     *
     * @param plugin the plugin
     * @return a File object of the folder
     */
    public File getApiDataFolder(Plugin plugin)
    {
        return new File(this.dataFolder, plugin.getDescription().getName());
    }


    /*
     * Proxy methods
     */

    /**
     * Returns the response format with the given name, the default response format or the plain response format.
     *
     * @param name the name of the response format
     * @return see description
     */
    public ApiResponseFormat getResponseFormat(String name)
    {
        if (this.webserver != null)
        {
            return this.webserver.getResponseFormat(name);
        }
        return null;
    }

    /**
     * Adds a new response format.
     *
     * @param name the name of the response format
     * @param format the response format
     */
    public final void addResponseFormat(String name, ApiResponseFormat format)
    {
        if (this.webserver != null)
        {
            this.webserver.addResponseFormat(name, format);
        }
    }

    /**
     * Removes a response format.
     *
     * @param name the name of the format to remove
     */
    public void removeResponseFormat(String name)
    {
        if (this.webserver != null)
        {
            this.webserver.removeResponseFormat(name);
        }
    }

    /**
     * Sets the default response format.
     *
     * @param format the name of a registered format
     * @return whether or not the default response format was set
     */
    public boolean setDefaultResponseFormat(String format)
    {
        if (this.webserver != null)
        {
            return this.webserver.setDefaultResponseFormat(format);
        }
        return false;
    }

    /**
     * Returns the default responce format.
     *
     * @return the default response format
     */
    public String getDefaultResponseFormat()
    {
        if (this.webserver != null)
        {
            return this.webserver.getDefaultResponseFormat();
        }
        return null;
    }

    /**
     * Returns a request controller.
     *
     * @param name the name of the request controller
     * @return a request controller or null
     */
    public ApiRequestController getRequestController(String name)
    {
        if (this.webserver != null)
        {
            return this.webserver.getRequestController(name);
        }
        return null;
    }

    /**
     * Returns a request controller by an alias.
     *
     * @param alias the alias
     * @return the controllers refered by the alias
     */
    public ApiRequestController getRequestControllerByAlias(String alias)
    {
        if (this.webserver != null)
        {
            return this.webserver.getRequestControllerByAlias(alias);
        }
        return null;
    }

    /**
     * Returns all controllers.
     *
     * @return a map of all controllers
     */
    public Map<String, ApiRequestController> getAllRequestControllers()
    {
        if (this.webserver != null)
        {
            return this.webserver.getAllRequestControllers();
        }
        return null;
    }

    /**
     * Sets a request controller.
     *
     * @param name the name of hte controller
     * @param controller the controller
     * @return false an failure
     */
    public boolean setRequestController(String name, ApiRequestController controller)
    {
        if (this.webserver != null)
        {
            return this.webserver.setRequestController(name, controller);
        }
        return false;
    }

    /**
     * Sets an alias for controller.
     *
     * @param alias the name of the alias
     * @param controller the name of the controller to refer
     * @return false on failure
     */
    public boolean setRequestControllerAlias(String alias, String controller)
    {
        if (this.webserver != null)
        {
            return this.webserver.setRequestControllerAlias(alias, controller);
        }
        return false;
    }

    /**
     * Removes a controller.
     * This also removes any alias which refered to the deleted controller.
     *
     * @param name the name of the controller
     */
    public void removeRequestController(String name)
    {
        if (this.webserver != null)
        {
            this.webserver.removeRequestController(name);
        }
    }

    /**
     * Removes a controller alias.
     *
     * @param name the name of the alias
     */
    public void removeRequestControllerAlias(String alias)
    {
        if (this.webserver != null)
        {
            this.webserver.removeRequestControllerAlias(alias);
        }
    }

    /**
     * Removes all controllers and aliases.
     */
    public void clearRequestControllers()
    {
        if (this.webserver != null)
        {
            this.webserver.clearRequestControllers();
        }
    }

    /**
     * Removes all aliases.
     */
    public void clearRequestControllerAliases()
    {
        if (this.webserver != null)
        {
            this.webserver.clearRequestControllerAliases();
        }
    }

    /*
     * Logging
     */
    public static void log(String message)
    {
        log(message, LogLevel.DEFAULT);
    }

    public static void log(String message, LogLevel requiredLogLevel)
    {
        log(message, null, requiredLogLevel);
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
