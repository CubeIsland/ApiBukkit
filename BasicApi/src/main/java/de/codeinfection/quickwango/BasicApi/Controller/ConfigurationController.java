package de.codeinfection.quickwango.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiRequestException;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiManager;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.BasicApi.BasicApi;
import java.io.*;
import java.util.List;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "configuration")
public class ConfigurationController extends ApiController
{
    protected List<String> availableConfigs;

    public ConfigurationController(Plugin plugin, List<String> paths)
    {
        super(plugin);
        BasicApi.debug("Got " + paths.size() + " config paths...");

        this.availableConfigs = paths;
    }
    
    @Action(parameters = {"file", "data"})
    public void write(ApiRequest request, ApiResponse response)
    {
        String fileParam = request.REQUEST.getString("file");
        String appendParam = request.REQUEST.getString("append");
        String dataParam = request.REQUEST.getString("data");
        if (availableConfigs.contains(fileParam))
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
                throw new ApiRequestException("Failed to write the data", 2);
            }
        }
        else
        {
            throw new ApiRequestException("Access denied for the requested file", 1);
        }
    }

    @Action(parameters = {"file"}, serializer = "raw")
    public void read(ApiRequest request, ApiResponse response)
    {
        String fileParam = request.REQUEST.getString("file");
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

                    response.setSerializer(ApiManager.getInstance().getSerializer("raw"));
                    response.setContent(stringBuilder.toString());
                }
                catch (IOException e)
                {
                    throw new ApiRequestException("Failed to read the file", 3);
                }
            }
            else
            {
                throw new ApiRequestException("File not found!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("Access denied for the requested file", 1);
        }
    }

    @Action(parameters = {"file"})
    public void remove(ApiRequest request, ApiResponse response)
    {
        String fileParam = request.REQUEST.getString("file");
        if (availableConfigs.contains(fileParam))
        {
            (new File(fileParam)).delete();
        }
        else
        {
            throw new ApiRequestException("Access denied for the requested file", 2);
        }
    }

    @Action(parameters = {"file"})
    public void exists(ApiRequest request, ApiResponse response)
    {
        String fileParam = request.REQUEST.getString("file");
        if (availableConfigs.contains(fileParam))
        {
            response.setContent(new File(fileParam).exists());
        }
        else
        {
            throw new ApiRequestException("Access denied for the requested file", 2);
        }
    }
}
