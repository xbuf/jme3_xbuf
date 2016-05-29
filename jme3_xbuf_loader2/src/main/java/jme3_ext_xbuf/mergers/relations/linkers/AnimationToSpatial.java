package jme3_ext_xbuf.mergers.relations.linkers;

import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.mergers.relations.LinkerHelpers.getRef2;

import org.slf4j.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.animation.SkeletonControl;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.animations.XbufAnimation;
import jme3_ext_xbuf.mergers.RelationsMerger;
import jme3_ext_xbuf.mergers.relations.Linker;
import jme3_ext_xbuf.mergers.relations.RefData;

public class AnimationToSpatial implements Linker{
	@Override
	public boolean doLink(RelationsMerger loader,RefData data, Logger log) {
		XbufAnimation op1=getRef1(data,XbufAnimation.class,log);
		Spatial op2=getRef2(data,Spatial.class,log);
		if(op1==null||op2==null)return false;
		AnimControl c=op2.getControl(AnimControl.class);
		if(c==null){
			SkeletonControl sc=op2.getControl(SkeletonControl.class);
			c=sc!=null?new AnimControl(sc.getSkeleton()):new AnimControl();
			op2.addControl(c);
		}
		c.addAnim(op1.toJME(c.getSkeleton()));
		return true;
	}

}
