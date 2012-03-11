package de.codeinfection.quickwango.ApiBukkit.Abstraction;

/**
 *
 * @author CodeInfection
 */
public interface Permissible extends Operator
{
    public boolean hasPermission(String permission);
}
