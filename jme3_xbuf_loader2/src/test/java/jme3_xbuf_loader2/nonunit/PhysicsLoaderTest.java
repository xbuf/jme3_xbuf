package jme3_xbuf_loader2.nonunit;

import java.io.File;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.vhacd.VHACDCollisionShapeFactory;
import com.jme3.bullet.vhacd.cache.PersistentByBuffersCaching;
import com.jme3.physicsloader.impl.bullet.BulletPhysicsLoader;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.XbufKey;
import jme3_ext_xbuf.XbufLoader;
import vhacd.VHACDParameters;

public class PhysicsLoaderTest extends SimpleApplication{



	@Override
	public void simpleInitApp() {
		Commons.initApp(this);
		assetManager.registerLoader(XbufLoader.class,"xbuf");

		BulletAppState bullet=stateManager.getState(BulletAppState.class);
		bullet.setDebugEnabled(true);

		// #############
		new File("cache").mkdirs();

		VHACDCollisionShapeFactory vhacd=new VHACDCollisionShapeFactory();
		PersistentByBuffersCaching caching=new PersistentByBuffersCaching("cache");
		vhacd.cachingQueue().add(caching);
		
		VHACDParameters vhacd_params=new VHACDParameters();
		vhacd_params.setMaxVerticesPerHull(8);
		vhacd.setParameters(vhacd_params);
		
		Spatial xbuf_scene=assetManager.loadModel(
				new XbufKey("models/physicsScene2/physicsScene2.xbuf")
				.usePhysics(new BulletPhysicsLoader().useCompoundCapsule(true))
				.useVHACD(vhacd)
				.useEnhancedRigidbodies(true)
				.useLightControls(true)
		);
		
		bullet.getPhysicsSpace().addAll(xbuf_scene);
		rootNode.attachChild(xbuf_scene);
	}

	public static void main(String[] args) {
		new PhysicsLoaderTest().start();
	}
}
