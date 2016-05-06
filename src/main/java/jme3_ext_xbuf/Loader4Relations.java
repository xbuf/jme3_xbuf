package jme3_ext_xbuf;

import static jme3_ext_xbuf.Converters.cnv;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl_31;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xbuf.Datas.Data;
import xbuf.Relations.Relation;
import xbuf.Tobjects.TObject;
import xbuf_ext.CustomParams.CustomParam;
import xbuf_ext.CustomParams.CustomParamList;

@RequiredArgsConstructor
public class Loader4Relations{
	public final MaterialReplicator materialReplicator;
	public final Loader4Materials loader4Materials;

	// TODO use dispatch or pattern matching of Xtend

	void merge(Data src, Node root, Map<String,Object> components, Logger log) {
		src.getRelationsList().stream().map(r -> newRelationExpanded(r,src,components,log))
		.filter(i -> i!=null).sorted(new KindComparator()).forEach(
				re->merge(re, src, root, components, log)
		);
	}

	@lombok.Data
	static class RelationExpanded{
		@NonNull String kind;
		@NonNull String ref1;
		@NonNull String ref2;
		@NonNull Object op1;
		@NonNull Object op2;
	}

	public static RelationExpanded newRelationExpanded(Relation r, Data src, Map<String,Object> components, Logger log) {
		System.out.println(r.getRef1()+"-"+r.getRef2());
		
		Object op1=components.get(r.getRef1());
		Object op2=components.get(r.getRef2());

		System.out.println(typeString(op1.getClass())+"-"+typeString(op2.getClass()));
		if(op1==null||op2==null){
			String t1str=op1!=null?op1.getClass().getSimpleName():"not found";
			String t2str=op2!=null?op2.getClass().getSimpleName():"not found";
			log.warn("can't link: missing entity,  {}({}) -- {}({})\n",new Object[]{r.getRef1(),t1str,r.getRef2(),t2str});
			return null;
		}
		if(op1==op2){
			log.warn("can't link: op to itself (op1 == op2): {}({})",new Object[]{r.getRef1(),op1.getClass()});
			return null;
		}
		return new RelationExpanded(typeString(op1.getClass())+"-"+typeString(op2.getClass()),r.getRef1(),r.getRef2(),op1,op2);
	}

	static String typeString(Class<?> clazz) { 
		if(clazz.isAssignableFrom(Animation.class))return "Animation";
		if(clazz.isAssignableFrom(CustomParamList.class))return "CustomParams";
		if(clazz.isAssignableFrom(XbufLightControl.class))return "Light";
		if(clazz.isAssignableFrom(Material.class))return "Material";
		if(clazz.isAssignableFrom(Geometry.class))return "Mesh";
		if(clazz.isAssignableFrom(Skeleton.class))return "Skeleton";
		if(clazz.isAssignableFrom(Node.class))return "TObject";
		return "XXX";
	}

	static class KindComparator implements Comparator<RelationExpanded>{
		// higher priority first
		String[] order=new String[]{"Material-Mesh", // before Mesh-TObject because mesh could be clone with material (shared in modeler)
				"TObject-TObject",
				"Mesh-TObject",
				"Material-TObject",
				"Skeleton-TObject",
				"Mesh-Skeleton",
				"Animation-TObject",
				"Animation-Mesh"};

		@Override
		public int compare(RelationExpanded o1, RelationExpanded o2) {
			if(o1.kind==o2.kind) return 0;
			for(String k:order){
				if(o1.kind==k) return -1;
				if(o2.kind==k) return 1;
			}
			return 1;
		}

		@Override
		public boolean equals(Object obj) {
			throw new UnsupportedOperationException("TODO: auto-generated method stub");
		}

	}

