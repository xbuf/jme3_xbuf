package jme3_ext_xbuf.mergers;

import java.lang.reflect.Array;
import java.util.List;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.animations.XbufAnimation;
import jme3_ext_xbuf.animations.XbufTrack;
import xbuf.Datas.Data;
import xbuf_ext.AnimationsKf;
import xbuf_ext.AnimationsKf.SampledTransform;

@SuppressWarnings("unchecked")

public class AnimationsMerger implements Merger{

	@Override
	public void apply(Data src, Node root, XbufContext context) {
		for(AnimationsKf.AnimationKF e:src.getAnimationsKfList()){
			java.lang.String id=e.getId();
			// TODO: merge with existing
			XbufAnimation a=new XbufAnimation(e.getName(),((float)e.getDuration())/1000f);
			for(AnimationsKf.Clip clip:e.getClipsList()){
				if(clip.hasSampledTransform()){					
					XbufTrack t=makeTrack(clip.getSampledTransform().hasBoneName(),clip.getSampledTransform());							
					a.getTracks().add(t);
				}
			}
			context.put(id,a);
		}
	}

	private XbufTrack makeTrack(boolean bone,SampledTransform bt) {
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
				
		return new XbufTrack(bone?bt.getBoneName():XbufTrack._SPATIAL,times,translations,rotations,scales);

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

	



}
