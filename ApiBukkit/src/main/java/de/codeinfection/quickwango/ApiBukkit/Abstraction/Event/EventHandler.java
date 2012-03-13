package de.codeinfection.quickwango.ApiBukkit.Abstraction.Event;

/**
 *
 * @author CodeInfection
 */
public @interface EventHandler
{
    public boolean ignoreCancelled() default false;
}
