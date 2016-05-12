package jme3_ext_xbuf.mergers;

import com.jme3.scene.Node;

import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.mergers.meshes.XbufMesh;
import xbuf.Datas.Data;

//@ExtensionMethod({jme3_ext_xbuf.ext.XbufMeshExt.class})
public class MeshesMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context) {
		for(xbuf.Meshes.Mesh g:src.getMeshesList())
			context.put(g.getId(),new XbufMesh(g,context.log));//g.toJME(context,log));
	}

}
