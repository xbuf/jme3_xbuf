package jme3_ext_xbuf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class XbufContext {
//	public Logger log=LoggerFactory.getLogger("Xbuf");
	private Map<String,Object> storage=new HashMap<String,Object> ();
	private Map<String,List<String>> links=new HashMap<String,List<String>>();
	private XbufKey settings;
	
	public synchronized void setSettings(XbufKey s){
		settings=s;
	}
	
	public synchronized XbufKey getSettings(){
		return settings;
	}
	
	public synchronized <T> T get(String ref){
		return (T)storage.get(ref);
	}
	
		
	public synchronized String idOf(Object val){
		for(Entry<String,Object> entry:storage.entrySet()){
			if(entry.getValue()==val)return entry.getKey();
		}
		return null;
	}
	
	public synchronized <T> T put(String ref,Object val){
		T t=(T)storage.put(ref,val);
		return t;
	}
	
	public synchronized <T> T put(String ref,Object val,String link_to){
		T out=put(ref,val);
		linkedRefs(link_to).add(ref);
		return out;		
	}
	

	public synchronized <T> T removeWithLinks(String ref){
		List<String> ls=links.get(ref);
		T out= (T)storage.remove(ref);
		for(String l:ls)remove(l);
		return out;
	}
	
	public synchronized <T> T remove(String ref){
		links.remove(ref);
		return (T) storage.remove(ref);
	}

	public synchronized List<String> linkedRefs(String ref) {
		List<String> linked=links.get(ref);
		if(linked==null){
			linked=new LinkedList<String>();
			links.put(ref,linked);
		}
		return linked;
	}

	
	public synchronized String toString(){
		StringBuilder sb=new StringBuilder();
		LinkedList<String> ignore=new LinkedList<String>();
		storage.forEach((k,v)->{
			if(!ignore.contains(k)){
				sb.append("$ ").append(k).append(" = ").append(v.getClass()).append("(").append(v.hashCode()).append(")\n");
				List<String> linked =linkedRefs(k);
				for(String l:linked){
					v=get(l);
					sb.append("$ ----| ").append(l).append(" = ").append(v.getClass()).append("(");
					try{
						sb.append(v.toString());
					}catch(Throwable t){
						sb.append(v.hashCode());
					}
					sb.append(")\n");			
				}
				ignore.addAll(linked);
			}			
		});
		return sb.toString();
	}

}
