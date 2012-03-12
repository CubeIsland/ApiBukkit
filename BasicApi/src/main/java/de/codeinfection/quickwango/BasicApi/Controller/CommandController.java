package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit.BukkitPlugin;
import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;
import de.codeinfection.quickwango.ApiBukkit.ApiCommandSender;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "command", serializer = "json")
public class CommandController extends ApiController
{
    private ApiCommandSender commandSender;

    public CommandController(Plugin plugin)
    {
        super(plugin);
        this.commandSender = new ApiCommandSender(((BukkitPlugin)getPlugin()).getHandle().getServer());
    }

    @Override
    public void defaultAction(String action, ApiRequest request, ApiResponse response) throws ApiRequestException
    {
        List<String> responseLines = null;
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
                player = request.server.getPlayerExact(senderParam);
            }

            BasicApi.debug("Commandline: " + commandLine);
            boolean commandSuccessful;
            if (player != null)
            {
                BasicApi.debug("Using the player " + player.getName() + " as CommandSender");
                commandSuccessful = request.server.dispatchCommand(player, commandLine);
            }
            else
            {
                BasicApi.debug("Using the ApiCommandSender");
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
            BasicApi.log("No command given!");
            throw new ApiRequestException("No command given!", 1);
        }
        response.setContent(responseLines);
    }
}
