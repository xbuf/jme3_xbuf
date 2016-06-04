package jme3_ext_xbuf.mergers;

import org.slf4j.Logger;

import com.jme3.scene.Node;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.scene.XbufMesh;
import lombok.RequiredArgsConstructor;
import xbuf.Datas.Data;

//@ExtensionMethod({jme3_ext_xbuf.ext.XbufMeshExt.class})
@RequiredArgsConstructor
public class MeshesMerger implements Merger{
	final MaterialsMerger loader4Materials;
	
	@Override
	public void apply(Data src, Node root, XbufContext context, Logger log) {
		for(xbuf.Meshes.Mesh g:src.getMeshesList())
			context.put(g.getId(),new XbufMesh(g, loader4Materials.newDefaultMaterial()));//g.toJME(context,log));
	}

}
