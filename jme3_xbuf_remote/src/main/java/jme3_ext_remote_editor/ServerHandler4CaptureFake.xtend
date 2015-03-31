package jme3_ext_remote_editor;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
class ServerHandler4CaptureFake extends ChannelInboundHandlerAdapter {

	override channelRead(ChannelHandlerContext ctx, Object msg0) {
		System.out.println(">>> channelRead")
		val msg = msg0 as ByteBuf
		val w = msg.readInt()
		val h = msg.readInt()
		msg.release()
		System.out.println(">>>" +  w + " x " + h);
		val out0 = newByteArrayOfSize(w * h * 4)
		for(var y = 0; y < h; y++) {
			for(var x =0; x < w; x++) {
				val offset = (x + y * w) * 4;
				out0.set(offset + 0, ((x * 255.0)/w) as byte)
				out0.set(offset + 1, ((y * 255.0)/h) as byte)
				out0.set(offset + 2, 0 as byte)
				out0.set(offset + 3, 0xFF as byte)
			}
		}
		val out = ctx.alloc().buffer(w * h * 4)
		out.writeBytes(out0)
		ctx.writeAndFlush(out)
		System.out.println(">>> send")
		//ctx.write(msg); // (1)
		//ctx.flush(); // (2)
	}

	override exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}