package jme3_ext_remote_editor;

import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import java.util.Map;
import java.util.TreeMap;

public class RemoteCtx {
	public Node root = new Node("remoteRootNode");
	public CameraNode cam = new CameraNode("eye", (Camera)null);
	public Map<String, Object> components = new TreeMap<String, Object>();
	public SceneProcessorCaptureToBGRA view = new SceneProcessorCaptureToBGRA();

	public RemoteCtx() {
		cam.setEnabled(false);
		root.attachChild(cam);
	}
}
