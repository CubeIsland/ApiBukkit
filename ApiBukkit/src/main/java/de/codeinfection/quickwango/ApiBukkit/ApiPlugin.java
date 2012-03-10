package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Abstraction.Plugin;

/**
 *
 * @author CodeInfection
 */
public interface ApiPlugin extends Plugin
{
    public ApiConfiguration getApiConfiguration();
}
