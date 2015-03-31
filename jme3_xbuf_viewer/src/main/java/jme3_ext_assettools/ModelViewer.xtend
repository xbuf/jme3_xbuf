package jme3_ext_assettools;

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.jme3.app.SimpleApplication
import com.jme3.asset.plugins.FileLocator
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.plugins.OBJLoader
import com.jme3.scene.plugins.blender.BlenderLoader
import com.jme3.system.AppSettings
import java.io.File
import java.net.URL
import java.util.ArrayList
import java.util.LinkedList
import java.util.List
import java.util.concurrent.CountDownLatch
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import jme3_ext_xbuf.Xbuf
import jme3_ext_xbuf.XbufLoader
import jme3_ext_remote_editor.AppState4RemoteCommand
import jme3_ext_spatial_explorer.Helper
import org.slf4j.bridge.SLF4JBridgeHandler

public class ModelViewer {
	/////////////////////////////////////////////////////////////////////////////////
	// Class
	/////////////////////////////////////////////////////////////////////////////////

	static def main(String[] args) {
		Logger.getLogger("").setLevel(Level.WARNING)
		//installSLF4JBridge()
		val options = new Options()
		val jc = new JCommander(options)
		try {
			jc.parse(args)
			if (options.help) {
				jc.usage()
			} else {
				val viewer = new ModelViewer(options)
				viewer.setupAll()
				viewer.start()
			}
		} catch(ParameterException exc) {
			jc.usage()
			System.err.println(exc.getMessage())
			System.exit(-2)
		} catch(Exception exc) {
			exc.printStackTrace()
			System.exit(-1)
		} finally {
			uninstallSLF4JBridge()
		}
	}

	/**
	 * Redirect java.util.logging to slf4j :
	 * * remove registered j.u.l.Handler
	 * * add a SLF4JBridgeHandler instance to jul's root logger.
	 */
	static def installSLF4JBridge() {
		val root = LogManager.getLogManager().getLogger("")
		for(Handler h : root.getHandlers()){
			root.removeHandler(h)
		}
		SLF4JBridgeHandler.install()
	}

	static def uninstallSLF4JBridge() {
		SLF4JBridgeHandler.uninstall();
	}

