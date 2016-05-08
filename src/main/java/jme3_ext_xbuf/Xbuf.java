package jme3_ext_xbuf;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;

import com.google.protobuf.ExtensionRegistry;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SpatialTrack;
import com.jme3.animation.Track;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import jme3_ext_animation.NamedBoneTrack;
import jme3_ext_xbuf.relations.Loader4Relations;
import lombok.experimental.ExtensionMethod;
import xbuf.Datas.Data;
import xbuf.Lights;
import xbuf.Primitives;
import xbuf.Relations.Relation;
import xbuf.Skeletons;
import xbuf.Tobjects.TObject;
import xbuf_ext.AnimationsKf;
import xbuf_ext.AnimationsKf.AnimationKF;
import xbuf_ext.AnimationsKf.SampledTransform;
import xbuf_ext.CustomParams;

@ExtensionMethod({jme3_ext_xbuf.ext.ListExt.class,jme3_ext_xbuf.ext.XbufMeshExt.class})
// TODO use a Validation object (like in scala/scalaz) with option to log/dump stacktrace
public class Xbuf{
	protected final AssetManager assetManager;
	protected final Loader4Materials loader4Materials;
	protected final Loader4Relations loader4Relations;
	protected final ExtensionRegistry registry;

	/**
	 * A full constructor that allow to define every service (to injection).
	 * @param assetManager the AssetManager used to load assets (texture, sound,...)
	 * @param registry the protobuf registry for extensions
	 * @param loader4Relations the xbuf way to load relations (null => default implementation)
	 * @param loader4Relations the xbuf way to load relations (null => default implementation)
	 */
	public Xbuf(AssetManager assetManager,ExtensionRegistry registry,Loader4Materials loader4Materials,Loader4Relations loader4Relations){
		this.assetManager=assetManager;
		this.loader4Materials=loader4Materials!=null?loader4Materials:new Loader4Materials(assetManager,null);
		this.loader4Relations=loader4Relations!=null?loader4Relations:new Loader4Relations(this.loader4Materials);
		this.registry=registry!=null?registry:ExtensionRegistry.newInstance();
		setupExtensionRegistry(this.registry);
	}

	public Xbuf(AssetManager assetManager){
		this(assetManager,null,null,null);
	}

	protected ExtensionRegistry setupExtensionRegistry(ExtensionRegistry r) {
		CustomParams.registerAllExtensions(r);
		AnimationsKf.registerAllExtensions(r);
		return r;
	}


	// TODO optimize to create less intermediate node
	public void merge(Data src, Node root, XbufContext context, Logger log) {
		mergeTObjects(src,root,context,log);
		mergeMeshes(src,root,context,log);
		loader4Materials.mergeMaterials(src,context,log);
		mergeLights(src,root,context,log);
		mergeSkeletons(src,root,context,log);
		mergeCustomParams(src,context,log);
		mergeAnimations(src,context,log);
		// relations should be the last because it reuse data provide by other (put in components)
		loader4Relations.merge(src,root,context,log);
	}

	public void mergeAnimations(Data src, XbufContext components, Logger log) {
		for(AnimationsKf.AnimationKF e:src.getAnimationsKfList()){
			java.lang.String id=e.getId();
			// TODO: merge with existing
			Animation child=makeAnimation(e,log);
			components.put(id,child);
		}
	}

	public Animation makeAnimation(AnimationKF e, Logger log) {
		Animation a=new Animation(e.getName(),((float)e.getDuration())/1000f);
		for(AnimationsKf.Clip clip:e.getClipsList()){
			if(clip.hasSampledTransform()){
				Track t=clip.getSampledTransform().hasBoneName()?makeTrackBone(clip.getSampledTransform()):makeTrackSpatial(clip.getSampledTransform());
				a.addTrack(t);
			}
		}
		return a;
	}

	public Vector3f[] cnvToVector3fArray(List<Float> xs, List<Float> ys, List<Float> zs) {
		int size=Math.max(Math.max(xs.size(),ys.size()),zs.size());
		if(size>0){
			Vector3f[] l=new Vector3f[size];
			for(int i=0;i<size;i++){
				l[i]=new Vector3f(xs.valOf(i,0f),ys.valOf(i,0f),zs.valOf(i,0f));
			}
			return l;
		}
		return null;
	}

