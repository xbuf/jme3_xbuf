package jme3_ext_xbuf;

import com.google.protobuf.ExtensionRegistry
import com.jme3.animation.Animation
import com.jme3.animation.Bone
import com.jme3.animation.Skeleton
import com.jme3.animation.SpatialTrack
import com.jme3.asset.AssetManager
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.light.Light
import com.jme3.light.PointLight
import com.jme3.light.SpotLight
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.VertexBuffer
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.TangentBinormalGenerator_31
import java.util.HashMap
import java.util.List
import java.util.Map
import jme3_ext_animation.NamedBoneTrack
import org.slf4j.Logger
import xbuf.Datas.Data
import xbuf.Lights
import xbuf.Meshes
import xbuf.Meshes.IndexArray
import xbuf.Meshes.Skin
import xbuf.Meshes.VertexArray
import xbuf.Primitives
import xbuf.Relations.Relation
import xbuf.Skeletons
import xbuf.Tobjects.TObject
import xbuf_ext.AnimationsKf
import xbuf_ext.AnimationsKf.AnimationKF
import xbuf_ext.AnimationsKf.SampledTransform
import xbuf_ext.CustomParams

import static jme3_ext_xbuf.Converters.*

// TODO use a Validation object (like in scala/scalaz) with option to log/dump stacktrace
public class Xbuf {
	public val AssetManager assetManager
	public val Loader4Materials loader4Materials
	public val Loader4Relations loader4Relations
	public val ExtensionRegistry registry

    /**
     * A full constructor that allow to define every service (to injection).
     * @param assetManager the AssetManager used to load assets (texture, sound,...)
     * @param registry the protobuf registry for extensions
     * @param loader4Relations the xbuf way to load relations (null => default implementation)
     * @param loader4Relations the xbuf way to load relations (null => default implementation)
     */
	new(AssetManager assetManager, ExtensionRegistry registry, Loader4Materials loader4Materials, Loader4Relations loader4Relations) {
		this.assetManager = assetManager
		this.loader4Materials = loader4Materials ?: new Loader4Materials(assetManager, null)
        this.loader4Relations = loader4Relations ?: new Loader4Relations(this.loader4Materials.materialReplicator, this.loader4Materials)
        this.registry = registry ?: ExtensionRegistry.newInstance()
        setupExtensionRegistry(this.registry)
	}

	new(AssetManager assetManager) {
		this(assetManager, null, null, null)
	}

	protected def ExtensionRegistry setupExtensionRegistry(ExtensionRegistry r) {
		CustomParams.registerAllExtensions(r)
		AnimationsKf.registerAllExtensions(r)
		r
	}

	def Mesh cnv(Meshes.Mesh src, Mesh dst, Logger log) {
		if (src.getIndexArraysCount() > 1) {
			throw new IllegalArgumentException("doesn't support more than 1 index array")
		}
		if (src.getLod() > 1) {
			throw new IllegalArgumentException("doesn't support lod > 1 : "+ src.getLod())
		}

		dst.setMode(cnv(src.getPrimitive()));
		for(VertexArray va : src.getVertexArraysList()) {
			val type = cnv(va.getAttrib())
			dst.setBuffer(type, va.getFloats().getStep(), hack_cnv(va.getFloats()))
			log.debug("add {}", dst.getBuffer(type))
		}
		for(IndexArray va : src.getIndexArraysList()) {
			dst.setBuffer(VertexBuffer.Type.Index, va.getInts().getStep(), hack_cnv(va.getInts()))
		}
		if (src.hasSkin) {
			applySkin(src.skin, dst, log)
		}
//		// basic check
//		val nbVertices = dst.getBuffer(VertexBuffer.Type.Position).getNumElements()
//		for(IntMap.Entry<VertexBuffer> evb : dst.getBuffers()) {
//			if (evb.getKey() != VertexBuffer.Type.Index.ordinal()) {
//				if (nbVertices != evb.getValue().getNumElements()) {
//					log.warn("size of vertex buffer {} is not equals to vertex buffer for position: {} != {}", VertexBuffer.Type.values().get(evb.getKey()), evb.getValue().getNumElements(), nbVertices)
//				}
//			}
//		}
		//TODO optimize lazy create Tangent when needed (for normal map ?)
		if ((dst.getBuffer(VertexBuffer.Type.Tangent) == null || dst.getBuffer(VertexBuffer.Type.Binormal) == null) 
			&& dst.getBuffer(VertexBuffer.Type.Normal) != null && dst.getBuffer(VertexBuffer.Type.TexCoord) != null
		) {
			TangentBinormalGenerator_31.setToleranceAngle(179) //remove warnings
			TangentBinormalGenerator_31.generate(dst)
		}

		dst.updateCounts()
		dst.updateBound()
		dst
	}

