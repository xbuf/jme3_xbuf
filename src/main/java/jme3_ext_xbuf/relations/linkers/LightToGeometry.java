package jme3_ext_xbuf.relations.linkers;

import jme3_ext_xbuf.XbufLightControl;
import jme3_ext_xbuf.relations.Linker;
import jme3_ext_xbuf.relations.Loader4Relations;
import jme3_ext_xbuf.relations.MergeData;
import static jme3_ext_xbuf.relations.LinkerHelpers.*;

import org.slf4j.Logger;

import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

public class LightToGeometry implements Linker{

	@Override
	public boolean doLink(Loader4Relations loader, MergeData data, Logger log) {
		XbufLightControl op1=getRef1(data,XbufLightControl.class);
		Spatial op2=getRef2(data,Spatial.class);
		if(op1==null||op2==null) return false;
		if(op2 instanceof Geometry)	log.warn("Do you really want to add this light to a Geometry? [{}]",data.ref1);
		
		op1.getSpatial().removeControl(((XbufLightControl)op1));
		op2.addControl(op1);
		return true;
	}
	
}
