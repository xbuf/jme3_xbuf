package jme3_ext_remote_editor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ServerHandler extends ChannelInboundHandlerAdapter {

	public final ReqHandler remoteHandler;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		remoteHandler.enable();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		remoteHandler.disable();
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
		remoteHandler.channelRead(ctx, msg0);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}
