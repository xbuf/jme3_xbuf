package jme3_ext_xbuf.mergers;

import java.util.Collection;
import java.util.LinkedList;

import com.jme3.scene.Node;

import jme3_ext_xbuf.XbufContext;
import jme3_ext_xbuf.mergers.relations.Linker;
import jme3_ext_xbuf.mergers.relations.RefData;
import jme3_ext_xbuf.mergers.relations.linkers.AnimationToSpatial;
import jme3_ext_xbuf.mergers.relations.linkers.CustomParamToSpatial;
import jme3_ext_xbuf.mergers.relations.linkers.GeometryToNode;
import jme3_ext_xbuf.mergers.relations.linkers.LightToGeometry;
import jme3_ext_xbuf.mergers.relations.linkers.MaterialToGeometry;
import jme3_ext_xbuf.mergers.relations.linkers.NodeToNode;
import jme3_ext_xbuf.mergers.relations.linkers.PhysicsToSpatial;
import jme3_ext_xbuf.mergers.relations.linkers.SkeletonToSpatial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xbuf.Datas.Data;
import xbuf.Relations.Relation;

@Slf4j
public class RelationsMerger implements Merger{
	protected @Getter final MaterialsMerger matMerger;
	protected @Getter final Collection<Linker> linkers;
	
	public RelationsMerger(MaterialsMerger mm){
		matMerger=mm;
		linkers=new LinkedList<Linker>();
		linkers.add(new AnimationToSpatial());
		linkers.add(new CustomParamToSpatial());
		linkers.add(new LightToGeometry());
		linkers.add(new MaterialToGeometry());
		linkers.add(new GeometryToNode());
		linkers.add(new SkeletonToSpatial());
		linkers.add(new NodeToNode());
		linkers.add(new PhysicsToSpatial());
	}

	public void apply(Data src, Node root, XbufContext components) {
		for(Relation r:src.getRelationsList()){
			merge(new RefData(r.getRef1(),r.getRef2(),src,root,components));
		} 
	}

	protected void merge(RefData data) {
		if(data.ref1.equals(data.ref2)){
			log.warn("can't link {} to itself",data.ref1);
			return;
		}
		boolean linked=false;
		String r1=data.ref1;
		String r2=data.ref2;
		// Linkers work with one relation per time, we want to process also linked generated relations, so we will do this:
		LinkedList<String> refs1=new LinkedList<String>();
		refs1.add(r1);
		refs1.addAll(data.context.linkedRefs(r1));
		
		LinkedList<String> refs2=new LinkedList<String>();
		refs2.add(r2);
		refs2.addAll(data.context.linkedRefs(r2));
		

		// Every possible combination
		for(String ref1:refs1){
			for(String ref2:refs2){
				data.ref1=ref1;
				data.ref2=ref2;
				for(Linker linker:linkers){
					if(linker.doLink(this,data)){
						linked=true;
						log.info("{} linked to {} with {}",data.ref1,data.ref2,linker.getClass());
						break;
					}
				}
			}
		}
		if(!linked) log.warn("can't link:   {} -- {}\n",data.ref1,data.ref2);
	}
}
