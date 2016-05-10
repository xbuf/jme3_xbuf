package jme3_ext_xbuf.mergers.relations;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

public class LinkerHelpers{
	public static <T> T getRef1(RefData data, Class<T> as) {
		return getRef(false,data,as);
	}

	public static <T> T getRef2(RefData data, Class<T> as) {
		return getRef(true,data,as); 
	}

	@SuppressWarnings("unchecked")
	private static <T> T getRef(boolean id, RefData data, Class<T> as) {
		Object op1_o=data.context.get(!id?data.ref1:data.ref2);
		if(op1_o==null||!(as.isAssignableFrom(op1_o.getClass()))){
//			System.out.println("Got "+op1_o.getClass()+ " expected "+as);
			// If we are picking a mesh as if it were a geometry, return a geometry automagically.
			if(op1_o instanceof Mesh&&(as.isAssignableFrom(Spatial.class)||as.isAssignableFrom(Geometry.class))){
				op1_o=getGeometry(id,data);
			}else op1_o=null;
		}		
//		System.out.println("Returned  "+(op1_o!=null?op1_o.getClass():"null"));

		return (T)op1_o;
	}

	public static Geometry getGeometry1(RefData data) {
		return getGeometry(false,data);
	}

	public static Geometry getGeometry2(RefData data) {
		return getGeometry(true,data);
	}

	private static Geometry getGeometry(boolean id, RefData data) {
		String ref=!id?data.ref1:data.ref2;
		// Generate geometry form mesh and keep it cached in the context
		Geometry geo=(Geometry)data.context.get("G~"+ref);
		if(geo==null){
			Object m=data.context.get(ref);
			if(m==null||!(m instanceof Mesh)) return null;
			

			geo=new Geometry("",(Mesh)m);
			geo.setName(data.context.get("G~meshName~"+m.hashCode()));

			data.context.put("G~"+ref,geo);
		}
		return geo;
	}
}
