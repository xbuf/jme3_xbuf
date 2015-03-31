package jme3_ext_animation

import com.jme3.export.Savable
import com.jme3.export.JmeImporter
import java.io.IOException
import com.jme3.export.JmeExporter

interface Interpolation extends Savable{
	def float apply(float ratio, float p0, float p1)
}

abstract class Interpolation0 implements Interpolation {
	override read(JmeImporter im) throws IOException {
	}

	override write(JmeExporter ex) throws IOException {
	}
}

// TODO add test to check savable (read/write) works
class Interpolations {
	public static val linear = new Interpolation0 {
		override apply(float ratio, float p0, float p1) {
			p0 + (p1 - p0) * ratio
		}
	}
	public static val constant = new Interpolation0 {
		override apply(float ratio, float p0, float p1) {
			if (ratio < 1) p0 else p1
		}
	}
	public static val inverse = new Interpolation0 {
		override apply(float ratio, float p0, float p1) {
			 p0 + (p1 - p0) * (1 - ratio)
		}
	}

	public static val sin = new Interpolation0 {
		override apply(float ratio, float p0, float p1) {
			 p0 + (p1 - p0) * (Math.sin(ratio * 2 * Math.PI) as float)
		}
	}

	static def cubicBezier(float p0HandleX, float p0HandleY, float p1HandleX, float p1HandleY) {
		new InterpolationCubicBezierYfromXBisec(p0HandleX, p0HandleY, p1HandleX, p1HandleY)
	}

}


class InterpolationCubicBezierYfromXBisec extends Interpolation0 {
	public static var float tolerance = 1f / 120f // 1 / (60fps * 2)
	public static var int maxIteration = 10

	var float p0HandleX
	var float p0HandleY
	var float p1HandleX
	var float p1HandleY
	new() {
	}

	new(float p0HandleX, float p0HandleY, float p1HandleX, float p1HandleY) {
		this.p0HandleX = p0HandleX
		this.p0HandleY = p0HandleY
		this.p1HandleX = p1HandleX
		this.p1HandleY = p1HandleY
	}

	override apply(float ratio, float p0, float p1) {
		//System.out.printf("CubicBezier.findYfromXBisec(%f, %f, %f, %f, %f, %f, %f, %f, %d)...", ratio, p0, p1, p0HandleX, p0HandleY, p1HandleX, p1HandleY, tolerance, maxIteration)
		val b = CubicBezier.findYfromXBisec(ratio, p0, p1, p0HandleX, p0HandleY, p1HandleX, p1HandleY, tolerance, maxIteration)
		//System.out.printf("ok\n")
		b
	}

	override read(JmeImporter im) throws IOException {
		val ic = im.getCapsule(this);
		this.p0HandleX = ic.readFloat("p0HandleX", 0)
		this.p0HandleY = ic.readFloat("p0HandleY", 0)
		this.p1HandleX = ic.readFloat("p1HandleX", 0)
		this.p1HandleY = ic.readFloat("p1HandleY", 0)
	}

	override write(JmeExporter ex) throws IOException {
		val oc = ex.getCapsule(this)
		oc.write(p0HandleX, "p0HandleX", 0)
		oc.write(p0HandleY, "p0HandleY", 0)
		oc.write(p1HandleX, "p1HandleX", 0)
		oc.write(p1HandleY, "p1HandleY", 0)
	}
}
