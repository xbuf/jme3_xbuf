package jme3_ext_xbuf.mergers;

import org.apache.logging.log4j.Logger;

import com.jme3.scene.Node;

import jme3_ext_xbuf.XbufContext;
import lombok.experimental.ExtensionMethod;
import xbuf.Datas.Data;

@ExtensionMethod({jme3_ext_xbuf.ext.XbufMeshExt.class})
public class MeshesMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context, Logger log) {
		for(xbuf.Meshes.Mesh g:src.getMeshesList())
			context.put(g.getId(),g.toJME(context,log));
	}

}
