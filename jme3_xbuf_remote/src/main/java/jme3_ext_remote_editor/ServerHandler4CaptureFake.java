package jme3_ext_remote_editor;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
class ServerHandler4CaptureFake extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg0) {
		System.out.println(">>> channelRead");
		ByteBuf msg = (ByteBuf)msg0;
		int w = msg.readInt();
		int h = msg.readInt();
		msg.release();
		System.out.println(">>>" +  w + " x " + h);
		byte[] out0 = new byte[w * h * 4];
		for(int y = 0; y < h; y++) {
			for(int x =0; x < w; x++) {
				int offset = (x + y * w) * 4;
				out0[offset + 0] = (byte)((x * 255.0)/w);
				out0[offset + 1] = (byte)((y * 255.0)/h);
				out0[offset + 2] = (byte)0;
				out0[offset + 3] = (byte)0xFF;
			}
		}
		ByteBuf out = ctx.alloc().buffer(w * h * 4);
		out.writeBytes(out0);
		ctx.writeAndFlush(out);
		System.out.println(">>> send");
		//ctx.write(msg); // (1)
		//ctx.flush(); // (2)
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}
