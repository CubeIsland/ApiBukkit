package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestAction;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class OperatorController extends ApiRequestController
{

    public OperatorController(Plugin plugin)
    {
        super(plugin, true);

        this.setAction("add", new AddAction());
        this.setAction("remove", new RemoveAction());
        this.setAction("is", new IsAction());
        this.setAction("get", new GetAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        return this.getActions().keySet();
    }

    private class AddAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
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
    }

    private class RemoveAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
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
    }

    private class IsAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                return server.getOfflinePlayer(playerName).isOp();
            }
            else
            {
                throw new ApiRequestException("No player given!", 1);
            }
        }
    }

    private class GetAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            List<String> operators = new ArrayList<String>();
            for (OfflinePlayer operator : server.getOperators())
            {
                operators.add(operator.getName());
            }
            return operators;
        }
    }
}
