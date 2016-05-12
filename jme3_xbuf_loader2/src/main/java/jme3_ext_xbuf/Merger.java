package jme3_ext_xbuf;

import com.jme3.scene.Node;

import xbuf.Datas.Data;

public interface Merger{
	public void apply(Data src, Node root, XbufContext context);
}
