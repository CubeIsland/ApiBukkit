package de.codeinfection.quickwango.ApiBukkit.Net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CodeInfection
 */
public class Parameters extends HashMap<String, Object>
{

    public Parameters(Map<? extends String, ? extends Object> m)
    {
        super(m);
    }

    public Parameters()
    {}

    public Parameters(int initialCapacity)
    {
        super(initialCapacity);
    }

    public Parameters(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }

    public String getString(String key)
    {
        return this.getString(key, null);
    }

    public String getString(String key, String def)
    {
        Object value = this.get(key);
        if (value != null && value instanceof String)
        {
            return (String)value;
        }
        else
        {
            return def;
        }
    }

    public void setProperty(String key, String value)
    {
        this.put(key, value);
    }

    public String getProperty(String key)
    {
        return this.getString(key);
    }

    public String getProperty(String key, String def)
    {
        return this.getString(key, def);
    }

    public Parameters getParameters(String key)
    {
        Object value = this.get(key);
        if (value != null && value instanceof Parameters)
        {
            return (Parameters)value;
        }
        else
        {
            return null;
        }
    }

    public List<Object> getList(String key, List<Object> def)
    {
        Object value = this.get(key);
        if (value != null && value instanceof List)
        {
            return (List<Object>)value;
        }
        else
        {
            return def;
        }
    }

    public void putList(String key, Object value)
    {
        Object tmp = this.get(key);
        if (tmp != null)
        {
            if (tmp instanceof List)
            {
                ((List<Object>)tmp).add(value);
            }
            else
            {
                List<Object> list = new ArrayList<Object>();
                list.add(tmp);
                list.add(value);
                this.put(key, list);
            }
        }
        else
        {
            this.put(key, value);
        }
    }

    public Object get(List<String> path)
    {
        int keyCount = path.size() - 1;

        String key = null;
        Parameters node = this;
        Object tmpNode = null;
        for (int i = 0; i < keyCount; ++i)
        {
            key = path.get(i);
            tmpNode = node.get(key);
            if (tmpNode != null)
            {
                if (tmpNode instanceof Parameters)
                {
                    node = (Parameters)tmpNode;
                }
                else
                {
                    throw new IllegalArgumentException("The given path cannot be mapped!");
                }
            }
            else
            {
                return null;
            }
        }

        return node.get(path.get(keyCount));
    }

    public void put(List<String> path, Object value)
    {
        int keyCount = path.size() - 1;

        String key = null;
        Parameters node = this;
        Object tmpNode = null;
        for (int i = 0; i < keyCount; ++i)
        {
            key = path.get(i);
            tmpNode = node.get(key);
            if (tmpNode != null)
            {
                if (tmpNode instanceof Parameters)
                {
                    node = (Parameters)tmpNode;
                }
                else
                {
                    throw new IllegalArgumentException("The given path cannot be mapped!");
                }
            }
            else
            {
                Parameters newNode = new Parameters();
                node.put(key, newNode);
                node = newNode;
            }
        }

        node.putList(path.get(keyCount), value);
    }

    public boolean containsKey(List<String> path)
    {
        int keyCount = path.size() - 1;

        String key = null;
        Parameters node = this;
        Object tmpNode = null;
        for (int i = 0; i < keyCount; ++i)
        {
            key = path.get(i);
            tmpNode = node.get(key);
            if (tmpNode != null)
            {
                if (tmpNode instanceof Parameters)
                {
                    node = (Parameters)tmpNode;
                }
                else
                {
                    throw new IllegalArgumentException("The given path cannot be mapped!");
                }
            }
            else
            {
                return false;
            }
        }

        return node.containsKey(path.get(keyCount));
    }
}
