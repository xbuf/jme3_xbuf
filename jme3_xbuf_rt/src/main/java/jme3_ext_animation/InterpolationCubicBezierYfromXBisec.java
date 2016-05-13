package jme3_ext_animation;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;

class InterpolationCubicBezierYfromXBisec extends Interpolation0 {
	public static float tolerance = 1f / 120f; // 1 / (60fps * 2)
	public static int maxIteration = 10;

	protected float p0HandleX;
	protected float p0HandleY;
	protected float p1HandleX;
	protected float p1HandleY;

	public InterpolationCubicBezierYfromXBisec() {
	}

	public InterpolationCubicBezierYfromXBisec(float p0HandleX, float p0HandleY, float p1HandleX, float p1HandleY) {
		this.p0HandleX = p0HandleX;
		this.p0HandleY = p0HandleY;
		this.p1HandleX = p1HandleX;
		this.p1HandleY = p1HandleY;
	}

	@Override
	public float apply(float ratio, float p0, float p1) {
		//System.out.printf("CubicBezier.findYfromXBisec(%f, %f, %f, %f, %f, %f, %f, %f, %d)...", ratio, p0, p1, p0HandleX, p0HandleY, p1HandleX, p1HandleY, tolerance, maxIteration)
		float b = CubicBezier.findYfromXBisec(ratio, p0, p1, p0HandleX, p0HandleY, p1HandleX, p1HandleY, tolerance, maxIteration);
		//System.out.printf("ok\n")
		return b;
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		this.p0HandleX = ic.readFloat("p0HandleX", 0);
		this.p0HandleY = ic.readFloat("p0HandleY", 0);
		this.p1HandleX = ic.readFloat("p1HandleX", 0);
		this.p1HandleY = ic.readFloat("p1HandleY", 0);
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(p0HandleX, "p0HandleX", 0);
		oc.write(p0HandleY, "p0HandleY", 0);
		oc.write(p1HandleX, "p1HandleX", 0);
		oc.write(p1HandleY, "p1HandleY", 0);
	}
}