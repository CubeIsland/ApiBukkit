package de.cubeisland.ApiBukkit.ApiServer;

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

    public String[] parameters() default
    {
    };

    public String serializer() default "plain";
}
