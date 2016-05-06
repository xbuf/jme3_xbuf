package jme3_ext_xbuf.ext;

import java.util.List;

public class ListExt{
	public static <T> T valOf(List<T> values, int i, T vdef) {
		return values.size() > i ? values.get(i) : vdef;
	}

}
