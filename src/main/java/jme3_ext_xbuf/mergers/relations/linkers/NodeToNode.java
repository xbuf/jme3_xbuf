package jme3_ext_xbuf.mergers.relations.linkers;
import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef2;

import org.apache.logging.log4j.Logger;

import com.jme3.scene.Node;

import jme3_ext_xbuf.mergers.RelationsMerger;
import jme3_ext_xbuf.mergers.relations.Linker;
import jme3_ext_xbuf.mergers.relations.RefData;

public class NodeToNode implements Linker{
 
	@Override
	public boolean doLink(RelationsMerger loader, RefData data, Logger log) {
		Node op1=getRef1(data,Node.class);
		Node op2=getRef2(data,Node.class);
		if(op1==null||op2==null) return false;
		op1.attachChild(op2);
		return true;
	}
}
