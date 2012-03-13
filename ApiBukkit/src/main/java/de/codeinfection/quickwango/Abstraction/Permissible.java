package de.codeinfection.quickwango.Abstraction;

/**
 *
 * @author CodeInfection
 */
public interface Permissible extends Operator
{
    public boolean hasPermission(String permission);
}
