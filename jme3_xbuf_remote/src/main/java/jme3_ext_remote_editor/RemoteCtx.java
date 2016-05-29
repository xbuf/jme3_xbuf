package jme3_ext_remote_editor;

import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;

import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.XbufKey;

public class RemoteCtx {
	public Node root = new Node("remoteRootNode");
	public CameraNode cam = new CameraNode("eye", (Camera)null);
	public XbufContext components = new XbufContext();
	public SceneProcessorCaptureToBGRA view = new SceneProcessorCaptureToBGRA();

	public RemoteCtx() {
		cam.setEnabled(false);
		root.attachChild(cam);
		XbufKey settings = new XbufKey("RemoteCtx")
				.useLightControls(true)
				;
		components.setSettings(settings);
	}
}