	public Quaternion[] cnvToQuaternionArray(List<Float> xs, List<Float> ys, List<Float> zs, List<Float> ws) {
		int size=Math.max(Math.max(xs.size(),ys.size()),zs.size());
		if(size>0){
			Quaternion l[]=new Quaternion[size];
			for(int i=0;i<size;i++){
				l[i]=(new Quaternion(xs.valOf(i,0f),ys.valOf(i,0f),zs.valOf(i,0f),ws.valOf(i,0f)));
			}
			return l;
		}
		return null;
	}

	public NamedBoneTrack makeTrackBone(SampledTransform bt) {
		float times[]=new float[bt.getAtCount()];
		List<Integer> at=bt.getAtList();
		int i=0;
		for(Integer v:at)
			times[i++]=((float)v)/1000f;
		Vector3f[] translations=cnvToVector3fArray(bt.getTranslationXList(),bt.getTranslationYList(),bt.getTranslationZList());
		Quaternion[] rotations=cnvToQuaternionArray(bt.getRotationXList(),bt.getRotationXList(),bt.getRotationZList(),bt.getRotationWList());
		Vector3f[] scales=cnvToVector3fArray(bt.getScaleXList(),bt.getScaleYList(),bt.getScaleZList());
		return new NamedBoneTrack(bt.getBoneName(),times,translations,rotations,scales);
	}

	public SpatialTrack makeTrackSpatial(SampledTransform bt) {
		float[] times=new float[bt.getAtCount()];
		List<Integer> at=bt.getAtList();
		int i=0;
		for(Integer v:at)
			times[i++]=((float)v)/1000f;
		Vector3f[] translations=cnvToVector3fArray(bt.getTranslationXList(),bt.getTranslationYList(),bt.getTranslationZList());
		Quaternion[] rotations=cnvToQuaternionArray(bt.getRotationXList(),bt.getRotationXList(),bt.getRotationZList(),bt.getRotationWList());
		Vector3f[] scales=cnvToVector3fArray(bt.getScaleXList(),bt.getScaleYList(),bt.getScaleZList());
		return new SpatialTrack(times,translations,rotations,scales);
	}

