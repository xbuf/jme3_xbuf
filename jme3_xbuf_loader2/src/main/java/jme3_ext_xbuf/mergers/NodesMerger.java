package jme3_ext_xbuf.mergers;

import org.slf4j.Logger;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import lombok.experimental.ExtensionMethod;
import xbuf.Datas.Data;
import xbuf.Primitives.Transform;
import xbuf.Tobjects.TObject;

@ExtensionMethod({jme3_ext_xbuf.ext.PrimitiveExt.class})
public class NodesMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context, Logger log) {
			for(TObject n:src.getTobjectsList()){
				String id=n.getId();
				Spatial child=(Spatial)context.get(id);
				if(child==null){
					child=new Node("");
					root.attachChild(child);
					context.put(id,child);
				}
				child.setName(n.hasName()?n.getName():n.getId());
				Transform transform=n.getTransform();
				child.setLocalRotation(transform.getRotation().toJME());
				child.setLocalTranslation(transform.getTranslation().toJME());
				child.setLocalScale(transform.getScale().toJME());
			}
	}
}
