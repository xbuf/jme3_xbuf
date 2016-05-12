package jme3_ext_xbuf.ext;

import com.google.common.primitives.Ints;

import xbuf.Meshes.UintBuffer;

public class UintBufferExt{
	//TODO use an optim version: including a patch for no autoboxing : https://code.google.com/p/protobuf/issues/detail?id=464
	public static int[] array(UintBuffer src) {
		return Ints.toArray(src.getValuesList());
	}
}