	// Translated from xbuf, but need to be cleaned (compact castings)
	public void merge(RelationExpanded re, Data src, Node root, Map<String, Object> components, Logger log) {
		System.out.println(re.ref1+"-"+re.ref2);

		Object op1 = re.op1;
		Object op2 = re.op2;
		boolean done = false;
		if (op1 instanceof Animation) {
			if (op2 instanceof Spatial) { // Geometry, Node
				AnimControl c = ((Spatial)op2).getControl(AnimControl.class);
				if (c == null) {
					SkeletonControl_31 sc = ((Spatial)op2).getControl(SkeletonControl_31.class);
					c = sc != null? new AnimControl(sc.getSkeleton()) : new AnimControl();
					((Spatial)op2).addControl(c);
				}
				c.addAnim((Animation)op1);
				done = true;
			}
		} else if (op1 instanceof CustomParamList) { // <--> xbuf_ext.Customparams.CustomParams
			if (op2 instanceof Spatial) { // Geometry, Node
				for (CustomParam p : ((CustomParamList)op1).getParamsList()) {
					mergeToUserData((CustomParam)p, (Spatial)op2, log);
				}
				done = true;
			}
		} else if (op1 instanceof XbufLightControl) { // <--> xbuf.Light
			if (op2 instanceof Geometry) { // ??? : Why not Spatial?
				((XbufLightControl)op1).getSpatial().removeControl(((XbufLightControl)op1));
				((Geometry)op2).addControl(((XbufLightControl)op1));
				// TODO raise an alert, strange to link LightNode and Geometry
				done = true;
			} else if (op2 instanceof Node) {
				((XbufLightControl)op1).getSpatial().removeControl(((XbufLightControl)op1));
				((Node)op2).addControl(((XbufLightControl)op1));
				done = true;
			}
		} else if (op1 instanceof Material) { // <--> xbuf.Material
			if (op2 instanceof Geometry) {
				System.out.println(((Geometry)op2).getParent().getName()+ " set material "+op1.hashCode()+ " "+((Material)op1).getName());

				((Geometry)op2).setMaterial((Material)op1);
//				cloneMaterialOnGeometry((Geometry)op2);
				done = true;
			} else if (op2 instanceof Node) {
				((Node)op2).setMaterial((Material)op1);
				System.out.println(((Geometry)op2).getParent().getName()+ " set material "+op1.hashCode()+ " "+((Material)op1).getName());

//				cloneMaterialOnGeometry((Geometry)op2);
				done = true;
			}
		} else if (op1 instanceof Geometry) { // <--> xbuf.Mesh
			if (op2 instanceof Node) {
				System.out.println(((Geometry)op1).getParent().getName()+ " has material "+((Geometry)op1).getMaterial().hashCode()+ " "+((Geometry)op1).getMaterial().getName());

				// TODO replace clone by instanciation
				Geometry mesh = ((Geometry)op1).getParent() != null && ((Geometry)op1).getParent()  != root?
						// TODO use materialReplicator ?
						((Geometry)op1).clone(false)
					:
						(Geometry)op1
					;
						System.out.println(((Geometry)op1).getParent().getName()+ " has now material "+((Geometry)op1).getMaterial().hashCode()+ " "+((Geometry)op1).getMaterial().getName());

						((Node)op2).attachChild(mesh);
				done = true;
			} else if (op2 instanceof Skeleton) {
				link((Spatial)op1,  (Skeleton)op2, findAnimLinkedToRef(src, re.ref2, components));
				done = true;
			}
		} else if (op1 instanceof Skeleton) { // <--> xbuf.Skeleton
			if (op2 instanceof Node) {
				link((Spatial)op2, (Skeleton)op1, findAnimLinkedToRef(src, re.ref1, components));
				done = true;
			}
		} else if (op1 instanceof Node) { // <--> xbuf.TObject
			if (op2 instanceof Node) {
				((Node)op1).attachChild((Node)op2);
				done = true;
			}
		}
		if (!done) {
			log.warn("can't link: doesn't know how to make relation {}({}) -- {}({})\n", new Object[]{re.ref1, op1.getClass(),
				re.ref2, op2.getClass()});
		}
	}

