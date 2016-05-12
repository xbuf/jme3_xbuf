package jme3_ext_xbuf.mergers;

import static jme3_ext_xbuf.Converters.cnv;

import org.slf4j.Logger;


import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.shader.VarType;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.Texture2D;

import jme3_ext_xbuf.XbufContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xbuf.Datas.Data;
import xbuf.Materials;
import xbuf.Primitives;
import xbuf.Primitives.Color;
import xbuf.Primitives.Texture2DInline;


@Slf4j
public class MaterialsMerger implements Merger{
	protected final AssetManager assetManager;
	protected @Setter @Getter Texture defaultTexture;
	protected @Setter @Getter Material defaultMaterial;


	public MaterialsMerger(AssetManager assetManager) {
		this.assetManager = assetManager;
		defaultTexture = newDefaultTexture();
		defaultMaterial = newDefaultMaterial();
	}
	
	public Material newDefaultMaterial() {
		Material m=new Material(assetManager,"MatDefs/MatCap.j3md");
		m.setTexture("DiffuseMap",assetManager.loadTexture("Textures/generator8.jpg"));
		m.setColor("Multiply_Color",ColorRGBA.Pink);
		m.setFloat("ChessSize",0.5f);
		m.setName("DEFAULT");
		return m;
	}

	public Texture newDefaultTexture() {
		Texture t=assetManager.loadTexture("Textures/debug_8_64.png");
		t.setWrap(WrapMode.Repeat);
		t.setMagFilter(MagFilter.Nearest);
		t.setMinFilter(MinFilter.NearestLinearMipMap);
		t.setAnisotropicFilter(2);
		return t;
	}

	public Material newMaterial(Materials.Material m) {
		boolean lightFamily=!m.getShadeless();
		String def=lightFamily?"Common/MatDefs/Light/Lighting.j3md":"Common/MatDefs/Misc/Unshaded.j3md";
		return new Material(assetManager,def);
	}

	public String findMaterialParamName(String[] names, VarType type, MaterialDef scope) {
		for(String name2:names){
			for(MatParam mp:scope.getMaterialParams()){
				if(mp.getVarType()==type&&mp.getName().equalsIgnoreCase(name2)){ return mp.getName(); }
			}
		}
		return null;
	}

	public void setColor(boolean has, Color src, Material dst, String[] names, MaterialDef scope) {
		if(has){
			String name=findMaterialParamName(names,VarType.Vector4,scope);
			if(name!=null){
				dst.setColor(name,cnv(src,new ColorRGBA()));
			}else{
				log.warn("can't find a matching name for : [{}] ({})",",",names,VarType.Vector4);
			}
		}
	}

	public void setBoolean(boolean has, Boolean src, Material dst, String[] names, MaterialDef scope) {
		if(has){
			String name=findMaterialParamName(names,VarType.Boolean,scope);
			if(name!=null){
				dst.setBoolean(name,src);
			}else{
				log.warn("can't find a matching name for : [{}] ({})",",",names,VarType.Vector4);
			}
		}
	}

	public Texture getValue(Primitives.Texture t) {
		Texture tex;
		switch(t.getDataCase()){
			case DATA_NOT_SET:
				tex=null;
				break;
			case RPATH:
				try{
					tex=assetManager.loadTexture(t.getRpath());
				}catch(AssetNotFoundException ex){
					log.warn("failed to load texture:",t.getRpath(),ex);
					tex=defaultTexture.clone();
				}
				break;
			case TEX2D:{
				Texture2DInline t2di=t.getTex2D();
				//TODO read ColorSpace from xbuf data
				Image img=new Image(getValue(t2di.getFormat()),t2di.getWidth(),t2di.getHeight(),t2di.getData().asReadOnlyByteBuffer(), ColorSpace.Linear);
				tex=new Texture2D(img);
				break;
			}
			default:
				throw new IllegalArgumentException("doesn't support more than texture format:"+t.getDataCase());
		}
		tex.setWrap(WrapMode.Repeat);
		return tex;
	}

