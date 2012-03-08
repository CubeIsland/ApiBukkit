package de.codeinfection.quickwango.ApiBukkit.Abstraction;

import java.util.List;
import java.util.Map;

/**
 *
 * @author CodeInfection
 */
public interface Configration
{
    public void set(String path, Object value);

    public Map<String, Object> getMap(String path, Map<String, Object> def);
    public <T> List<T> getList(String path, List<T> def);
    public <T> T get(String path, T def);
    public boolean save();
    public boolean reload();
}
