package jme3_ext_remote_editor

import com.jme3.post.SceneProcessor
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.renderer.queue.RenderQueue
import com.jme3.texture.FrameBuffer
import com.jme3.texture.Image.Format
import com.jme3.util.BufferUtils
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtext.xbase.lib.Functions.Function1
import org.eclipse.xtend.lib.annotations.Accessors

class SceneProcessorCaptureToBGRA implements SceneProcessor {

	@Accessors(PUBLIC_GETTER) var RenderManager renderManager
	@Accessors(PUBLIC_GETTER) var ViewPort viewPort

	public val askReshape = new AtomicReference<ReshapeInfo>()
	public val askNotify = new AtomicReference<Function1<ByteBuffer, Boolean>>()

	private var TransfertImage timage
	private var Function1<ByteBuffer, Boolean> notify;


	override initialize(RenderManager rm, ViewPort vp) {
		this.renderManager = rm
		this.viewPort = vp
	}

	private def TransfertImage reshapeInThread(int width0, int height0, boolean fixAspect) {
		val ti = new TransfertImage(width0, height0)
		viewPort.getCamera().resize(width0, height0, fixAspect)
		//renderManager.getRenderer().setMainFrameBufferOverride(ti.fb);

		//renderManager.notifyReshape(ti.width, ti.height) // side effect on every viewport
		// NOTE: Hack alert. This is done ONLY for custom framebuffers.
		// Main framebuffer should use RenderManager.notifyReshape().
		for (SceneProcessor sp : this.viewPort.getProcessors()){
			sp.reshape(this.viewPort, ti.width, ti.height);
		}
		return ti;
	}


	override boolean isInitialized() {
		viewPort != null && renderManager != null
	}

	override preFrame(float tpf) {
		if (timage != null) {
			renderManager.getRenderer().setMainFrameBufferOverride(timage.fb);
		} else {
			renderManager.getRenderer().setMainFrameBufferOverride(null);
		}
	}

	override postQueue(RenderQueue rq) {
	}

	override postFrame(FrameBuffer out) {
		if (timage != null && notify != null) {
			//		if (out != timage.fb){
			//			throw new IllegalStateException("Why did you change the output framebuffer? " + out + " != " + timage.fb);
			//		}
			if (timage.copyFrameBufferToBGRA(renderManager, notify)) {
				notify = null;
			}
		}
		// for the next frame
		val askR = askReshape.getAndSet(null);
		if (askR != null){
			val w = Math.max(1, askR.with);
			val h = Math.max(1, askR.height);
			if (timage != null && (timage.width != w || timage.height != h)) {
				timage.dispose();
				timage = null;
			}
			if (timage == null) {
				timage = reshapeInThread(w, h, askR.fixAspect);
			}
		}
		val askN = askNotify.getAndSet(null);
		if (askN != null) {
			notify = askN;
		}
		renderManager.getRenderer().setMainFrameBufferOverride(null);
	}

	override cleanup() {
		if (timage != null) {
			timage.dispose()
			timage = null;
		}
		renderManager.getRenderer().setMainFrameBufferOverride(null)
	}

	override reshape(ViewPort vp, int w, int h) {
	}

	@Data
	static class ReshapeInfo {
		int with
		int height
		boolean fixAspect
	}

	//TODO try to use netty Bytebuff with refcount, to avoid destroying buffer when other (network) read it
	//TODO try to use netty Bytebuff, to avoid synchronized(bytebuf){...} that doesn't prevent trying to read a destroyed bytebuf
	static class TransfertImage {
		val int width
		val int height
		val FrameBuffer fb
		val ByteBuffer byteBuf

		//static final int BGRA_size = 8 * 4; // format of image returned by  readFrameBuffer (ignoring format in framebuffer.color
		static val BGRA_size = 4; // format of image returned by  readFrameBuffer (ignoring format in framebuffer.color

		new(int width, int height) {
			this.width = width
			this.height = height
			fb = new FrameBuffer(width, height, 1)
			fb.setDepthBuffer(Format.Depth)
			fb.setColorBuffer(Format.ABGR8)
			byteBuf = BufferUtils.createByteBuffer(width * height * BGRA_size)
		}

		/** SHOULD run in JME'Display thread */
		def boolean copyFrameBufferToBGRA(RenderManager rm, Function1<ByteBuffer, Boolean> notify) {
			synchronized (byteBuf) {
				byteBuf.clear()
				rm.getRenderer().readFrameBuffer(null, byteBuf)
				//System.out.println("copyFrameBufferToBGRA :"+ byteBuf.hashCode() +  " .. " + byteBuf.position() + "/" + byteBuf.limit() + "/" + fb.getNumColorBuffers());
				//byteBuf.position(0);
			}
			//TODO return an slice of byteBuf ??
			return notify.apply(byteBuf)
		}

		def dispose() {
			fb.dispose();
			synchronized (byteBuf) {
				BufferUtils.destroyDirectBuffer(byteBuf)
			}
		}
	}
}