package jme3_ext_xbuf.mergers;

import com.jme3.scene.Node;

import jme3_ext_xbuf.XbufContext;
import xbuf.Datas.Data;

public interface Merger{
	public void apply(Data src, Node root, XbufContext context);
}
