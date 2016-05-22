package jme3_ext_xbuf.mergers;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;

import jme3_ext_xbuf.Merger;
import jme3_ext_xbuf.XbufContext;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import xbuf.Datas.Data;
import xbuf.Lights;

@ExtensionMethod({jme3_ext_xbuf.ext.PrimitiveExt.class})
@Slf4j
public class LightsMerger implements Merger{

	public void apply(Data src, Node root, XbufContext context) {
		for(xbuf.Lights.Light srcl:src.getLightsList()){
			// TODO manage parent hierarchy
			String id=srcl.getId();
			Light light=context.get(id);
			if(light==null){
				light=makeLight(srcl);
				context.put(id,light);
			}

			if(srcl.hasColor()){
				light.setColor(srcl.getColor().toJME().mult(srcl.getIntensity()));
			}

			// TODO manage attenuation
			// TODO manage conversion of type
			switch(srcl.getKind()){
				case spot:{
					SpotLight l=(SpotLight)light;
					if(srcl.hasSpotAngle()){
						float max=srcl.getSpotAngle().getMax();
						switch(srcl.getSpotAngle().getCurveCase()){
							case CURVE_NOT_SET:
								l.setSpotOuterAngle(max);
								l.setSpotInnerAngle(max);
								break;
							case LINEAR:
								l.setSpotOuterAngle(max*srcl.getSpotAngle().getLinear().getEnd());
								l.setSpotInnerAngle(max*srcl.getSpotAngle().getLinear().getBegin());
								break;
							default:{
								l.setSpotOuterAngle(max);
								l.setSpotInnerAngle(max);
								log.warn("doesn't support curve like {} for spot_angle",srcl.getSpotAngle().getCurveCase());
							}
						}
					}
					if(srcl.hasRadialDistance()){
						l.setSpotRange(srcl.getRadialDistance().getMax());
					}
					break;
				}
				case point:{
					PointLight l=(PointLight)light;
					if(srcl.hasRadialDistance()){
						float max=srcl.getRadialDistance().getMax();
						switch(srcl.getRadialDistance().getCurveCase()){
							case CURVE_NOT_SET:{
								l.setRadius(max);
								break;
							}
							case LINEAR:{
								l.setRadius(max*srcl.getSpotAngle().getLinear().getEnd());
								break;
							}
							case SMOOTH:{
								l.setRadius(max*srcl.getSpotAngle().getSmooth().getEnd());
								break;
							}
							default:{
								l.setRadius(max);
								log.warn("doesn't support curve like {} for spot_angle",srcl.getSpotAngle().getCurveCase());
							}
						}
					}
					break;
				}
				case ambient:{
					break;
				}
				case directional:{
					break;
				}
			}
		}
	}

	private Light makeLight(Lights.Light srcl) {
		Light l0=null;
		switch(srcl.getKind()){
			case ambient:
				l0=new AmbientLight();
				break;
			case directional:
				l0=new DirectionalLight();
				break;
			case spot:{
				SpotLight l=new SpotLight();
				l.setSpotRange(1000);
				l.setSpotInnerAngle(5f*FastMath.DEG_TO_RAD);
				l.setSpotOuterAngle(10f*FastMath.DEG_TO_RAD);
				l0=l;
				break;
			}
			case point:
				l0=new PointLight();
				break;
		}
		l0.setColor(ColorRGBA.White.mult(2));
		l0.setName(srcl.hasName()?srcl.getName():srcl.getId());
		return l0;
	}

}
