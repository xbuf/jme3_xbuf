package jme3_ext_xbuf;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.util.TempVars;

import lombok.Getter;

public class XbufLightControl extends AbstractControl{
	private static final String LIGHT_NAME="light";

	protected @Getter Light light;

	// fields used, when inversing ControlDirection:
	@Override
	public void controlUpdate(float tpf) {
		if(spatial!=null&&light!=null){
			TempVars vars=TempVars.get();
			try{
				if(light instanceof PointLight){
					((PointLight)light).setPosition(spatial.getWorldTranslation());
				}

				if(light instanceof DirectionalLight){
					((DirectionalLight)light).setDirection(spatial.getWorldRotation().multLocal(vars.vect1.set(Vector3f.UNIT_Z)));
				}

				if(light instanceof SpotLight){
					((SpotLight)light).setPosition(spatial.getWorldTranslation());
					((SpotLight)light).setDirection(spatial.getWorldRotation().multLocal(vars.vect1.set(Vector3f.UNIT_Z)));
				}
			}finally{
				vars.release();
			}
		}
	}

	@Override
	public void controlRender(RenderManager rm, ViewPort vp) {}

	@Override
	public Control cloneForSpatial(Spatial newSpatial) {
		XbufLightControl control=new XbufLightControl();
		control.light=light;
		control.setSpatial(newSpatial);
		control.setEnabled(isEnabled());
		return control;
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		super.read(im);
		InputCapsule ic=im.getCapsule(this);
		light=(Light)ic.readSavable(LIGHT_NAME,null);
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		super.write(ex);
		OutputCapsule oc=ex.getCapsule(this);
		oc.write(light,LIGHT_NAME,null);

	}
}
