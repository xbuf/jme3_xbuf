package jme3_ext_xbuf.mergers.meshes;

import org.apache.logging.log4j.Logger;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;

import jme3_ext_xbuf.Converters;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import xbuf.Meshes.Skin;

@Data @Log4j2
public class XbufMesh{
	protected final xbuf.Meshes.Mesh src;
	
	public String getName(){
		return src.getName();
	}
	
	public Mesh toJME() throws IllegalArgumentException {
		if(src.getIndexArraysCount()>1) throw new IllegalArgumentException("doesn't support more than 1 index array");
		if(src.getLod()>1) throw new IllegalArgumentException("doesn't support lod > 1 : "+src.getLod());
		
		Mesh dst=new Mesh();

		//		context.put("G~meshName~"+dst.hashCode(),src.getName());
		dst.setMode(Converters.cnv(src.getPrimitive()));
		src.getVertexArraysList().forEach(va -> {
			Type type=Converters.cnv(va.getAttrib());
			dst.setBuffer(type,va.getFloats().getStep(),Converters.hack_cnv(va.getFloats()));
			log.debug("add {}",dst.getBuffer(type));
		});
		
		src.getIndexArraysList().forEach(va -> dst.setBuffer(VertexBuffer.Type.Index,va.getInts().getStep(),Converters.hack_cnv(va.getInts())));
		if(src.hasSkin()) applySkin(src.getSkin(),dst);

		dst.updateCounts();
		dst.updateBound();
		return dst;
	}
	
	protected Mesh applySkin(Skin skin, Mesh dst) {
		dst.clearBuffer(Type.BoneIndex);
		dst.clearBuffer(Type.BoneWeight);
		int nb=skin.getBoneCountCount();
		// val maxWeightPerVert = Math.min(4, skin.boneCountList.reduce[p1, p2|Math.max(p1,p2)])
		int maxWeightPerVert=4;// jME 3.0 only support fixed 4 weights per vertex
		byte[] indexPad=new byte[nb*maxWeightPerVert];
		float weightPad[]=new float[nb*maxWeightPerVert];
		int isrc=0;
		for(int i=0;i<nb;i++){
			float totalWeightPad=0f;
			int cnt=skin.getBoneCountList().get(i);
			int k0=i*maxWeightPerVert;
			for(int j=0;j<maxWeightPerVert;j++){
				int k=k0+j;
				byte index=0;
				float weight=0f;
				if(j<cnt){
					weight=skin.getBoneWeightList().get(isrc+j);
					index=skin.getBoneIndexList().get(isrc+j).byteValue();
				}
				totalWeightPad+=weight;
				indexPad[k]=index;
				weightPad[k]=weight;
			}
			if(totalWeightPad>0){
				float totalWeight=0.0f;
				for(int j=0;j<cnt;j++)
					totalWeight+=skin.getBoneWeightList().get(isrc+j);

				float normalizer=totalWeight/totalWeightPad;
				int wpv=Math.min(maxWeightPerVert,cnt);
				for(int j=0;j<wpv;j++){
					int k=k0+j;
					weightPad[k]=weightPad[k]*normalizer;
				}
				if(cnt>maxWeightPerVert&&totalWeight!=totalWeightPad){
					log.warn("vertex influenced by more than {} bones : {}, only the {} higher are keep for total weight keep/orig: {}/{}.",maxWeightPerVert,cnt,wpv,totalWeightPad,totalWeight);
				}
			}
			isrc+=cnt;
		}
		dst.setBuffer(Type.BoneIndex,maxWeightPerVert,indexPad);
		dst.setBuffer(Type.BoneWeight,maxWeightPerVert,weightPad);
		dst.setMaxNumWeights(maxWeightPerVert);

		// creating empty buffers for HW skinning
		// the buffers will be setup if ever used.
		VertexBuffer weightsHW=new VertexBuffer(Type.HWBoneWeight);
		VertexBuffer indicesHW=new VertexBuffer(Type.HWBoneIndex);
		// setting usage to cpuOnly so that the buffer is not send empty to the GPU
		indicesHW.setUsage(VertexBuffer.Usage.CpuOnly);
		weightsHW.setUsage(VertexBuffer.Usage.CpuOnly);
		dst.setBuffer(weightsHW);
		dst.setBuffer(indicesHW);
		dst.generateBindPose(true);
		return dst;
	}


}
