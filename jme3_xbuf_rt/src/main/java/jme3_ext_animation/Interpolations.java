package jme3_ext_animation;

//TODO add test to check savable (read/write) works
public class Interpolations {

	public static Interpolation0 linear = new Interpolation0(){
		@Override
		public float apply(float ratio, float p0, float p1) {
			return p0 + (p1 - p0) * ratio;
		}
	};

	public static Interpolation0 constant = new Interpolation0(){
		@Override
		public float apply(float ratio, float p0, float p1) {
			return (ratio < 1) ? p0 : p1;
		}
	};

	public static Interpolation0 inverse = new Interpolation0(){
		@Override
		public float apply(float ratio, float p0, float p1) {
			return p0 + (p1 - p0) * (1 - ratio);
		}
	};
	
	public static Interpolation0 sin = new Interpolation0(){
		@Override
		public float apply(float ratio, float p0, float p1) {
			return p0 + (p1 - p0) * ((float)Math.sin(ratio * 2 * Math.PI));
		}
	};

	public static Interpolation0 cubicBezier(float p0HandleX, float p0HandleY, float p1HandleX, float p1HandleY) {
		return new InterpolationCubicBezierYfromXBisec(p0HandleX, p0HandleY, p1HandleX, p1HandleY);
	}

}
