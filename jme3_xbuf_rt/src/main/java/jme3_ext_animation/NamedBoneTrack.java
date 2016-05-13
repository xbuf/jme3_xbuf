package jme3_ext_animation;

import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.Track;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.util.TempVars;

import lombok.Getter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

public class NamedBoneTrack implements Track{
	@Getter protected BoneTrack delegate;

	@Getter protected String boneName;
	protected int boneIndex = -1;
	
	protected float[] times;
	protected Vector3f[] translations;
	protected Quaternion[] rotations;
	protected Vector3f[] scales;

	public NamedBoneTrack(){}

	/**
	 * translations, rotations and scales are in the PARENT space (and not relative to RestPose)
	 */
	public NamedBoneTrack(String name, float[] times, Vector3f[] translations, Quaternion[] rotations, Vector3f[] scales){
		boneName = name;
		this.times = times;
		this.translations = translations;
		this.rotations = rotations;
		this.scales = scales;
		delegate = null;
	}

	@Override
	public String toString() {
		float startAt = (times.length > 0) ? times[0] : 0;
		float stopAt = (times.length > 0) ? times[times.length-1] : 0;
		return String.format("%s(%s, %.2f-%.2f, %d, %d, %d):%d", this.getClass().getSimpleName(), boneName, startAt, stopAt, translations.length, rotations.length, scales.length, boneIndex);
	}

	public int setupBoneTrack(AnimControl control) {
		Skeleton skel = control.getSkeleton();
		if (skel != null && boneIndex < 0) {
			boneIndex = skel.getBoneIndex(boneName);
			if (boneIndex > -1) {
				Bone bone = skel.getBone(boneIndex);
				//Convert rotations, translations, scales to the "bind pose" space (BoneTrack combine initialXxx with transformation)
				Quaternion rotationInv = bone.getBindRotation().inverse(); // wrong name : it's the initialRot in PARENT Bone space
				Vector3f scaleInv = new Vector3f(1f/bone.getBindScale().x, 1/bone.getBindScale().y, 1/bone.getBindScale().z); // wrong name : it's the initialScale in PARENT Bone space
				Vector3f translationInv = bone.getBindPosition().mult(-1); // wrong name : it's the initialPos in PARENT Bone space
				delegate = new BoneTrack(boneIndex, times,
					(Vector3f[])Arrays.stream(translations).map((v) -> v.add(translationInv)).collect(Collectors.toList()).toArray(),
					(Quaternion[])Arrays.stream(rotations).map((v) -> rotationInv.mult(v)).collect(Collectors.toList()).toArray(),
					(Vector3f[])Arrays.stream(scales).map((v) -> v.mult(scaleInv)).collect(Collectors.toList()).toArray()
				);
			}
		}
		return boneIndex;
	}

	@Override
	public void setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		try {
			if (setupBoneTrack(control) > -1) {
				delegate.setTime(time, weight, control, channel, vars);
			} else {
				System.out.println("no boneId for boneName :" + boneName + " on " + control.getSkeleton());
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public NamedBoneTrack clone() {
		return new NamedBoneTrack(boneName, times, translations, rotations, scales);
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(boneName, "boneName", null);
		oc.write(times, "times", null);
		oc.write(translations, "translations", null);
		oc.write(rotations, "rotations", null);
		oc.write(scales, "scales", null);
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		boneName = ic.readString("boneName", null);
		times = ic.readFloatArray("times", null);
		translations = (Vector3f[]) ic.readSavableArray("translations", null);
		rotations = (Quaternion[]) ic.readSavableArray("rotations", null);
		scales = (Vector3f[]) ic.readSavableArray("scales", null);
	}

	@Override
	public float getLength() {
		return delegate.getLength();
	}

	@Override
	public float[] getKeyFrameTimes() {
		return delegate.getKeyFrameTimes();
	}
}
