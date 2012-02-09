package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Server.Action;
import de.codeinfection.quickwango.ApiBukkit.Server.Controller;
import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
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

    @Action
    public Object add(Parameters params, Server server)
    {
        String playerName = params.getString("player");
        if (playerName != null)
        {
            OfflinePlayer player = server.getOfflinePlayer(playerName);
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
        return null;
    }

    @Action
    public Object execute(Parameters params, Server server)
    {
        String playerName = params.getString("player");
        if (playerName != null)
        {
            OfflinePlayer player = server.getOfflinePlayer(playerName);
            if (player.isOp())
            {
                player.setOp(false);
            }
            else
            {
                throw new ApiRequestException("Player is not a Op!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No player given!", 1);
        }
        return null;
    }

    @Action
    public Object is(Parameters params, Server server)
    {
        String playerName = params.getString("player");
        if (playerName != null)
        {
            return server.getOfflinePlayer(playerName).isOp();
        }
        else
        {
            throw new ApiRequestException("No player given!", 1);
        }
    }

    @Action
    public Object get(Parameters params, Server server)
    {
        List<String> operators = new ArrayList<String>();
        for (OfflinePlayer operator : server.getOperators())
        {
            operators.add(operator.getName());
        }
        return operators;
    }
}
