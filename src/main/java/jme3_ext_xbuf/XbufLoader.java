package jme3_ext_xbuf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

import xbuf.Datas.Data;

public class XbufLoader implements AssetLoader {
	public static  Function<AssetManager,Xbuf> xbufFactory = Xbuf::new;

	@Override 
	public Object load(AssetInfo assetInfo) throws IOException {
		Xbuf xbuf = XbufLoader.xbufFactory.apply(assetInfo.getManager());
		Node root = new Node(assetInfo.getKey().getName());
		InputStream in = null ;
		LoggerCollector logger = new LoggerCollector("parse:" + assetInfo.getKey().getName());
		try {
			in = assetInfo.openStream();
			Data src = Data.parseFrom(in, xbuf.registry);
			xbuf.merge(src, root, new HashMap<String, Object>(), logger);
		} finally {
			if(in!=null)in.close();
		}
		logger.dumpTo(LoggerFactory.getLogger(this.getClass()));
		// TODO check and transfert Lights on root if quantity == 1
		if (root.getQuantity() == 1) {
			return root.getChild(0);
		} else {
			return root;
		}
	}

}
