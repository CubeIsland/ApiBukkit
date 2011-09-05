package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class WorldController extends AbstractRequestController
{
    public WorldController(Plugin plugin)
    {
        super(plugin, true);
        
        this.registerAction("info",         new InfoAction());
        this.registerAction("create",       new CreateAction());
        this.registerAction("time",         new TimeAction());
        this.registerAction("pvp",          new PvpAction());
        this.registerAction("storm",        new StormAction());
        this.registerAction("spawn",        new SpawnAction());
        this.registerAction("list",         new ListAction());
        this.registerAction("players",      new PlayersAction());
        this.registerAction("spawnflags",   new SpawnflagsAction());
        this.registerAction("save",         new SaveAction());
    }

    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        return this.getActions().keySet();
    }
    
    private class InfoAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("name", world.getName());
                    data.put("time", world.getTime());
                    data.put("fullTime", world.getFullTime());
                    data.put("seed", world.getSeed());
                    data.put("pvp", world.getPVP());
                    data.put("environment", world.getEnvironment().toString());
                    data.put("thunderDuration", world.getThunderDuration());
                    data.put("weatherDuration", world.getWeatherDuration());
                    data.put("animals", world.getAllowAnimals());
                    data.put("monsters", world.getAllowMonsters());

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
                    throw new RequestException("World not found!", 1);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
        }
    }
    
    private class CreateAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world == null)
                {
                    World.Environment env = World.Environment.NORMAL;
                    String envParam = params.getProperty("environment");
                    if (envParam != null)
                    {
                        if (envParam.equalsIgnoreCase("nether"))
                        {
                            env = World.Environment.NETHER;
                        }
                        else if (envParam.equalsIgnoreCase("normal"))
                        {
                            env = World.Environment.NORMAL;
                        }
                        else if (envParam.equalsIgnoreCase("skylands"))
                        {
                            env = World.Environment.SKYLANDS;
                        }
                        else
                        {
                            throw new RequestException("Invalid environment specified!", 3);
                        }
                    }

                    String seed = params.getProperty("seed");
                    if (seed != null)
                    {
                        Long seedValue;
                        if (seed.matches("/^\\d+$/"))
                        {
                            seedValue = Long.valueOf(seed);
                        }
                        else
                        {
                            seedValue = (long)seed.hashCode();
                        }
                        server.createWorld(worldName, env, seedValue);
                    }
                    else
                    {
                        server.createWorld(worldName, env);
                    }
                }
                else
                {
                    throw new RequestException("World does already exist!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class TimeAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String timeParam = params.getProperty("time");
                    if (timeParam != null)
                    {
                        try
                        {
                            long time = Long.valueOf(timeParam);
                            world.setTime(time);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new RequestException("Time must be a valid number!", 4);
                        }
                    }
                    else
                    {
                        throw new RequestException("No time given!", 3);
                    }
                }
                else
                {
                    throw new RequestException("World not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class PvpAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String state = params.getProperty("pvp");
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
                            throw new RequestException("Invalid state given! Use on or off", 3);
                        }
                    }
                    else
                    {
                        world.setPVP(world.getPVP() ? false : true);
                    }
                }
                else
                {
                    throw new RequestException("World not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class StormAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String state = params.getProperty("storm");
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
                            throw new RequestException("Invalid state given! Use on or off", 4);
                        }
                    }
                    else
                    {
                        throw new RequestException("No state given!", 3);
                    }
                }
                else
                {
                    throw new RequestException("World not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class SpawnAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    String locationParam = params.getProperty("location");
                    String playerName = params.getProperty("player");
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
                                throw new RequestException("No valid location given!", 5);
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            throw new RequestException("No valid location given!", 5);
                        }
                    }
                    else if (playerName != null)
                    {
                        Player player = server.getPlayer(playerName);
                        if (player != null)
                        {
                            Location playerLocation = player.getLocation();
                            world.setSpawnLocation(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
                        }
                        else
                        {
                            throw new RequestException("Given player not found", 4);
                        }
                    }
                    else
                    {
                        throw new RequestException("No location given!", 3);
                    }
                }
                else
                {
                    throw new RequestException("World not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
            return null;
        }
    }
    
    private class ListAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            List<World> worlds = server.getWorlds();
            List<String> data = new ArrayList<String>();
            for (World currentWorld : worlds)
            {
                data.add(currentWorld.getName());
            }
            return data;
        }
    }

    private class PlayersAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
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
                    throw new RequestException("World " + worldName + " not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
        }
    }

    private class SpawnflagsAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    boolean monsters = world.getAllowMonsters();
                    boolean animals = world.getAllowAnimals();

                    String state = params.getProperty("monsters");
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
                    state = params.getProperty("animals");
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
                    throw new RequestException("World " + worldName + " not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
        }
    }

    private class SaveAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            String worldName = params.getProperty("world");
            if (worldName != null)
            {
                World world = server.getWorld(worldName);
                if (world != null)
                {
                    world.save();
                }
                else
                {
                    throw new RequestException("Given world not found!", 2);
                }
            }
            else
            {
                throw new RequestException("No world given!", 1);
            }
            
            return null;
        }
    }
}
