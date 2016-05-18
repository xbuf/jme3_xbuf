package jme3_ext_xbuf;

import com.jme3.physicsloader.PhysicsLoader;
import com.jme3.physicsloader.constraint.GenericConstraint;
import com.jme3.physicsloader.impl.PhysicsLoaderModelKey;
import com.jme3.physicsloader.impl.bullet.BulletPhysicsLoader;

import lombok.extern.slf4j.Slf4j;
import xbuf_rt.XbufPhysicsLoaderSettings;


@Slf4j
public class XbufKey extends PhysicsLoaderModelKey<XbufKey> implements XbufPhysicsLoaderSettings{

	public XbufKey(){}
	
	public XbufKey(String s){
		super(s);
	}
	
	protected boolean useLightControls;
	public XbufKey useLightControls(boolean x){
		useLightControls=x;
		return this;
	}
	
	@Override
	public XbufKey usePhysics(PhysicsLoader<?,?> l){
		if(l!=null&&!(l instanceof BulletPhysicsLoader)){
			log.warn("Cannot use {}, physicsloader not supported",l.getClass());
			return this;
		}
		super.usePhysics(l);
		return this;
	}
	
	
	public boolean useLightControls(){
		return useLightControls;
	}

	private final static Class<?>[] supportedConstraints={
			GenericConstraint.class
	};
	
	@Override
	public Class<?>[] getSupportedConstraints() {
		return supportedConstraints;
	}
	
}
