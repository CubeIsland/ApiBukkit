package de.codeinfection.quickwango.ApiBukkit;

import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public interface ApiPlugin extends Plugin
{
    public ApiConfiguration getApiConfiguration();
}
