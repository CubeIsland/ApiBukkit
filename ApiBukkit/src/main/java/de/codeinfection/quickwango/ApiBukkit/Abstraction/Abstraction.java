package de.codeinfection.quickwango.ApiBukkit.Abstraction;

/**
 *
 * @author CodeInfection
 */
public class Abstraction
{
    private static ImplementationProvider implementationProvider = null;

    public static void initialize(ImplementationProvider provider)
    {
        if (implementationProvider == null && provider != null)
        {
            implementationProvider = provider;
        }
    }

    public static String getImplementationName()
    {
        return implementationProvider.getImplementationName();
    }

    public static Server getServer()
    {
        return implementationProvider.getServer();
    }

    public static PluginManager getPluginManager()
    {
        return implementationProvider.getPluginManager();
    }
}
