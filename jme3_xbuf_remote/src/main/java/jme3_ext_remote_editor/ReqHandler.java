package jme3_ext_remote_editor;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.events.AnimationEvent;
import com.jme3.cinematic.events.CinematicEvent;
import com.jme3.cinematic.events.CinematicEventListener;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Spatial;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import jme3_ext_xbuf.LoggerCollector;
import jme3_ext_xbuf.Xbuf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import xbuf.Cmds;
import xbuf.Cmds.Cmd;
import xbuf.Cmds.SetEye.ProjMode;
import xbuf.Datas;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static jme3_ext_xbuf.ext.PrimitiveExt.*;
import com.jme3.math.FastMath;

@Slf4j
@RequiredArgsConstructor
class ReqHandler {
	public final ExecutorService executor = Executors.newSingleThreadExecutor();
	public final RemoteCtx remoteCtx = new RemoteCtx();

	public final SimpleApplication app;
	public final Xbuf xbuf;
	private final HashMap<String, File> folders = new HashMap<String, File>();

	public void enable() {
		log.info("Enable");
		app.enqueue(()->{
			remoteCtx.cam.setCamera(app.getCamera().clone());
			//cam0.setViewPort(1f, 0f, 2f, 1f) // black screen if the camera is outside of viewport(0-1, 0-1, 0-1, 0-1)
			ViewPort vp = app.getRenderManager().createPreView("remoteHandler_" + System.currentTimeMillis(), remoteCtx.cam.getCamera());
			//val vp = app.getRenderManager().createPostView("remoteHandler_" + System.currentTimeMillis(), cam0)
			vp.setBackgroundColor(ColorRGBA.Gray);
			vp.addProcessor(remoteCtx.view);
			vp.setClearFlags(true, true, true);
			vp.attachScene(app.getRootNode());
			app.getRootNode().attachChild(remoteCtx.root);
			log.info("connected");
			return null;
		});
	}

	public void disable() {
		log.info("Disable");
		app.enqueue(()->{
			remoteCtx.view.getViewPort().removeProcessor(remoteCtx.view);
			//TODO only clean root when no remote client
			app.getRootNode().detachChild(remoteCtx.root);
			remoteCtx.root.detachAllChildren();
			remoteCtx.root.getLocalLightList().clear();
			log.info("disconnected");
			return null;
		});
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg0) {
		ByteBuf msg = (ByteBuf)msg0;
		byte k = msg.readByte();
		try {
			switch(k) {
				case Protocol.Kind.askScreenshot : askScreenshot(ctx, msg); break;
				case Protocol.Kind.xbufCmd : xbufCmd(ctx, msg); break;
				default : log.warn("Unsupported kind of message : " + k);
			}
		} catch(Exception exc) {
			log.warn("channel read", exc);
		} finally {
			msg.release();
		}
	}

	public void askScreenshot(ChannelHandlerContext ctx, ByteBuf msg) {
		int w = msg.readInt();
		int h = msg.readInt();
		todo((rc) -> {
			rc.view.askReshape.set(new SceneProcessorCaptureToBGRA.ReshapeInfo(w, h, true));
			//TODO run notify in async (in an executor)
			rc.view.askNotify.set((bytes)-> {
				if (bytes.limit() != (w * h * 4)) {
					log.warn("bad size : {} != {}", bytes.limit(), w*h*4 );
					return false;
				}
				ByteBuf out = wrappedBuffer(bytes); // use a bytes.slice() internally
				out.resetReaderIndex();
				executor.execute(()->{
					ByteBuf header = ctx.alloc().buffer(4+1);
					header.writeInt(out.readableBytes());
					header.writeByte(Protocol.Kind.rawScreenshot);
					ctx.write(header);
					ctx.writeAndFlush(out);
				});
				return true;
			});
		});
	}

	public void xbufCmd(ChannelHandlerContext ctx, ByteBuf msg) {
		try {
			byte[] b = new byte[msg.readableBytes()];
			msg.readBytes(b);
			Cmd cmd0 = Cmd.parseFrom(b, xbuf.registry);
			switch(cmd0.getCmdCase()) {
				case SETEYE: setEye(ctx, cmd0.getSetEye()); break;
				case SETDATA: setData(ctx, cmd0.getSetData()); break;
				case CHANGEASSETFOLDERS: changeAssetFolders(ctx, cmd0.getChangeAssetFolders()); break;
				case PLAYANIMATION: playAnimation(ctx, cmd0.getPlayAnimation());
				//case : setCamera(ctx, cmd0); break;
				default:
					log.warn("unsupported cmd : {}", cmd0.getCmdCase().name() );
			}
		} catch(Exception exc) {
			log.warn("xbuf cmd", exc);
		}
	}

	public void setData(ChannelHandlerContext ctx, Datas.Data data) {
		todo((rc)-> {
			LoggerCollector xbufLogger = new LoggerCollector("xbuf");
			xbuf.merge(data, rc.root, rc.components, xbufLogger);
			xbufLogger.dumpTo(log);
			int errorsCnt = xbufLogger.countOf(LoggerCollector.Level.ERROR);
			if (errorsCnt > 0) {
				log.error("xbuf reading, error count : {}", errorsCnt);
			}
			int warnsCnt = xbufLogger.countOf(LoggerCollector.Level.WARN);
			if (warnsCnt > 0) {
				log.warn("xbuf reading, warn count : {}", warnsCnt);
			}
		});
	}

