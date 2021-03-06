package de.cubeisland.ApiBukkit.ApiServer;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
 *
 * @author CodeInfection
 */
public class ApiServerPipelineFactory implements ChannelPipelineFactory
{
    public ChannelPipeline getPipeline() throws Exception
    {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(ApiServer.getInstance().getMaxContentLength()));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", new ApiServerHandler());

        return pipeline;
    }
}
