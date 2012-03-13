package de.codeinfection.quickwango.Abstraction.Event;

/**
 *
 * @author CodeInfection
 */
public @interface EventHandler
{
    public boolean ignoreCancelled() default false;
}