	//	def Animation makeAnimation(AnimationKF e, Logger log) {
	//		val a =  new Animation(e.getName(), (e.getDuration() as float) / 1000f)
	//		for(AnimationsKf.Clip clip: e.getClipsList()) {
	//			System.out.println("add clip : " + clip.hasTransforms())
	//			if (clip.hasTransforms()) {
	//				val t = if (clip.getTransforms().hasBoneName) {
	//					makeTrackBone(clip.getTransforms())
	//				} else {
	//					makeTrackSpatial(clip.getTransforms())
	//				}
	//				a.addTrack(t)
	//			}
	//		}
	//		a
	//	}
	//	def Track makeTrackSpatial(TransformKF transforms) {
	//		val track = new CompositeTrack()
	//		val vkf = transforms.getTranslation()
	//		if (vkf.hasX) {
	//			track.tracks.add(TrackFactory.translationX(cnv(vkf.x, new FloatKeyPoints())))
	//		}
	//		if (vkf.hasY) {
	//			track.tracks.add(TrackFactory.translationY(cnv(vkf.y, new FloatKeyPoints())))
	//		}
	//		if (vkf.hasZ) {
	//			track.tracks.add(TrackFactory.translationZ(cnv(vkf.z, new FloatKeyPoints())))
	//		}
	//
	//		val vkf2 = transforms.getScale()
	//		if (vkf2.hasX) {
	//			track.tracks.add(TrackFactory.scaleX(cnv(vkf2.x, new FloatKeyPoints())))
	//		}
	//		if (vkf2.hasY) {
	//			track.tracks.add(TrackFactory.scaleY(cnv(vkf2.y, new FloatKeyPoints())))
	//		}
	//		if (vkf2.hasZ) {
	//			track.tracks.add(TrackFactory.scaleZ(cnv(vkf2.z, new FloatKeyPoints())))
	//		}
	//
	//		track
	//	}
	//
	//	def Track makeTrackBone(TransformKF transforms) {
	//		val track = new CompositeTrack()
	//		val vkf = transforms.getTranslation()
	//		val boneName = transforms.boneName
	//		if (vkf.hasX) {
	//			track.tracks.add(TrackFactory.translationX(cnv(vkf.x, new FloatKeyPoints()), boneName))
	//		}
	//		if (vkf.hasY) {
	//			track.tracks.add(TrackFactory.translationY(cnv(vkf.y, new FloatKeyPoints()), boneName))
	//		}
	//		if (vkf.hasZ) {
	//			track.tracks.add(TrackFactory.translationZ(cnv(vkf.z, new FloatKeyPoints()), boneName))
	//		}
	//
	//		val vkf2 = transforms.getScale()
	//		if (vkf2.hasX) {
	//			track.tracks.add(TrackFactory.scaleX(cnv(vkf2.x, new FloatKeyPoints()), boneName))
	//		}
	//		if (vkf2.hasY) {
	//			track.tracks.add(TrackFactory.scaleY(cnv(vkf2.y, new FloatKeyPoints()), boneName))
	//		}
	//		if (vkf2.hasZ) {
	//			track.tracks.add(TrackFactory.scaleZ(cnv(vkf2.z, new FloatKeyPoints()), boneName))
	//		}
	//
	//		track
	//	}
	//
	//	def FloatKeyPoints cnv(KeyPoints src, FloatKeyPoints dst) {
	//		if (src.atCount != src.valueCount) {
	//			throw new IllegalStateException(String.format("at.size %d != value.size %d", src.atCount, src.valueCount))
	//		}
	//		val times = newFloatArrayOfSize(src.atCount)
	//		val values = newFloatArrayOfSize(src.atCount)
	//		for(var i = 0; i < times.length; i++) {
	//			times.set(i, (src.atList.get(i) as float) / 1000f)
	//			values.set(i, src.valueList.get(i))
	//		}
	//		dst.setKeyPoints(times, values)
	//		if (src.interpolationCount  == src.atCount) {
	//			val eases = <Interpolation>newArrayOfSize(src.interpolationCount)
	//			for(var i = 0; i < eases.length; i++) {
	//				eases.set(i, cnv(i, src.interpolationList, src.bezierParamsList))
	//			}
	//			dst.setEases(eases, Interpolations.linear)
	//		} else {
	//			dst.setEases(null, Interpolations.linear)
	//		}
	//		dst
	//	}
	//
	//	def Interpolation cnv(int i, List<InterpolationFct> fcts, List<BezierParams> bps) {
	//		switch(fcts.get(i)) {
	//			case linear: Interpolations.linear
	//			case constant: Interpolations.constant
	//			case bezier: {
	//				val bp = bps.get(i)
	//				Interpolations.cubicBezier(bp.h0X, bp.h0Y, bp.h1X, bp.h1Y)
	//			}
	//		}
	//	}
	public void mergeSkeletons(Data src, Node root, XbufContext components, Logger log) {
		for(xbuf.Skeletons.Skeleton e:src.getSkeletonsList()){
			// TODO manage parent hierarchy
			String id=e.getId();
			// TODO: merge with existing
			Skeleton child=makeSkeleton(e,log);
			components.put(id,child);
			// Skeleton child = (Skeleton)components.get(id);
		}
	}

