/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.codeinfection.quickwango.ApiBukkit;

import de.codeinfection.quickwango.ApiBukkit.Request.AbstractRequestController;
import de.codeinfection.quickwango.ApiBukkit.Request.RequestException;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class ValidateController extends AbstractRequestController
{
    public ValidateController(Plugin plugin)
    {
        super(plugin, true);
        
        this.setAction("authkey", new AuthkeyAction());
    }
    
    @Override
    public Object defaultAction(String action, Properties params, Server server) throws RequestException
    {
        throw new RequestException("No default action!", 1);
    }
    
    private class AuthkeyAction extends RequestAction
    {
        @Override
        public Object run(Properties params, Server server) throws RequestException
        {
            return null;
        }
    }
}