	def Mesh applySkin(Skin skin, Mesh dst, Logger log) {
		dst.clearBuffer(Type.BoneIndex)
		dst.clearBuffer(Type.BoneWeight)
		val nb = skin.boneCountCount
		//val maxWeightPerVert = Math.min(4, skin.boneCountList.reduce[p1, p2|Math.max(p1,p2)])
		val maxWeightPerVert = 4// jME 3.0 only support fixed 4 weights per vertex
		val indexPad = newByteArrayOfSize(nb * maxWeightPerVert)
		val weightPad = newFloatArrayOfSize(nb * maxWeightPerVert)
		var isrc = 0
		for(var i = 0; i < nb; i++) {
			var totalWeightPad = 0f
			val cnt = skin.boneCountList.get(i)
			val k0 = i * maxWeightPerVert
			for(var j = 0;  j < maxWeightPerVert; j++) {
				val k = k0 + j
				var index = 0 as byte
				var weight = 0f
				if (j < cnt) {
					weight = skin.boneWeightList.get(isrc + j)
					index = skin.boneIndexList.get(isrc + j).byteValue
				}
				totalWeightPad += weight
				indexPad.set(k, index)
				weightPad.set(k, weight)
			}
			if (totalWeightPad > 0) {
				var totalWeight = 0.0f
				for(var j = 0;  j < cnt; j++) {
					totalWeight += skin.boneWeightList.get(isrc + j)
				}
				
				val normalizer = totalWeight / totalWeightPad
                val wpv = Math.min(maxWeightPerVert, cnt)
				for(var j = 0;  j < wpv; j++) {
					val k = k0 + j
					weightPad.set(k, weightPad.get(k) * normalizer)
				}
				if (cnt > maxWeightPerVert && totalWeight != totalWeightPad) {
					log.warn("vertex influenced by more than {} bones : {}, only the {} higher are keep for total weight keep/orig: {}/{}.", maxWeightPerVert, cnt, wpv, totalWeightPad, totalWeight)
				}
			}
			isrc += cnt
		}
		dst.setBuffer(Type.BoneIndex, maxWeightPerVert, indexPad)
		dst.setBuffer(Type.BoneWeight, maxWeightPerVert, weightPad)
		dst.setMaxNumWeights(maxWeightPerVert)

		//creating empty buffers for HW skinning
		//the buffers will be setup if ever used.
		val weightsHW = new VertexBuffer(Type.HWBoneWeight);
		val indicesHW = new VertexBuffer(Type.HWBoneIndex);
		//setting usage to cpuOnly so that the buffer is not send empty to the GPU
		indicesHW.setUsage(VertexBuffer.Usage.CpuOnly)
		weightsHW.setUsage(VertexBuffer.Usage.CpuOnly)
		dst.setBuffer(weightsHW)
		dst.setBuffer(indicesHW)
		dst.generateBindPose(true)
		dst
	}

	//TODO support multi mesh, may replace geometry by node and use one Geometry per mesh
	def Geometry cnv(Meshes.Mesh src, Geometry dst, Logger log) {
		dst.setName(if (src.hasName()) { src.getName() } else { src.getId()})
		val mesh = cnv(src, new Mesh(), log)
		dst.setMesh(mesh)
		dst.updateModelBound()
		dst
	}

	//TODO optimize to create less intermediate node
	def merge(Data src, Node root, Map<String, Object> components, Logger log) {
		mergeTObjects(src, root, components, log)
		mergeMeshes(src, root, components, log)
		loader4Materials.mergeMaterials(src, components, log)
		mergeLights(src, root, components, log)
		mergeSkeletons(src, root, components, log)
		mergeCustomParams(src, components, log)
		mergeAnimations(src, components, log)
		// relations should be the last because it reuse data provide by other (put in components)
		loader4Relations.merge(src, root, components, log)
	}

	def mergeAnimations(Data src, Map<String, Object> components, Logger log) {
		for(AnimationsKf.AnimationKF e : src.animationsKfList) {
			val id = e.getId()
			//TODO: merge with existing
			val child = makeAnimation(e, log)
			components.put(id, child)
		}
	}

