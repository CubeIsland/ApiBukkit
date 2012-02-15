package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

/**
 *
 * @author CodeInfection
 */
public class PluginListener implements Listener
{
    private static final ApiManager manager = ApiManager.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void unregisterControllers(PluginDisableEvent event)
    {
        manager.unregisterControllers(event.getPlugin());
    }
}
