package de.codeinfection.quickwango.ApiBukkit.Server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author CodeInfection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action
{
    public String name() default "";
    public boolean authenticate() default true;
    public String[] parameters() default {};
}
