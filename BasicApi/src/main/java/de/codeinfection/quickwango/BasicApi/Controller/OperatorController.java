package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "operator")
public class OperatorController extends ApiController
{

    public OperatorController(Plugin plugin)
    {
        super(plugin);
    }

    @Action(parameters = {"player"})
    public void add(ApiRequest request, ApiResponse response)
    {
        String playerName = request.REQUEST.getString("player");
        if (playerName != null)
        {
            OfflinePlayer player = request.server.getOfflinePlayer(playerName);
            if (!player.isOp())
            {
                player.setOp(true);
            }
            else
            {
                throw new ApiRequestException("Player already Op!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No player given!", 1);
        }
    }

    @Action(parameters = {"player"})
    public void remove(ApiRequest request, ApiResponse response)
    {
        String playerName = request.REQUEST.getString("player");
        OfflinePlayer player = request.server.getOfflinePlayer(playerName);
        if (player.isOp())
        {
            player.setOp(false);
        }
        else
        {
            throw new ApiRequestException("Player is not a Op!", 1);
        }
    }

    @Action(parameters = {"player"})
    public void is(ApiRequest request, ApiResponse response)
    {
        String playerName = request.REQUEST.getString("player");
        if (playerName != null)
        {
            response.setContent(request.server.getOfflinePlayer(playerName).isOp());
        }
        else
        {
            throw new ApiRequestException("No player given!", 1);
        }
    }

    @Action
    public Object get(ApiRequest request, ApiResponse response)
    {
        List<String> operators = new ArrayList<String>();
        for (OfflinePlayer operator : request.server.getOperators())
        {
            operators.add(operator.getName());
        }
        return operators;
    }
}
