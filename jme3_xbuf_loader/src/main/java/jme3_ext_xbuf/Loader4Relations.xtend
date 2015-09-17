package jme3_ext_xbuf

import com.jme3.animation.AnimControl
import com.jme3.animation.Animation
import com.jme3.animation.Skeleton
import com.jme3.animation.SkeletonControl_31
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Matrix4f
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.math.Vector4f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import java.util.HashMap
import java.util.Map
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.slf4j.Logger
import xbuf.Datas.Data
import xbuf.Relations.Relation
import xbuf_ext.CustomParams.CustomParam
import xbuf_ext.CustomParams.CustomParamList

import static jme3_ext_xbuf.Converters.*
import java.util.Comparator

@FinalFieldsConstructor
class Loader4Relations {
	val MaterialReplicator materialReplicator
	val Loader4Materials loader4Materials
	
	//TODO use dispatch or pattern matching of Xtend
	def void merge(Data src, Node root, Map<String, Object> components, Logger log) {
		src.relationsList.map[r|
			newRelationExpanded(r, src, components, log)
		].filter[it != null].sortWith(new KindComparator()).forEach[re |
			merge(re, src, root, components, log)
		]
	}
	
	@org.eclipse.xtend.lib.annotations.Data
	static class RelationExpanded {
		val String kind
		val String ref1
		val String ref2
		val Object op1
		val Object op2
	}
	
	static def newRelationExpanded(Relation r, Data src, Map<String, Object> components, Logger log) {
		val op1 = components.get(r.ref1)
		val op2 = components.get(r.ref2)
		
		if (op1 == null || op2 == null) {
			val t1str = if (op1 != null) op1.getClass.simpleName else "not found"
			val t2str = if (op2 != null) op2.getClass.simpleName else "not found"
			log.warn("can't link: missing entity,  {}({}) -- {}({})\n", r.getRef1(), t1str, r.getRef2(), t2str);
			return null
		}
		if (op1 == op2) {
			log.warn("can't link: op to itself (op1 == op2): {}({})", r.getRef1(), op1.getClass);
			return null
		}
		new RelationExpanded(
			typeString(op1.getClass) + "-" + typeString(op2.getClass)
			, r.ref1
			, r.ref2
			, op1
			, op2
		)
	}
	
	static def typeString(Class<?> clazz){
		switch(clazz) {
			case Animation: "Animation"
			case CustomParamList: "CustomParams" 	
			case XbufLightControl: "Light"	
			case Material: "Material"
			case Geometry: "Mesh"
			case Skeleton: "Skeleton"
			case Node: "TObject"
			default: "XXX"
		}
	}
	
	static class KindComparator implements Comparator<RelationExpanded> {
		
		override compare(RelationExpanded o1, RelationExpanded o2) {
			if (o1.kind == o2.kind) return 0
			if (o1.kind == "Material-Mesh") return 1
			if (o1.kind == "Mesh-TObject") return 1
			if (o1.kind == "Material-TObject") return 1
			if (o1.kind == "Animation-Mesh") return 1
			return -1
		}
		
		override equals(Object obj) {
			throw new UnsupportedOperationException("TODO: auto-generated method stub")
		}
		
	}

	def void merge(RelationExpanded re, Data src, Node root, Map<String, Object> components, Logger log) {
		val op1 = re.op1
		val op2 = re.op2
		var done = false
		if (op1 instanceof Animation) {
			if (op2 instanceof Spatial) { // Geometry, Node
				var c = op2.getControl(typeof(AnimControl))
				if (c == null) {
					val sc = op2.getControl(typeof(SkeletonControl_31))
					c = if (sc != null) new AnimControl(sc.getSkeleton) else new AnimControl()
					op2.addControl(c)
				}
				c.addAnim(op1)
				done = true
			}
		} else if (op1 instanceof CustomParamList) { // <--> xbuf_ext.Customparams.CustomParams
			if (op2 instanceof Spatial) { // Geometry, Node
				for(CustomParam p : op1.getParamsList()) {
					mergeToUserData(p, op2, log)
				}
				done = true
			}
		} else if (op1 instanceof XbufLightControl) { // <--> xbuf.Light
			if (op2 instanceof Geometry) {
				op1.getSpatial().removeControl(op1);
				op2.addControl(op1)
				// TODO raise an alert, strange to link LightNode and Geometry
				done = true
			} else if (op2 instanceof Node) {
				op1.getSpatial().removeControl(op1)
				op2.addControl(op1)
				done = true
			}
		} else if (op1 instanceof Material) { // <--> xbuf.Material
			if (op2 instanceof Geometry) {
				op2.setMaterial(op1)
				cloneMaterialOnGeometry(op2)
				done = true
			} else if (op2 instanceof Node) {
				op2.setMaterial(op1)
                cloneMaterialOnGeometry(op2)
				done = true
			}
		} else if (op1 instanceof Geometry) { // <--> xbuf.Mesh
			if (op2 instanceof Node) {
				//TODO replace clone by instanciation
				val mesh  = if (op1.parent != null) {
					println(">> double mesh :" + re.ref1 + " .. " + op1.name + " from " + re.ref2)
					val c = op1.clone(false)
					c.material = materialReplicator.newReplica(op1.material)
					c
				} else {
					op1
				}
				op2.attachChild(mesh)
				done = true
			} else if (op2 instanceof Skeleton) {
				link(op1, op2, findAnimLinkedToRef(src, re.ref2, components))
				done = true
			}
		} else if (op1 instanceof Skeleton) { // <--> xbuf.Skeleton
			if (op2 instanceof Node) {
				link(op2, op1, findAnimLinkedToRef(src, re.ref1, components))
				done = true
			}
		} else if (op1 instanceof Node) { // <--> xbuf.TObject
			if (op2 instanceof Node) {
				op1.attachChild(op2)
				done = true
			}
		}
		if (!done) {
			log.warn("can't link: doesn't know how to make relation {}({}) -- {}({})\n", re.ref1, op1.getClass(), re.ref2, op2.getClass());
		}
	}
	