	static class Options {
		@Parameter(names = #["-h", "-?", "--help"], help = true)
		private var boolean help;

		@Parameter(description = "files")
		private var List<String> files = new ArrayList<String>();

		@Parameter(names = #["-f", "--fullscreen"], description = "3D view in fullscreen")
		public var boolean fullscreen = false;

		@Parameter(names = #["--width"], description = "width of 3D view", arity = 1)
		public var int width = 1280;

		@Parameter(names = #["-height"], description = "height of 3D view", arity = 1)
		public var int height = 720;

		@Parameter(names = "--showJmeSettings", description = "show jmonkeyengine settings before displayed 3D view", arity = 1)
		public var boolean showJmeSettings = true;

		@Parameter(names = "--assetCfg", description = "url of config file for assetManager", arity = 1)
		public var URL assetCfg;

		@Parameter(names = "--addGrid", description = "add grid + axis in 3D scene")
		public var boolean addGrid = true;

		@Parameter(names = "--addLights", description = "add a directionnal + ambient light in 3D scene")
		public var boolean addLights = false;

		@Parameter(names = #["-e", "--spatialExplorer"], description = "enable Spatial Explorer")
		public var boolean spatialExplorer = true;

		@Parameter(names = #["-r", "--remoteCommand"], description = "enable Remote Command")
		public var boolean remoteCommand = true;

		@Parameter(names = #["-p", "--port"], description = "port used by Remote Command")
		public var int port = 4242;
	}
	/////////////////////////////////////////////////////////////////////////////////
	// Object
	/////////////////////////////////////////////////////////////////////////////////

	public val assetDirs = new LinkedList<File>() as List<File>
	public val SimpleApplication app
	protected val Options options

	new(Options options) {
		this.options = options;

		val settings = new AppSettings(true)
		settings.setResolution(options.width, options.height)
		settings.setVSync(true)
		settings.setFullscreen(options.fullscreen)

//		try {
//			ClassLoader cl = Thread.currentThread().getContextClassLoader();
//			settings.setIcons(new BufferedImage[]{
//				ImageIO.read(cl.getResourceAsStream("shortcut-128.png")),
//				ImageIO.read(cl.getResourceAsStream("shortcut-64.png")),
//				ImageIO.read(cl.getResourceAsStream("shortcut-32.png")),
//				ImageIO.read(cl.getResourceAsStream("shortcut-16.png"))
//			});
//		} catch (Exception e) {
//			//log.log(java.util.logging.Level.WARNING, "Unable to load program icons", e);
//			e.printStackTrace();
//		}

		if (options.assetCfg != null) {
			settings.putString("AssetConfigURL", options.assetCfg.toExternalForm());
		}

		app = new SimpleApplication(){
			val CountDownLatch running = new CountDownLatch(1)

			override simpleInitApp() {
			}

			override destroy() {
				super.destroy()
				running.countDown()
			}
		}

		app.setSettings(settings)
		app.setShowSettings(options.showJmeSettings)
		app.setDisplayStatView(true)
		app.setDisplayFps(true)
		// !!!! without .setPauseOnLostFocus(false) server will only send screenshot to blender,... when jme main screen have focus
		app.setPauseOnLostFocus(false)
	}

	def setupAll() {
		setupCamera()
		setupLoaders()
		setupDefaultScene()
		if (options.spatialExplorer) setupSpatialExplorer()
		if (options.remoteCommand) setupRemoteCommand(options.port)
		setupModels(options.files)
	}

	def setupCamera() {
		app.enqueue[
			app.getFlyByCamera().setEnabled(true)
			app.getFlyByCamera().setDragToRotate(true)
			app.getFlyByCamera().setMoveSpeed(4f)
			//app.getStateManager().detach(app.getStateManager().getState(FlyCamAppState.class))
			app.getCamera().setLocation(new Vector3f(-5f,5f,5f))
			app.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Y)
			app.getInputManager().setCursorVisible(true)
			null
		]
	}

	def setupLoaders() {
		app.enqueue[
			val assetManager = app.getAssetManager()
			assetManager.registerLoader(typeof(OBJLoader), "obj")
			assetManager.registerLoader(typeof(MTLoaderExt), "mtl")
			assetManager.registerLoader(typeof(BlenderLoader), "blend")
			assetManager.registerLoader(typeof(XbufLoader), "xbuf")
			null
		]
	}

	//Setup a default scene (grid + axis)
	def setupDefaultScene() {
		app.enqueue[
			val anchor = app.getRootNode()
			if (options.addGrid) {
				anchor.attachChild(Helper.makeScene(app));
			}
			if (options.addLights) {
				val dl = new DirectionalLight()
				dl.setColor(ColorRGBA.White)
				dl.setDirection(Vector3f.UNIT_XYZ.negate())
				anchor.addLight(dl)

				val al = new AmbientLight()
				al.setColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f))
				anchor.addLight(al)
			}
			null
		]
	}

	//Setup SpatialExplorer
	def setupSpatialExplorer() {
		Helper.setupSpatialExplorerWithAll(app)
	}

	def setupRemoteCommand(int port) {
		app.enqueue[
			val xbuf = new Xbuf(app.getAssetManager())
			app.getStateManager().attach(new AppState4RemoteCommand(port, xbuf))
			null
		]
	}

	def setupModels(List<String> models) {
		app.enqueue[
			for(String p : models) {
				try {
					val model = p.split("@");
					if (model.length < 2) {
						val f = new File(model.get(0))
						showModel(f.getName(), f, true)
					} else {
						showModel(model.get(0), new File(model.get(1)), true)
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			//showModel("Jaime", "Models/Jaime/Jaime.j3o");
			null
		]
	}
	def start() {
		app.start()
	}

	def addClassLoader(ClassLoader cl) {
		app.enqueue[
			app.getAssetManager().addClassLoader(cl)
			null
		]
	}

	def addAssetDirs(List<File> dirs) {
		if (dirs == null || dirs.isEmpty()){
			 return
		}
		for(File f : dirs) {
			if (f.isDirectory()) {
				this.assetDirs.add(f)
				app.enqueue [
					app.getAssetManager().registerLocator(f.getAbsolutePath(), typeof(FileLocator))
					null
				]
			}
		}
	}

	def showModel(String name, File f, boolean autoAddAssetFolder) {
		val apath = f.getAbsolutePath()
		var rpath = null as String
		for(File d : assetDirs) {
			if (apath.startsWith(d.getAbsolutePath())) {
				rpath = apath.substring(d.getAbsolutePath().length()+1)
			}
		}
		if (rpath == null) {
			app.getAssetManager().registerLocator(f.getParentFile().getAbsolutePath(), typeof(FileLocator))
			rpath = f.getName();
		}
		if (rpath != null) {
			showModel(name, rpath)
		}
	}

	def showModel(String name, String path) {
		app.enqueue[
			val v = app.getAssetManager().loadModel(path);
			v.setName(name);
			app.getRootNode().detachChildNamed(name);
			app.getRootNode().attachChild(v);
			null
		]
	}
}
