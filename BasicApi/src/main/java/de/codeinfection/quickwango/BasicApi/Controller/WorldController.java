package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiBukkit;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Parameters;
import de.codeinfection.quickwango.BasicApi.Utils;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "world")
public class WorldController extends ApiController
{
    public WorldController(Plugin plugin)
    {
        super(plugin);
    }
    
    @Action
    public Object info(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("name",                world.getName());
                data.put("time",                world.getTime());
                data.put("fullTime",            world.getFullTime());
                data.put("seed",                world.getSeed());
                data.put("pvp",                 world.getPVP());
                data.put("environment",         world.getEnvironment().getId());
                data.put("thunderDuration",     world.getThunderDuration());
                data.put("weatherDuration",     world.getWeatherDuration());
                data.put("allowAnimals",        world.getAllowAnimals());
                data.put("allowMonsters",       world.getAllowMonsters());
                data.put("keepSpawnInMemory",   world.getKeepSpawnInMemory());
                data.put("entities",            world.getEntities().size());
                data.put("livingEntities",      world.getLivingEntities().size());
                data.put("difficulty",          world.getDifficulty().getValue());
                data.put("loadedChunks",        world.getLoadedChunks().length);
                data.put("players",             world.getPlayers().size());
                data.put("players",             world.getPlayers().size());
                data.put("maxHeight",           world.getMaxHeight());
                data.put("seaLevel",            world.getSeaLevel());
                data.put("seaLevel",            world.getSeaLevel());
                data.put("worldFolder",         world.getWorldFolder().toString());

                Location spawnLoc = world.getSpawnLocation();
                data.put("spawnLocation", new Integer[] {
                    spawnLoc.getBlockX(),
                    spawnLoc.getBlockY(),
                    spawnLoc.getBlockZ()
                });
                data.put("players", world.getPlayers().size());

                return data;
            }
            else
            {
                throw new ApiRequestException("World not found!", 1);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
    }
    
