package jme3_ext_remote_editor;

import jme3_ext_xbuf.Xbuf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor
class AppState4RemoteCommand extends AbstractAppState {

	public val int port
	public val Xbuf xbuf

	var ChannelFuture f;
	var EventLoopGroup bossGroup
	var EventLoopGroup workerGroup
	var SimpleApplication app

	def start() {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();

		val b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
		.channel(typeof(NioServerSocketChannel))
		.childHandler(new ChannelInitializer<SocketChannel>() {
			override initChannel(SocketChannel ch) {
				val rh = new ReqHandler(
					AppState4RemoteCommand.this.app
					, AppState4RemoteCommand.this.xbuf
				);
				ch.pipeline().addLast(
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 1, 4)
					,new ServerHandler(rh)
				);
			}
		})
		.option(ChannelOption.SO_BACKLOG, 128)
		.childOption(ChannelOption.SO_KEEPALIVE, true)

		// Bind and start to accept incoming connections.
		f = b.bind(port).sync();

	}

	def stop(){
		workerGroup?.shutdownGracefully();
		bossGroup?.shutdownGracefully();
		f?.channel().close().sync();
	}

	override initialize(com.jme3.app.state.AppStateManager stateManager0, com.jme3.app.Application app0) {
		try {
			app = app0 as SimpleApplication
			start()
		} catch (Exception e) {
			e.printStackTrace()
		}
	}

	override cleanup() {
		try {
			stop()
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
