package jme3_ext_xbuf.mergers.animations;

import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SpatialTrack;
import com.jme3.animation.Track;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import lombok.Data;

@Data
public class XbufTrack{
	public static final String _SPATIAL="";
	protected final String boneName;
	protected final float[] times;
	protected final Vector3f[] translations;
	protected final Quaternion[] rotations;
	protected final Vector3f[] scales;

	public Track toJME(Skeleton skel) {
		if(boneName==_SPATIAL){
			return new SpatialTrack(times,translations,rotations,scales);
		}
		
		if(skel!=null){
			int boneIndex=skel.getBoneIndex(boneName);
			if(boneIndex>-1){
				Bone bone=skel.getBone(boneIndex);
				//Convert rotations, translations, scales to the "bind pose" space (BoneTrack combine initialXxx with transformation)
				Quaternion rotationInv=bone.getBindRotation().inverse();// it's the initialRot in PARENT Bone space
				Vector3f scaleInv=new Vector3f(1f/bone.getBindScale().x,1/bone.getBindScale().y,1/bone.getBindScale().z); //it's the initialScale in PARENT Bone space
				Vector3f translationInv=bone.getBindPosition().mult(-1);// it's the initialPos in PARENT Bone space

				Vector3f actualTranslations[]=new Vector3f[translations.length];
				for(int i=0;i<translations.length;i++)
					actualTranslations[i]=translations[i].add(translationInv);

				Vector3f actualScales[]=new Vector3f[scales.length];
				for(int i=0;i<scales.length;i++)
					actualScales[i]=scales[i].mult(scaleInv);

				Quaternion actualRotations[]=new Quaternion[rotations.length];
				for(int i=0;i<rotations.length;i++)
					actualRotations[i]=rotationInv.mult(rotations[i]);

				BoneTrack track=new BoneTrack(boneIndex,times,actualTranslations,actualRotations,actualScales);
				return track;
			}
		}
		return null;
	}
}
