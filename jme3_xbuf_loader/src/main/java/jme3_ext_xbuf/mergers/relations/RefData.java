package jme3_ext_xbuf.mergers.relations;

import com.jme3.scene.Node;

import jme3_ext_xbuf.XbufContext;
import lombok.AllArgsConstructor;
import xbuf.Datas.Data;

@AllArgsConstructor
public class RefData {
	public String ref1,ref2;
	public Data src;
	public Node root;
	public XbufContext context;
}