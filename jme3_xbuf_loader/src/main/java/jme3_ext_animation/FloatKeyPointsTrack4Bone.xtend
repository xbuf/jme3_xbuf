package jme3_ext_animation;

import com.jme3.animation.AnimControl
import com.jme3.animation.Bone
import com.jme3.animation.Track
import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter

/**
 * This class represents the track for float animation.
 *
 * @author David Bernard
 */
class FloatKeyPointsTrackBone extends FloatKeyPointsTrack {
	/**
	 * The times of the animations frames.
	 */
	protected var String boneName
	protected var int boneId = -1

	new() {
	}

	new(FloatKeyPoints points, String boneName) {
		super(points)
		this.boneName = boneName
	}

	def Bone findBone(AnimControl control) {
		val skel = control.skeleton
		if (boneId < 0) {
			boneId = skel.getBoneIndex(boneName)
		}
		if (boneId < 0) null else skel.getBone(boneId)
	}

	/**
	 * This method creates a clone of the current object.
	 * @return a clone of the current object
	 */
	override Track clone() {
		val c = new FloatKeyPointsTrackBone()
		c.points = this.points
		c.boneName = this.boneName
		c
	}

	override write(JmeExporter ex) {
		super.write(ex)
		val oc = ex.getCapsule(this)
		oc.write(boneName, "boneName", null)
	}

	override read(JmeImporter im) {
		super.read(im)
		val ic = im.getCapsule(this)
		boneName = ic.readString("boneName", null)
	}
}