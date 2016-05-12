package jme3_ext_xbuf;

import com.jme3.asset.ModelKey;

public class XbufKey extends ModelKey{
	public XbufKey(){}
	
	public XbufKey(String s){
		super(s);
	}
	
	protected boolean useLightControls;
	public ModelKey useLightControls(boolean x){
		useLightControls=x;
		return this;
	}
	
	
	public boolean useLightControls(){
		return useLightControls;
	}
	
}
