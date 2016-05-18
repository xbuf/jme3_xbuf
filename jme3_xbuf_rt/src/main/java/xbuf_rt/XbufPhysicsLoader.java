package xbuf_rt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.physicsloader.ConstraintData;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XbufPhysicsLoader{
	
	public static void load(XbufPhysicsLoaderSettings settings,Spatial root,PhysicsSpace space){
		Map<String,List<?>> constraints=new HashMap<String,List<?>>();
		
		// Extract constraint definitions
		root.depthFirstTraversal(s->{
			if(s.getName().equals("Xbuf::Constraint")){
				List<?> constraint=s.getUserData("Xbuf::Constraints::Data");
				String id=s.getUserData("Xbuf::Constraints::ID");

				constraints.put(id,constraint);
			}
			
			//Only rb support for now
			RigidBodyControl rb=s.getControl(RigidBodyControl.class);			
			if(rb!=null){ // Clear broken constraints...
				Collection<PhysicsJoint> joints=rb.getJoints();
				for(PhysicsJoint j:joints)rb.removeJoint(j);
			}
		});
		
		// Add scene to physpace
		space.addAll(root);
		
		
		Map<List<?>,List<Spatial>> constraintsXspatials=new HashMap<List<?>,List<Spatial>>();
		
		// Find constraintsXspatials associations
		root.depthFirstTraversal(s->{
			Collection<String> ctsr=getConstraints(s,false);
			List<Byte> ctsro=getConstraintsOrder(s,false);
			int i=0;
			for(String ctr:ctsr){
				List<?> ct_o=constraints.get(ctr);
				List<Spatial> spatials=constraintsXspatials.get(ct_o);
				if(spatials==null){
					spatials=new LinkedList<Spatial>();
					constraintsXspatials.put(ct_o,spatials);
					spatials.add(s);
				}else{
					if(ctsro.get(i)==0){ // Place in correct order.
						spatials.add(0,s);						
					}else{
						spatials.add(s);
					}
				}
				i++;
			}
		});
			
		log.debug("Found {} constraints",constraintsXspatials.size());
		// Finally apply constraints to rbs...
		constraintsXspatials.forEach((ct,spatials)->{
			if(spatials.size()>2){
				log.warn("Constraint with more or less than 2 spatials? EXTERMINATE!1!");
				return;
			}
			
			RigidBodyControl rb1=spatials.get(0).getControl(RigidBodyControl.class);			
			RigidBodyControl rb2=spatials.get(1).getControl(RigidBodyControl.class);			
			if(rb1==null||rb2==null){
				log.warn("Constraint not supported for this physics object.");
				return ;
			}
			
			Class<?> map[]=settings.getSupportedConstraints();
			try{
				byte data[]=new byte[ct.size()];
				for(int i=0;i<data.length;i++)data[i]=((Number)ct.get(i)).byteValue();
				ByteArrayInputStream bis=new ByteArrayInputStream(data);
				int cid=bis.read();
				if(cid>=0&&cid<data.length){
					Class<?> cl=map[cid];
					ConstraintData c=(ConstraintData)cl.newInstance();
					c.read(bis);			

					Object ctg=settings.getPhysicsLoader().loadConstraint(settings,rb1,rb2,c);
					// Only rb support for now
					if(!(ctg instanceof PhysicsJoint))log.warn("Constraint type not supported. {}",ctg.getClass());
					
					PhysicsJoint pj=(PhysicsJoint)ctg;

					space.add(pj);
				}else log.warn("Constraint type not supported. Id: {}",cid);
			}catch(Exception e){
				e.printStackTrace();
			}
		});
	}
	
	public static void removeConstraintsInScene(Spatial scene){
		scene.depthFirstTraversal(s->{
			if(s.getName().startsWith("Xbuf::Constraint"))s.removeFromParent();
			else{
				String[] ukeys=s.getUserDataKeys().toArray(new String[0]);
				for(String ukey:ukeys)if(ukey.startsWith("Xbuf::Constraints::"))s.setUserData(ukey,null);
			}
		});
	}
	public static Spatial storeConstraintInScene(XbufPhysicsLoaderSettings settings,Spatial s1,Spatial s2,ConstraintData constraint) throws IOException{		
		String id=constraint.hashCode()+"_"+System.currentTimeMillis();
		applyConstraintID(s1,id,true);
		applyConstraintID(s2,id,false);		
		
		Class<?> map[]=settings.getSupportedConstraints();
		byte cid=0;
		for(Class<?> c:map){
			if(constraint.getClass().isAssignableFrom(c)){
				break;
			}
			cid++;
		}
		
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		bos.write(cid);
		constraint.write(bos);   
	    byte data[]=bos.toByteArray();
	    bos.close();
	    
	    ArrayList<Byte> dataO=new ArrayList<Byte>();
	    for(int i =0;i<data.length;i++)dataO.add(data[i]);
	    
		Node constraint_node=new Node("Xbuf::Constraint");
		constraint_node.setUserData("Xbuf::Constraints::Data",dataO);
		constraint_node.setUserData("Xbuf::Constraints::ID",id);
		return constraint_node;
	}
	
	
	private static Collection<String>  getConstraints(Spatial s,boolean createMapIfRequired){
		Collection<String> constraints=s.getUserData("Xbuf::Constraints::Apply");
		if(constraints==null){
			constraints=new LinkedList<String>();
			if(createMapIfRequired)s.setUserData("Xbuf::Constraints::Apply",constraints);
		}
		return constraints;
	}
	
	private static List<Byte>  getConstraintsOrder(Spatial s,boolean createMapIfRequired){
		List<Byte> constraints=s.getUserData("Xbuf::Constraints::Order");
		if(constraints==null){
			constraints=new ArrayList<Byte>();
			if(createMapIfRequired)s.setUserData("Xbuf::Constraints::Order",constraints);
		}
		return constraints;
	}
	
	private static void applyConstraintID(Spatial s,String id,boolean first){
		getConstraints(s,true).add(id);
		getConstraintsOrder(s,true).add((byte)(first?0:1));
	}
	
}
