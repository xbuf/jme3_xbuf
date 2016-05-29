package jme3_ext_assettools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.LightNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.plugins.OBJLoader;
import com.jme3.scene.plugins.blender.BlenderLoader;
import com.jme3.scene.plugins.fbx.FbxLoader;
import com.jme3.system.AppSettings;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import jme3_ext_remote_editor.AppState4RemoteCommand;
import jme3_ext_spatial_explorer.AppStateSpatialExplorer;
import jme3_ext_spatial_explorer.Helper;
import jme3_ext_spatial_explorer.SpatialExplorer;
import jme3_ext_xbuf.Xbuf;
import jme3_ext_xbuf.XbufLoader;
import org.controlsfx.glyphfont.FontAwesome;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ModelViewer {
	/////////////////////////////////////////////////////////////////////////////////
	// Class
	/////////////////////////////////////////////////////////////////////////////////

	static public void main(String[] args) {
		Logger.getLogger("").setLevel(Level.WARNING);
		//installSLF4JBridge()
		Options options = new Options();
		JCommander jc = new JCommander(options);
		try {
			jc.parse(args);
			if (options.help) {
				jc.usage();
			} else {
				ModelViewer viewer = new ModelViewer(options);
				viewer.setupAll();
				viewer.start();
			}
		} catch(ParameterException exc) {
			jc.usage();
			System.err.println(exc.getMessage());
			System.exit(-2);
		} catch(Exception exc) {
			exc.printStackTrace();
			System.exit(-1);
		} finally {
			uninstallSLF4JBridge();
		}
	}

	/**
	 * Redirect java.util.logging to slf4j :
	 * * remove registered j.u.l.Handler
	 * * add a SLF4JBridgeHandler instance to jul's root logger.
	 */
	static public void installSLF4JBridge() {
		Logger root = LogManager.getLogManager().getLogger("");
		for(Handler h : root.getHandlers()){
			root.removeHandler(h);
		}
		SLF4JBridgeHandler.install();
	}

	static public void uninstallSLF4JBridge() {
		SLF4JBridgeHandler.uninstall();
	}

	static class Options {
		@Parameter(names = {"-h", "-?", "--help"}, help = true)
		private boolean help;

		@Parameter(description = "files")
		private List<String> files = new ArrayList<String>();

		@Parameter(names = {"-f", "--fullscreen"}, description = "3D view in fullscreen")
		public boolean fullscreen = false;

		@Parameter(names = {"--width"}, description = "width of 3D view", arity = 1)
		public int width = 1280;

		@Parameter(names = {"-height"}, description = "height of 3D view", arity = 1)
		public int height = 720;

		@Parameter(names = "--showJmeSettings", description = "show jmonkeyengine settings before displayed 3D view", arity = 1)
		public boolean showJmeSettings = true;

		@Parameter(names = "--assetCfg", description = "url of config file for assetManager", arity = 1)
		public URL assetCfg;

		@Parameter(names = "--addGrid", description = "add grid + axis in 3D scene")
		public boolean addGrid = true;

		@Parameter(names = "--addLights", description = "add a directionnal + ambient light in 3D scene")
		public boolean addLights = true;

		@Parameter(names = {"-e", "--spatialExplorer"}, description = "enable Spatial Explorer")
		public boolean spatialExplorer = true;

		@Parameter(names = {"-r", "--remoteCommand"}, description = "enable Remote Command")
		public boolean remoteCommand = true;

		@Parameter(names = {"-p", "--port"}, description = "port used by Remote Command")
		public int port = 4242;
	}
	/////////////////////////////////////////////////////////////////////////////////
	// Object
	/////////////////////////////////////////////////////////////////////////////////

	public List<File> assetDirs = new LinkedList<>();
	public SimpleApplication app;
	protected Options options;

	public ModelViewer(Options options) {
		this.options = options;

		AppSettings settings = new AppSettings(true);
		settings.setResolution(options.width, options.height);
		settings.setVSync(true);
		settings.setFullscreen(options.fullscreen);

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
			CountDownLatch running = new CountDownLatch(1);

			@Override public void simpleInitApp() {
			}

			@Override public void destroy() {
				super.destroy();
				running.countDown();
			}
		};

		app.setSettings(settings);
		app.setShowSettings(options.showJmeSettings);
		app.setDisplayStatView(true);
		app.setDisplayFps(true);
		// !!!! without .setPauseOnLostFocus(false) server will only send screenshot to blender,... when jme main screen have focus
		app.setPauseOnLostFocus(false);
	}

	public void setupAll() {
		setupCamera();
		setupLoaders();
		setupDefaultScene();
		if (options.spatialExplorer) setupSpatialExplorer();
		if (options.remoteCommand) setupRemoteCommand(options.port);
		setupModels(options.files);
	}

	public void setupCamera() {
		app.enqueue(() ->{
			app.getFlyByCamera().setEnabled(true);
			app.getFlyByCamera().setDragToRotate(true);
			app.getFlyByCamera().setMoveSpeed(4f);
			//app.getStateManager().detach(app.getStateManager().getState(FlyCamAppState.class))
			app.getCamera().setLocation(new Vector3f(-5f,5f,5f));
			app.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
			app.getInputManager().setCursorVisible(true);
			return null;
		});
	}

	public void setupLoaders() {
		app.enqueue(()->{
			AssetManager assetManager = app.getAssetManager();
			assetManager.registerLoader(OBJLoader.class, "obj");
			assetManager.registerLoader(MTLoaderExt.class, "mtl");
			assetManager.registerLoader(FbxLoader.class, "fbx");
			assetManager.registerLoader(BlenderLoader.class, "blend");
			assetManager.registerLoader(XbufLoader.class, "xbuf");
			return null;
		});
	}

	//Setup a default scene (grid + axis)
	public void setupDefaultScene() {
		app.enqueue(()->{
			Node anchor = app.getRootNode();
			if (options.addGrid) {
				anchor.attachChild(Helper.makeScene(app));
			}
			if (options.addLights) {
				DirectionalLight dl = new DirectionalLight();
				dl.setColor(ColorRGBA.White);
				dl.setDirection(Vector3f.UNIT_XYZ.negate());
				anchor.addLight(dl);

				AmbientLight al = new AmbientLight();
				al.setColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
				anchor.addLight(al);
			}
			return null;
		});
	}

	//Setup SpatialExplorer
	public void setupSpatialExplorer() {
		Helper.initJfx();
		Helper.setupSpatialExplorerWithAll(app);
		FileChooser fileChooser = new FileChooser();
		DirectoryChooser dirChooser = new DirectoryChooser();
		fileChooser.initialDirectoryProperty().bindBidirectional(dirChooser.initialDirectoryProperty());
		//val lastDirectory = new SimpleProperty<java.io.File>
		app.enqueue(()->{
			SpatialExplorer exp = app.getStateManager().getState(AppStateSpatialExplorer.class).spatialExplorer;
			exp.barActions.add(Helper.makeAction("Add AssetDir...", FontAwesome.Glyph.PICTURE_ALT, (evt) -> {
				dirChooser.setTitle("Add AssetDir...");
				File f = dirChooser.showDialog(exp.getStage());
				if (f != null) {
					addAssetDirs(f);
					dirChooser.setInitialDirectory(f);
				}
			}));
			exp.barActions.add(Helper.makeAction("Import Model...", FontAwesome.Glyph.FOLDER_OPEN, (evt) -> {
				fileChooser.setTitle("Import Model...");
				List<File> fs = fileChooser.showOpenMultipleDialog(exp.getStage());
				if (fs != null && !fs.isEmpty()) {
					((DesktopAssetManager)app.getAssetManager()).clearCache();
					for(File f : fs) {
						showModel(f.getName(), f, true);
					}
					fileChooser.setInitialDirectory(fs.get(0).getParentFile());
				}
				//TODO refresh treeItem
			}));
			PointLight cameraLight = new PointLight();
			cameraLight.setColor(ColorRGBA.White);
			cameraLight.setRadius(20f);
			LightNode cameraLightNode = new LightNode("cameraLight", cameraLight);
			cameraLightNode.addControl(new CameraControl(app.getCamera(), CameraControl.ControlDirection.CameraToSpatial));
			exp.barActions.add(Helper.makeAction("CameraLight", FontAwesome.Glyph.LIGHTBULB_ALT, (evt) -> {
				app.enqueue(()->{
					if (cameraLightNode.getParent() == null) {
						app.getRootNode().addLight(cameraLight);
						app.getRootNode().attachChild(cameraLightNode);
					} else {
						app.getRootNode().removeLight(cameraLight);
						app.getRootNode().detachChild(cameraLightNode);
					}
					return null;
				});
			}));
			return null;
		});
	}

	public void setupRemoteCommand(int port) {
		app.enqueue(()->{
			Xbuf xbuf = new Xbuf(app.getAssetManager());
			app.getStateManager().attach(new AppState4RemoteCommand(port, xbuf));
			return null;
		});
	}

	public void setupModels(List<String> models) {
		app.enqueue(()->{
			for(String p : models) {
				try {
					String[] model = p.split("@");
					if (model.length < 2) {
						File f = new File(model[0]);
						showModel(f.getName(), f, true);
					} else {
						showModel(model[0], new File(model[1]), true);
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			//showModel("Jaime", "Models/Jaime/Jaime.j3o");
			return null;
		});
	}
	public void start() {
		app.start();
	}

	public void addClassLoader(ClassLoader cl) {
		app.enqueue(()->{
			app.getAssetManager().addClassLoader(cl);
			return null;
		});
	}

	public void addAssetDirs(File... dirs) {
		if (dirs == null || dirs.length == 0){
			 return;
		}
		app.enqueue(()->{
			for(File f : dirs) {
				if (f.isDirectory()) {
					this.assetDirs.add(f);
					app.getAssetManager().registerLocator(f.getAbsolutePath(), FileLocator.class);
				}
			}
			return null;
		});
	}

	public void showModel(String name, File f, boolean autoAddAssetFolder) {
		final String apath = f.getAbsolutePath();
		String rpath = null;
		for(File d : assetDirs) {
			if (apath.startsWith(d.getAbsolutePath())) {
				rpath = apath.substring(d.getAbsolutePath().length()+1);
			}
		}
		if (rpath == null) {
			app.getAssetManager().registerLocator(f.getParentFile().getAbsolutePath(), FileLocator.class);
			rpath = f.getName();
		}
		if (rpath != null) {
			showModel(name, rpath);
		}
	}

	public void showModel(String name, String path) {
		app.enqueue(()->{
			Spatial v = app.getAssetManager().loadModel(path);
			v.setName(name);
			app.getRootNode().detachChildNamed(name);
			app.getRootNode().attachChild(v);
			return null;
		});
	}
}
