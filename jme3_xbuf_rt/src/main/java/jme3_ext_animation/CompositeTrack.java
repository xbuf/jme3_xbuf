package jme3_ext_animation;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Track;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.util.TempVars;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompositeTrack implements Track {
	public final List<Track> tracks = new ArrayList<>();

	@Override
	public void setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {
		for (Track t : tracks) {
			t.setTime(time, weight, control, channel, vars);
		}
	}

	@Override
	public float getLength() {
		float lg = 0f;
		for (Track t : tracks) {
			lg = Math.max(lg,  t.getLength());
		}
		return lg;
	}

	@Override
	public CompositeTrack clone() {
		CompositeTrack c = new CompositeTrack();
		for (Track t : tracks) {
			c.tracks.add(t.clone());
		}
		return c;
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write((Track[])tracks.toArray(), "tracks", null);
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		Track[] ts = (Track[]) ic.readSavableArray("tracks", null);
		if (ts != null) {
			System.arraycopy(ts, 0, tracks, 0, ts.length);
		}
	}
	
	@Override
	public float[] getKeyFrameTimes() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
}
