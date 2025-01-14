package org.mastodon.mamut.example.detection;
import static org.mastodon.mamut.example.detection.RandomSpotDetectorOp.KEY_N_SPOTS;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_RADIUS;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_SETUP_ID;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_RADIUS;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_SETUP_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.tracking.mamut.detection.AbstractSpotDetectorOp;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.viewer.SourceAndConverter;

@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "Random detector",
		description = "<html>"
				+ "This example detector generates a fixed number of spots at random "
				+ "locations."
				+ "<p>"
				+ "It is only used as an example to show how to implement a custom "
				+ "detector in Mastodon."
				+ "</html>" )
public class RandomSpotDetectionExampleMamut extends AbstractSpotDetectorOp
{

	@Override
	public void compute( final List< SourceAndConverter< ? > > sources, final ModelGraph graph )
	{
		exec( sources, graph, RandomSpotDetectorOp.class );
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > ds = new HashMap< String, Object >();
		ds.put( KEY_SETUP_ID, DEFAULT_SETUP_ID );
		ds.put( KEY_MIN_TIMEPOINT, DEFAULT_MIN_TIMEPOINT );
		ds.put( KEY_MAX_TIMEPOINT, DEFAULT_MAX_TIMEPOINT );
		ds.put( KEY_RADIUS, DEFAULT_RADIUS );
		ds.put( KEY_N_SPOTS, 30 );
		return ds;
	}
}
