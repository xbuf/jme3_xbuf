import java.io.File;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.UrlLocator;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.XbufLoader;

public class TestMaterialSharing extends SimpleApplication{

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
	@Override
	public void simpleInitApp() {
		loadTestData(assetManager);
		assetManager.registerLoader(XbufLoader.class,"xbuf");
		flyCam.setMoveSpeed(200f);
		Spatial scene=assetManager.loadModel("models/sharedMeshMatTestScene.xbuf");
		rootNode.attachChild(scene);
		rootNode.depthFirstTraversal(s -> {
			if(s instanceof Geometry){
				Geometry g=(Geometry)s;
				System.out.println(g.getParent().getName());
				System.out.println("   Material: "+g.getMaterial().hashCode()+" "+g.getMaterial().getName());
				System.out.println("   Mesh: "+g.getMesh().hashCode());
			}
		});
	}

	public static void main(String[] args) {
		new TestMaterialSharing().start();
	}

}
