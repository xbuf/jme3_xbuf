package jme3_ext_xbuf.mergers;

import java.util.HashMap;

import org.apache.logging.log4j.Logger;

import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import jme3_ext_xbuf.Converters;
import jme3_ext_xbuf.XbufContext;
import xbuf.Datas.Data;
import xbuf.Relations.Relation;
import xbuf.Skeletons;

public class SkeletonsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context) {
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
			b.setBindTransforms(Converters.cnv(src.getTransform().getTranslation(),new Vector3f()),Converters.cnv(src.getTransform().getRotation(),new Quaternion()),Converters.cnv(src.getTransform().getScale(),new Vector3f()));
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
