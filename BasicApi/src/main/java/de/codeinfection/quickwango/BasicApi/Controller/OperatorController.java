package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import org.bukkit.craftbukkit.CraftServer;
import net.minecraft.server.ServerConfigurationManager;
import java.lang.reflect.Field;

/**
 *
 * @author CodeInfection
 */
public class OperatorController extends AbstractRequestController
{
    protected ServerConfigurationManager cserver;

    public OperatorController(Plugin plugin)
    {
        super(plugin, true);
        this.cserver = ((CraftServer)plugin.getServer()).getHandle();

        this.registerAction("add", new AddAction());
        this.registerAction("remove", new RemoveAction());
        this.registerAction("is", new IsAction());
        this.registerAction("get", new GetAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }

    private class AddAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                OfflinePlayer player = server.getOfflinePlayer(playerName);
                if (!player.isOp())
                {
                    player.setOp(false);
                    return null;
                }
                else
                {
                    throw new RequestException("Player already Op!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }
    }

    private class RemoveAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                OfflinePlayer player = server.getOfflinePlayer(playerName);
                if (player.isOp())
                {
                    player.setOp(false);
                    return null;
                }
                else
                {
                    throw new RequestException("Player is not a Op!", 2);
                }
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }
    }

    private class IsAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String playerName = params.getProperty("player");
            if (playerName != null)
            {
                return server.getOfflinePlayer(playerName).isOp();
            }
            else
            {
                throw new RequestException("No player given!", 1);
            }
        }
    }

    private class GetAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            List<String> operators = new ArrayList<String>();
            try
            {
                Field oplist = ServerConfigurationManager.class.getDeclaredField("h");
                oplist.setAccessible(true);
                operators.addAll((Set)oplist.get(cserver));
            }
            catch (Throwable t)
            {
                ApiBukkit.error("Failed to get the ops!");
                ApiBukkit.error("Error: " + t.getClass().getName().replaceFirst(t.getClass().getPackage().getName() + ".", ""));
                ApiBukkit.error(t.getLocalizedMessage());
            }

            return operators;
        }
    }
}
