package ApiServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public class ApiRequest
{
    public final Map<String, List<String>> GET;
    public final Map<String, List<String>> POST;
    public final Map<String, List<String>> REQUEST;
    public final Map<String, Object> SERVER;
    public final Map<String, String> headers;
    public final ApiResponse response;
    public final Server server;

    public ApiRequest(ApiResponse response, Server server)
    {
        this.GET = new HashMap<String, List<String>>();
        this.POST = new HashMap<String, List<String>>();
        this.REQUEST = new HashMap<String, List<String>>();
        this.SERVER = new HashMap<String, Object>();
        this.headers = new HashMap<String, String>();

        this.response = response;
        this.server = server;
    }    
}
