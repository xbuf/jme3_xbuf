package jme3_ext_xbuf.relations.linkers;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef2;

import java.util.Optional;

import org.slf4j.Logger;

import com.jme3.animation.SkeletonControl;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;

import jme3_ext_xbuf.relations.Linker;
import jme3_ext_xbuf.relations.Loader4Relations;
import jme3_ext_xbuf.relations.MergeData;

public class MaterialToGeometry  implements Linker{
 
	@Override
	public boolean doLink(Loader4Relations loader, MergeData data, Logger log) {
		Material op1=getRef1(data,Material.class);
		Geometry op2=getRef2(data,Geometry.class);
		if(op1==null||op2==null) return false;
		if(op2.getControl(SkeletonControl.class)!=null){
			op1=op1.clone();
			data.context.put("G~"+data.ref1+"~cloned~"+System.currentTimeMillis(),op1,data.ref1);
		}else{
			String refusage="G~usage~"+data.ref1;
			int n=(int)Optional.ofNullable(data.context.get(refusage)).orElse(0);
			data.context.put(refusage,n++);
		}
		op2.setMaterial(op1);
		return true;
	}
}
