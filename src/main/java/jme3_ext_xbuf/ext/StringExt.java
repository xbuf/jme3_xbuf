package jme3_ext_xbuf.ext;

public class StringExt{
	public static String join(String sep, String[] values) {
		StringBuilder s=new StringBuilder();
		for(String v:values){
			if(s.length()!=0) s.append(sep);
			s.append(v);
		}
		return s.toString();
	}
}
