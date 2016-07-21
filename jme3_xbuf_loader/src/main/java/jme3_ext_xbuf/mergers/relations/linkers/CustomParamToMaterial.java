package jme3_ext_xbuf.mergers.relations.linkers;

import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef2;

import org.slf4j.Logger;

import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector4f;

import jme3_ext_xbuf.mergers.RelationsMerger;
import jme3_ext_xbuf.mergers.relations.Linker;
import jme3_ext_xbuf.mergers.relations.RefData;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import xbuf_ext.CustomParams.CustomParam;
import xbuf_ext.CustomParams.CustomParamList;

@ExtensionMethod({jme3_ext_xbuf.ext.PrimitiveExt.class})
@Slf4j
public class CustomParamToMaterial implements Linker{

	@Override
	public boolean doLink(RelationsMerger loader,RefData data, Logger log) {
		CustomParamList op1=getRef1(data,CustomParamList.class,log);
		Material op2=getRef2(data,Material.class,log);
		if(op1==null||op2==null) return false;
		for(CustomParam p:op1.getParamsList())merge(loader,p,op2);
		return true;
	}

	protected Material merge(RelationsMerger loader,CustomParam p, Material dst) {
		String name=p.getName();
		System.err.println("set ....." + name);
		switch(p.getValueCase()){
			case VBOOL:
				dst.setBoolean(name,p.getVbool());
				break;
			case VCOLOR:
				dst.setColor(name,p.getVcolor().toJME());
				break;
			case VFLOAT:
				dst.setFloat(name,p.getVfloat());
				break;
			case VINT:
				dst.setInt(name,p.getVint());
				break;
			case VMAT4:
				dst.setMatrix4(name,p.getVmat4().toJME());
				break;
			case VQUAT:
				Quaternion q = p.getVquat().toJME();
				dst.setVector4(name,new Vector4f(q.getX(), q.getY(), q.getZ(), q.getW()));
				break;
			case VTEXTURE:
				dst.setTexture(name,loader.getMatMerger().getValue(p.getVtexture()));
				break;
			case VVEC2:
				dst.setVector2(name,p.getVvec2().toJME());
				break;
			case VVEC3:
				dst.setVector3(name,p.getVvec3().toJME());
				break;
			case VVEC4:
				dst.setVector4(name,p.getVvec4().toJME());
				break;
			default:
				log.warn("Material doesn't support parameter : {} of type {}",name,p.getValueCase().name());
		}
		return dst;
	}
}
