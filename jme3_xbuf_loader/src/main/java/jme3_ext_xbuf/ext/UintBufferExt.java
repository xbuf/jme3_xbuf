package jme3_ext_xbuf.ext;

import java.util.List;


import xbuf.Meshes.UintBuffer;

public class UintBufferExt{
	//TODO use an optim version: including a patch for no autoboxing : https://code.google.com/p/protobuf/issues/detail?id=464
	public static int[] array(UintBuffer src) {
		List<Integer> list=src.getValuesList();
		int arr[]=new int[list.size()];
		int i=0;
		for(Integer f:list)	arr[i++]=(int)f;
		return arr;
	}
}
