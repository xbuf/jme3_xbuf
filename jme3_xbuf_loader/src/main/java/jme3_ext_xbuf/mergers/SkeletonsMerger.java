package jme3_ext_xbuf.mergers;

import java.util.HashMap;

import org.slf4j.Logger;

import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.scene.Node;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import lombok.experimental.ExtensionMethod;
import xbuf.Datas.Data;
import xbuf.Relations.Relation;
import xbuf.Skeletons;

@ExtensionMethod({jme3_ext_xbuf.ext.PrimitiveExt.class})
public class SkeletonsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context, Logger log) {
		for(xbuf.Skeletons.Skeleton e:src.getSkeletonsList()){
			// TODO manage parent hierarchy
			String id=e.getId();
			// TODO: merge with existing
			Skeleton child=makeSkeleton(e);
			context.put(id,child);
			// Skeleton child = (Skeleton)components.get(id);
		}
	}

	private Skeleton makeSkeleton(Skeletons.Skeleton e) {
		Bone[] bones=new Bone[e.getBonesCount()];
		HashMap<String,Bone> db=new HashMap<String,Bone>();
		for(int i=0;i<bones.length;i++){
			xbuf.Skeletons.Bone src=e.getBones(i);
			Bone b=new Bone(src.getName());
			b.setBindTransforms(src.getTransform().getTranslation().toJME(),src.getTransform().getRotation().toJME(),src.getTransform().getScale().toJME());
			db.put(src.getId(),b);
			bones[i]=b;
		}
		for(Relation r:e.getBonesGraphList()){
			Bone parent=db.get(r.getRef1());
			Bone child=db.get(r.getRef2());
			parent.addChild(child);
		}
		Skeleton sk=new Skeleton(bones);
		sk.setBindingPose();
		return sk;
	}
}
