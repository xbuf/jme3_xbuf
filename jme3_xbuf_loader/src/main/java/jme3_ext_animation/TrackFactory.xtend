package jme3_ext_animation;

import com.jme3.animation.AnimChannel
import com.jme3.animation.AnimControl
import com.jme3.util.TempVars

public class TrackFactory {

	static def FloatKeyPointsTrack translationX(FloatKeyPoints points) {
		new FloatKeyPointsTrack(points){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = control.getSpatial()
				val v = s.getLocalTranslation()
				v.x += delta
				s.localTranslation = v
			}
		}
	}
	static def FloatKeyPointsTrack translationY(FloatKeyPoints points) {
		new FloatKeyPointsTrack(points){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = control.getSpatial()
				val v = s.getLocalTranslation()
				v.y += delta
				s.localTranslation = v
			}
		}
	}
	static def FloatKeyPointsTrack translationZ(FloatKeyPoints points) {
		new FloatKeyPointsTrack(points){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = control.getSpatial()
				val v = s.getLocalTranslation()
				v.z += delta
				s.localTranslation = v
			}
		}
	}
	static def FloatKeyPointsTrack scaleX(FloatKeyPoints points) {
		new FloatKeyPointsTrack(points){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = control.getSpatial()
				val v = s.getLocalScale()
				v.x += delta
				s.localScale = v
			}
		}
	}
	static def FloatKeyPointsTrack scaleY(FloatKeyPoints points) {
		new FloatKeyPointsTrack(points){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = control.getSpatial()
				val v = s.getLocalScale()
				v.y += delta
				s.localScale = v
			}
		}
	}
	static def FloatKeyPointsTrack scaleZ(FloatKeyPoints points) {
		return new FloatKeyPointsTrack(points){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = control.getSpatial()
				val v = s.getLocalScale()
				v.z += delta
				s.localScale = v
			}
		}
	}

	// Bones
	static def FloatKeyPointsTrack translationX(FloatKeyPoints points, String boneName) {
		new FloatKeyPointsTrackBone(points, boneName){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = findBone(control)
				if (s != null) {
					s.localPosition.x += delta * weight
				}
			}
		}
	}
	static def FloatKeyPointsTrack translationY(FloatKeyPoints points, String boneName) {
		new FloatKeyPointsTrackBone(points, boneName){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = findBone(control)
				if (s != null) {
					s.localPosition.y += delta * weight
				}
			}
		}
	}
	static def FloatKeyPointsTrack translationZ(FloatKeyPoints points, String boneName) {
		new FloatKeyPointsTrackBone(points, boneName){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = findBone(control)
				if (s != null) {
					s.localPosition.z += delta * weight
				}
			}
		}
	}
	static def FloatKeyPointsTrack scaleX(FloatKeyPoints points, String boneName) {
		new FloatKeyPointsTrackBone(points, boneName){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = findBone(control)
				if (s != null) {
					s.localScale.x += delta * weight
				}
			}
		}
	}
	static def FloatKeyPointsTrack scaleY(FloatKeyPoints points, String boneName) {
		new FloatKeyPointsTrackBone(points, boneName){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = findBone(control)
				if (s != null) {
					s.localScale.y += delta * weight
				}
			}
		}
	}
	static def FloatKeyPointsTrack scaleZ(FloatKeyPoints points, String boneName) {
		new FloatKeyPointsTrackBone(points, boneName){
			override apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
				val s = findBone(control)
				if (s != null) {
					s.localScale.z += delta * weight
				}
			}
		}
	}
}
