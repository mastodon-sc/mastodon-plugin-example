package org.mastodon.mamut.example.detection;

import java.util.List;

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
}
