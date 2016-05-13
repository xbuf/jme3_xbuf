package jme3_ext_animation;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;

public class TrackFactory {

	static FloatKeyPointsTrack translationX(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Spatial s = control.getSpatial();
				Vector3f v = s.getLocalTranslation();
				v.x += delta;
				s.setLocalTranslation(v);
			}
		};
	}
	static FloatKeyPointsTrack translationY(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Spatial s = control.getSpatial();
				Vector3f v = s.getLocalTranslation();
				v.y += delta;
				s.setLocalTranslation(v);
			}
		};
	}
	static FloatKeyPointsTrack translationZ(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Spatial s = control.getSpatial();
				Vector3f v = s.getLocalTranslation();
				v.z += delta;
				s.setLocalTranslation(v);
			}
		};
	}
	static FloatKeyPointsTrack scaleX(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Spatial s = control.getSpatial();
				Vector3f v = s.getLocalScale();
				v.x += delta;
				s.setLocalScale(v);
			}
		};
	}
	static FloatKeyPointsTrack scaleY(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Spatial s = control.getSpatial();
				Vector3f v = s.getLocalScale();
				v.y += delta;
				s.setLocalScale(v);
			}
		};
	}
	static FloatKeyPointsTrack scaleZ(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Spatial s = control.getSpatial();
				Vector3f v = s.getLocalScale();
				v.z += delta;
				s.setLocalScale(v);
			}
		};
	}

	// Bones
	static FloatKeyPointsTrack translationX(FloatKeyPoints points, String boneName) {
		return new FloatKeyPointsTrackBone(points, boneName){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Bone s = findBone(control);
				if (s != null) {
					s.getLocalPosition().x += delta * weight;
				}
			}
		};
	}
	static FloatKeyPointsTrack translationY(FloatKeyPoints points, String boneName) {
		return new FloatKeyPointsTrackBone(points, boneName){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Bone s = findBone(control);
				if (s != null) {
					s.getLocalPosition().y += delta * weight;
				}
			}
		};
	}
	static FloatKeyPointsTrack translationZ(FloatKeyPoints points, String boneName) {
		return new FloatKeyPointsTrackBone(points, boneName){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Bone s = findBone(control);
				if (s != null) {
					s.getLocalPosition().z += delta * weight;
				}
			}
		};
	}
	static FloatKeyPointsTrack scaleX(FloatKeyPoints points, String boneName) {
		return new FloatKeyPointsTrackBone(points, boneName){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Bone s = findBone(control);
				if (s != null) {
					s.getLocalScale().x += delta * weight;
				}
			}
		};
	}
	static FloatKeyPointsTrack scaleY(FloatKeyPoints points, String boneName) {
		return new FloatKeyPointsTrackBone(points, boneName){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Bone s = findBone(control);
				if (s != null) {
					s.getLocalScale().y += delta * weight;
				}
			}
		};
	}
	static FloatKeyPointsTrack scaleZ(FloatKeyPoints points, String boneName) {
		return new FloatKeyPointsTrackBone(points, boneName){
			@Override public void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				Bone s = findBone(control);
				if (s != null) {
					s.getLocalScale().z += delta * weight;
				}
			}
		};
	}
}
