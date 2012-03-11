package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Abstraction;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Configuration;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout.SpoutImplementation;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
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
import org.spout.api.Server;
import org.spout.api.command.CommandRegistrationsFactory;
import org.spout.api.command.annotated.AnnotatedCommandRegistrationFactory;
import org.spout.api.command.annotated.SimpleAnnotatedCommandExecutorFactory;
import org.spout.api.command.annotated.SimpleInjector;
import org.spout.api.event.Listener;
import org.spout.api.plugin.CommonPlugin;
import org.spout.api.plugin.PluginDescriptionFile;
import org.spout.api.plugin.PluginManager;

public class ApiSpout extends CommonPlugin implements ApiPlugin, Listener
{
    private static Logger logger = null;
    private static ApiSpout instance = null;

    private Server server;
    private PluginManager pm;
    private PluginDescriptionFile pdf;
    private File dataFolder;
    private Configuration config;
    private ApiConfiguration apiConfig;
    private static ApiLogLevel logLevel = ApiLogLevel.DEFAULT;

    public ApiSpout()
    {
        instance = this;
    }

    @Override
    public void onEnable()
    {
        Abstraction.initialize(new SpoutImplementation());
        Plugin wrappedThis = Abstraction.getPluginManager().getPlugin(this.getName());
        logger = this.getLogger();
        this.pdf = this.getDescription();
        this.server = (Server)this.getGame();
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
            error("Disabling...");
            error("#################################");
            this.disable();
            return;
        }
        
        this.config.save();

        this.apiConfig = new ApiConfiguration(this.config);

        logLevel = this.apiConfig.logLevel;

        ApiSpout.log("Log level is: " + this.apiConfig.logLevel.name(), ApiLogLevel.INFO);


        CommandRegistrationsFactory<Class<?>> commandRegFactory = new AnnotatedCommandRegistrationFactory(new SimpleInjector(this), new SimpleAnnotatedCommandExecutorFactory());

        this.server.getRootCommand().addSubCommand(this, "test").setExecutor(null);
//        this.server.getRootCommand().addSubCommands(this, AdministrationCommands.class, commandRegFactory);
//        this.getCommand("apibukkit").setExecutor(new ApibukkitCommand(this));

        this.server.getEventManager().registerEvents(this, this);

        ApiManager.getInstance()
            .registerController(new ApibukkitController(wrappedThis))
            .registerSerializer(new JsonSerializer())
            .registerSerializer(new XmlSerializer())
            .registerSerializer(new RawSerializer())
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
            
            ApiServer.getInstance()
                .setIp(InetAddress.getByName(this.server.getAddress()))
                .setPort(this.apiConfig.port)
                .setAuthenticationKey(this.apiConfig.authKey)
                .setMaxContentLength(this.apiConfig.maxContentLength)
                .start();

            log("Web server started!");
        }
        catch (Throwable t)
        {
            error("Failed to start the web server!", t);
            error("Disabling...");
            this.disable();
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
        byte[] byteBuffer = (this.server.getName() + this.server.getVersion() + System.currentTimeMillis()).getBytes();
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
    public static ApiSpout getInstance()
    {
        return instance;
    }

    public ApiConfiguration getApiConfig()
    {
        return this.apiConfig;
    }

//    @EventHandler(order = Order.MONITOR)
//    public void unregisterControllers(PluginDisableEvent event)
//    {
//        ApiManager.getInstance().unregisterControllers(event.getPlugin());
//    }

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

    public String getVersion()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void enable()
    {
        this.pm.enablePlugin(this);
    }

    public void disable()
    {
        this.pm.disablePlugin(this);
    }

    public void reload()
    {
        this.disable();
        this.enable();
    }

    public Configuration getConfiguration()
    {
        return this.config;
    }

    public ApiConfiguration getApiConfiguration()
    {
        return this.apiConfig;
    }
}
