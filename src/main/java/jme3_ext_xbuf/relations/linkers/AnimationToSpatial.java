package jme3_ext_xbuf.relations.linkers;

import static jme3_ext_xbuf.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef2;

import org.slf4j.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.SkeletonControl_31;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.relations.Linker;
import jme3_ext_xbuf.relations.Loader4Relations;
import jme3_ext_xbuf.relations.MergeData;

public class AnimationToSpatial implements Linker{
	@Override
	public boolean doLink(Loader4Relations loader,MergeData data, Logger log) {
		Animation op1=getRef1(data,Animation.class);
		Spatial op2=getRef2(data,Spatial.class);
		if(op1==null||op2==null)return false;
		 
		AnimControl c=op2.getControl(AnimControl.class);
		if(c==null){
			SkeletonControl_31 sc=op2.getControl(SkeletonControl_31.class);
			c=sc!=null?new AnimControl(sc.getSkeleton()):new AnimControl();
			op2.addControl(c);			
		}
		c.addAnim(op1);

		return true;
	}

}
