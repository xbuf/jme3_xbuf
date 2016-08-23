package jme3_ext_xbuf.ext;


import java.util.List;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import xbuf.Meshes.FloatBuffer;

public class FloatBufferExt{

	//	//TODO use an optim version: including a patch for no autoboxing : https://code.google.com/p/protobuf/issues/detail?id=464
	public static float[] array(FloatBuffer src) {
		List<Float> list=src.getValuesList();
		float arr[]=new float[list.size()];
		int i=0;
		for(Float f:list) {
			arr[i++]=(float)f;
		}
		return arr;
	}

	public static float[] arrayQuatMult(FloatBuffer src, float x, float y, float z) {
		List<Float> list=src.getValuesList();
		int nb = list.size() / 4;
		float arr[]=new float[nb * 3];
		Vector3f in = new Vector3f(x, y, z);
		Vector3f out = new Vector3f();
		Quaternion q = new Quaternion();
		for(int i=0; i < nb; i++){
			q.set(src.getValues(i*4 + 0), src.getValues(i*4 + 1), src.getValues(i*4 + 2), src.getValues(i*4 + 3));
			q.mult(in, out);
			arr[i*3 + 0]= out.x;
			arr[i*3 + 1]= out.y;
			arr[i*3 + 2]= out.z;
		}
		return arr;
	}
	
}
