package jme3_ext_remote_editor

import com.jme3.animation.LoopMode
import com.jme3.app.SimpleApplication
import com.jme3.asset.plugins.FileLocator
import com.jme3.cinematic.Cinematic
import com.jme3.cinematic.events.AnimationEvent
import com.jme3.cinematic.events.CinematicEvent
import com.jme3.cinematic.events.CinematicEventListener
import com.jme3.math.ColorRGBA
import com.jme3.math.Matrix4f
import com.jme3.scene.Spatial
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import java.io.File
import java.util.HashMap
import java.util.concurrent.Executors
import jme3_ext_xbuf.LoggerCollector
import jme3_ext_xbuf.Xbuf
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.slf4j.LoggerFactory
import xbuf.Cmds
import xbuf.Cmds.Cmd
import xbuf.Cmds.SetEye.ProjMode
import xbuf.Datas

import static io.netty.buffer.Unpooled.wrappedBuffer
import com.jme3.math.FastMath

@FinalFieldsConstructor
class ReqHandler {
	public val executor = Executors.newSingleThreadExecutor();
	public val remoteCtx = new RemoteCtx();
	public val log = LoggerFactory.getLogger(this.getClass)

	public val SimpleApplication app;
	public val Xbuf xbuf;
	private val folders = new HashMap<String, File>();

	def enable() {
		println("ENABLE")
		app.enqueue[|
			val cam0 = app.getCamera().clone()
			//cam0.setViewPort(1f, 0f, 2f, 1f) // black screen if the camera is outside of viewport(0-1, 0-1, 0-1, 0-1)
			val vp = app.getRenderManager().createPreView("remoteHandler_" + System.currentTimeMillis(), cam0)
			//val vp = app.getRenderManager().createPostView("remoteHandler_" + System.currentTimeMillis(), cam0)
			vp.setBackgroundColor(ColorRGBA.Gray)
			vp.addProcessor(remoteCtx.view)
			vp.setClearFlags(true, true, true)
			vp.attachScene(app.getRootNode())
			app.getRootNode().attachChild(remoteCtx.root)
			log.info("connected")
			null
		]
	}

	def disable() {
		app.enqueue[|
			remoteCtx.view.getViewPort().removeProcessor(remoteCtx.view)
			//TODO only clean root when no remote client
			app.getRootNode().detachChild(remoteCtx.root)
			remoteCtx.root.detachAllChildren()
			remoteCtx.root.getLocalLightList().clear()
			log.info("disconnected")
			null
		]
	}

	def channelRead(ChannelHandlerContext ctx, Object msg0) {
		val msg = msg0 as ByteBuf;
		val k = msg.readByte();
		try {
			switch(k) {
				case Protocol.Kind.askScreenshot : askScreenshot(ctx, msg)
				case Protocol.Kind.xbufCmd : xbufCmd(ctx, msg)
				default : System.out.println("Unsupported kind of message : " + k)
			}
		} catch(Exception exc) {
			exc.printStackTrace()
		} finally {
			msg.release()
		}
	}

	def askScreenshot(ChannelHandlerContext ctx, ByteBuf msg) {
		val w = msg.readInt()
		val h = msg.readInt()
		todo[rc|
			rc.view.askReshape.set(new SceneProcessorCaptureToBGRA.ReshapeInfo(w, h, true))
			//TODO run notify in async (in an executor)
			rc.view.askNotify.set [bytes |
				if (bytes.limit() != (w * h * 4)) {
					log.warn("bad size : {} != {}", bytes.limit(), w*h*4 )
					return false
				}
				val out = wrappedBuffer(bytes)  // use a bytes.slice() internally
				out.resetReaderIndex()
				executor.execute [
					val header = ctx.alloc().buffer(4+1)
					header.writeInt(out.readableBytes())
					header.writeByte(Protocol.Kind.rawScreenshot)
					ctx.write(header)
					ctx.writeAndFlush(out)
				]
				return true
			]
		]
	}

