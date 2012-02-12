package ApiServer;

import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 *
 * @author CodeInfection
 */
public class ApiServer
{
    private static ApiServer instance = null;

    private int port;
    private int maxContentLength;
    private String authenticationKey;

    public ApiServer()
    {
        this.port = 6561;
        this.maxContentLength = 1048576;
    }

    public static ApiServer getInstance()
    {
        if (instance == null)
        {
            instance = new ApiServer();
        }
        return instance;
    }

    public int getPort()
    {
        return this.port;
    }

    public ApiServer setPort(int port)
    {
        this.port = port;
        return this;
    }

    public int getMaxContentLength()
    {
        return this.maxContentLength;
    }

    public ApiServer setmaxContentLength(int maxContentLength)
    {
        this.maxContentLength = maxContentLength;
        return this;
    }

    public String getAuthenticationKey()
    {
        return this.authenticationKey;
    }

    public ApiServer setAuthenticationKey(String authkey)
    {
        this.authenticationKey = authkey;
        return this;
    }

    public void start()
    {
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ApiServerPipelineFactory());
    }
}
