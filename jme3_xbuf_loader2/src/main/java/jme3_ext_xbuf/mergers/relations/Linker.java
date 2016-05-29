package jme3_ext_xbuf.mergers.relations;

import org.slf4j.Logger;

import jme3_ext_xbuf.mergers.RelationsMerger;

public interface Linker{
	public boolean doLink(RelationsMerger loader,RefData data, Logger log);


}
