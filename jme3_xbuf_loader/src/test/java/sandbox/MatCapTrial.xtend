package sandbox

import com.jme3.app.SimpleApplication
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Sphere

import static sandbox.MatCapTrial.*

class MatCapTrial {

    static def void main(String[] args) {
        val app = new SimpleApplication(){
            override simpleInitApp() {
            }
        }
        app.start();
        setupScene(app)
        setupCamera(app)
    }

    static def setupScene(SimpleApplication app) {
        app.enqueue[
            //val shape = new Box(1, 1, 1)
            val shape = new Sphere(16, 16, 1)
            val geom = new Geometry("shape", shape)
    
            val mat = new Material(app.assetManager, "MatDefs/MatCap.j3md")
            mat.setTexture("DiffuseMap", app.assetManager.loadTexture("Textures/generator8.jpg"))
            mat.setColor("Multiply_Color", ColorRGBA.Pink)
            mat.setFloat("ChessSize", 0.5f)
            geom.setMaterial(mat)
    
            app.rootNode.attachChild(geom)
        ]
    }
    
     static def setupCamera(SimpleApplication app) {
        app.enqueue[
            app.getFlyByCamera().setEnabled(true)
            app.getFlyByCamera().setDragToRotate(true)
            app.getFlyByCamera().setMoveSpeed(4f)
            //app.getStateManager().detach(app.getStateManager().getState(FlyCamAppState.class))
            app.getCamera().setLocation(new Vector3f(-5f,5f,5f))
            app.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Y)
            app.getInputManager().setCursorVisible(true)
            null
        ]
    }
}