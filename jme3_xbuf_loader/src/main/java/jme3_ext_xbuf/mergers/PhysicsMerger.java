package jme3_ext_xbuf.mergers;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;

import com.jme3.physicsloader.PhysicsShape;
import com.jme3.physicsloader.rigidbody.RigidBody;
import com.jme3.physicsloader.rigidbody.RigidBodyType;
import com.jme3.scene.Node;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import lombok.experimental.ExtensionMethod;
import xbuf.Datas.Data;
import xbuf_ext.Physics.Constraint;

@ExtensionMethod({jme3_ext_xbuf.ext.PrimitiveExt.class})
public class PhysicsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context, Logger log) {
		for(xbuf_ext.Physics.PhysicsData data:src.getPhysicsList()){
			if(data.getRigidbody()!=null)loadRB(data.getRigidbody(),context);
			if(data.getConstraint()!=null)loadCT(data.getConstraint(),context);
		}
	}

	protected void loadCT(Constraint xbufct,XbufContext context) {
		Collection<Constraint> constraints=context.get("G~constraints");
		if(constraints==null){
			constraints=new LinkedList<Constraint>();
			context.put("G~constraints",constraints);
		}
		constraints.add(xbufct);
		// do parsing during linking.
	}


	protected void loadRB( xbuf_ext.Physics.RigidBody xbufrb,XbufContext context) {
		RigidBody rb=new RigidBody();
		rb.type=RigidBodyType.values()[xbufrb.getType().ordinal()];
		rb.shape=PhysicsShape.values()[xbufrb.getShape().ordinal()];
		rb.mass=xbufrb.getMass();
		rb.friction=xbufrb.getFriction();
		rb.angularDamping=xbufrb.getAngularDamping();
		rb.linearDamping=xbufrb.getLinearDamping();
		rb.margin=xbufrb.getMargin();
		rb.restitution=xbufrb.getRestitution();
		rb.angularFactor=xbufrb.getAngularFactor().toJME();
		rb.linearFactor=xbufrb.getLinearFactor().toJME();
		rb.isKinematic=xbufrb.getIsKinematic();
		rb.collisionGroup=xbufrb.getCollisionGroup();
		rb.collisionMask=xbufrb.getCollisionMask();

		String id=xbufrb.getId();
		context.put(id,rb);
	}
}
