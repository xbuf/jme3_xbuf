package jme3_ext_animation

/**
 * Some links:
 *
 * * [Bézier curve - Wikipedia, the free encyclopedia](http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Animation)
 * * [In Python, given x-value on 2D Bezier Curve, return y-value - Blender Stack Exchange](http://blender.stackexchange.com/questions/8465/in-python-given-x-value-on-2d-bezier-curve-return-y-value)
 * * [java - Determining the real roots of a polynomial within a specific range - Stack Overflow](http://stackoverflow.com/questions/21869760/determining-the-real-roots-of-a-polynomial-within-a-specific-range)
 * * [Blender: curve.c](https://developer.blender.org/diffusion/B/browse/master/source/blender/blenkernel/intern/curve.c;7c9b1065895e0a6a12555075980d7a77d1dea8c7%241362)
 * * [javascript - y coordinate for a given x cubic bezier - Stack Overflow](http://stackoverflow.com/questions/7348009/y-coordinate-for-a-given-x-cubic-bezier)
 */
class CubicBezier {

	/**
	 * formula from http://blender.stackexchange.com/questions/6692/mathematical-formula-for-bezier-curves
	 */
	static def bezier1D(float p0, float h0, float p1, float h1, float t) {
		val rt = 1 - t
		val rt2 = rt *rt
		val t2 = t * t
		rt * rt2 * p0 + 3 * rt2 * t * h0 + 3 * rt * t2 * h1 + t * t2 *p1
	}

	/**
	 * This method only work when bezier are curve where f(x) = y and y is unique (eg. when x is the timeline).
	 * 1. search t from x by approximation (binary search)
	 * 2. bezier(t).y
	 *
	 * @param x the target x from witch we search y
	 * @param yp0 the y for p0 (xp0 = 0)
	 * @param yp1 the y for p1 (xp1 = 1)
	 * @param xh0 the x for handle h0 of p0
	 * @param yh0 the y for handle h0 of p0
	 * @param xh1 the x for handle h1 of p1
	 * @param yh1 the y for handle h1 of p1
	 * @param xTolerance the acceptable tolerance for x when searchint t (eg. 0.0001f)
	 */
	static def float findYfromXBisec(float x, float yp0, float yp1, float xh0, float yh0, float xh1, float yh1, float xTolerance, int maxIteration ) {
		// we could do something less stupid, but since the x is monotonic
		// increasing given the problem constraints, we'll do a binary search.

		//establish bounds
		var lower = 0f
		var upper = 1f
		var t = (upper + lower) / 2f

		var xt = bezier1D(0, xh0, 1, xh1, t)


		for(var ite = maxIteration; Math.abs(x - xt) > xTolerance && ite > 0; ite--) {
			if(x > xt) {
				lower = t
			} else {
				upper = t
			}
			t = (upper + lower) / 2f
			xt = bezier1D(0, xh0, 1, xh1, t)
		}
		//we're within tolerance of the desired x value.
		//return the y value.
		bezier1D(yp0, yh0, yp1, yh1, t)
	}
}
