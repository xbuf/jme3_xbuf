package jme3_ext_xbuf;

import org.slf4j.Logger;

import com.jme3.scene.Node;

import xbuf.Datas.Data;

public interface Merger{
	public void apply(Data src, Node root, XbufContext context,Logger log);
}
