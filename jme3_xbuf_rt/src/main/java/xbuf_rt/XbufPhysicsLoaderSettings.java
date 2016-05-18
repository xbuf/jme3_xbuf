package xbuf_rt;

import com.jme3.physicsloader.PhysicsLoaderSettings;

public interface XbufPhysicsLoaderSettings extends PhysicsLoaderSettings{
	public Class<?>[] getSupportedConstraints();
}
