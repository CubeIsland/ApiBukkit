package de.codeinfection.quickwango.DynmapApi;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.Server;
import org.dynmap.DynmapPlugin;

public class DynmapApi extends JavaPlugin
{
    protected static final Logger log = Logger.getLogger("Minecraft");
    public static boolean debugMode = false;
    
    protected Server server;
    protected PluginManager pm;
    public Configuration config;
    protected File dataFolder;

    public DynmapApi()
    {
    }

    public void onEnable()
    {
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder();

        this.dataFolder.mkdirs();
        // Create default config if it doesn't exist.
        if (!(new File(this.dataFolder, "config.yml")).exists())
        {
            this.defaultConfig();
        }
        this.loadConfig();

        DynmapPlugin dynmap = (DynmapPlugin)this.pm.getPlugin("dynmap");
        if (dynmap == null)
        {
            error("Failed to hook into dynmap!");
            return;
        }

        DynmapController dynmapController = new DynmapController(this, dynmap);

        System.out.println(this.getDescription().getName() + " (v" + this.getDescription().getVersion() + ") enabled");
    }

    public void onDisable()
    {
        System.out.println(this.getDescription().getName() + " Disabled");
    }

    private void loadConfig()
    {
        this.config.load();
    }

    private void defaultConfig()
    {
        this.config.save();
    }

    public static void log(String msg)
    {
        log.log(Level.INFO, "[DynmapApi] " + msg);
    }

    public static void error(String msg)
    {
        log.log(Level.SEVERE, "[DynmapApi] " + msg);
    }

    public static void error(String msg, Throwable t)
    {
        log.log(Level.SEVERE, "[DynmapApi] " + msg, t);
    }

    public static void debug(String msg)
    {
        if (debugMode)
        {
            log("[debug] " + msg);
        }
    }
}
