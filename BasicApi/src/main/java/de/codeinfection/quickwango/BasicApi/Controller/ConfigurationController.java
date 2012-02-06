package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.Server.Action;
import de.codeinfection.quickwango.ApiBukkit.Server.Controller;
import de.codeinfection.quickwango.ApiBukkit.Server.Parameters;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.io.*;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller
public class ConfigurationController extends ApiController
{
    protected List<String> availableConfigs;

    public ConfigurationController(Plugin plugin, List<String> paths)
    {
        super(plugin);
        BasicApi.debug("Got " + paths.size() + " config paths...");

        this.availableConfigs = paths;
    }

    @Override
    public Object defaultAction(String action, Parameters params, Server server)
    {
        return this.getActions().keySet();
    }
    
    @Action
    public Object write(Parameters params, Server server)
    {
        String fileParam = params.getString("file");
        String appendParam = params.getString("append");
        String dataParam = params.getString("data");
        if (fileParam != null)
        {
            if (availableConfigs.contains(fileParam))
            {
                if (dataParam != null)
                {
                    try
                    {
                        System.out.println(dataParam);
                        boolean append = false;
                        if (appendParam != null && appendParam.equalsIgnoreCase("true"))
                        {
                            append = true;
                        }
                        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileParam)));
                        if (append)
                        {
                            writer.append(dataParam);
                        }
                        else
                        {
                            writer.write(dataParam);
                        }
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        throw new ApiRequestException("Failed to write the data", 4);
                    }
                }
                else
                {
                    throw new ApiRequestException("No data given", 3);
                }
            }
            else
            {
                throw new ApiRequestException("Access denied for the requested file", 2);
            }
            return null;
        }
        else
        {
            throw new ApiRequestException("No file given!", 1);
        }
    }
    
    @Action
    public Object read(Parameters params, Server server)
    {
        String fileParam = params.getString("file");
        if (fileParam != null)
        {
            if (availableConfigs.contains(fileParam))
            {
                File file = new File(fileParam);
                if (file.exists())
                {
                    try
                    {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            stringBuilder.append(line).append("\n");
                        }

                        params.put("format", "raw");
                        return stringBuilder.toString();
                    }
                    catch (IOException e)
                    {
                        throw new ApiRequestException("Failed to read the file", 4);
                    }
                }
                else
                {
                    throw new ApiRequestException("File not found!", 3);
                }
            }
            else
            {
                throw new ApiRequestException("Access denied for the requested file", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No file given!", 1);
        }
    }

    @Action
    public Object remove(Parameters params, Server server)
    {
        String fileParam = params.getString("file");
        if (fileParam != null)
        {
            if (availableConfigs.contains(fileParam))
            {
                (new File(fileParam)).delete();
            }
            else
            {
                throw new ApiRequestException("Access denied for the requested file", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No file given!", 1);
        }
        return null;
    }
    
    @Action
    public Object exists(Parameters params, Server server)
    {
        String fileParam = params.getString("file");
        if (fileParam != null)
        {
            if (availableConfigs.contains(fileParam))
            {
                return (new File(fileParam).exists());
            }
            else
            {
                throw new ApiRequestException("Access denied for the requested file", 2);
            }
        }
        else
        {
            throw new ApiRequestException("No file given!", 1);
        }
    }
}
