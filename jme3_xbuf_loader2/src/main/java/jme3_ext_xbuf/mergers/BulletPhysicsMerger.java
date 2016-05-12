package jme3_ext_xbuf.mergers;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.plugins.physics4loaders.PhysicsData;
import com.jme3.scene.plugins.physics4loaders.PhysicsData.PhysicsShape;
import com.jme3.scene.plugins.physics4loaders.PhysicsData.PhysicsType;

import jme3_ext_xbuf.Converters;
import jme3_ext_xbuf.XbufContext;
import xbuf.Datas.Data;
import xbuf_ext.Bullet.BulletPhysics;

public class BulletPhysicsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context) {
		for(BulletPhysics p:src.getBulletPhysicsList()){
			PhysicsData phydata=new PhysicsData();
			phydata.type=PhysicsType.values()[p.getType().ordinal()];
			phydata.shape=PhysicsShape.values()[p.getShape().ordinal()];
			phydata.mass=p.getMass();
			phydata.friction=p.getFriction();
			phydata.angularDamping=p.getAngularDamping();
			phydata.linearDamping=p.getLinearDamping();
			phydata.margin=p.getMargin();
			phydata.restitution=p.getRestitution();
			phydata.angularFactor=Converters.cnv(p.getAngularFactor(),new Vector3f());
			phydata.linearFactor=Converters.cnv(p.getLinearFactor(),new Vector3f());
			phydata.isGhost=p.getIsGhost();
			phydata.isKinematic=p.getIsKinematic();
			phydata.collisionGroup=p.getCollisionGroup();
			phydata.collisionMask=p.getCollisionMask();

			String id=p.getId();
			context.put(id,phydata);
		}
	}
}
