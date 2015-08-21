package sandbox;

import jme3_ext_remote_editor.AppState4RemoteCommand;
import jme3_ext_xbuf.Xbuf;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;

public class SimpleViewer  extends SimpleApplication {
    SimpleViewer app = this;
    
    public static void main(String... argv){
        SimpleViewer app = new SimpleViewer();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setupLights();
        
        app.setPauseOnLostFocus(false); //<-- Required else remote application will not receive image (eg: blender freeze)
        app.getStateManager().attach(new AppState4RemoteCommand(4242, new Xbuf(app.getAssetManager())));
    }

    public void setupLights() {
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1));
        rootNode.addLight(al);
    }

}