	// see http://hub.jmonkeyengine.org/t/skeletoncontrol-or-animcontrol-to-host-skeleton/31478/4
	public void link(Spatial v, Skeleton sk, Iterable<Animation> skAnims) {
		v.removeControl(SkeletonControl_31.class);
		// update AnimControl if related to skeleton
		AnimControl ac0 = v.getControl(AnimControl.class);
		AnimControl ac1;
		if (ac0 != null /* && ac.getSkeleton() != null*/ ) {
				v.removeControl(ac0);
				AnimControl ac2 = new AnimControl(sk);
				HashMap<String, Animation> anims = new HashMap<String, Animation>();
				ac0.getAnimationNames().forEach(name->anims.put(name, ac0.getAnim(name).clone()));
				ac2.setAnimations( anims);
				v.addControl(ac2);
				ac1= ac2;
			} else {
				// always add AnimControl else NPE when SkeletonControl.clone
				AnimControl ac2 = new AnimControl(sk);
				v.addControl(ac2);
				ac1=ac2;
			}
		// SkeletonControl should be after AnimControl in the list of Controls
		v.addControl(new SkeletonControl_31(sk));
		skAnims.forEach(ac1::addAnim);
		cloneMaterialOnGeometry(v);
	}

	@SuppressWarnings("unchecked")
	public Iterable<Animation> findAnimLinkedToRef(Data src, String ref, Map<String,Object> components) {
		return (Iterable<Animation>)src.getRelationsList().stream().map(r -> {
			if(r.getRef1()==ref&&components.get(r.getRef2()) instanceof Animation) return components.get(r.getRef2());
			else if(r.getRef2()==ref&&components.get(r.getRef1()) instanceof Animation) return components.get(r.getRef1());
			else return null;
		}).filter(i -> i!=null).iterator();
	}

	public Spatial mergeToUserData(CustomParam p, Spatial dst, Logger log) {
		String name=p.getName();
		switch(p.getValueCase()){
			case VALUE_NOT_SET:
				dst.setUserData(name,null);
				break;
			case VBOOL:
				dst.setUserData(name,p.getVbool());
				break;
			case VCOLOR:
				dst.setUserData(name,cnv(p.getVcolor(),new ColorRGBA()));
				break;
			case VFLOAT:
				dst.setUserData(name,p.getVfloat());
				break;
			case VINT:
				dst.setUserData(name,p.getVint());
				break;
			case VMAT4:
				dst.setUserData(name,cnv(p.getVmat4(),new Matrix4f()));
				break;
			case VQUAT:
				dst.setUserData(name,cnv(p.getVquat(),new Vector4f()));
				break;
			case VSTRING:
				dst.setUserData(name,p.getVstring());
				break;
			case VTEXTURE:
				dst.setUserData(name,loader4Materials.getValue(p.getVtexture(),log));
				break;
			case VVEC2:
				dst.setUserData(name,cnv(p.getVvec2(),new Vector2f()));
				break;
			case VVEC3:
				dst.setUserData(name,cnv(p.getVvec3(),new Vector3f()));
				break;
			case VVEC4:
				dst.setUserData(name,cnv(p.getVvec4(),new Vector4f()));
				break;
			default:
				log.warn("Material doesn't support parameter : {} of type {}",name,p.getValueCase().name());
		}
		return dst;
	}

	// TO avoid "java.lang.UnsupportedOperationException: Material instances cannot be shared when hardware skinning is used. Ensure all models use unique material instances."

	public void cloneMaterialOnGeometry(Spatial v) {
		
//		if(v instanceof Geometry) if(!materialReplicator.isReplica(((Geometry)v).getMaterial())) v.setMaterial(materialReplicator.newReplica(((Geometry)v).getMaterial()));
//
//		else if(v instanceof Node) ((Node)v).getChildren().forEach(this::cloneMaterialOnGeometry);

	}


}
