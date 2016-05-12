package jme3_ext_xbuf.mergers;

import com.jme3.scene.Node;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import xbuf.Datas.Data;
import xbuf_ext.CustomParams;

public class CustomParamsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context) {
		for(CustomParams.CustomParamList srccp:src.getCustomParamsList()){
			// TODO merge with existing
			context.put(srccp.getId(),srccp);
		}
	}

}