	public Image.Format getValue(Texture2DInline.Format f) {
		switch(f){
			// case bgra8: return Image.Format.BGR8;
			case rgb8:
				return Image.Format.RGB8;
			case rgba8:
				return Image.Format.RGBA8;
			default:
				throw new UnsupportedOperationException("image format :"+f);
		}
	}

	public void setTexture2D(boolean has, Primitives.Texture src, Material dst, String[] names, MaterialDef scope) {
		if(has){
			String name=findMaterialParamName(names,VarType.Texture2D,scope);
			if(name!=null){
				dst.setTexture(name,getValue(src));
			}else{
				log.warn("can't find a matching name for : [{}] ({})",",",names,VarType.Texture2D);
			}
		}
	}

	public void setFloat(boolean has, float src, Material dst, String[] names, MaterialDef scope) {
		if(has){
			String name=findMaterialParamName(names,VarType.Float,scope);
			if(name!=null){
				dst.setFloat(name,src);
			}else{
				log.warn("can't find a matching name for : [{}] ({})",",",names,VarType.Float);
			}
		}
	}

	public Material mergeToMaterial(Materials.Material src, Material dst) {
		MaterialDef md=dst.getMaterialDef();
		setColor(src.hasColor(),src.getColor(),dst,new String[]{"Color","Diffuse"},md);
		setTexture2D(src.hasColorMap(),src.getColorMap(),dst,new String[]{"ColorMap","DiffuseMap"},md);
		// setTexture2D(src.hasNormalMap(), src.getNormalMap(), dst, new String[]{"ColorMap", "DiffuseMap"], md, log)
		setFloat(src.hasOpacity(),src.getOpacity(),dst,new String[]{"Alpha","Opacity"},md);
		setTexture2D(src.hasOpacityMap(),src.getOpacityMap(),dst,new String[]{"AlphaMap","OpacityMap"},md);
		setTexture2D(src.hasNormalMap(),src.getNormalMap(),dst,new String[]{"NormalMap"},md);
		setFloat(src.hasRoughness(),src.getRoughness(),dst,new String[]{"Roughness"},md);
		setTexture2D(src.hasRoughnessMap(),src.getRoughnessMap(),dst,new String[]{"RoughnessMap"},md);
		setFloat(src.hasMetalness(),src.getMetalness(),dst,new String[]{"Metalness"},md);
		setTexture2D(src.hasMetalnessMap(),src.getMetalnessMap(),dst,new String[]{"MetalnessMap"},md);
		setColor(src.hasSpecular(),src.getSpecular(),dst,new String[]{"Specular"},md);
		setTexture2D(src.hasSpecularMap(),src.getSpecularMap(),dst,new String[]{"SpecularMap"},md);
		setFloat(src.hasSpecularPower(),src.getSpecularPower(),dst,new String[]{"SpecularPower","Shininess"},md);
		setTexture2D(src.hasSpecularPowerMap(),src.getSpecularPowerMap(),dst,new String[]{"SpecularPowerMap","ShininessMap"},md);
		setColor(src.hasEmission(),src.getEmission(),dst,new String[]{"Emission","GlowColor"},md);
		setTexture2D(src.hasEmissionMap(),src.getEmissionMap(),dst,new String[]{"EmissionMap","GlowMap"},md);
		if(!src.getShadeless()){
			if(!src.hasColorMap()){
				if(src.hasColor()){
					setBoolean(true,true,dst,new String[]{"UseMaterialColors"},md);
				}else{
					setBoolean(true,true,dst,new String[]{"UseVertexColor"},md);
				}
			}
		}
		return dst;
	}

	public void apply(Data src, Node root, XbufContext context) {
		src.getMaterialsList().stream().forEach(m -> {
			String id=m.getId();
			Material mat=newMaterial(m);
			context.put(id,mat);
			mat.setName(m.hasName()?m.getName():m.getId());
			mergeToMaterial(m,mat);
//			materialReplicator.syncReplicas(mat);
		});

	}


}
