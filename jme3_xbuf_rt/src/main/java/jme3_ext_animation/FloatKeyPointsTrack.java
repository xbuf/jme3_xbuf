package jme3_ext_animation;

import java.io.IOException;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Track;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.util.TempVars;

/**
 * This class represents the track for float animation.
 *
 * @author David Bernard
 */
public class FloatKeyPointsTrack implements Track {

	/**
	 * The times of the animations frames.
	 */
	protected FloatKeyPoints points;
	protected float lastValue = Float.MIN_VALUE;

	public FloatKeyPointsTrack() {
	}

	public FloatKeyPointsTrack(FloatKeyPoints points) {
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
	protected void apply(float value, float delta, float weight, AnimControl control, AnimChannel channel, TempVars vars){
	}

	/**
	 * Modify the spatial which this track modifies.
	 *
	 * @param time the current time of the animation
	 */
	@Override 
	public void setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		try {
			if (points != null) {
				float value = points.valueAt(time);
				if (lastValue == Float.MIN_VALUE || time <= 0) {
					lastValue = value;
				}
				apply(value, value - lastValue, weight, control, channel, vars);
				lastValue = value;
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * @return the length of the track
	 */
	@Override
	public float getLength() {
		return points.getLength();
	}

	/**
	 * This method creates a clone of the current object.
	 * @return a clone of the current object
	 */
	@Override
	public Track clone() {
		FloatKeyPointsTrack c = new FloatKeyPointsTrack();
		c.points = this.points;
		return c;
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(points, "points", null);
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		points = (FloatKeyPoints) ic.readSavable("points", null);
	}

	@Override
	public float[] getKeyFrameTimes() {
		return points.times;
	}

}
