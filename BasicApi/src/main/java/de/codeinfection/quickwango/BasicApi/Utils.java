package de.codeinfection.quickwango.BasicApi;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class Utils
{
    private Utils()
    {}

    private static final double armorPoints[] = {
        3, 6, 8, 3
    };

    public static int getArmorPoints(Player player)
    {
        int currentDurability = 0;
        int baseDurability = 0;
        int baseArmorPoints = 0;
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorItems = new ItemStack[4];
        armorItems[0] = inventory.getBoots();
        armorItems[1] = inventory.getLeggings();
        armorItems[2] = inventory.getChestplate();
        armorItems[3] = inventory.getHelmet();
        for (int i = 0; i < 4; ++i)
        {
            if (armorItems[i] == null)
            {
                continue;
            }
            int dur = armorItems[i].getDurability();
            int max = armorItems[i].getType().getMaxDurability();
            if (max <= 0)
            {
                continue;
            }
            if (i == 2)
            {
                max = max + 1; /*
                 * Always 1 too low for chestplate
                 */
            }
            else
            {
                max = max - 3; /*
                 * Always 3 too high, versus how client calculates it
                 */
            }
            baseDurability += max;
            currentDurability += max - dur;
            baseArmorPoints += armorPoints[i];
        }
        int ap = 0;
        if (baseDurability > 0)
        {
            ap = ((baseArmorPoints - 1) * currentDurability) / baseDurability + 1;
        }
        return ap;
    }

    public static String getCardinalDirection(float yaw)
    {
        yaw = (yaw - 90) % 360;
        if (yaw < 0)
        {
            yaw += 360.0;
        }
        if (0 <= yaw && yaw < 22.5)
        {
            return "N";
        }
        else if (22.5 <= yaw && yaw < 67.5)
        {
            return "NE";
        }
        else if (67.5 <= yaw && yaw < 112.5)
        {
            return "E";
        }
        else if (112.5 <= yaw && yaw < 157.5)
        {
            return "SE";
        }
        else if (157.5 <= yaw && yaw < 202.5)
        {
            return "S";
        }
        else if (202.5 <= yaw && yaw < 247.5)
        {
            return "SW";
        }
        else if (247.5 <= yaw && yaw < 292.5)
        {
            return "W";
        }
        else if (292.5 <= yaw && yaw < 337.5)
        {
            return "NW";
        }
        else if (337.5 <= yaw && yaw < 360.0)
        {
            return "N";
        }
        else
        {
            return null;
        }
    }

    public static Map<String, String> getPropertiesByPrefix(String prefix)
    {
        Map<String, String> properties = new HashMap<String, String>();
        int prefixLen = prefix.length();

        for (Map.Entry entry : System.getProperties().entrySet())
        {
            String key = String.valueOf(entry.getKey());
            if (key.startsWith(prefix))
            {
                String value = String.valueOf(entry.getValue());

                properties.put(key.substring(prefixLen), value);
            }
        }

        return properties;
    }
    private static final Set<Map.Entry<String, String>> colorReplacements;

    static
    {
        Map<String, String> tmp = new HashMap<String, String>();
        tmp.put("\033[0m", "&0");
        tmp.put("\033[34m", "&1");
        tmp.put("\033[32m", "&2");
        tmp.put("\033[36m", "&3");
        tmp.put("\033[31m", "&4");
        tmp.put("\033[35m", "&5");
        tmp.put("\033[33m", "&6");
        tmp.put("\033[37m", "&7");

        colorReplacements = tmp.entrySet();
    }

    public static String reverseChatColors(String string)
    {
        for (Map.Entry<String, String> entry : colorReplacements)
        {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }
    private static Throwable worldCreationException = null;

    public static void createWorldSync(final Plugin plugin, final String name, final Environment env, final long seed, final ChunkGenerator generator)
    {
        final Thread executionThread = Thread.currentThread();

        if (plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                try
                {
                    WorldCreator creator = new WorldCreator(name);
                    creator.environment(env).seed(seed).generator(generator).createWorld();
                }
                catch (Throwable t)
                {
                    worldCreationException = t;
                }
                synchronized (executionThread)
                {
                    executionThread.notify();
                }
            }
        }) < 0)
        {
            throw new ApiRequestException("Failed to schedule creation task!", 6);
        }
        try
        {
            synchronized (executionThread)
            {
                executionThread.wait();
            }
        }
        catch (InterruptedException e)
        {
            ApiBukkit.error("World generation was interupted!", e);
        }
        if (worldCreationException != null)
        {
            ApiBukkit.logException(worldCreationException);
            worldCreationException = null;
            throw new ApiRequestException("World generation failed due to an unknown error!", 7);
        }
    }
}
