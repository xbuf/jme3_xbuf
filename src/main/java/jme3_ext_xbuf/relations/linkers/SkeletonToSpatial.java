package jme3_ext_xbuf.relations.linkers;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef1;
import static jme3_ext_xbuf.relations.LinkerHelpers.getRef2;

import java.util.HashMap;
import java.util.Optional;

import org.slf4j.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl_31;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.relations.Linker;
import jme3_ext_xbuf.relations.Loader4Relations;
import jme3_ext_xbuf.relations.MergeData;
import xbuf.Datas.Data;
 
public class SkeletonToSpatial implements Linker{
	@Override
	public boolean doLink(Loader4Relations loader, MergeData data, Logger log) {
		Object op1=getRef1(data,Skeleton.class);
		Object op2=getRef2(data,Spatial.class);
		if(op1==null||op2==null) { // The other way around. (The original code did this)
			op1=getRef2(data,Spatial.class);
			op2=getRef1(data,Skeleton.class);
			return false;
		}
		if(op1==null||op2==null)return false;
        link((Spatial)op2, (Skeleton)op1, findAnimLinkedToRef(data.src,data.ref1, data.context));
		
        ((Spatial)op2).depthFirstTraversal(s->{ 
			if(s instanceof Geometry)	{
				Material m=((Geometry)s).getMaterial();
				String matref=data.context.refOf(m);
				if(matref==null){ // should never happen!
					log.error("Mat is not referred?");
					return;
				} 
				String refusage="G~usage~"+matref;
				int n=(int)Optional.ofNullable(data.context.get(refusage)).orElse(0);
				if(n>1){
					Material clone=m.clone();
					data.context.put("G~"+matref+"~cloned~"+System.currentTimeMillis(),clone,matref);
					s.setMaterial(clone);
				}
			}
		});
		return true;
	}
	

	@SuppressWarnings("unchecked")
	public Iterable<Animation> findAnimLinkedToRef(Data src, String ref, XbufContext components) {
		return (Iterable<Animation>)src.getRelationsList().stream().map(r -> {
			if(r.getRef1()==ref&&components.get(r.getRef2()) instanceof Animation) return components.get(r.getRef2());
			else if(r.getRef2()==ref&&components.get(r.getRef1()) instanceof Animation) return components.get(r.getRef1());
			else return null;
		}).filter(i -> i!=null).iterator();
	}
	
	// see http://hub.jmonkeyengine.org/t/skeletoncontrol-or-animcontrol-to-host-skeleton/31478/4
	public void link(Spatial v, Skeleton sk, Iterable<Animation> skAnims) {
		v.removeControl(SkeletonControl_31.class);
		// update AnimControl if related to skeleton
		AnimControl ac0=v.getControl(AnimControl.class);
		AnimControl ac1;
		if(ac0!=null /* && ac.getSkeleton() != null*/ ){
			v.removeControl(ac0);
			AnimControl ac2=new AnimControl(sk);
			HashMap<String,Animation> anims=new HashMap<String,Animation>();
			ac0.getAnimationNames().forEach(name -> anims.put(name,ac0.getAnim(name).clone()));
			ac2.setAnimations(anims);
			v.addControl(ac2);
			ac1=ac2;
		}else{
			// always add AnimControl else NPE when SkeletonControl.clone
			AnimControl ac2=new AnimControl(sk);
			v.addControl(ac2);
			ac1=ac2;
		}
		// SkeletonControl should be after AnimControl in the list of Controls
		v.addControl(new SkeletonControl_31(sk));
		skAnims.forEach(ac1::addAnim);
//		cloneMaterialOnGeometry(v);
	}

}
