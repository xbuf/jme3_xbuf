package jme3_ext_xbuf.animations;

import java.util.LinkedList;
import java.util.List;

import com.jme3.animation.Animation;
import com.jme3.animation.Skeleton;

import lombok.Data;

@Data
public class XbufAnimation{
	protected final String name;
	protected final float duration;
	protected final List<XbufTrack> tracks=new LinkedList<XbufTrack>();
	
	public Animation toJME(Skeleton sk){
		Animation anim=new Animation(getName(),getDuration());
		for(XbufTrack t:tracks)anim.addTrack(t.toJME(sk));
		return anim;
	}
}
