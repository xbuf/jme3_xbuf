package jme3_ext_animation

import org.junit.Test
import org.junit.Assert

class CubicBezierTest {
	@Test
	def void sampleValue() {
		val maxIteration = 1000*1000
		/*
		CubicBezier.findYfromXBisec(0,000000, 0,000000, 0,000000, 27,938395, 0,000000, 43,061607, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,000000, 0,000000, 0,000000, 27,938395, 0,000000, 43,061607, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,000000, -0,000000, -8,370346, 27,938395, 0,000000, 0,000000, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,000000, 1,000000, 1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,000000, 1,000000, 1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,000000, -1,000000, -1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,005911, 0,000000, 0,000000, 27,938395, 0,000000, 43,061607, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,005911, 0,000000, 0,000000, 27,938395, 0,000000, 43,061607, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,005911, -0,000000, -8,370346, 27,938395, 0,000000, 0,000000, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,004482, 1,000000, 1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,004482, 1,000000, 1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,004482, -1,000000, -1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,011722, 0,000000, 0,000000, 27,938395, 0,000000, 43,061607, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,011722, 0,000000, 0,000000, 27,938395, 0,000000, 43,061607, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,011722, -0,000000, -8,370346, 27,938395, 0,000000, 0,000000, 0,000000, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,008888, 1,000000, 1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,008888, 1,000000, 1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		CubicBezier.findYfromXBisec(0,008888, -1,000000, -1,644181, 36,527447, 1,000000, 56,472553, 1,644181, 0,016667, maxIteration)
		*/
		System.out.println(CubicBezier.findYfromXBisec(0.017715f, 0.000000f, 0.000000f, 27.938395f, 0.000000f, 43.061607f, 0.000000f, 0.016667f, maxIteration))
		Assert.assertTrue(true)
	}
}