	public Skeleton makeSkeleton(Skeletons.Skeleton e, Logger log) {
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

	public void mergeCustomParams(Data src,XbufContext components, Logger log) {
		for(CustomParams.CustomParamList srccp:src.getCustomParamsList()){
			// TODO merge with existing
			components.put(srccp.getId(),srccp);
		}
	}

	public void mergeLights(Data src, Node root, XbufContext components, Logger log) {
		for(xbuf.Lights.Light srcl:src.getLightsList()){
			// TODO manage parent hierarchy
			String id=srcl.getId();
			XbufLightControl dst=(XbufLightControl)components.get(id);
			if(dst==null){
				dst=new XbufLightControl();
				components.put(id,dst);
				root.addControl(dst);
			}
			if(dst.light!=null){
				root.removeLight(dst.light);
			}
			dst.light=makeLight(srcl,log);
			root.addLight(dst.light);

			if(srcl.hasColor()){
				dst.light.setColor(Converters.cnv(srcl.getColor(),new ColorRGBA()).mult(srcl.getIntensity()));
			}
			// TODO manage attenuation
			// TODO manage conversion of type
			switch(srcl.getKind()){
				case spot:{
					SpotLight l=(SpotLight)dst.getLight();
					if(srcl.hasSpotAngle()){
						float max=srcl.getSpotAngle().getMax();
						switch(srcl.getSpotAngle().getCurveCase()){
							case CURVE_NOT_SET:{
								l.setSpotOuterAngle(max);
								l.setSpotInnerAngle(max);
							}
							case LINEAR:{
								l.setSpotOuterAngle(max*srcl.getSpotAngle().getLinear().getEnd());
								l.setSpotInnerAngle(max*srcl.getSpotAngle().getLinear().getBegin());
							}
							default:{
								l.setSpotOuterAngle(max);
								l.setSpotInnerAngle(max);
								log.warn("doesn't support curve like {} for spot_angle",srcl.getSpotAngle().getCurveCase());
							}
						}

					}
					if(srcl.hasRadialDistance()){
						l.setSpotRange(srcl.getRadialDistance().getMax());
					}
				}
				case point:{
					PointLight l=(PointLight)dst.getLight();
					if(srcl.hasRadialDistance()){
						float max=srcl.getRadialDistance().getMax();
						switch(srcl.getRadialDistance().getCurveCase()){
							case CURVE_NOT_SET:{
								l.setRadius(max);
							}
							case LINEAR:{
								l.setRadius(max*srcl.getSpotAngle().getLinear().getEnd());
							}
							case SMOOTH:{
								l.setRadius(max*srcl.getSpotAngle().getSmooth().getEnd());
							}
							default:{
								l.setRadius(max);
								log.warn("doesn't support curve like {} for spot_angle",srcl.getSpotAngle().getCurveCase());
							}
						}
					}
				}
				case ambient:{}
				case directional:{}
			}
		}
	}

	public Light makeLight(Lights.Light srcl, Logger log) {
		Light l0=null;
		switch(srcl.getKind()){
			case ambient:
				l0=new AmbientLight();
			case directional:
				l0=new DirectionalLight();
			case spot:{
				SpotLight l=new SpotLight();
				l.setSpotRange(1000);
				l.setSpotInnerAngle(5f*FastMath.DEG_TO_RAD);
				l.setSpotOuterAngle(10f*FastMath.DEG_TO_RAD);
				l0=l;
			}
			case point:
				l0=new PointLight();
		}
		l0.setColor(ColorRGBA.White.mult(2));
		l0.setName(srcl.hasName()?srcl.getName():srcl.getId());
		return l0;
	}

	public void mergeTObjects(Data src, Node root, XbufContext components, Logger log) {
		for(TObject n:src.getTobjectsList()){
			String id=n.getId();
			Spatial child=(Spatial)components.get(id);
			if(child==null){
				child=new Node("");
				root.attachChild(child);
				components.put(id,child);
			}
			child.setName(n.hasName()?n.getName():n.getId());
			merge(n.getTransform(),child,log);
		}
	}

	public void mergeMeshes(Data src, Node root,XbufContext context, Logger log) {
		for(xbuf.Meshes.Mesh g:src.getMeshesList())	context.put(g.getId(),g.toJME(context,log));		
	}

	public void merge(Primitives.Transform src, Spatial dst, Logger log) {
		dst.setLocalRotation(Converters.cnv(src.getRotation(),dst.getLocalRotation()));
		dst.setLocalTranslation(Converters.cnv(src.getTranslation(),dst.getLocalTranslation()));
		dst.setLocalScale(Converters.cnv(src.getScale(),dst.getLocalScale()));
	}

}
