package jme3_ext_assettools;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.scene.plugins.MTLLoader;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.util.PlaceholderAssets;

public class MTLoaderExt extends MTLLoader {
	private static final Logger logger = Logger.getLogger(MTLLoader.class.getName());
	public static String[] preferedExtension = new String[]{"dds", "png", "tga", "jpg"};

	protected Texture loadTexture(String path){
		String[] split = path.trim().split("\\p{javaWhitespace}+");

		// will crash if path is an empty string
		path = split[split.length-1];

		String name = path.replace('\\', '/');//.replace('/', File.separatorChar);
		TextureKey texKey = null;
		Texture texture = null;
		String basename = name.substring(0, name.lastIndexOf('.') +1);
		for (int i = 0; i < preferedExtension.length && texture == null; i++) {
			try {
				texKey = new TextureKey(folderName + basename + preferedExtension[i]);
				texKey.setGenerateMips(true);
				texture = assetManager.loadTexture(texKey);
				texture.setWrap(WrapMode.Repeat);
			} catch (AssetNotFoundException ex){
				try {
					texKey = new TextureKey(folderName + basename.toLowerCase() + preferedExtension[i]);
					texKey.setGenerateMips(true);
					texture = assetManager.loadTexture(texKey);
					texture.setWrap(WrapMode.Repeat);
				}catch (AssetNotFoundException ex2){}
//			} catch(Exception exc){
//				throw new RuntimeException("wrap ! for texKey : " + texKey + " .. " + (folderName + basename + preferedExtension[i]) + ".." + name + ".." + path, exc);
			}

		}
		if (texture == null) {
			try {
				texKey = new TextureKey(folderName + name);
				texKey.setGenerateMips(true);
				texture = assetManager.loadTexture(texKey);
				texture.setWrap(WrapMode.Repeat);
			} catch (AssetNotFoundException ex){
				try {
					texKey = new TextureKey(folderName + name.toLowerCase());
					texKey.setGenerateMips(true);
					texture = assetManager.loadTexture(texKey);
					texture.setWrap(WrapMode.Repeat);
				}catch (AssetNotFoundException ex2){}
			}
		}
		if (texture == null) {
			StringBuilder l = new StringBuilder();
			for (int i = 0; i < preferedExtension.length; i++) l.append(preferedExtension[i]).append(","); //String.join(",", preferedExtension)
			logger.log(Level.WARNING, "Cannot locate {0} for material {1}, try extensions : {2}", new Object[]{texKey, key, l.toString()});
			texture = new Texture2D(PlaceholderAssets.getPlaceholderImage());
			texture.setWrap(WrapMode.Repeat);
			texture.setKey(texKey);
		}
		return texture;
	}
}