    @Action
    public Object create(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world == null)
            {
                String environmentParam = params.getString("environment");
                Environment env = null;
                if (environmentParam != null)
                {
                    try
                    {
                        env = World.Environment.getEnvironment(Integer.valueOf(environmentParam));
                    }
                    catch (NumberFormatException e)
                    {}
                }
                else
                {
                    throw new ApiRequestException("No environment specified!", 3);
                }

                if (env == null)
                {
                    throw new ApiRequestException("Invalid environment specified!", 4);
                }

                ChunkGenerator generator = null;
                String generatorParam = params.getString("generator");
                if (generatorParam != null)
                {
                    generatorParam = generatorParam.trim();
                    if (generatorParam.length() > 0)
                    {
                        String[] split = generatorParam.split(":", 2);
                        String id = (split.length > 1) ? split[1] : null;
                        Plugin plugin = server.getPluginManager().getPlugin(split[0]);

                        if (plugin == null || !plugin.isEnabled())
                        {
                            ApiBukkit.error("Could not set generator for default world '" + worldName + "': Plugin '" + split[0] + "' does not exist");
                            throw new ApiRequestException("Failed to load generator plugin", 5);
                        }
                        else
                        {
                            generator = plugin.getDefaultWorldGenerator(worldName, id);
                        }
                    }
                }

                long seed;
                String seedParam = params.getString("seed");
                if (seedParam != null)
                {
                    if (seedParam.matches("/^\\d+$/"))
                    {
                        seed = Long.valueOf(seedParam);
                    }
                    else
                    {
                        seed = (long)seedParam.hashCode();
                    }
                }
                else
                {
                    seed = (new Random()).nextLong();
                }

                
                Utils.createWorldSync(getPlugin(), worldName, env, seed, generator);
            }
            else
            {
                throw new ApiRequestException("World does already exist!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
        return null;
    }

    @Action
    public Object time(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                String timeParam = params.getString("time");
                if (timeParam != null)
                {
                    try
                    {
                        long time = Long.valueOf(timeParam);
                        world.setTime(time);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ApiRequestException("Time must be a valid number!", 4);
                    }
                }
                else
                {
                    throw new ApiRequestException("No time given!", 3);
                }
            }
            else
            {
                throw new ApiRequestException("World not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
        return null;
    }
    
    @Action
    public Object pvp(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                String state = params.getString("pvp");
                if (state != null)
                {
                    if (state.equalsIgnoreCase("on"))
                    {
                        world.setPVP(true);
                    }
                    else if (state.equalsIgnoreCase("off"))
                    {
                        world.setPVP(false);
                    }
                    else
                    {
                        throw new ApiRequestException("Invalid state given! Use on or off", 3);
                    }
                }
                else
                {
                    world.setPVP(world.getPVP() ? false : true);
                }
            }
            else
            {
                throw new ApiRequestException("World not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
        return null;
    }
    
    @Action
    public Object storm(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                String state = params.getString("storm");
                if (state != null)
                {
                    if (state.equalsIgnoreCase("on"))
                    {
                        world.setStorm(true);
                    }
                    else if (state.equalsIgnoreCase("off"))
                    {
                        world.setStorm(false);
                    }
                    else
                    {
                        throw new ApiRequestException("Invalid state given! Use on or off", 4);
                    }
                }
                else
                {
                    throw new ApiRequestException("No state given!", 3);
                }
            }
            else
            {
                throw new ApiRequestException("World not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
        return null;
    }

    @Action
    public Object spawn(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                String locationParam = params.getString("location");
                String playerName = params.getString("player");
                if (locationParam != null)
                {
                    String[] locationParts = locationParam.split(",");
                    try
                    {
                        int x, y, z;
                        if (locationParts.length > 2)
                        {
                            x = Integer.valueOf(locationParts[0]);
                            y = Integer.valueOf(locationParts[1]);
                            z = Integer.valueOf(locationParts[2]);
                            world.setSpawnLocation(x, y, z);
                        }
                        else
                        {
                            throw new ApiRequestException("No valid location given!", 5);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ApiRequestException("No valid location given!", 5);
                    }
                }
                else if (playerName != null)
                {
                    Player player = server.getPlayerExact(playerName);
                    if (player != null)
                    {
                        Location playerLocation = player.getLocation();
                        world.setSpawnLocation(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
                    }
                    else
                    {
                        throw new ApiRequestException("Given player not found", 4);
                    }
                }
                else
                {
                    throw new ApiRequestException("No location given!", 3);
                }
            }
            else
            {
                throw new ApiRequestException("World not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
        return null;
    }
    
    @Action
    public Object list(Parameters params, Server server)
    {
        List<World> worlds = server.getWorlds();
        List<String> data = new ArrayList<String>();
        for (World currentWorld : worlds)
        {
            data.add(currentWorld.getName());
        }
        return data;
    }
    
    @Action
    public Object players(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                List<String> data = new ArrayList<String>();
                Iterator<Player> playerIter = world.getPlayers().listIterator();
                while (playerIter.hasNext())
                {
                    data.add(playerIter.next().getName());
                }
                return data;
            }
            else
            {
                throw new ApiRequestException("World " + worldName + " not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
    }
    
    @Action
    public Object spawnflags(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                boolean monsters = world.getAllowMonsters();
                boolean animals = world.getAllowAnimals();

                String state = params.getString("monsters");
                if (state != null)
                {
                    if (state.equalsIgnoreCase("on"))
                    {
                        monsters = true;
                    }
                    else if (state.equalsIgnoreCase("off"))
                    {
                        monsters = false;
                    }
                }
                state = params.getString("animals");
                if (state != null)
                {
                    if (state.equalsIgnoreCase("on"))
                    {
                        animals = true;
                    }
                    else if (state.equalsIgnoreCase("off"))
                    {
                        animals = false;
                    }
                }

                world.setSpawnFlags(monsters, animals);
                return null;
            }
            else
            {
                throw new ApiRequestException("World " + worldName + " not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }
    }

    @Action
    public Object save(Parameters params, Server server)
    {
        String worldName = params.getString("world");
        if (worldName != null)
        {
            World world = server.getWorld(worldName);
            if (world != null)
            {
                world.save();
            }
            else
            {
                throw new ApiRequestException("Given world not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No world given!", 1);
        }

        return null;
    }
}
