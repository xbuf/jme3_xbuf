package jme3_ext_xbuf.relations;

import org.slf4j.Logger;

public interface Linker{
	public boolean doLink(Loader4Relations loader,MergeData data, Logger log);


}