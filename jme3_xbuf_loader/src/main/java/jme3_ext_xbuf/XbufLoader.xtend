package jme3_ext_xbuf;

import com.jme3.asset.AssetInfo
import com.jme3.asset.AssetLoader
import com.jme3.scene.Node
import java.io.InputStream
import java.util.HashMap
import org.slf4j.LoggerFactory
import xbuf.Datas.Data

class XbufLoader implements AssetLoader {
	override Object load(AssetInfo assetInfo) {
		val xbuf = new Xbuf(assetInfo.getManager())
		val root = new Node(assetInfo.getKey().getName())
		var in = null as InputStream
		val logger = new LoggerCollector("parse:"+ assetInfo.getKey().getName())
		try {
			in = assetInfo.openStream()
			val src = Data.parseFrom(in, xbuf.registry)
			xbuf.merge(src, root, new HashMap<String, Object>(), logger)
		} finally {
			in?.close()
		}
		logger.dumpTo(LoggerFactory.getLogger(this.getClass()))
		//TODO check and transfert Lights on root if quantity == 1
		if (root.getQuantity() == 1) { root.getChild(0) } else { root }
	}

}
