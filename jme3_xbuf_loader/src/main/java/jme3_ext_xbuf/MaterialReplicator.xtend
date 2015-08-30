package jme3_ext_xbuf

import com.jme3.material.Material
import com.google.common.collect.Multimap
import com.google.common.collect.ArrayListMultimap
import java.util.HashMap
import java.util.Map

// not threadsafe
class MaterialReplicator {
	val Multimap<Material, Material> replicas = ArrayListMultimap.create()
	val Map<Material, Material> sources = new HashMap()
	
	def newReplica(Material src) {
		val src0 = sources.getOrDefault(src, src)
		val dst = src0.clone()
        dst.clearParam("BoneMatrices")
        dst.name = src0.name + "_"+ replicas.keys.size
        if (replicas.put(src0, dst)) {
        	sources.put(dst, src0)
        }
        dst
	}
	
	def syncReplicas(Material src) {
		replicas.get(src).forEach[ dst|
			src.params.forEach[ param |
				dst.setParam(param.name, param.varType, param.value)
			]
	        dst.clearParam("BoneMatrices")
		]
	}
	
	def isReplica(Material dst) {
		sources.containsKey(dst)
	}
	
	def unlink(Material dst){
		val src = sources.get(dst)
		if (src != null) {
			replicas.remove(src, dst)
			sources.remove(dst)
		}
	}
}