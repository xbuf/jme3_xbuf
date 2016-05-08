package jme3_ext_xbuf.relations.linkers;

import static jme3_ext_xbuf.Converters.cnv;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef2;

import org.slf4j.Logger;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Spatial;
 
import jme3_ext_xbuf.relations.Linker;
import jme3_ext_xbuf.relations.Loader4Relations;
import jme3_ext_xbuf.relations.MergeData;
import xbuf_ext.CustomParams.CustomParam;
import xbuf_ext.CustomParams.CustomParamList;

public class CustomParamToSpatial implements Linker{

	@Override
	public boolean doLink(Loader4Relations loader,MergeData data, Logger log) {
		CustomParamList op1=getRef1(data,CustomParamList.class);
		Spatial op2=getRef2(data,Spatial.class);
		if(op1==null||op2==null) return false;
		for(CustomParam p:op1.getParamsList())merge(loader,p,op2,log);
		return true;
	}
		
	protected Spatial merge(Loader4Relations loader,CustomParam p, Spatial dst, Logger log) {
		String name=p.getName();
		switch(p.getValueCase()){
			case VALUE_NOT_SET:
				dst.setUserData(name,null);
				break;
			case VBOOL:
				dst.setUserData(name,p.getVbool());
				break;
			case VCOLOR:
				dst.setUserData(name,cnv(p.getVcolor(),new ColorRGBA()));
				break;
			case VFLOAT:
				dst.setUserData(name,p.getVfloat());
				break;
			case VINT:
				dst.setUserData(name,p.getVint());
				break;
			case VMAT4:
				dst.setUserData(name,cnv(p.getVmat4(),new Matrix4f()));
				break;
			case VQUAT:
				dst.setUserData(name,cnv(p.getVquat(),new Vector4f()));
				break;
			case VSTRING:
				dst.setUserData(name,p.getVstring());
				break;
			case VTEXTURE:
				dst.setUserData(name,loader.loader4Materials.getValue(p.getVtexture(),log));
				break;
			case VVEC2:
				dst.setUserData(name,cnv(p.getVvec2(),new Vector2f()));
				break;
			case VVEC3:
				dst.setUserData(name,cnv(p.getVvec3(),new Vector3f()));
				break;
			case VVEC4:
				dst.setUserData(name,cnv(p.getVvec4(),new Vector4f()));
				break;
			default:
				log.warn("Material doesn't support parameter : {} of type {}",name,p.getValueCase().name());
		}
		return dst;
	}
}
