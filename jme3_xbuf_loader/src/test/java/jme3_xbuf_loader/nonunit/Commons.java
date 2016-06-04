package jme3_xbuf_loader.nonunit;
import java.io.File;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.UrlLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.BulletAppState.ThreadingType;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class Commons{

    public static Geometry getGeom(Spatial s){
    	if(s instanceof Geometry)return (Geometry)s;
    	else if(s instanceof Node){
    		List<Spatial> children=((Node)s).getChildren();
    		for(Spatial child:children){
    			Geometry g=getGeom(child);
    			if(g!=null)return g;
    		}
    	}
    	return null;
    }
    
    public static void loadTestData(AssetManager am){

    	String home=(System.getProperty("os.name").toUpperCase().contains("WIN")?System.getenv("APPDATA"):System.getProperty("user.home"))+File.separator;
		
		String possible_paths[]=new String[]{
				home+".TestData/",
				home+"TestData/",
				new File("TestData/").getAbsolutePath(),
				"/DEV/TestData/"
		};
		
		boolean found_test_data=false;
		for(String path:possible_paths){
			if(new File(path).exists()){
				found_test_data=true;
				System.out.println("TestData found "+path);
				am.registerLocator(path+"/assets",FileLocator.class);
				break;
			}
		}
		if(!found_test_data){
			System.out.println("TestData not found. The assets will be loaded from web.");
			System.out.println("This could take a while.");
			System.out.println("To speedup the loading you can clone the TestData repository (from https://github.com/riccardobl/TestData)");
			System.out.println("in one of the following directories ");
			for(String path:possible_paths){
				System.out.println("   "+path);
			}
			System.out.println("and restart the application.");
		}
        am.registerLocator("https://github.com/riccardobl/TestData/raw/master/assets/",UrlLocator.class);

    }

	public static void initApp(SimpleApplication simpleapp) {
		loadTestData(simpleapp.getAssetManager());
		
		simpleapp.getFlyByCamera().setMoveSpeed(100f);
		simpleapp.setPauseOnLostFocus(false);		
		BulletAppState bullet=new BulletAppState();
		bullet.setThreadingType(ThreadingType.PARALLEL);

		simpleapp.getStateManager().attach(bullet);
		Node rootNode=simpleapp.getRootNode();
		DirectionalLight dl=new DirectionalLight(new Vector3f(0f, -1f, 0),new ColorRGBA(.72f,.97f,1f,1f).mult(1.4f));
		rootNode.addLight(dl);
		dl=new DirectionalLight(new Vector3f(0f, 0f, -1),new ColorRGBA(.72f,.97f,1f,1f).mult(.4f));
		rootNode.addLight(dl);
		dl=new DirectionalLight(new Vector3f(0f, 0f, 1),new ColorRGBA(.72f,.97f,1f,1f).mult(.4f));
		rootNode.addLight(dl);
		dl=new DirectionalLight(new Vector3f(-1f, 0f, 0),new ColorRGBA(.72f,.97f,1f,1f).mult(.4f));
		rootNode.addLight(dl);
		dl=new DirectionalLight(new Vector3f(1f, 0f, 0),new ColorRGBA(.72f,.97f,1f,1f).mult(.4f));
		rootNode.addLight(dl);
	}
	

    public static void makeFloor(SimpleApplication app) {    	
        Box box=new Box(160,.2f,160);
        Geometry floor=new Geometry("the Floor",box);
        floor.setLocalTranslation(0,-4f,0);
        Material mat1=new Material(app.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
        mat1.setColor("Diffuse",new ColorRGBA(.54f,.68f,.16f,1f));
        mat1.setBoolean("UseMaterialColors",true);
        floor.setMaterial(mat1);
        
        CollisionShape floorcs=new MeshCollisionShape(floor.getMesh());
		RigidBodyControl floor_phy=new RigidBodyControl(floorcs,0);
		floor.addControl(floor_phy);
		floor_phy.setPhysicsLocation(new Vector3f(0,-30,0));		
		app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(floor_phy);		
		app.getRootNode().attachChild(floor);			
    }
}
