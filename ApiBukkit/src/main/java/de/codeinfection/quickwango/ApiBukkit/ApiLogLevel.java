package de.codeinfection.quickwango.ApiBukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author CodeInfection
 */
public enum ApiLogLevel
{
    QUIET(0),
    ERROR(1, "ERROR", Level.SEVERE),
    DEFAULT(2),
    INFO(3),
    DEBUG(4, "DEBUG");

    private final static Map<Integer, ApiLogLevel> levelIdMap = new HashMap<Integer, ApiLogLevel>();
    private final static Map<String, ApiLogLevel> levelNameMap = new HashMap<String, ApiLogLevel>();
    public final int level;
    public final String prefix;
    public final Level logLevel;

    ApiLogLevel(int level, String prefix, Level logLevel)
    {
        this.level = level;
        this.prefix = prefix.toUpperCase();
        this.logLevel = logLevel;
    }

    ApiLogLevel(int level, String prefix)
    {
        this.level = level;
        this.prefix = prefix.toUpperCase();
        this.logLevel = Level.INFO;
    }

    ApiLogLevel(int level)
    {
        this.level = level;
        this.prefix = null;
        this.logLevel = Level.INFO;
    }

    public static ApiLogLevel getLogLevel(int level) throws Exception
    {
        ApiLogLevel logLevel = levelIdMap.get(level);
        if (logLevel == null)
        {
            throw new Exception("unknown LogLevel " + level);
        }
        return logLevel;
    }

    public static ApiLogLevel getLogLevel(String level) throws Exception
    {
        level = level.trim();
        try
        {
            return getLogLevel(Integer.valueOf(level));
        }
        catch (NumberFormatException e)
        {
        }

        ApiLogLevel logLevel = levelNameMap.get(level.toUpperCase());
        if (logLevel == null)
        {
            throw new Exception("unknown LogLevel " + level);
        }
        return logLevel;
    }

    static
    {
        for (ApiLogLevel logLevel : values())
        {
            levelIdMap.put(logLevel.level, logLevel);
            levelNameMap.put(logLevel.name(), logLevel);
        }
    }
}