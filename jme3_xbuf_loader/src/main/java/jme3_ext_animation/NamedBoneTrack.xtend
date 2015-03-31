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


	new(){}

	new(String name, float[] times, Vector3f[] translations, Quaternion[] rotations, Vector3f[] scales){
		boneName = name
		delegate = new BoneTrack(boneIndex, times, translations, rotations, scales)
	}

	def int findBoneIndex(AnimControl control) {
		val skel = control.skeleton
		if (skel != null && boneIndex < 0) {
			boneIndex = skel.getBoneIndex(boneName)
			if (boneIndex > -1) {
				delegate = new BoneTrack(boneIndex, delegate.times, delegate.translations, delegate.rotations, delegate.scales)
			}
		}
		boneIndex
	}

	override setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		try {
			if (findBoneIndex(control) > -1) {
				delegate.setTime(time, weight, control, channel, vars)
			} else {
				System.out.println("no boneId for boneName :" + boneName + " on " + control.skeleton)
			}
		} catch(Exception exc) {
			exc.printStackTrace
		}
	}

	override clone() {
		new NamedBoneTrack(boneName, delegate.times, delegate.translations, delegate.rotations, delegate.scales)
	}

	override write(JmeExporter ex) {
		val oc = ex.getCapsule(this)
		oc.write(boneName, "boneName", null)
		oc.write(delegate, "delegate", null)
	}

	override read(JmeImporter im) {
		val ic = im.getCapsule(this)
		boneName = ic.readString("boneName", null)
		delegate = ic.readSavable("delegate", null) as BoneTrack
	}
}