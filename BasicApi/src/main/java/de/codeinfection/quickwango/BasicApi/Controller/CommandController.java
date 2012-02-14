package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiCommandSender;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "command")
public class CommandController extends ApiController
{
    private ApiCommandSender commandSender;
    
    public CommandController(Plugin plugin)
    {
        super(plugin);
        this.commandSender = new ApiCommandSender(this.getPlugin().getServer());
    }

    @Override
    public void defaultAction(String action, ApiRequest request, ApiResponse response) throws ApiRequestException
    {
        List<String> responseLines = null;
        if (action != null)
        {
            ApiBukkit.log("Command " + action + " requested");
            
            String commandLine = action;
            String paramsParam = request.REQUEST.getString("params");
            if (paramsParam != null)
            {
                commandLine += " " + BasicApi.implode(" ", Arrays.asList(paramsParam.split(",")));
            }
            
            Player player = null;
            String senderParam = request.REQUEST.getString("sender");
            if (senderParam != null)
            {
                player = request.server.getPlayerExact(senderParam);
            }
            
            ApiBukkit.debug("Commandline: " + commandLine);
            boolean commandSuccessful;
            if (player != null)
            {
                ApiBukkit.debug("Using the player " + player.getName() + " as CommandSender");
                commandSuccessful = request.server.dispatchCommand(player, commandLine);
            }
            else
            {
                ApiBukkit.debug("Using the ApiCommandSender");
                this.commandSender.toggleActive();
                commandSuccessful = request.server.dispatchCommand(this.commandSender, commandLine);
                this.commandSender.toggleActive();
                responseLines = this.commandSender.getResponse();
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
        response.setContent(responseLines);
    }
}
