package jme3_ext_xbuf

import com.jme3.asset.AssetManager
import com.jme3.asset.AssetNotFoundException
import com.jme3.material.MatParam
import com.jme3.material.Material
import com.jme3.material.MaterialDef
import com.jme3.math.ColorRGBA
import com.jme3.shader.VarType
import com.jme3.texture.Image
import com.jme3.texture.Texture
import com.jme3.texture.Texture.MagFilter
import com.jme3.texture.Texture.MinFilter
import com.jme3.texture.Texture.WrapMode
import com.jme3.texture.Texture2D
import java.util.Map
import org.slf4j.Logger
import xbuf.Datas.Data
import xbuf.Materials
import xbuf.Primitives
import xbuf.Primitives.Color
import xbuf.Primitives.Texture2DInline

import static jme3_ext_xbuf.Converters.*

import static extension java.lang.String.*

class Loader4Materials {
	val AssetManager assetManager
	val MaterialReplicator materialReplicator
	val Texture  defaultTexture
	val Material defaultMaterial

	new(AssetManager assetManager, MaterialReplicator materialReplicator) {
		this.assetManager = assetManager
		this.materialReplicator = materialReplicator
		defaultTexture = newDefaultTexture()
		defaultMaterial = newDefaultMaterial()
	}
		
	def Material newDefaultMaterial() {
        val mat = new Material(assetManager, "MatDefs/MatCap.j3md")
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/generator8.jpg"))
        mat.setColor("Multiply_Color", ColorRGBA.Pink)
        mat.setFloat("ChessSize", 0.5f)
        mat
	}

	protected def Texture newDefaultTexture() {
		val tex = assetManager.loadTexture("Textures/debug_8_64.png")
		tex.wrap = WrapMode.Repeat
		tex.magFilter = MagFilter.Nearest
		tex.minFilter = MinFilter.NearestLinearMipMap
		tex.anisotropicFilter = 2
		tex
	}

	def mergeMaterials(Data src, Map<String, Object> components, Logger log) {
		for(m : src.materialsList) {
			//TODO update / remove previous material
			val id = m.getId()
			//val mat = components.get(id) as Material
			//if (mat == null) {
				val mat = newMaterial(m, log)
				components.put(id, mat)
			//}
			mat.setName(if (m.hasName()) m.getName() else m.getId())
			mergeToMaterial(m, mat, log)
			materialReplicator.syncReplicas(mat)
		}
	}

	def Image.Format getValue(Texture2DInline.Format f, Logger log) {
		switch(f){
			//case bgra8: return Image.Format.BGR8;
			case rgb8: Image.Format.RGB8
			case rgba8: Image.Format.RGBA8
			default: throw new UnsupportedOperationException("image format :" + f)
		}
	}


	def Material newMaterial(Materials.Material m, Logger log) {
		val lightFamily = !m.getShadeless()
		val def = if (lightFamily) "Common/MatDefs/Light/Lighting.j3md" else "Common/MatDefs/Misc/Unshaded.j3md"
		val mat = new Material(assetManager, def)
		mat
	}