	def Animation makeAnimation(AnimationKF e, Logger log) {
		val a =  new Animation(e.getName(), (e.getDuration() as float) / 1000f)
		for(AnimationsKf.Clip clip: e.getClipsList()) {
			if (clip.hasSampledTransform()) {
				val t = if (clip.sampledTransform.hasBoneName()) {
					makeTrackBone(clip.sampledTransform)
				} else {
					makeTrackSpatial(clip.sampledTransform)
				}
				a.addTrack(t)
			}
		}
		a
	}

	static def valOf(List<Float> values, int i, float vdef) {
		if (values.size > i) values.get(i) else vdef
	}

	def cnvToVector3fArray(List<Float> xs, List<Float> ys, List<Float> zs) {
		val size = Math.max(Math.max(xs.size, ys.size), zs.size)
		if (size > 0) {
			val l = <Vector3f>newArrayOfSize(size)
			for(var i = 0; i < size; i++) {
				l.set(i, new Vector3f(xs.valOf(i, 0f), ys.valOf(i, 0f), zs.valOf(i, 0f)))
			}
			l
		} else null
	}

	def cnvToQuaternionArray(List<Float> xs, List<Float> ys, List<Float> zs, List<Float> ws) {
		val size = Math.max(Math.max(xs.size, ys.size), zs.size)
		if (size > 0) {
			val l = <Quaternion>newArrayOfSize(size)
			for(var i = 0; i < size; i++) {
				l.set(i, new Quaternion(xs.valOf(i, 0f), ys.valOf(i, 0f), zs.valOf(i, 0f), ws.valOf(i, 0f)))
			}
			l
		} else null
	}

	def makeTrackBone(SampledTransform bt) {
		val times = newFloatArrayOfSize(bt.atCount)
		bt.atList.forEach[v, i| times.set(i, (v as float)/ 1000f)]
		val translations = cnvToVector3fArray(bt.translationXList, bt.translationYList, bt.translationZList)
		val rotations = cnvToQuaternionArray(bt.rotationXList, bt.rotationYList, bt.rotationZList, bt.rotationWList)
		val scales = cnvToVector3fArray(bt.scaleXList, bt.scaleYList, bt.scaleZList)
		new NamedBoneTrack(bt.boneName, times, translations, rotations, scales)
	}

