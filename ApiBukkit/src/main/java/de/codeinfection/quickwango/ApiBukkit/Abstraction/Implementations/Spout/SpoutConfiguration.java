package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Spout;

import java.util.List;
import java.util.Map;
import org.spout.api.util.config.Configuration;

/**
 *
 * @author CodeInfection
 */
public class SpoutConfiguration implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Configration
{
    private final Configuration config;

    public SpoutConfiguration(Configuration config)
    {
        this.config = config;
    }

    public void set(String path, Object value)
    {
        this.config.setValue(path, value);
    }

    public Map<String, Object> getMap(String path, Map<String, Object> def)
    {
        Object value = this.config.getValue(path);
        if (value != null && value instanceof Map)
        {
            return def;
        }
        else
        {
            return (Map<String, Object>)value;
        }
    }

    public <T> List<T> getList(String path, List<T> def)
    {
        Object value = this.config.getValue(path);
        if (value != null && value instanceof List)
        {
            return def;
        }
        else
        {
            return (List<T>)value;
        }
    }

    public <T> T get(String path, T def)
    {
        Object value = this.config.getValue(path);
        if (value != null)
        {
            return def;
        }
        else
        {
            return (T)value;
        }
    }

}
