package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import java.util.Properties;
import org.bukkit.Server;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import org.bukkit.command.CommandSender;
//import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class CommandController extends AbstractRequestController
{
    private CommandSender commandSender;
    
    public CommandController(Plugin plugin)
    {
        super(plugin, true);
        //this.commandSender = new ConsoleCommandSender(plugin.getServer());
        this.commandSender = ((CraftServer) plugin.getServer()).getHandle().server.console;
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        if (action != null)
        {
            ApiBukkit.log("Command " + action + " requested");
            
            String commandLine = action;
            String paramsParam = params.getProperty("params");
            if (paramsParam != null)
            {
                commandLine += " " + BasicApi.implode(" ", paramsParam.split(","));
            }
            
            CommandSender sender = this.commandSender;
            String senderParam = params.getProperty("sender");
            if (senderParam != null)
            {
                Player player = server.getPlayer(senderParam);
                if (player != null)
                {
                    sender = player;
                }
            }
            
            ApiBukkit.debug("Commandline: " + commandLine);
            
            if (!server.dispatchCommand(sender, commandLine))
            {
                throw new RequestException("Command not found!", 2);
            }
        }
        else
        {
            ApiBukkit.log("No command given!");
            throw new RequestException("No command given!", 1);
        }
        return null;
    }
    
}
