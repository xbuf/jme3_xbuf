package jme3_ext_animation;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

/**
 * This class represents the track for float animation.
 *
 * @author David Bernard
 */
public class FloatKeyPoints implements Savable {

	/**
	 * The times of the animations frames.
	 */
	protected float[] times;

	/**
	 * The values of the animations frames.
	 */
	protected float[] values;

	/**
	 * The between frame, eases[i] to use for values[i] and values[i+1];
	 */
	protected Interpolation[] eases;

	/**
	 * The default ease to use.
	 */
	protected Interpolation easeDefault = Interpolations.linear;

	public FloatKeyPoints() {
	}

	public void setKeyPoints(float[] times, float[] values){
		if (times.length != values.length) {
			throw new IllegalArgumentException("times and values should have the same length : " + times.length + " != " + values.length);
		}
		this.times = times;
		this.values = values;
	}

	public void setEases(Interpolation[] eases, Interpolation easeDefault) {
		this.eases = eases;
		this.easeDefault = easeDefault;
	}

	public float valueAt(float time) {
		int lastFrame = times.length - 1;
		if (lastFrame == -1) {
			throw new IllegalStateException("empty keyframes");
		} else  if (time < 0 || lastFrame == 0) {
			return values[0];
		} else if (time >= times[lastFrame]) {
			return values[lastFrame];
		} else {
			int startFrame = 0;
			int endFrame = 1;
			// use lastFrame so we never overflow the array
			for (int i = 0; i < lastFrame && times[i] < time; i++) {
				startFrame = i;
				endFrame = i + 1;
			}
			float blend = (time - times[startFrame]) / (times[endFrame] - times[startFrame]);
			return easeAt(startFrame).apply(blend, values[startFrame], values[endFrame]);
		}
	}

	public Interpolation easeAt(int idx) {
		return (eases != null && idx < eases.length)? eases[idx] : easeDefault;
	}

	public float getLength() {
		return (times == null) ? 0 : times[times.length - 1] - times[0];
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(times, "times", null);
		oc.write(values, "values", null);
		//TODO store ease
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		times = ic.readFloatArray("times", null);
		values = ic.readFloatArray("values", null);
		//TODO restore ease
	}
}
