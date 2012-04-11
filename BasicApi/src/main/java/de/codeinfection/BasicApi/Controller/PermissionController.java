package de.codeinfection.BasicApi.Controller;

import de.codeinfection.quickwango.ApiBukkit.ApiServer.Action;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiController;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiRequest;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.ApiResponse;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Controller;
import de.codeinfection.quickwango.ApiBukkit.ApiServer.Exceptions.ApiRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
@Controller(name = "permission")
public class PermissionController extends ApiController
{
    public PermissionController(Plugin plugin)
    {
        super(plugin);
    }

    @Action(serializer = "json")
    public void getall(ApiRequest request, ApiResponse response)
    {
        response.setContent(getServer().getPluginManager().getPermissions());
    }

    @Action(parameters = {"node"}, serializer = "json")
    public void getdefault(ApiRequest request, ApiResponse response)
    {
        Permission permission = getServer().getPluginManager().getPermission(request.params.getString("node"));
        if (permission != null)
        {
            response.setContent(permission.getDefault().toString());
        }
        else
        {
            throw new ApiRequestException("permission not found!", 1);
        }
    }

    @Action(parameters = {"player"}, serializer = "json")
    public void getplayerpermissions(ApiRequest request, ApiResponse response)
    {
        Permissible permissible = getServer().getPlayerExact(request.params.getString("player"));
        if (permissible != null)
        {
            Set<PermissionAttachmentInfo> permissionInfo = permissible.getEffectivePermissions();
            Map<String, Boolean> permissions = new HashMap<String, Boolean>();

            for (PermissionAttachmentInfo current : permissionInfo)
            {
                permissions.put(current.getPermission(), current.getValue());
            }

            response.setContent(permissions);
        }
        else
        {
            throw new ApiRequestException("player not found!", 1);
        }
    }

    @Action(parameters = {"player", "permission", "value", "ticks"})
    public void setplayerpermissions(ApiRequest request, ApiResponse response)
    {
        Permissible permissible = getServer().getPlayerExact(request.params.getString("player"));
        if (permissible != null)
        {
            try
            {
                permissible.addAttachment(
                    getPlugin(),
                    request.params.getString("permission"),
                    Boolean.parseBoolean(request.params.getString("value")),
                    Math.abs(Integer.parseInt(request.params.getString("ticks")))
                );
            }
            catch (NumberFormatException e)
            {
                throw new ApiRequestException("The value given for ticks is not a valid number!", 2);
            }
        }
        else
        {
            throw new ApiRequestException("player not found!", 1);
        }
    }
}
