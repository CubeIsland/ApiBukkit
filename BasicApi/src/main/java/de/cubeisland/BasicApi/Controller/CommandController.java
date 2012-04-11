package de.cubeisland.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiCommandSender;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import de.cubeisland.BasicApi.BasicApi;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "command", serializer = "json", unknownToDefault = true)
public class CommandController extends ApiController
{
    private ApiCommandSender commandSender;

    public CommandController(Plugin plugin)
    {
        super(plugin);
        this.commandSender = new ApiCommandSender(getServer());
    }

    @Override
    public void defaultAction(ApiRequest request, ApiResponse response) throws ApiRequestException
    {
        List<String> responseLines = null;
        String action = request.getAction();
        if (action != null)
        {
            BasicApi.log("Command " + action + " requested");

            String commandLine = action;
            String paramsParam = request.params.getString("params");
            if (paramsParam != null)
            {
                commandLine += " " + BasicApi.implode(" ", Arrays.asList(paramsParam.split(",")));
            }

            Player player = null;
            String senderParam = request.params.getString("sender");
            if (senderParam != null)
            {
                player = getServer().getPlayerExact(senderParam);
            }

            BasicApi.debug("Commandline: " + commandLine);
            boolean commandSuccessful;
            if (player != null)
            {
                BasicApi.debug("Using the player " + player.getName() + " as CommandSender");
                commandSuccessful = getServer().dispatchCommand(player, commandLine);
            }
            else
            {
                BasicApi.debug("Using the ApiCommandSender");
                this.commandSender.toggleActive();
                commandSuccessful = getServer().dispatchCommand(this.commandSender, commandLine);
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
            BasicApi.log("No command given!");
            throw new ApiRequestException("No command given!", 1);
        }
        response.setContent(responseLines);
    }
}
