package org.kokakiwi.apicraft;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import org.bukkit.Server;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.kokakiwi.apicraft.net.*;
import org.kokakiwi.apicraft.net.Request.AbstractRequestController;

public class ApiCraft extends JavaPlugin
{
    
    public static final Logger logger = Logger.getLogger("Minecraft.ApiCraft");
    protected Server server;
    protected PluginManager pm;
    protected PluginDescriptionFile pdf;
    protected File dataFolder;
    protected ApiWebServer webserver;
    protected Configuration config;
    
    protected boolean initiated = false;

    public void onDisable()
    {
        logger.info("Stopping Web Server...");
        
        Iterator<AbstractRequestController> controllerIter = this.webserver.getAllRequestControllers().iterator();
        
        while (controllerIter.hasNext())
        {
            this.pm.disablePlugin(controllerIter.next().getPlugin());
        }
        
        this.webserver.stop();
        this.webserver = null;
        logger.info("ApiCraft is disabled!");
    }

    public void onEnable()
    {
        this.init();
        
        config = new Configuration(new File(this.dataFolder, "config.yml"));
        config.load();
        
        logger.info("ApiCraft: Starting the webserver...");
        try
        {
            this.webserver = new ApiWebServer(this, config);
            logger.info((new StringBuilder()).append("ApiCraft: Web server started on port ").append(config.getString("Configuration.webServerPort", "6561")).append("!").toString());
        }
        catch (IOException e)
        {
            logger.severe("ApiCraft: Failed to start the web server!");
            this.pm.disablePlugin(this);
            return;
        }
        
        logger.info((new StringBuilder()).append(this.pdf.getName()).append(" ").append(this.pdf.getVersion()).append(" is enabled!").toString());
    }
    
    public void init()
    {
        if (!this.initiated)
        {
            this.pdf = this.getDescription();
            this.dataFolder = this.getDataFolder();
            this.server = this.getServer();
            this.pm = this.server.getPluginManager();
        
            if (!getDataFolder().exists())
            {
                getDataFolder().mkdirs();
            }

            if (!new File(getDataFolder(), "cache/").exists())
            {
                new File(getDataFolder(), "cache/").mkdirs();
            }

            if (!new File(getDataFolder(), "config.yml").exists())
            {
                try
                {
                    new File(getDataFolder(), "config.yml").createNewFile();
                    config.setProperty("Configuration.webServerPort", 6561);
                    config.setProperty("Configuration.APIPassword", "changeMe");
                    config.save();
                }
                catch (IOException e)
                {
                    logger.severe("ApiCraft : Error during creating config file!");
                    e.printStackTrace();
                    getServer().getPluginManager().disablePlugin(this);
                }
            }
            
            this.initiated = true;
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
            this.webserver.setRequestController(name, controller);
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
        }
    }

}
