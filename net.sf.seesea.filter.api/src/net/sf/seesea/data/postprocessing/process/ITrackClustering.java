package net.sf.seesea.data.postprocessing.process;

import java.util.List;

import net.sf.seesea.track.api.data.ITrackFile;

public interface ITrackClustering {

	TrackClusterResult classifyTracks(List<ITrackFile> orderedFiles) ;

}
