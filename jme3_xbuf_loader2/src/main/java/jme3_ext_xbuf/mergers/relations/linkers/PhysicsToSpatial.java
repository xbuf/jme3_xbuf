package jme3_ext_xbuf.mergers.relations.linkers;

import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef2;

import com.jme3.physicsloader.PhysicsData;
import com.jme3.physicsloader.PhysicsLoader;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.Control;

import jme3_ext_xbuf.mergers.RelationsMerger;
import jme3_ext_xbuf.mergers.relations.Linker;
import jme3_ext_xbuf.mergers.relations.RefData;

public class PhysicsToSpatial  implements Linker{

	@Override
	public boolean doLink(RelationsMerger rloader, RefData data) {
		PhysicsData op1=getRef1(data,PhysicsData.class);
		Geometry op2=getRef2(data,Geometry.class);
		if(op1==null||op2==null)return false;
		PhysicsLoader<?> loader=data.context.getSettings().getPhysicsLoader();
		Control pc=(Control)loader.load(data.context.getSettings(),op2,op1);	
		if(pc!=null)op2.addControl(pc);
		return true;
	}

}