		// see http://hub.jmonkeyengine.org/t/skeletoncontrol-or-animcontrol-to-host-skeleton/31478/4
	def link(Spatial v, Skeleton sk, Iterable<Animation> skAnims) {
		v.removeControl(typeof(SkeletonControl_31))
		//update AnimControl if related to skeleton
		val ac0 = v.getControl(typeof(AnimControl))
		val ac1 = if (ac0 != null/* && ac.getSkeleton() != null*/) {
			v.removeControl(ac0)
			val ac2 = new AnimControl(sk)
			val anims = new HashMap<String, Animation>()
			ac0.animationNames.forEach[name | anims.put(name, ac0.getAnim(name).clone())]
			ac2.animations = anims
			v.addControl(ac2)
			ac2
		} else {
			//always add AnimControl else NPE when SkeletonControl.clone
			val ac2 = new AnimControl(sk)
			v.addControl(ac2)
			ac2
		}
		// SkeletonControl should be after AnimControl in the list of Controls
		v.addControl(new SkeletonControl_31(sk))
		skAnims.forEach[ac1.addAnim(it)]
		cloneMaterialOnGeometry(v)
	}
	
	def findAnimLinkedToRef(Data src, String ref,  Map<String, Object> components) {
		src.getRelationsList().map[r|
			if (r.ref1 == ref && components.get(r.ref2) instanceof Animation) {
				components.get(r.ref2) as Animation
			} else if (r.ref2 == ref && components.get(r.ref1) instanceof Animation) {
				components.get(r.ref1) as Animation
			} else {
				null
			}
		].filter[it != null]
	}

	
    // TO avoid "java.lang.UnsupportedOperationException: Material instances cannot be shared when hardware skinning is used. Ensure all models use unique material instances."
    def void cloneMaterialOnGeometry(Spatial v) {
        if (v instanceof Geometry) {
        	if (!materialReplicator.isReplica(v.material)) {
            	v.material = materialReplicator.newReplica(v.material)
           	}
        } else if (v instanceof Node) {
            for (child : v.children) {
                cloneMaterialOnGeometry(child)
            }
        }        
    }
	
	def Spatial mergeToUserData(CustomParam p, Spatial dst, Logger log) {
		val name = p.getName()
		switch(p.getValueCase()) {
		case VALUE_NOT_SET:
			dst.setUserData(name, null)
		case VBOOL:
			dst.setUserData(name, p.vbool)
		case VCOLOR:
			dst.setUserData(name, cnv(p.vcolor, new ColorRGBA()))
		case VFLOAT:
			dst.setUserData(name, p.vfloat)
		case VINT:
			dst.setUserData(name, p.vint)
		case VMAT4:
			dst.setUserData(name, cnv(p.vmat4, new Matrix4f()))
		case VQUAT:
			dst.setUserData(name, cnv(p.vquat, new Vector4f()))
		case VSTRING:
			dst.setUserData(name, p.vstring)
		case VTEXTURE:
			dst.setUserData(name, loader4Materials.getValue(p.vtexture, log))
		case VVEC2:
			dst.setUserData(name, cnv(p.vvec2, new Vector2f()))
		case VVEC3:
			dst.setUserData(name, cnv(p.vvec3, new Vector3f()))
		case VVEC4:
			dst.setUserData(name, cnv(p.vvec4, new Vector4f()))
		default:
			log.warn("Material doesn't support parameter : {} of type {}", name, p.getValueCase().name())
		}
		return dst;
	}
	
}