	public void setEye(ChannelHandlerContext ctx, Cmds.SetEye cmd) {
		todo((rc)-> {
			CameraNode cam = rc.cam;
			Quaternion rot = toJME(cmd.getRotation());
			cam.setLocalRotation(rot.clone());
			cam.setLocalTranslation(toJME(cmd.getLocation()));
			//if (cmd.hasNear()) cam0.setFrustumNear(cmd.getNear())
			//if (cmd.hasFar()) cam0.setFrustumFar(cmd.getFar())
			if (cmd.hasProjection()) {
				Matrix4f proj = toJME(cmd.getProjection());
				cam.getCamera().setParallelProjection(cmd.getProjMode() == ProjMode.orthographic);
				if (cmd.getProjMode() == ProjMode.orthographic) {
					float[] lr = pairOf(proj.m00, proj.m03);
					float[] bt = pairOf(proj.m11, proj.m13);
					float[] nf = pairOf(-proj.m22, proj.m23);
					cam.getCamera().setFrustum(nf[0], nf[1], lr[0], lr[1], bt[1], bt[0]);
				} else {
					float fovY = 2f * FastMath.RAD_TO_DEG * FastMath.atan(1f / proj.m11);
					float aspect = proj.m11 / proj.m00;
					cam.getCamera().setFrustumPerspective(fovY, aspect, cmd.getNear(), cmd.getFar());
				}
			}
			cam.getCamera().update();
			cam.setEnabled(true);
		});
	}

	public float[] pairOf(float m0, float m3) {
		// m00 = 2.0f / (right - left);
		// m03 = -(right + left) / (right - left);
		// m11 = 2.0f / (top - bottom)
		// m13 = -(top + bottom) / (top - bottom);
		// m22 = -2.0f / (far - near)
		// m23 = -(far + near) / (far - near);
		float l = (-m3 - 1) / m0;
		float r = (2f + (m0 * l)) / m0;
		//System.out.printf("m0 %s - m0' %s = %s\n", m0, (2.0f / (r - l)), m0 - (2.0f / (r - l)))
		//System.out.printf("m3 %s - m3' %s = %s\n ", m3, (-1 * (r + l) / (r - l)), m3 - (-1 * (r + l) / (r - l)))
		return new float[]{l,r};
	}

	public void changeAssetFolders(ChannelHandlerContext ctx, Cmds.ChangeAssetFolders cmd) {
		todo((rc)-> {
			AssetManager am = app.getAssetManager();
			if (cmd.getUnregisterOther()) {
				for (String p: folders.keySet()) {
					if (!cmd.getPathList().contains(p)) {
						File f = folders.get(p);
						am.unregisterLocator(f.getAbsolutePath(), FileLocator.class);
						log.warn("unregister assets folder : {}", f);
					}
				}
			}
			if (cmd.getRegister()) {
				for (String p: cmd.getPathList()) {
					if (!folders.containsKey(p)) {
						File f = new File(p);
						folders.put(p, f);
						am.registerLocator(f.getAbsolutePath(), FileLocator.class);
						log.warn("register assets folder : {}", f);
					}
				}
			} else {
				for (String p: cmd.getPathList()) {
					if (folders.containsKey(p)) {
						File f = folders.get(p);
						am.unregisterLocator(f.getAbsolutePath(), FileLocator.class);
						log.warn("unregister assets folder : {}", f);
					}
				}
			}
		});
	}

	public void playAnimation(ChannelHandlerContext ctx, Cmds.PlayAnimation cmd) {
		todo((rc)-> {
			Spatial target = (Spatial) rc.components.get(cmd.getRef());
			if (target != null) {
				Cinematic cinematic = new Cinematic(rc.root, LoopMode.DontLoop);
				for (String animName : cmd.getAnimationsNamesList()) {
					cinematic.enqueueCinematicEvent(new AnimationEvent(target, animName));
				}
				CinematicEventListener cel = new CinematicEventListener() {
					@Override public void onPlay(CinematicEvent e) {
					}
					@Override public void onPause(CinematicEvent e) {
					}
					@Override public void onStop(CinematicEvent e) {
						app.enqueue(()->{
							app.getStateManager().detach(cinematic);
							return null;
						});
					}
				};
				cinematic.addListener(cel);
				try {
					cinematic.fitDuration();
				} catch(NullPointerException exc) {
					log.warn("ignore NPE in cinematic.fitDuration() (it's a bug in jME 3.0)");
				}
				app.getStateManager().attach(cinematic);
				cinematic.play();
			}
		});
	}

	public void todo(Consumer<RemoteCtx> job) {
		app.enqueue(()->{
			try {
				job.accept(remoteCtx);
				return true;
			} catch(Exception exc) {
				log.warn("todo exception", exc);
				return false;
			}
		});
	}
}
