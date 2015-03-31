package jme3_ext_remote_editor

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor
class ServerHandler extends ChannelInboundHandlerAdapter {

	val ReqHandler remoteHandler

	override channelActive(ChannelHandlerContext ctx) {
		super.channelActive(ctx);
		remoteHandler.enable();
	}

	override channelInactive(ChannelHandlerContext ctx) {
		remoteHandler.disable();
		super.channelInactive(ctx);
	}

	override channelRead(ChannelHandlerContext ctx, Object msg0) {
		remoteHandler.channelRead(ctx, msg0);
	}

	override exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}