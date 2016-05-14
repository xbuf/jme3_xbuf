package jme3_ext_xbuf.mergers.relations.linkers;

import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef2;

import com.jme3.light.Light;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.mergers.RelationsMerger;
import jme3_ext_xbuf.mergers.relations.Linker;
import jme3_ext_xbuf.mergers.relations.RefData;
import lombok.extern.slf4j.Slf4j;
import xbuf_rt.XbufLightControl;


@Slf4j
public class LightToGeometry implements Linker{

	@Override
	public boolean doLink(RelationsMerger loader, RefData data) {
		Light op1=getRef1(data,Light.class);
		Spatial op2=getRef2(data,Spatial.class);
		if(op1==null||op2==null) return false;
		if(op2 instanceof Geometry)	log.warn("Do you really want to add this light to a Geometry? [{}]",data.ref1);
		
		if(data.context.getSettings().useLightControls()){
			XbufLightControl xbuflc=new XbufLightControl();
			xbuflc.setLight(op1);	
			op2.addControl(xbuflc);
			data.root.addLight(op1); 
		}else{
			op2.addLight(op1); 
		}	
				
		return true;
	}
	
}
