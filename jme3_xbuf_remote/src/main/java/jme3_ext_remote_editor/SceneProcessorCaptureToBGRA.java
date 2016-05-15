package jme3_ext_remote_editor;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;

import lombok.Data;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

class SceneProcessorCaptureToBGRA implements SceneProcessor {

	@Getter RenderManager renderManager;
	@Getter ViewPort viewPort;

	public final AtomicReference<ReshapeInfo> askReshape = new AtomicReference<ReshapeInfo>();
	public final AtomicReference<Function<ByteBuffer, Boolean>> askNotify = new AtomicReference<Function<ByteBuffer, Boolean>>();

	private TransfertImage timage;
	private Function<ByteBuffer, Boolean> notify;


	@Override
	public void initialize(RenderManager rm, ViewPort vp) {
		this.renderManager = rm;
		this.viewPort = vp;
	}

	private TransfertImage reshapeInThread(int width0, int height0, boolean fixAspect) {
		TransfertImage ti = new TransfertImage(width0, height0);
		viewPort.getCamera().resize(width0, height0, fixAspect);
		//renderManager.getRenderer().setMainFrameBufferOverride(ti.fb);

		//renderManager.notifyReshape(ti.width, ti.height) // side effect on every viewport
		// NOTE: Hack alert. This is done ONLY for custom framebuffers.
		// Main framebuffer should use RenderManager.notifyReshape().
		for (SceneProcessor sp : this.viewPort.getProcessors()){
			sp.reshape(this.viewPort, ti.width, ti.height);
		}
		return ti;
	}


	@Override
	public boolean isInitialized() {
		return viewPort != null && renderManager != null;
	}

	@Override
	public void preFrame(float tpf) {
		if (timage != null) {
			renderManager.getRenderer().setMainFrameBufferOverride(timage.fb);
		} else {
			renderManager.getRenderer().setMainFrameBufferOverride(null);
		}
	}

	@Override
	public void postQueue(RenderQueue rq) {
	}

	@Override
	public void postFrame(FrameBuffer out) {
		if (timage != null && notify != null) {
			//		if (out != timage.fb){
			//			throw new IllegalStateException("Why did you change the output framebuffer? " + out + " != " + timage.fb);
			//		}
			if (timage.copyFrameBufferToBGRA(renderManager, notify)) {
				notify = null;
			}
		}
		// for the next frame
		ReshapeInfo askR = askReshape.getAndSet(null);
		if (askR != null){
			int w = Math.max(1, askR.with);
			int h = Math.max(1, askR.height);
			if (timage != null && (timage.width != w || timage.height != h)) {
				timage.dispose();
				timage = null;
			}
			if (timage == null) {
				timage = reshapeInThread(w, h, askR.fixAspect);
			}
		}
		Function<ByteBuffer, Boolean> askN = askNotify.getAndSet(null);
		if (askN != null) {
			notify = askN;
		}
		renderManager.getRenderer().setMainFrameBufferOverride(null);
	}

	@Override
	public void cleanup() {
		if (timage != null) {
			timage.dispose();
			timage = null;
		}
		renderManager.getRenderer().setMainFrameBufferOverride(null);
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {
	}

	@Data
	static class ReshapeInfo {
		final int with;
		final int height;
		final boolean fixAspect;
	}

	//TODO try to use netty Bytebuff with refcount, to avoid destroying buffer when other (network) read it
	//TODO try to use netty Bytebuff, to avoid synchronized(bytebuf){...} that doesn't prevent trying to read a destroyed bytebuf
	static class TransfertImage {
		public final int width;
		public final int height;
		public final FrameBuffer fb;
		public final ByteBuffer byteBuf;

		//static final int BGRA_size = 8 * 4; // format of image returned by  readFrameBuffer (ignoring format in framebuffer.color
		static int BGRA_size = 4; // format of image returned by  readFrameBuffer (ignoring format in framebuffer.color

		TransfertImage(int width, int height) {
			this.width = width;
			this.height = height;
			fb = new FrameBuffer(width, height, 1);
			fb.setDepthBuffer(Format.Depth);
			fb.setColorBuffer(Format.BGRA8);
			byteBuf = BufferUtils.createByteBuffer(width * height * BGRA_size);
		}

		/** SHOULD run in JME'Display thread */
		public boolean copyFrameBufferToBGRA(RenderManager rm, Function<ByteBuffer, Boolean> notify) {
			synchronized (byteBuf) {
				byteBuf.clear();
				rm.getRenderer().readFrameBufferWithFormat(fb, byteBuf, Format.BGRA8);
				//System.out.println("copyFrameBufferToBGRA :"+ byteBuf.hashCode() +  " .. " + byteBuf.position() + "/" + byteBuf.limit() + "/" + fb.getNumColorBuffers());
				//byteBuf.position(0);
			}
			//TODO return an slice of byteBuf ??
			return notify.apply(byteBuf);
		}

		public void dispose() {
			fb.dispose();
			synchronized (byteBuf) {
				BufferUtils.destroyDirectBuffer(byteBuf);
			}
		}
	}
}
