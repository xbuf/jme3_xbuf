package jme3_ext_animation;

import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.export.Savable

/**
 * This class represents the track for float animation.
 *
 * @author David Bernard
 */
class FloatKeyPoints implements Savable {

	/**
	 * The times of the animations frames.
	 */
	var float[] times

	/**
	 * The values of the animations frames.
	 */
	var float[] values

	/**
	 * The between frame, eases[i] to use for values[i] and values[i+1];
	 */
	var Interpolation[] eases

	/**
	 * The default ease to use.
	 */
	var Interpolation easeDefault = Interpolations.linear

	new() {
	}

	def setKeyPoints(float[] times, float[] values){
		if (times.length != values.length) {
			throw new IllegalArgumentException("times and values should have the same length : " + times.length + " != " + values.length)
		}
		this.times = times
		this.values = values
	}

	def setEases(Interpolation[] eases, Interpolation easeDefault) {
		this.eases = eases
		this.easeDefault = easeDefault
	}

	def float valueAt(float time) {
		val lastFrame = times.length - 1
		if (lastFrame == -1) {
			throw new IllegalStateException("empty keyframes")
		} else  if (time < 0 || lastFrame == 0) {
			values.get(0)
		} else if (time >= times.get(lastFrame)) {
			values.get(lastFrame)
		} else {
			var startFrame = 0
			var endFrame = 1
			// use lastFrame so we never overflow the array
			for (var i = 0; i < lastFrame && times.get(i) < time; i++) {
				startFrame = i
				endFrame = i + 1
			}
			var blend = (time - times.get(startFrame)) / (times.get(endFrame) - times.get(startFrame));
			easeAt(startFrame).apply(blend, values.get(startFrame), values.get(endFrame))
		}
	}

	def Interpolation easeAt(int idx) {
		if (eases != null && idx < eases.length) eases.get(idx) else easeDefault
	}

	def getLength() {
		if (times == null)  0 else times.get(times.length - 1) - times.get(0)
	}

	override write(JmeExporter ex) {
		val oc = ex.getCapsule(this)
		oc.write(times, "times", null)
		oc.write(values, "values", null)
		//TODO store ease
	}

	override read(JmeImporter im) {
		val ic = im.getCapsule(this)
		times = ic.readFloatArray("times", null)
		values = ic.readFloatArray("values", null)
		//TODO restore ease
	}
}
