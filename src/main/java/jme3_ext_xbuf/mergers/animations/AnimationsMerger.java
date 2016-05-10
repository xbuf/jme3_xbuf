package jme3_ext_xbuf.mergers.animations;

import java.lang.reflect.Array;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.jme3.animation.Animation;
import com.jme3.animation.SpatialTrack;
import com.jme3.animation.Track;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import jme3_ext_animation.NamedBoneTrack;
import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.mergers.Merger;
import lombok.experimental.ExtensionMethod;
import xbuf.Datas.Data;
import xbuf_ext.AnimationsKf;
import xbuf_ext.AnimationsKf.AnimationKF;
import xbuf_ext.AnimationsKf.SampledTransform;

@SuppressWarnings("unchecked")

@ExtensionMethod({jme3_ext_xbuf.ext.ListExt.class,jme3_ext_xbuf.ext.XbufMeshExt.class})
public class AnimationsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context, Logger log) {
		for(AnimationsKf.AnimationKF e:src.getAnimationsKfList()){
			java.lang.String id=e.getId();
			// TODO: merge with existing
			Animation a=new Animation(e.getName(),((float)e.getDuration())/1000f);
			for(AnimationsKf.Clip clip:e.getClipsList()){
				if(clip.hasSampledTransform()){
					Track t=clip.getSampledTransform().hasBoneName()?makeTrackBone(clip.getSampledTransform()):makeTrackSpatial(clip.getSampledTransform());
					a.addTrack(t);
				}
			}
			context.put(id,a);
		}
	}

	private NamedBoneTrack makeTrackBone(SampledTransform bt) {
		float times[]=new float[bt.getAtCount()];
		List<Integer> at=bt.getAtList();
		int i=0;
		for(Integer v:at)times[i++]=((float)v)/1000f;
		Vector3f[] translations=toArray(Vector3f.class,
				this::vector3fCollector,
				bt.getTranslationXList(),bt.getTranslationYList(),bt.getTranslationZList());
		
		Quaternion[] rotations=toArray(Quaternion.class,
				this::quaternionCollector,
				bt.getRotationXList(),bt.getRotationYList(),bt.getRotationZList(),bt.getRotationWList());
				
		Vector3f[] scales=toArray(Vector3f.class,
				this::vector3fCollector,
				bt.getScaleXList(),bt.getScaleYList(),bt.getScaleZList());				
				
		return new NamedBoneTrack(bt.getBoneName(),times,translations,rotations,scales);
	}
	
	
	
	private interface ArrayCollector {
		<T> T collect(Float vals[]);
	}
	
	private <T> T quaternionCollector(Float vals[]){
		return (T) new Quaternion(vals[0],vals[1],vals[2],vals[3]);
	}
	
	private <T> T vector3fCollector(Float vals[]){
		return (T) new Vector3f(vals[0],vals[1],vals[2]);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T[] toArray(Class<T> type,ArrayCollector collector,List<Float> ...lists){
		int i=-1;
		for(List<Float> l:lists){
			if(l.size()>i)i=l.size();		
		}
		T output[]=(T[])Array.newInstance(type,i);		
		for(int j=0;j<i;j++){
			Float cv[]=new Float[lists.length];
			for(int q=0;q<cv.length;q++){
				cv[q]=lists[q].get(j);
			}
			output[j]=collector.collect(cv);	
		}		
		return output;
	}
//
	private SpatialTrack makeTrackSpatial(SampledTransform bt) {
		float[] times=new float[bt.getAtCount()];
		List<Integer> at=bt.getAtList();
		int i=0;
		for(Integer v:at) times[i++]=((float)v)/1000f;
		Vector3f[] translations=toArray(Vector3f.class,
				this::vector3fCollector,
				bt.getTranslationXList(),bt.getTranslationYList(),bt.getTranslationZList());
		
		Quaternion[] rotations=toArray(Quaternion.class,
				this::quaternionCollector,
				bt.getRotationXList(),bt.getRotationYList(),bt.getRotationZList(),bt.getRotationWList());
				
		Vector3f[] scales=toArray(Vector3f.class,
				this::vector3fCollector,
				bt.getScaleXList(),bt.getScaleYList(),bt.getScaleZList());				
		return new SpatialTrack(times,translations,rotations,scales);
	}
//
//	private Vector3f[] cnvToVector3fArray(List<Float> xs, List<Float> ys, List<Float> zs) {
//		int size=Math.max(Math.max(xs.size(),ys.size()),zs.size());
//		if(size>0){
//			Vector3f[] l=new Vector3f[size];
//			for(int i=0;i<size;i++){
//				l[i]=new Vector3f(xs.valOf(i,0f),ys.valOf(i,0f),zs.valOf(i,0f));
//			}
//			return l;
//		}
//		return null;
//	}
//
//	private Quaternion[] cnvToQuaternionArray(List<Float> xs, List<Float> ys, List<Float> zs, List<Float> ws) {
//		int size=Math.max(Math.max(xs.size(),ys.size()),zs.size());
//		if(size>0){
//			Quaternion l[]=new Quaternion[size];
//			for(int i=0;i<size;i++){
//				l[i]=(new Quaternion(xs.valOf(i,0f),ys.valOf(i,0f),zs.valOf(i,0f),ws.valOf(i,0f)));
//			}
//			return l;
//		}
//		return null;
//	}

}
