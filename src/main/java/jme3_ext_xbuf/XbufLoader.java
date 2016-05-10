package jme3_ext_xbuf;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;


import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;

import lombok.extern.log4j.Log4j2;
import xbuf.Datas.Data;

@Log4j2
public class XbufLoader implements AssetLoader {
	public static  Function<AssetManager,Xbuf> xbufFactory = Xbuf::new;

	@Override 
	public Object load(AssetInfo assetInfo) throws IOException {
		Xbuf xbuf = XbufLoader.xbufFactory.apply(assetInfo.getManager());
		Node root = new Node(assetInfo.getKey().getName());
		InputStream in = null ;
		try {
			in = assetInfo.openStream();
			Data src = Data.parseFrom(in, xbuf.registry);
			XbufContext context=new XbufContext();
			xbuf.merge(src, root, context, log);
			log.debug("Context:\n{}",context.toString());
		} finally {
			if(in!=null)in.close();
		}
		// if
//		MikktspaceTangentGenerator.generate(root);
		//		if (root.getQuantity() == 1) { < not good. will cause inconsistent behaviour ...
		//			return root.getChild(0);
		//		} else {
		//			return root;
		//		}
		return root;
	}

}
