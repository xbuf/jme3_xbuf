package jme3_ext_animation;

import com.jme3.animation.AnimChannel
import com.jme3.animation.AnimControl
import com.jme3.animation.Track
import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.util.TempVars
import java.util.ArrayList
import java.util.List

class CompositeTrack implements Track {
	public val tracks = new ArrayList<Track>() as List<Track>

	override setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		for (Track t : tracks) {
			t.setTime(time, weight, control, channel, vars)
		}
	}

	override getLength() {
		var lg = 0f;
		for (Track t : tracks) {
			lg = Math.max(lg,  t.getLength())
		}
		lg
	}

	override clone() {
		val c = new CompositeTrack();
		for (Track t : tracks) {
			c.tracks.add(t.clone());
		}
		c
	}

	override write(JmeExporter ex) {
		val oc = ex.getCapsule(this);
		oc.write(tracks.toArray() as Track[], "tracks", null)
	}

	override read(JmeImporter im) {
		val ic = im.getCapsule(this);
		val ts = ic.readSavableArray("tracks", null) as Track[]
		if (ts != null) {
			tracks.addAll(ts)
		}
	}
}
