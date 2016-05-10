import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.SkeletonDebugger;

public class UnitTests{
	public boolean headless=true;
	@Test
	public void testHwSkinning() {
		SimpleApplication app=TestHelpers.buildApp(headless);
		TestHelpers.hijackUpdateThread(app);
		
		boolean created=false;
		try{
			Spatial scene=app.getAssetManager().loadModel("unit_tests/xbuf/hw_skinning.xbuf");
			app.getRootNode().attachChild(scene);
			scene.depthFirstTraversal(s -> {
				SkeletonControl sk=s.getControl(SkeletonControl.class);
				if(sk!=null){
					System.out.println("Found skeletoncontrol: "+sk+" on "+s);

					System.out.println("Set "+sk+".hwSkinning=true");
					sk.setHardwareSkinningPreferred(true);

					SkeletonDebugger skeletonDebug=new SkeletonDebugger("skeleton",sk.getSkeleton());
					Material mat=new Material(app.getAssetManager(),"Common/MatDefs/Misc/Unshaded.j3md");
					mat.setColor("Color",ColorRGBA.Green);
					mat.getAdditionalRenderState().setDepthTest(false);
					skeletonDebug.setMaterial(mat);
				    app.getRootNode().attachChild(skeletonDebug);
				    skeletonDebug.setLocalTranslation(s.getWorldTranslation());
				}
				
				AnimControl ac=s.getControl(AnimControl.class);

				if(ac!=null){
				
					System.out.println("Found animcontrol: "+ac+" on "+s);

					Collection<String> anims=ac.getAnimationNames();
					for(String a:anims){
						AnimChannel channel = ac.createChannel();
						channel.setAnim(a);
						channel.setLoopMode(LoopMode.Cycle);
						System.out.println("Set "+a+" to "+s);
					}
				}
			});
			created=true;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		TestHelpers.releaseUpdateThread(app);
		if(!headless)TestHelpers.waitFor(app);
		TestHelpers.closeApp(app);

		assertTrue("Hardware skinning cannot be used.",created);



	}

	@Test
	public void testMeshSharing() {
		boolean headless=true;
		SimpleApplication app=TestHelpers.buildApp(headless);
		TestHelpers.hijackUpdateThread(app);

		Spatial scene=app.getAssetManager().loadModel("unit_tests/xbuf/shared_mesh.xbuf");
		app.getRootNode().attachChild(scene);

		// All mesh instances
		LinkedList<Mesh> meshes=new LinkedList<Mesh>();
		scene.depthFirstTraversal(s -> {
			if(s instanceof Geometry){
				Geometry geom=(Geometry)s;
				Mesh mesh=geom.getMesh();
				if(!meshes.contains(mesh)) meshes.add(mesh);
			}
		});
		TestHelpers.releaseUpdateThread(app);
		if(!headless)TestHelpers.waitFor(app);
		TestHelpers.closeApp(app);

		assertTrue("Two different meshes are used, but loaded "+meshes.size(),meshes.size()==2);
	}

	
	
	@Test
	public void testMatSharing() {
		boolean headless=true;
		SimpleApplication app=TestHelpers.buildApp(headless);
		TestHelpers.hijackUpdateThread(app);

		Spatial scene=app.getAssetManager().loadModel("unit_tests/xbuf/shared_mat.xbuf");
		app.getRootNode().attachChild(scene);

		// All material instances
		LinkedList<Material> materials_instances=new LinkedList<Material>();
		scene.depthFirstTraversal(s -> {
			if(s instanceof Geometry){
				Geometry geom=(Geometry)s;
				Material mat=geom.getMaterial();
				materials_instances.add(mat);
			}
		});

		// The materials with name "shared" should be used 2 times.
		int n_shared=0;
		// The materials with name "not_shared" should be used 1 time.
		int n_not_shared=0;

		// Unique materials
		LinkedList<Material> materials=new LinkedList<Material>();
		for(Material m:materials_instances){
			if(!materials.contains(m)) materials.add(m);
			if(m.getName().equals("shared")) n_shared++;
			if(m.getName().equals("not_shared")) n_not_shared++;

		}
		
		TestHelpers.releaseUpdateThread(app);
		if(!headless)TestHelpers.waitFor(app);
		TestHelpers.closeApp(app);
		assertTrue("'shared' material is used twice, but loaded "+n_shared,n_shared==2);
		assertTrue("'not_shared' material is used once, but loaded "+n_not_shared,n_not_shared==1);
		assertTrue("Two unique materials are used, but "+materials.size()+"  loaded",materials.size()==2);
		assertTrue("Three materials instance are used, but "+materials.size()+"  loaded",materials_instances.size()==3);
	}
}
