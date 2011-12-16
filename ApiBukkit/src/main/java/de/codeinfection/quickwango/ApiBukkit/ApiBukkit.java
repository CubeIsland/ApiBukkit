package de.codeinfection.quickwango.ApiBukkit;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.codeinfection.quickwango.ApiBukkit.Net.ApiBukkitServer;
import de.codeinfection.quickwango.ApiBukkit.ResponseFormat.ApiResponseFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.bukkit.configuration.Configuration;

public class ApiBukkit extends JavaPlugin
{
    protected static final Logger logger = Logger.getLogger("Minecraft");
    private static ApiBukkit instance = null;

    private Server server;
    private PluginManager pm;
    private PluginDescriptionFile pdf;
    private File dataFolder;
    private ApiBukkitServer webserver;
    private ApiConfiguration config;
    private boolean zombie = false;
    private static ApiLogLevel logLevel = ApiLogLevel.DEFAULT;

    public ApiBukkit()
    {
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

        Configuration configFile = this.getConfig();
        configFile.options().copyDefaults(true);

        try
        {
            configFile.addDefault("Network.authKey", this.generateAuthKey());
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

        this.config = new ApiConfiguration(configFile);

        logLevel = this.config.logLevel;

        this.saveConfig();

        ApiBukkit.log("Log level is: " + this.config.logLevel.name(), ApiLogLevel.INFO);

        this.getCommand("apibukkit").setExecutor(new ApibukkitCommand(this));
        
        try
        {
            if (webserver == null)
            {
                this.webserver = new ApiBukkitServer(this.config);
            }
            log(String.format("Starting the web server on port %s!", this.config.port));
            log(String.format("Using %s as the auth key", this.config.authKey));
            log(String.format("with a maximum of %s parallel sessions!", this.config.maxSessions));
            this.webserver.start(this.config.port, this.config.maxSessions, this.config.authKey);
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
        
        log(String.format("Version %s is now enabled!", this.pdf.getVersion()), ApiLogLevel.QUIET);
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

        log(String.format("Version %s is now disabled!", this.pdf.getVersion()), ApiLogLevel.QUIET);
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

    public ApiConfiguration getApiConfig()
    {
        return this.config;
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
        log(message, ApiLogLevel.DEFAULT);
    }

    public static void log(String message, ApiLogLevel requiredLogLevel)
    {
        log(message, null, requiredLogLevel);
    }
    
    public static void log(String message, Throwable t, ApiLogLevel requiredLogLevel)
    {
        if (requiredLogLevel.level <= logLevel.level)
        {
            String prefix = (requiredLogLevel.prefix == null ? "" : "[" + requiredLogLevel.prefix + "] ");
            message = "[ApiBukkit] " + prefix + message;
            logger.log(requiredLogLevel.logLevel, message);
        }
    }
    
    public static void error(String message)
    {
        log(message, ApiLogLevel.ERROR);
    }

    public static void error(String msg, Throwable t)
    {
        log(msg, t, ApiLogLevel.ERROR);
    }
    
    public static void debug(String message)
    {
        log(message, ApiLogLevel.DEBUG);
    }
    
    public static void logException(Throwable t)
    {
        error(t.getLocalizedMessage(), t);
        t.printStackTrace(System.err);
    }
}
