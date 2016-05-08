package jme3_ext_xbuf.relations;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;

import com.jme3.scene.Node;

import jme3_ext_xbuf.Loader4Materials;
import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.relations.linkers.AnimationToSpatial;
import jme3_ext_xbuf.relations.linkers.CustomParamToSpatial;
import jme3_ext_xbuf.relations.linkers.GeometryToNode;
import jme3_ext_xbuf.relations.linkers.LightToGeometry;
import jme3_ext_xbuf.relations.linkers.MaterialToGeometry;
import jme3_ext_xbuf.relations.linkers.NodeToNode;
import jme3_ext_xbuf.relations.linkers.SkeletonToSpatial;
import lombok.RequiredArgsConstructor;
import xbuf.Datas.Data;
import xbuf.Relations.Relation;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class Loader4Relations{
	public final Loader4Materials loader4Materials;

	protected final Collection<Linker> linkers=new LinkedList<Linker>(){
		{
			add(new AnimationToSpatial());
			add(new CustomParamToSpatial());
			add(new LightToGeometry());
			add(new MaterialToGeometry());
			add(new GeometryToNode());
			add(new SkeletonToSpatial());
			add(new NodeToNode());
		}
	};

	public void merge(Data src, Node root, XbufContext components, Logger log) {
		for(Relation r:src.getRelationsList()){
			merge(new MergeData(r.getRef1(),r.getRef2(),src,root,components),log);
		} 
	}

	public void merge(MergeData data, Logger log) {
		if(data.ref1.equals(data.ref2)){
			log.warn("can't link {} to itself",data.ref1);
			return;
		}
		boolean linked=false;
		String r1=data.ref1;
		String r2=data.ref2;
		// Linkers work with one relation per time, we want to process also linked generated relations, so we will do this:
		LinkedList<String> refs1=new LinkedList<String>(){{
				add(r1);
				addAll(data.context.linkedRefs(r1));
		}};
		LinkedList<String> refs2=new LinkedList<String>(){{
				add(r2);
				addAll(data.context.linkedRefs(r2));
		}};

		// Every possible combination
		for(String ref1:refs1){
			for(String ref2:refs2){
				data.ref1=ref1;
				data.ref2=ref2;
				for(Linker linker:linkers){
					if(linker.doLink(this,data,log)){
						linked=true;
						log.debug("{} linked to {} with {}",new Object[]{data.ref1,data.ref2,linker.getClass()});
						break;
					}
				}
			}
		}
		if(!linked) log.warn("can't link:   {} -- {}\n",new Object[]{data.ref1,data.ref2});
	}
}
