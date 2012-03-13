package de.codeinfection.quickwango.Abstraction;

/**
 *
 * @author CodeInfection
 */
public interface Server
{
    public void registerCommand(Plugin plugin, String name, CommandExecutor command);

    public String getVersion();

    public PluginManager getPluginManager();

    public Scheduler getScheduler();
}