	def xbufCmd(ChannelHandlerContext ctx, ByteBuf msg) {
		try {
			val b = newByteArrayOfSize(msg.readableBytes())
			msg.readBytes(b);
			val cmd0 = Cmd.parseFrom(b, xbuf.registry);
			switch(cmd0.getCmdCase()) {
				case SETEYE: setEye(ctx, cmd0.getSetEye())
				case SETDATA: setData(ctx, cmd0.getSetData())
				case CHANGEASSETFOLDERS: changeAssetFolders(ctx, cmd0.getChangeAssetFolders())
				case PLAYANIMATION: playAnimation(ctx, cmd0.getPlayAnimation())
				//case : setCamera(ctx, cmd0); break;
				default:
					log.warn("unsupported cmd : {}", cmd0.getCmdCase().name() )
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}

	def setData(ChannelHandlerContext ctx, Datas.Data data) {
		todo[RemoteCtx rc |
			val xbufLogger = new LoggerCollector("xbuf");
			xbuf.merge(data, rc.root, rc.components, xbufLogger);
			xbufLogger.dumpTo(log);
			val errorsCnt = xbufLogger.countOf(LoggerCollector.Level.ERROR);
			if (errorsCnt > 0) {
				log.error("xbuf reading, error count : {}", errorsCnt);
			}
			val warnsCnt = xbufLogger.countOf(LoggerCollector.Level.WARN);
			if (warnsCnt > 0) {
				log.warn("xbuf reading, warn count : {}", warnsCnt);
			}
		]
	}

	def setEye(ChannelHandlerContext ctx, Cmds.SetEye cmd) {
		todo[rc |
			val cam = rc.cam
			val rot = xbuf.cnv(cmd.getRotation(), cam.getLocalRotation());
			cam.setLocalRotation(rot.clone());
			cam.setLocalTranslation(xbuf.cnv(cmd.getLocation(), cam.getLocalTranslation()));
			val cam0 = rc
					.view
					.getViewPort()
					.getCamera()
			cam.setCamera(cam0);
			//if (cmd.hasNear()) cam0.setFrustumNear(cmd.getNear())
			//if (cmd.hasFar()) cam0.setFrustumFar(cmd.getFar())
			if (cmd.hasProjection()) {
				val proj = xbuf.cnv(cmd.getProjection(), new Matrix4f());
				cam0.setParallelProjection(cmd.getProjMode() == ProjMode.orthographic)
				if (cmd.getProjMode() == ProjMode.orthographic) {
					val lr = pairOf(proj.m00, proj.m03)
					val bt = pairOf(proj.m11, proj.m13)
					val nf = pairOf(-proj.m22, proj.m23)
					cam0.setFrustum(nf.key, nf.value, lr.key, lr.value, bt.value, bt.key)
				} else {
					val fovY = 2f * FastMath.RAD_TO_DEG * FastMath.atan(1f / proj.m11)
					val aspect = proj.m11 / proj.m00
					cam0.setFrustumPerspective(fovY, aspect, cmd.getNear(), cmd.getFar())
				}
			}
			cam0.update()
			cam.setEnabled(true)
		]
	}

	def Pair<Float, Float> pairOf(float m0, float m3) {
		// m00 = 2.0f / (right - left);
		// m03 = -(right + left) / (right - left);
		// m11 = 2.0f / (top - bottom)
		// m13 = -(top + bottom) / (top - bottom);
		// m22 = -2.0f / (far - near)
		// m23 = -(far + near) / (far - near);
		val l = (-m3 - 1) / m0
		val r = (2f + (m0 * l)) / m0
		//System.out.printf("m0 %s - m0' %s = %s\n", m0, (2.0f / (r - l)), m0 - (2.0f / (r - l)))
		//System.out.printf("m3 %s - m3' %s = %s\n ", m3, (-1 * (r + l) / (r - l)), m3 - (-1 * (r + l) / (r - l)))
		new Pair(l,r)
	}

	def changeAssetFolders(ChannelHandlerContext ctx, Cmds.ChangeAssetFolders cmd) {
		todo[RemoteCtx rc |
			val am = app.getAssetManager();
			if (cmd.getUnregisterOther()) {
				for (String p: folders.keySet()) {
					if (!cmd.getPathList().contains(p)) {
						val f = folders.get(p);
						am.unregisterLocator(f.getAbsolutePath(), typeof(FileLocator))
						log.warn("unregister assets folder : {}", f);
					}
				}
			}
			if (cmd.getRegister()) {
				for (String p: cmd.getPathList()) {
					if (!folders.containsKey(p)) {
						val f = new File(p)
						folders.put(p, f);
						am.registerLocator(f.getAbsolutePath(), typeof(FileLocator))
						log.warn("register assets folder : {}", f);
					}
				}
			} else {
				for (String p: cmd.getPathList()) {
					if (folders.containsKey(p)) {
						val f = folders.get(p)
						am.unregisterLocator(f.getAbsolutePath(), typeof(FileLocator))
						log.warn("unregister assets folder : {}", f);
					}
				}
			}
		]
	}

	def playAnimation(ChannelHandlerContext ctx, Cmds.PlayAnimation cmd) {
		todo[RemoteCtx rc |
			val target = rc.components.get(cmd.ref) as Spatial
			if (target != null) {
				val cinematic = new Cinematic(rc.root, LoopMode.DontLoop)
				for (String animName : cmd.animationsNamesList) {
					cinematic.enqueueCinematicEvent(new AnimationEvent(target, animName))
				}
				val cel = new CinematicEventListener() {
					override onPlay(CinematicEvent e) {
					}
					override onPause(CinematicEvent e) {
					}
					override onStop(CinematicEvent e) {
						app.enqueue([
							app.stateManager.detach(cinematic)
						])
					}
				}
				cinematic.addListener(cel)
				cinematic.fitDuration()
				app.stateManager.attach(cinematic)
				cinematic.play()
			}
		]
	}

	def todo(Procedures.Procedure1<RemoteCtx> job) {
		app.enqueue[|
			try {
				job.apply(remoteCtx)
				true
			} catch(Exception exc) {
				exc.printStackTrace()
				false
			}
		]
	}
}