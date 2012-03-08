package de.codeinfection.quickwango.ApiBukkit.Abstraction;

/**
 *
 * @author CodeInfection
 */
public class ServerHolder<T>
{
    private final T server;

    private ServerHolder(T server)
    {
        this.server = server;
    }

    public T getServer()
    {
        return this.server;
    }
}
