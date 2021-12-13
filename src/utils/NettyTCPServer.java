package dronelab.utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

//https://itsallbinary.com/netty-project-understanding-netty-using-simple-real-world-example-of-chat-server-client-good-for-beginners/
//https://github.com/narkhedesam/Netty-Simple-UDP-TCP-server-client
public final class NettyTCPServer {
 
	// Port where chat server will listen for connections. 
	// Command line arg: -Dport=8807
	static final int PORT =  Integer.parseInt(System.getProperty("port"));
 
	public void server() throws Exception {
        System.out.println("Starting TCP server on port: "+ System.getProperty("port"));
		 
		/*
		 * Configure the server.
		 */
 
		// Create boss & worker groups. Boss accepts connections from client. Worker
		// handles further communication through connections.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
 
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup) // Set boss & worker groups
					.channel(NioServerSocketChannel.class)// Use NIO to accept new connections.
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							/*
							 * Socket/channel communication happens in byte streams. String decoder &
							 * encoder helps conversion between bytes & String.
							 */
							p.addLast(new StringDecoder());
							p.addLast(new StringEncoder());
 
							// This is our custom server handler which will have logic for chat.
							p.addLast(new NettyTCPServerHandler());
						}
					});
 
			// Start the server.
			ChannelFuture f = b.bind(PORT).sync();
			System.out.println("TCP Server started. Ready to accept clients.");
 
			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