	def Material mergeToMaterial(Materials.Material src, Material dst, Logger log) {
		val md = dst.getMaterialDef()
		setColor(src.hasColor(), src.getColor(), dst, #["Color", "Diffuse"], md, log)
		setTexture2D(src.hasColorMap(), src.getColorMap(), dst, #["ColorMap", "DiffuseMap"], md, log)
		//setTexture2D(src.hasNormalMap(), src.getNormalMap(), dst, new String[]{"ColorMap", "DiffuseMap"], md, log)
		setFloat(src.hasOpacity(), src.getOpacity(), dst, #["Alpha", "Opacity"], md, log)
		setTexture2D(src.hasOpacityMap(), src.getOpacityMap(), dst, #["AlphaMap", "OpacityMap"], md, log)
		setTexture2D(src.hasNormalMap(), src.getNormalMap(), dst, #["NormalMap"], md, log)
		setFloat(src.hasRoughness(), src.getRoughness(), dst, #["Roughness"], md, log)
		setTexture2D(src.hasRoughnessMap(), src.getRoughnessMap(), dst, #["RoughnessMap"], md, log)
		setFloat(src.hasMetalness(), src.getMetalness(), dst, #["Metalness"], md, log)
		setTexture2D(src.hasMetalnessMap(), src.getMetalnessMap(), dst, #["MetalnessMap"], md, log)
		setColor(src.hasSpecular(), src.getSpecular(), dst, #["Specular"], md, log)
		setTexture2D(src.hasSpecularMap(), src.getSpecularMap(), dst, #["SpecularMap"], md, log)
		setFloat(src.hasSpecularPower(), src.getSpecularPower(), dst, #["SpecularPower", "Shininess"], md, log)
		setTexture2D(src.hasSpecularPowerMap(), src.getSpecularPowerMap(), dst, #["SpecularPowerMap", "ShininessMap"], md, log)
		setColor(src.hasEmission(), src.getEmission(), dst, #["Emission", "GlowColor"], md, log)
		setTexture2D(src.hasEmissionMap(), src.getEmissionMap(), dst, #["EmissionMap", "GlowMap"], md, log)
		if (!src.getShadeless()) {
    		if (!src.hasColorMap) {
    		    if (src.hasColor) {
    		        dst.setBoolean("UseMaterialColors", true)
    		    } else {
    		        dst.setBoolean("UseVertexColor", true)
    		    }
    		}
		}
		dst
	}

	def setColor(boolean has, Color src, Material dst, String[] names, MaterialDef scope, Logger log){
		if (has) {
			val name = findMaterialParamName(names, VarType.Vector4, scope, log)
			if (name != null) {
				dst.setColor(name, cnv(src, new ColorRGBA()))
			} else {
				log.warn("can't find a matching name for : [{}] ({})", ",".join(names), VarType.Vector4)
			}
		}
	}

	def setTexture2D(boolean has, Primitives.Texture src, Material dst, String[] names, MaterialDef scope, Logger log){
		if (has) {
			val name = findMaterialParamName(names, VarType.Texture2D, scope, log)
			if (name != null) {
				dst.setTexture(name, getValue(src, log))
			} else {
				log.warn("can't find a matching name for : [{}] ({})", ",".join(names), VarType.Texture2D)
			}
		}
	}

	def setFloat(boolean has, float src, Material dst, String[] names, MaterialDef scope, Logger log){
		if (has) {
			val name = findMaterialParamName(names, VarType.Float, scope, log)
			if (name != null) {
				dst.setFloat(name, src)
			} else {
				log.warn("can't find a matching name for : [{}] ({})", ",".join(names), VarType.Float)
			}
		}
	}

	def String findMaterialParamName(String[] names, VarType type, MaterialDef scope, Logger log) {
		for(String name2 : names){
			for(MatParam mp : scope.getMaterialParams()) {
				if (mp.getName().equalsIgnoreCase(name2) && mp.getVarType() == type) {
					return mp.getName()
				}
			}
		}
		null
	}
	
	def Texture getValue(Primitives.Texture t, Logger log) {
		val tex = switch(t.getDataCase()){
			case DATA_NOT_SET: null
			case RPATH:
				try {
					assetManager.loadTexture(t.rpath)
				} catch(AssetNotFoundException ex) {
					log.warn("failed to load texture:", t.rpath, ex)
					 defaultTexture.clone()
				}
			case TEX2D: {
				val t2di = t.getTex2D()
				val img = new Image(getValue(t2di.getFormat(), log), t2di.getWidth(), t2di.getHeight(), t2di.getData().asReadOnlyByteBuffer());
				new Texture2D(img)
			}
			default:
				throw new IllegalArgumentException("doesn't support more than texture format:" + t.getDataCase())
		}
		tex.wrap = WrapMode.Repeat
		tex
	}
}