package jme3_ext_xbuf.ext;

import com.google.common.primitives.Floats;

import xbuf.Meshes.FloatBuffer;

public class FloatBufferExt{

	//	//TODO use an optim version: including a patch for no autoboxing : https://code.google.com/p/protobuf/issues/detail?id=464
	public static float[] array(FloatBuffer src) {
		return Floats.toArray(src.getValuesList());
	}


}
