package jme3_ext_animation

import com.jme3.animation.BoneTrack
import org.eclipse.xtend.lib.annotations.Delegate
import com.jme3.animation.Track
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import com.jme3.util.TempVars
import com.jme3.animation.AnimChannel
import com.jme3.animation.AnimControl
import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import org.eclipse.xtend.lib.annotations.Accessors

class NamedBoneTrack implements Track{
	@Accessors(PUBLIC_GETTER) @Delegate BoneTrack delegate

	@Accessors(PUBLIC_GETTER) var String boneName
	var int boneIndex = -1
	
	var float[] times
	var Vector3f[] translations
	var Quaternion[] rotations
	var Vector3f[] scales

	new(){}

	/**
	 * translations, rotations and scales are in the PARENT space (and not relative to RestPose)
	 */
	new(String name, float[] times, Vector3f[] translations, Quaternion[] rotations, Vector3f[] scales){
		boneName = name
		this.times = times
		this.translations = translations
		this.rotations = rotations
		this.scales = scales
		delegate = null
	}
	
	override toString() {
		val startAt = if (times.length > 0) times.get(0) else 0
		val stopAt = if (times.length > 0) times.get(times.length-1) else 0
		String.format("%s(%s, %.2f-%.2f, %d, %d, %d):%d", this.class.simpleName, boneName, startAt, stopAt, translations.length, rotations.length, scales.length, boneIndex) 
	}
	
	def setupBoneTrack(AnimControl control) {
		val skel = control.skeleton
		if (skel != null && boneIndex < 0) {
			boneIndex = skel.getBoneIndex(boneName)
			if (boneIndex > -1) {
				val bone = skel.getBone(boneIndex)
				//Convert rotations, translations, scales to the "bind pose" space (BoneTrack combine initialXxx with transformation)
				val rotationInv = bone.worldBindRotation.inverse() // wrong name : it's the initialRot in PARENT Bone space
				val scaleInv = new Vector3f(1f/bone.worldBindScale.x, 1/bone.worldBindScale.y, 1/bone.worldBindScale.z)  // wrong name : it's the initialScale in PARENT Bone space
				val translationInv = bone.worldBindPosition.mult(-1) // wrong name : it's the initialPos in PARENT Bone space
				delegate = new BoneTrack(boneIndex, times,
					translations.map[v|v.add(translationInv)],
					rotations.map[v|rotationInv.mult(v)],
					scales.map[v|v.mult(scaleInv)]
				)
			}
		}
		boneIndex
	}

	override setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		try {
			if (setupBoneTrack(control) > -1) {
				delegate.setTime(time, weight, control, channel, vars)
			} else {
				System.out.println("no boneId for boneName :" + boneName + " on " + control.skeleton)
			}
		} catch(Exception exc) {
			exc.printStackTrace
		}
	}

	override clone() {
		new NamedBoneTrack(boneName, times, translations, rotations, scales)
	}

	override write(JmeExporter ex) {
		val oc = ex.getCapsule(this)
		oc.write(boneName, "boneName", null)
        oc.write(times, "times", null);
        oc.write(translations, "translations", null);
        oc.write(rotations, "rotations", null);
        oc.write(scales, "scales", null);
	}

	override read(JmeImporter im) {
		val ic = im.getCapsule(this)
		boneName = ic.readString("boneName", null)
        times = ic.readFloatArray("times", null);
		translations = ic.readSavableArray("translations", null) as Vector3f[]
        rotations = ic.readSavableArray("rotations", null)  as Quaternion[]
        scales = ic.readSavableArray("scales", null) as Vector3f[]
	}
}