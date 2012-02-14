package de.codeinfection.quickwango.ApiBukkit.ApiServer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author CodeInfection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller
{
    public String name();
    public boolean authenticate() default true;
    public String serializer() default "plain";
}
