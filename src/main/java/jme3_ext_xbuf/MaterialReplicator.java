package jme3_ext_xbuf;

public class MaterialReplicator{
//	protected Multimap<Material,Material> replicas=  ArrayListMultimap.create();
//	protected Map<Material, Material> sources = new HashMap<Material, Material> ();
//	
//	public Material newReplica(Material src) {
//		Material src0 = sources.getOrDefault(src, src);
//		Material dst = src0.clone();
//		dst.clearParam("BoneMatrices");
//		dst.setName(src0.getName() + "_" + replicas.keys().size());
//		if (replicas.put(src0, dst)) sources.put(dst, src0);
//		return dst;
//	}
//
//	public void syncReplicas(Material src) {
//		replicas.get(src).forEach(dst->{
//			src.getParams().forEach(param->dst.setParam(param.getName(),param.getVarType(),param.getValue()));
//			dst.clearParam("BoneMatrices");
//		});
//	}
//
//	public boolean isReplica(Material dst) {
//		return sources.containsKey(dst);
//	}
//
//	public void unlink(Material dst) {
//		Material src = sources.get(dst);
//		if (src != null) {
//			replicas.remove(src, dst);
//			sources.remove(dst);
//		}
//	}
}
