package jme3_ext_xbuf.relations.linkers;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef2;

import org.slf4j.Logger;

import com.jme3.scene.Node;

import jme3_ext_xbuf.relations.Linker;
import jme3_ext_xbuf.relations.Loader4Relations;
import jme3_ext_xbuf.relations.MergeData;

public class NodeToNode implements Linker{
 
	@Override
	public boolean doLink(Loader4Relations loader, MergeData data, Logger log) {
		Node op1=getRef1(data,Node.class);
		Node op2=getRef2(data,Node.class);
		if(op1==null||op2==null) return false;
		op1.attachChild(op2);
		return true;
	}
}
