package jme3_ext_xbuf;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

import lombok.extern.slf4j.Slf4j;
import xbuf.Datas.Data;

@Slf4j
public class XbufLoader implements AssetLoader {
	public static  Function<AssetManager,Xbuf> xbufFactory = Xbuf::new;

	@Override 
	public Object load(AssetInfo assetInfo) throws IOException {
		Node root = new Node(assetInfo.getKey().getName());
		InputStream in = null ;
		try {
			XbufKey xbufkey=null;
			AssetKey<?> key=assetInfo.getKey();
			if(key instanceof XbufKey){
				xbufkey=(XbufKey)key;
			}else{
				xbufkey=new XbufKey(key.getName());
			}
			in = assetInfo.openStream();
			
			Xbuf xbuf = XbufLoader.xbufFactory.apply(assetInfo.getManager());
			Data src = Data.parseFrom(in, xbuf.registry);		
			XbufContext context=new XbufContext();
//			context.log=log;
			context.setSettings(xbufkey);
			xbuf.merge(src, root, context);
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
