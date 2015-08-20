package sandbox;

import jme3_ext_xbuf.XbufLoader;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

public class SimpleXbuf  extends SimpleApplication {

    public static void main(String... argv){
        SimpleXbuf app = new SimpleXbuf();
        app.start();
    }



    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(XbufLoader.class, "xbuf");
        Spatial spatial = assetManager.loadModel("Models/xxx.xbuf");
        rootNode.attachChild(spatial);
        setupLights();
    }

    public void setupLights() {
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        rootNode.addLight(al);
    }

}
