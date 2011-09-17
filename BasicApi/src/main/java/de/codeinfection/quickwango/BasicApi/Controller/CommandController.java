package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import java.util.Properties;
import org.bukkit.Server;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.BasicApi.ApiCommandSender;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class CommandController extends ApiRequestController
{
    private ApiCommandSender commandSender;
    
    public CommandController(Plugin plugin)
    {
        super(plugin, true);
        this.commandSender = new ApiCommandSender(this.plugin.getServer());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        List<String> response = null;
        if (action != null)
        {
            ApiBukkit.log("Command " + action + " requested");
            
            String commandLine = action;
            String paramsParam = params.getProperty("params");
            if (paramsParam != null)
            {
                commandLine += " " + BasicApi.implode(" ", Arrays.asList(paramsParam.split(",")));
            }
            
            Player player = null;
            String senderParam = params.getProperty("sender");
            if (senderParam != null)
            {
                player = server.getPlayer(senderParam);
            }
            
            ApiBukkit.debug("Commandline: " + commandLine);
            boolean commandSuccessful = false;
            if (player != null)
            {
                ApiBukkit.debug("Using the player " + player.getName() + " as CommandSender");
                commandSuccessful = server.dispatchCommand(player, commandLine);
            }
            else
            {
                ApiBukkit.debug("Using the ApiCommandSender");
                this.commandSender.toggleActive();
                commandSuccessful = server.dispatchCommand(this.commandSender, commandLine);
                this.commandSender.toggleActive();
                response = this.commandSender.getResponse();
            }
            if (!commandSuccessful)
            {
                throw new ApiRequestException("Command not found!", 2);
            }
        }
        else
        {
            ApiBukkit.log("No command given!");
            throw new ApiRequestException("No command given!", 1);
        }
        return response;
    }
    
}
