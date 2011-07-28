package de.codeinfection.quickwango.BasicApi;

import de.codeinfection.quickwango.BasicApi.Controller.*;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author CodeInfection
 */
public class BasicApi extends JavaPlugin
{
    protected static boolean initiated = false;
    protected Server server;
    protected PluginManager pm;
    protected ApiBukkit api;
    protected PluginDescriptionFile pdf;
    
    public static String implode(String delim, String[] array)
    {
        if (array.length == 0)
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(array[0]);
            for (int i = 1; i < array.length; i++)
            {
                sb.append(delim);
                sb.append(array[i]);
            }
            return sb.toString();
        }
    }

    public void onEnable()
    {
        this.init();
        
        this.api = (ApiBukkit) this.pm.getPlugin("ApiBukkit");
        if (this.api == null)
        {
            System.err.println("Could not hook into ApiBukkit! Staying inactive...");
            return;
        }
        
        this.api.setRequestController("command", new CommandController(this));
        this.api.setControllerAlias("cmd", "command");
        
        this.api.setRequestController("serverinfos", new CompatController(this));
        
        this.api.setRequestController("plugin", new PluginController(this));
        this.api.setControllerAlias("pluginmanager", "plugin");
        
        this.api.setRequestController("server", new ServerController(this));
        
        this.api.setRequestController("player", new PlayerController(this));

        this.api.setRequestController("world", new WorldController(this));

        this.api.setRequestController("ban", new BanController(this));

        this.api.setRequestController("whitelist", new WhitelistController(this));

        this.api.setRequestController("operator", new OperatorController(this));
        
        System.out.println(this.pdf.getName() + " " + this.pdf.getVersion() + " is now enabled!");
    }

    public void onDisable()
    {
        this.api.removeRequestController("command");
        this.api.removeRequestController("cmd");

        this.api.removeRequestController("serverinfos");

        this.api.removeRequestController("plugin");
        this.api.removeRequestController("pluginmanager");

        this.api.removeRequestController("server");

        this.api.removeRequestController("world");

        this.api.removeRequestController("player");

        this.api.removeRequestController("ban");

        this.api.removeRequestController("whitelist");

        this.api.removeRequestController("operator");

        System.out.println(this.pdf.getName() + " " + this.pdf.getVersion() + " is now disabled!");
    }

    private void init()
    {
        if (!initiated)
        {
            this.server = this.getServer();
            this.pm = this.server.getPluginManager();
            this.pdf = this.getDescription();
            
            initiated = true;
        }
    }

    public static boolean classMethodExists(String methodName, Class classObj)
    {
        try
        {
            Method method = classObj.getDeclaredMethod(methodName);
            return method.isAccessible();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean classFieldExists(String fieldName, Class classObj)
    {
        try
        {
            Field field = classObj.getDeclaredField(fieldName);
            return field.isAccessible();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
