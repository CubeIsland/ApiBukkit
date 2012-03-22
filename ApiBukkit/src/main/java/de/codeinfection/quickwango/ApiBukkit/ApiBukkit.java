package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.Abstraction.Abstraction;
import de.codeinfection.quickwango.Abstraction.Configuration;
import de.codeinfection.quickwango.Abstraction.Implementations.Bukkit.BukkitImplementation;
import de.codeinfection.quickwango.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiManager;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiServer;
import java.io.File;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ApiBukkit extends JavaPlugin implements ApiPlugin, Listener
{
    private static Logger logger = null;
    private static ApiBukkit instance = null;
    private Server server;
    private PluginManager pm;
    private PluginDescriptionFile pdf;
    private File dataFolder;
    private Configuration config;
    private ApiConfiguration apiConfig;
    private static ApiLogLevel logLevel = ApiLogLevel.DEFAULT;

    public ApiBukkit()
    {
        instance = this;
    }

    @Override
    public void onEnable()
    {
        Abstraction.initialize(new BukkitImplementation());
        Plugin wrappedThis = Abstraction.getPluginManager().getPlugin(this.getName());
        logger = this.getLogger();
        this.pdf = this.getDescription();
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder().getAbsoluteFile();
        if (!this.dataFolder.exists())
        {
            this.dataFolder.mkdirs();
        }

        this.config = Abstraction.loadConfiguration(new File(dataFolder, "config.yml"));
        this.config.load();

        try
        {
            if (this.config.<String>get("Network.authKey") == null)
            {
                this.config.set("Network.authKey", this.generateAuthKey());
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            error("#################################");
            error("Failed to generate an auth key!", e);
            error("Staying in a zombie state...");
            error("#################################");
            wrappedThis.disable();
            return;
        }

        this.apiConfig = new ApiConfiguration(this.config);

        logLevel = this.apiConfig.logLevel;

        this.config.save();

        ApiBukkit.log("Log level is: " + this.apiConfig.logLevel.name(), ApiLogLevel.INFO);

        this.getCommand("apibukkit").setExecutor(new ApibukkitCommand(wrappedThis));

        this.pm.registerEvents(this, this);

        ApiManager.getInstance()
            .registerController(new ApibukkitController(wrappedThis))
            .setWhitelist(this.apiConfig.whitelist)
            .setWhitelistEnabled(this.apiConfig.whitelistEnabled)
            .setBlacklist(this.apiConfig.blacklist)
            .setBlacklistEnabled(this.apiConfig.blacklistEnabled)
            .setDisabledActions(this.apiConfig.disabledActions);

        try
        {
            log(String.format("Starting the web server on port %s!", this.apiConfig.port));
            log(String.format("Using %s as the auth key", this.apiConfig.authKey));
            log(String.format("with a maximum of %s parallel sessions!", this.apiConfig.maxContentLength));

            ApiServer.getInstance().setIp(InetAddress.getByName(this.server.getIp())).setPort(this.apiConfig.port).setAuthenticationKey(this.apiConfig.authKey).setMaxContentLength(this.apiConfig.maxContentLength).start();

            log("Web server started!");
        }
        catch (Throwable t)
        {
            error("Failed to start the web server!", t);
            error("Staying in a zombie state...");
            wrappedThis.disable();
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
        this.apiConfig = null;

        log(String.format("Version %s is now disabled!", this.pdf.getVersion()), ApiLogLevel.QUIET);
    }

    protected String generateAuthKey() throws NoSuchAlgorithmException
    {
        MessageDigest hasher = MessageDigest.getInstance("SHA1");
        hasher.reset();
        byte[] byteBuffer = (this.server.getServerName() + this.server.getVersion() + System.currentTimeMillis()).getBytes();
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
        return this.apiConfig;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void unregisterControllers(PluginDisableEvent event)
    {
        ApiManager.getInstance().unregisterControllers(event.getPlugin());
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

    public ApiConfiguration getApiConfiguration()
    {
        return this.apiConfig;
    }
}
