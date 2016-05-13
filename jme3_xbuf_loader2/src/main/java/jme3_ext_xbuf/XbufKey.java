package jme3_ext_xbuf;

import com.jme3.physicsloader.impl.PhysicsLoaderModelKey;


public class XbufKey extends PhysicsLoaderModelKey<XbufKey>{

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
