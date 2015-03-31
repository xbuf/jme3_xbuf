package jme3_ext_animation;

import com.jme3.animation.AnimChannel
import com.jme3.animation.AnimControl
import com.jme3.animation.Track
import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.util.TempVars

/**
 * This class represents the track for float animation.
 *
 * @author David Bernard
 */
class FloatKeyPointsTrack implements Track {

	/**
	 * The times of the animations frames.
	 */
	protected var FloatKeyPoints points
	protected var lastValue = Float.MIN_VALUE

	new() {
	}

	new(FloatKeyPoints points) {
		this.points = points;
	}

	/**
	 * method to override by concrete Track
	 * @param value the result value from interpolation between keypoints
	 * @param delta the delta (value - previous value), can be used to do relative transformation
	 * @param weight same as setTime()
	 * @param control same as setTime()
	 * @param channel same as setTime()
	 * @param vars same as setTime()
	 */
	protected def void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars){
	}

	/**
	 * Modify the spatial which this track modifies.
	 *
	 * @param time the current time of the animation
	 */
	override setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		try {
			if (points != null) {
				val value = points.valueAt(time)
				if (lastValue == Float.MIN_VALUE || time <= 0) {
					lastValue = value
				}
				apply(value, value - lastValue, weight, control, channel, vars);
				lastValue = value
			}
		} catch(Exception exc) {
			exc.printStackTrace()
		}
	}

	/**
	 * @return the length of the track
	 */
	override getLength() {
		points.getLength()
	}

	/**
	 * This method creates a clone of the current object.
	 * @return a clone of the current object
	 */
	override Track clone() {
		val c = new FloatKeyPointsTrack()
		c.points = this.points
		c
	}

	override write(JmeExporter ex) {
		val oc = ex.getCapsule(this)
		oc.write(points, "points", null)
	}

	override read(JmeImporter im) {
		val ic = im.getCapsule(this)
		points = ic.readSavable("points", null) as FloatKeyPoints
	}
}
