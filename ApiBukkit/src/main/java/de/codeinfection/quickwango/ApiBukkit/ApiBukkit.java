package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiManager;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiServer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer.JsonSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer.RawSerializer;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Serializer.XmlSerializer;
import java.io.File;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ApiBukkit extends JavaPlugin
{
    private static Logger logger = null;
    private static ApiBukkit instance = null;

    private Server server;
    private PluginManager pm;
    private PluginDescriptionFile pdf;
    private File dataFolder;
    private ApiConfiguration config;
    private boolean zombie = false;
    private static ApiLogLevel logLevel = ApiLogLevel.DEFAULT;

    public ApiBukkit()
    {
        instance = this;
    }

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        this.pdf = this.getDescription();
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder().getAbsoluteFile();
        if (!this.dataFolder.exists())
        {
            this.dataFolder.mkdirs();
        }

        this.reloadConfig();
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

        this.pm.registerEvents(new PluginListener(), this);

        ApiManager.getInstance()
            .registerController(new ApibukkitController(this))
            .registerSerializer(new JsonSerializer())
            .registerSerializer(new XmlSerializer())
            .registerSerializer(new RawSerializer())
            .setWhitelist(this.config.whitelist)
            .setWhitelistEnabled(this.config.whitelistEnabled)
            .setBlacklist(this.config.blacklist)
            .setBlacklistEnabled(this.config.blacklistEnabled)
            .setDisabledActions(this.config.disabledActions);
        
        try
        {
            log(String.format("Starting the web server on port %s!", this.config.port));
            log(String.format("Using %s as the auth key", this.config.authKey));
            log(String.format("with a maximum of %s parallel sessions!", this.config.maxContentLength));
            
            ApiServer.getInstance()
                .setIp(InetAddress.getByName(this.server.getIp()))
                .setPort(this.config.port)
                .setAuthenticationKey(this.config.authKey)
                .setMaxContentLength(this.config.maxContentLength)
                .start();

            log("Web server started!");
        }
        catch (Throwable t)
        {
            error("Failed to start the web server!", t);
            error("Staying in a zombie state...");
            this.zombie = true;
            this.pm.disablePlugin(this);
            return;
        }
        
        log(String.format("Version %s is now enabled!", this.pdf.getVersion()), ApiLogLevel.QUIET);
    }

    @Override
    public void onDisable()
    {
        this.onDisable(true);
    }

    public void onDisable(boolean complete)
    {
        log("Stopping Web Server...");

        ApiServer.getInstance().stop();
        this.config = null;

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
            message = prefix + message;
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
