package de.codeinfection.quickwango.DynmapApi;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import java.util.HashMap;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapPlugin;

/**
 *
 * @author CodeInfection
 */
public class DynmapController extends ApiRequestController
{
    protected final DynmapPlugin dynmap;

    public DynmapController(Plugin plugin, DynmapPlugin dynmap)
    {
        super(plugin, true);
        this.dynmap = dynmap;
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        return this.getActions();
    }

    private class InfoAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws ApiRequestException
        {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("port", dynmap.configuration.getInteger("webserver-port", -1));

            return data;
        }
    }
}
