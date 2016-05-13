package jme3_ext_animation;

import com.jme3.export.Savable;

public interface Interpolation extends Savable {
	public float apply(float ratio, float p0, float p1);
}