	def makeTrackSpatial(SampledTransform bt) {
		val times = newFloatArrayOfSize(bt.atCount)
		bt.atList.forEach[v, i| times.set(i, (v as float)/ 1000f)]
		val translations = cnvToVector3fArray(bt.translationXList, bt.translationYList, bt.translationZList)
		val rotations = cnvToQuaternionArray(bt.rotationXList, bt.rotationYList, bt.rotationZList, bt.rotationWList)
		val scales = cnvToVector3fArray(bt.scaleXList, bt.scaleYList, bt.scaleZList)
		new SpatialTrack(times, translations, rotations, scales)
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

	def void mergeSkeletons(Data src, Node root, Map<String, Object> components, Logger log) {
		for(e : src.skeletonsList) {
			//TODO manage parent hierarchy
			val id = e.getId();
			//TODO: merge with existing
			val child = makeSkeleton(e, log);
			components.put(id, child);
			//Skeleton child = (Skeleton)components.get(id);
		}
	}

	def Skeleton makeSkeleton(Skeletons.Skeleton e, Logger log) {
		val bones = <Bone>newArrayOfSize(e.bonesCount)
		val db = new HashMap<String, Bone>()
		for(var i = 0; i < bones.length; i++) {
			val src = e.getBones(i)
			val b = new Bone(src.getName())
			b.setBindTransforms(cnv(src.getTransform().getTranslation(), new Vector3f())
				, cnv(src.getTransform().getRotation(), new Quaternion())
				, cnv(src.getTransform().getScale(), new Vector3f())
			)
			db.put(src.getId(), b)
			bones.set(i, b)
		}
		for(Relation r : e.getBonesGraphList()) {
			val parent = db.get(r.getRef1())
			val child = db.get(r.getRef2())
			parent.addChild(child)
		}
		val sk = new Skeleton(bones)
		sk.setBindingPose()
		sk
	}

	def mergeCustomParams(Data src, Map<String, Object> components, Logger log) {
		for(CustomParams.CustomParamList srccp : src.customParamsList) {
			//TODO merge with existing
			components.put(srccp.getId(), srccp)
		}
	}

	def mergeLights(Data src, Node root, Map<String, Object> components, Logger log) {
		for(srcl : src.lightsList) {
			//TODO manage parent hierarchy
			val id = srcl.getId()
			var dst = components.get(id) as XbufLightControl
			if (dst == null) {
				dst = new XbufLightControl()
				components.put(id, dst)
				root.addControl(dst)
			}
			if (dst.light != null) {
				root.removeLight(dst.light)
			}
			dst.light = makeLight(srcl, log)
			root.addLight(dst.light)

			if (srcl.hasColor()) {
				dst.light.setColor(cnv(srcl.getColor(), new ColorRGBA()).mult(srcl.getIntensity()))
			}
			//TODO manage attenuation
			//TODO manage conversion of type
			switch(srcl.getKind()) {
			case spot: {
				val l = dst.light as SpotLight
				if (srcl.hasSpotAngle()) {
					val max = srcl.getSpotAngle().getMax()
					switch(srcl.getSpotAngle().getCurveCase()) {
						case CURVE_NOT_SET: {
							l.setSpotOuterAngle(max)
							l.setSpotInnerAngle(max)
						}
						case LINEAR: {
							l.setSpotOuterAngle(max * srcl.getSpotAngle().getLinear().getEnd())
							l.setSpotInnerAngle(max * srcl.getSpotAngle().getLinear().getBegin())
						}
						default: {
							l.setSpotOuterAngle(max)
							l.setSpotInnerAngle(max)
							log.warn("doesn't support curve like {} for spot_angle", srcl.getSpotAngle().getCurveCase())
						}
					}

				}
				if (srcl.hasRadialDistance()) {
					l.setSpotRange(srcl.getRadialDistance().getMax());
				}
			}
			case point: {
				val l = dst.light as PointLight
				if (srcl.hasRadialDistance()) {
					val max = srcl.getRadialDistance().getMax();
					switch(srcl.getRadialDistance().getCurveCase()) {
					case CURVE_NOT_SET: {
						l.setRadius(max);
					}
					case LINEAR: {
						l.setRadius(max * srcl.getSpotAngle().getLinear().getEnd());
					}
					case SMOOTH: {
						l.setRadius(max * srcl.getSpotAngle().getSmooth().getEnd());
					}
					default: {
						l.setRadius(max);
						log.warn("doesn't support curve like {} for spot_angle", srcl.getSpotAngle().getCurveCase());
					}
					}
				}
			}
			case ambient: {}
			case directional: {}
			}
		}
	}

	def Light makeLight(Lights.Light srcl, Logger log) {
		var l0 = null as Light
		switch(srcl.getKind()) {
			case ambient:
				l0 = new AmbientLight()
			case directional:
				l0 = new DirectionalLight()
			case spot: {
				val l = new SpotLight()
				l.setSpotRange(1000)
				l.setSpotInnerAngle(5f * FastMath.DEG_TO_RAD)
				l.setSpotOuterAngle(10f * FastMath.DEG_TO_RAD)
				l0 = l
			}
			case point:
				l0 = new PointLight()
		}
		l0.setColor(ColorRGBA.White.mult(2))
		l0.setName(if (srcl.hasName()) srcl.getName() else srcl.getId())
		l0
	}

	def mergeTObjects(Data src, Node root, Map<String, Object> components, Logger log) {
		for(TObject n : src.getTobjectsList()) {
			val id = n.getId()
			var child = components.get(id) as Spatial
			if (child == null) {
				child = new Node("")
				root.attachChild(child)
				components.put(id, child)
			}
			child.setName(if (n.hasName()) n.getName() else n.getId())
			merge(n.getTransform(), child, log)
		}
	}

	def mergeMeshes(Data src, Node root, Map<String, Object> components, Logger log) {
		for(g : src.meshesList) {
			//TODO manage parent hierarchy
			val id = g.getId()
			var child = components.get(id) as Geometry
			if (child == null) {
				child = new Geometry()
				//child.setMaterial(materialReplicator.newReplica(defaultMaterial))
				child.setMaterial(loader4Materials.newDefaultMaterial())
				root.attachChild(child)
				//log.debug("add Geometry for xbuf.Mesh.id: {}", id)
				components.put(id, child)
			}
			child = cnv(g, child, log)
		}
	}

	def void merge(Primitives.Transform src, Spatial dst, Logger log) {
		dst.setLocalRotation(cnv(src.getRotation(), dst.getLocalRotation()))
		dst.setLocalTranslation(cnv(src.getTranslation(), dst.getLocalTranslation()))
		dst.setLocalScale(cnv(src.getScale(), dst.getLocalScale()))
	}

	static def String join(String sep, String[] values) {
		val s = new StringBuilder()
		for(v : values) {
			if (s.length != 0) s.append(sep)
			s.append(v)
		}
		s.toString()
	}
}

