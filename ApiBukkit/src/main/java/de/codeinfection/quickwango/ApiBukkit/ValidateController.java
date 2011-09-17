/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.codeinfection.quickwango.ApiBukkit;

import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ValidateController extends ApiRequestController
{
    public ValidateController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("authkey", new AuthkeyAction());
    }
    
    @Override
    public Object defaultAction(String action, Properties params, Server server) throws ApiRequestException
    {
        throw new ApiRequestException("No default action!", 1);
    }
    
    private class AuthkeyAction extends ApiRequestAction
    {
        @Override
        public Object execute(Properties params, Server server) throws ApiRequestException
        {
            return null;
        }
    }
}
