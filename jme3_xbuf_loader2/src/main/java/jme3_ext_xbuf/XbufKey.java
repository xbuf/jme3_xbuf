package jme3_ext_xbuf;

import com.jme3.scene.plugins.physics4loaders.impl.PhysicsLoaderModelKey;



public class XbufKey extends PhysicsLoaderModelKey{
	public XbufKey(){}
	
	public XbufKey(String s){
		super(s);
	}
	
	protected boolean useLightControls;
	public XbufKey useLightControls(boolean x){
		useLightControls=x;
		return this;
	}
	
	
	public boolean useLightControls(){
		return useLightControls;
	}
	
}
