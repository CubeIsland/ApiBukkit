package de.codeinfection.quickwango.ApiBukkit.Abstraction.Implementations.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author CodeInfection
 */
public class BukkitConfigration implements de.codeinfection.quickwango.ApiBukkit.Abstraction.Configration
{
    private final Configuration config;

    public BukkitConfigration(Configuration config)
    {
        this.config = config;
    }

    public void set(String path, Object value)
    {
        this.config.set(path, value);
    }

    private static Map<String, Object> mapFromSection(ConfigurationSection section)
    {
        if (section == null)
        {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Object value;

        for (String key : section.getKeys(false))
        {
            value = section.get(key);
            if (value instanceof ConfigurationSection)
            {
                value = mapFromSection((ConfigurationSection)value);
            }
        }

        return map;
    }

    public Map<String, Object> getMap(String path, Map<String, Object> def)
    {
        Map<String, Object> map = mapFromSection(this.config.getConfigurationSection(path));
        if (map == null)
        {
            return def;
        }
        else
        {
            return map;
        }
    }

    public <T> List<T> getList(String path, List<T> def)
    {
        List<T> list = (List<T>)this.config.getList(path);
        if (list == null)
        {
            return def;
        }
        else
        {
            return list;
        }
    }

    public <T> T get(String path, T def)
    {
        T value = (T)this.config.get(path);
        if (value == null)
        {
            return def;
        }
        else
        {
            return value;
        }
    }

    public boolean save() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean reload() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
