package jme3_ext_remote_editor;

import com.jme3.renderer.Camera
import com.jme3.scene.CameraNode
import com.jme3.scene.Node
import java.util.Map
import java.util.TreeMap

public class RemoteCtx {
	public val root = new Node("remoteRootNode");
	public val cam = new CameraNode("eye", null as Camera)
	public val components = new TreeMap<String, Object>() as Map<String, Object>
	public val view = new SceneProcessorCaptureToBGRA()

	new() {
		cam.setEnabled(false);
		root.attachChild(cam);
	}
}
