package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import net.minecraft.server.ServerConfigurationManager;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;

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
                cserver.e(playerName);
                return null;
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
                cserver.f(playerName);
                return null;
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
                return cserver.isOp(playerName);
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
                Field oplist = ServerConfigurationManager.class.getField("h");
                oplist.setAccessible(true);
                operators.addAll((HashSet)oplist.get(cserver));
            }
            catch (Throwable t)
            {
                ApiBukkit.error("Failed to get the ops!");
                ApiBukkit.error("Error:");
                ApiBukkit.error(t.getLocalizedMessage());
            }

            return operators;
        }
    }
}