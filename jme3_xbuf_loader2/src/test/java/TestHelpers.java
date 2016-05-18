import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.BulletAppState.ThreadingType;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;

import jme3_ext_xbuf.XbufLoader;

public class TestHelpers{
	private static  Map<SimpleApplication,Object> run_tab=new ConcurrentHashMap<SimpleApplication,Object>();
	private static  Map<SimpleApplication,Object> join_tab=new ConcurrentHashMap<SimpleApplication,Object>();

	public static BulletAppState buildBullet(SimpleApplication app,boolean debug){
		BulletAppState bullet=new BulletAppState();
		bullet.setThreadingType(ThreadingType.SEQUENTIAL);
		bullet.setDebugEnabled(debug);
		 app.getStateManager().attach(bullet);
		return bullet;
	}
	public static SimpleApplication buildApp(boolean headless){
		SimpleApplication app=new SimpleApplication(){
			@Override
			public void destroy(){
				super.destroy();
				run_tab.remove(this);
			}
			public void simpleInitApp() {
				AmbientLight al=new AmbientLight();
				al.setColor(ColorRGBA.White.mult(10f));
				
				rootNode.addLight(al);
				
				DirectionalLight dl=new DirectionalLight(new Vector3f(0f, -1f, 0),new ColorRGBA(.72f,.97f,1f,1f).mult(1.4f));
				rootNode.addLight(dl);
				dl=new DirectionalLight(new Vector3f(0f, 1f, 0),new ColorRGBA(.72f,.97f,1f,1f).mult(1.4f));
				rootNode.addLight(dl);
				
				assetManager.registerLoader(XbufLoader.class,"xbuf");
				flyCam.setMoveSpeed(200f);
				run_tab.put(this,new Object());
			}
		};
		app.start(headless?JmeContext.Type.Headless:JmeContext.Type.Display);
		while(run_tab.get(app)==null){
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		return app;
	}
	public static void hijackUpdateThread(SimpleApplication app){
		AtomicBoolean wait=new AtomicBoolean(true);
		app.enqueue(()->{
			join_tab.put(app,new Object());
			wait.set(false);
			while(join_tab.get(app)!=null){
				try{
					Thread.sleep(10);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		});
		while(wait.get()){
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void releaseUpdateThread(SimpleApplication app){
		join_tab.remove(app);
	}
	

	public static void waitFor(SimpleApplication app){
		while(run_tab.get(app)!=null){
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void closeApp(SimpleApplication app){
		app.stop(true);
	}

}
