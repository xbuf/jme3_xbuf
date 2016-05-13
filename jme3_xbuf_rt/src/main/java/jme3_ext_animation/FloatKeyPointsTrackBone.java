package jme3_ext_animation;

import java.io.IOException;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.Track;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

/**
 * This class represents the track for float animation.
 *
 * @author David Bernard
 */
class FloatKeyPointsTrackBone extends FloatKeyPointsTrack {
	/**
	 * The times of the animations frames.
	 */
	protected String boneName;
	protected int boneId = -1;

	public FloatKeyPointsTrackBone() {
	}

	public FloatKeyPointsTrackBone(FloatKeyPoints points, String boneName) {
		super(points);
		this.boneName = boneName;
	}

	public Bone findBone(AnimControl control) {
		Skeleton skel = control.getSkeleton();
		if (boneId < 0) {
			boneId = skel.getBoneIndex(boneName);
		}
		return (boneId < 0) ? null : skel.getBone(boneId);
	}

	/**
	 * This method creates a clone of the current object.
	 * @return a clone of the current object
	 */
	@Override
	public Track clone() {
		FloatKeyPointsTrackBone c = new FloatKeyPointsTrackBone();
		c.points = this.points;
		c.boneName = this.boneName;
		return c;
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		super.write(ex);
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(boneName, "boneName", null);
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		super.read(im);
		InputCapsule ic = im.getCapsule(this);
		boneName = ic.readString("boneName", null);
	}
}
