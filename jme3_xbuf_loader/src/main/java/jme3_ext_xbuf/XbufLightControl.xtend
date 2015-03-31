package jme3_ext_xbuf;

import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.light.DirectionalLight
import com.jme3.light.Light
import com.jme3.light.PointLight
import com.jme3.light.SpotLight
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.Control
import com.jme3.util.TempVars

/**
 * Apply current spatial's Transform to the light, following xbuf rules (!= jme's LightControl).
 */
class XbufLightControl extends AbstractControl {
	private static val LIGHT_NAME = "light"

	public var Light light

	new() {
	}

	// fields used, when inversing ControlDirection:
	override controlUpdate(float tpf) {
		if (spatial != null && light != null) {
			val vars = TempVars.get()
			try {
				if (light instanceof PointLight) {
					light.setPosition(spatial.getWorldTranslation())
				}

				if (light instanceof DirectionalLight) {
					light.setDirection(spatial.getWorldRotation().multLocal(vars.vect1.set(Vector3f.UNIT_Z)));
				}

				if (light instanceof SpotLight) {
					light.setPosition(spatial.getWorldTranslation());
					light.setDirection(spatial.getWorldRotation().multLocal(vars.vect1.set(Vector3f.UNIT_Z)));
				}
			} finally {
				vars.release()
			}
		}
	}

	override controlRender(RenderManager rm, ViewPort vp) {
	}

	override Control cloneForSpatial(Spatial newSpatial) {
		val control = new XbufLightControl()
		control.light = light
		control.setSpatial(newSpatial);
		control.setEnabled(isEnabled());
		control
	}

	override read(JmeImporter im) {
		super.read(im)
		val ic = im.getCapsule(this)
		light = ic.readSavable(LIGHT_NAME, null) as Light
	}

	override write(JmeExporter ex) {
		super.write(ex);
		val oc = ex.getCapsule(this)
		oc.write(light, LIGHT_NAME, null)
	}
}