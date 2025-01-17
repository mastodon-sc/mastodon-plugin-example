/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2023 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.example.detection;

import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_RADIUS;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_SETUP_ID;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mastodon.tracking.detection.AbstractDetectorOp;
import org.mastodon.tracking.detection.DetectionCreatorFactory;
import org.mastodon.tracking.detection.DetectionCreatorFactory.DetectionCreator;
import org.mastodon.tracking.detection.DetectionUtil;
import org.mastodon.tracking.detection.DetectorOp;
import org.scijava.plugin.Plugin;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

@Plugin( type = DetectorOp.class )
public class RandomSpotDetectorOp extends AbstractDetectorOp implements DetectorOp
{

	/**
	 * The key for the parameter that specifies how many spots to create with
	 * this detector, in each frame.
	 */
	public static final String KEY_N_SPOTS = "N_SPOTS";

	@Override
	public void mutate1( final DetectionCreatorFactory detectionCreatorFactory, final List< SourceAndConverter< ? > > sources )
	{
		/*
		 * The abstract class `AbstractDetectorOp` we inherit provides several
		 * useful fields that are used to store settings, communicate success or
		 * failure with an error message, or sends messages to the user
		 * interface.
		 * 
		 * The first one is the `ok` flag, that states whether the computation
		 * finished successfully. If not, a meaningful error message should be
		 * provided in the `errorMessage` field. The user interface will use
		 * them.
		 * 
		 * We start by settings the `ok` flag to false. If we break before the
		 * end, this will signal something wrong happened.
		 */
		ok = false;

		/*
		 * And we clear the status display.
		 */
		statusService.clearStatus();

		/*
		 * A. Read the settings map, and check validity.
		 * 
		 * Let's be a bit thorough with this part.
		 * 
		 * The `settings` variable (stored in the mother abstract class) is a
		 * `Map<String, Object>` that will be passed with all the settings the
		 * user will specify, either programmatically or in the wizard. For our
		 * dummy detector example, we have 5 parameters: 1. the number of spots
		 * we will create, 2. their radius, 3. with respect to what source of
		 * channel, 4. and 5. the min and max time-points we will process.
		 * 
		 * To check that they are present in the map and of the right class, we
		 * use a utility function defined in `LinkingUtils` that accepts the
		 * settings map, the key of the parameter to test, its desired class,
		 * and a holder to store error messages. It goes like this:
		 */
		final StringBuilder errorHolder = new StringBuilder();
		boolean good = true;
		good = good & checkParameter( settings, KEY_N_SPOTS, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_RADIUS, Double.class, errorHolder );
		good = good & checkParameter( settings, KEY_SETUP_ID, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_MIN_TIMEPOINT, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_MAX_TIMEPOINT, Integer.class, errorHolder );
		if ( !good )
		{
			errorMessage = errorHolder.toString();
			return;
		}
		// Now we are sure that they are here, and of the right class.

		final int n = ( int ) settings.get( KEY_N_SPOTS );
		final int minTimepoint = ( int ) settings.get( KEY_MIN_TIMEPOINT );
		final int maxTimepoint = ( int ) settings.get( KEY_MAX_TIMEPOINT );
		final int setup = ( int ) settings.get( KEY_SETUP_ID );
		final double radius = ( double ) settings.get( KEY_RADIUS );

		// Extra checks.
		if ( n < 1 )
		{
			errorMessage = "The parameter " + KEY_N_SPOTS + " has a value lower than 1: " + n;
			return;
		}
		if ( radius <= 0 )
		{
			errorMessage = "Radius is equal to or smaller than 0: " + radius;
		}
		if ( setup < 0 || setup >= sources.size() )
		{
			errorMessage = "The parameter " + KEY_SETUP_ID + " is not in the range of available sources ("
					+ sources.size() + "): " + setup;
			return;
		}
		if ( maxTimepoint < minTimepoint )
		{
			errorHolder.append( "Min time-point should smaller than or equal to max time-point, be was min = "
					+ minTimepoint + " and max = " + maxTimepoint + "\n" );
			return;
		}
		// Now we are sure they are valid.

		/*
		 * B. Loop over all time-points and create spots.
		 */

		final Random ran = new Random();

		// The `statusService` can be used to show short messages.
		statusService.showStatus( "Creating random spots." );
		for ( int tp = minTimepoint; tp <= maxTimepoint; tp++ )
		{
			// We use the `statusServive to show progress.
			statusService.showProgress( tp - minTimepoint + 1, maxTimepoint - minTimepoint + 1 );

			/*
			 * The detection process can be canceled. For instance, if the user
			 * clicks on the 'cancel' button, this class will be notified via
			 * the `isCanceled()` method.
			 * 
			 * You can check if the process has been canceled as you wish (you
			 * can even ignore it), but we recommend checking every time-point.
			 */
			if ( isCanceled() )
				break; // Exit but don't fail.

			/*
			 * Important: With the image data structure we use, some time-points
			 * may be devoid of a certain source. We need to test for this, and
			 * should it be the case, to skip the time-point.
			 * 
			 * Again, there is a utility function to do this:
			 */
			if ( !DetectionUtil.isPresent( sources, setup, tp ) )
				continue;

			/*
			 * Now here is the part specific to our dummy detector. First we get
			 * the source for the current channel (or setup) at the desired
			 * time-point. In BDV jargon, this is a source.
			 */
			final Source< ? > source = sources.get( setup ).getSpimSource();

			/*
			 * This source has possibly several resolution levels. And for your
			 * own real detector, it might be very interesting to work on a
			 * lower resolution (higher level). Check the DogDetectorOp code for
			 * instance. For us, we don't even care for pixels, we just want to
			 * have the image boundary from the highest resolution (level 0).
			 */
			final int level = 0;
			final RandomAccessibleInterval< ? > image = source.getSource( tp, level );
			/*
			 * This is the 3D image of the current time-point, specified
			 * channel. It always 3D. If the source is 2D, the 3rd dimension
			 * will have a size of 1.
			 */

			// The image bounds. It might be different for every time-point.
			final int[] mins = Intervals.minAsIntArray( image );
			final int[] maxs = Intervals.maxAsIntArray( image );

			// Now let's create N random points within these bounds.
			final List< double[] > points = new ArrayList<>( n );
			for ( int i = 0; i < n; i++ )
			{
				final double x = mins[ 0 ] + ran.nextDouble() * ( maxs[ 0 ] - mins[ 0 ] );
				final double y = mins[ 1 ] + ran.nextDouble() * ( maxs[ 1 ] - mins[ 1 ] );
				final double z = mins[ 2 ] + ran.nextDouble() * ( maxs[ 2 ] - mins[ 2 ] );
				final double[] pos = new double[] { x, y, z };
				points.add( pos );
			}

			/*
			 * We have now to create the `Spot` objects corresponding to these
			 * detections to the model.
			 * 
			 * To simplify doing so, the detection framework uses a factory,
			 * that can be configured elsewhere, and that provides facilities to
			 * add new spots in a safe way. It is the second argument of the
			 * `mutate1` method we implement. It has only one useful method,
			 * that creates a spot adder for the current time-point.
			 */
			final DetectionCreator spotAdder = detectionCreatorFactory.create( tp );
			/*
			 * This spot adder is actually important. It is the medium through
			 * which we offer *generalisability*.
			 * 
			 * Indeed, notice that we did not use any of the class specific to
			 * the Mamut application in this class: there is no mention of the
			 * `Spot` class nor to the `Model` class. Yet we will need to create
			 * `Spot` objects from the detections and add them to the `Model`
			 * instance of the data. This is what the `spotAdder` does.
			 * 
			 * The one we use in the Mamut application takes the detection,
			 * creates spots from them and add them to the model. We could have
			 * made another design devoid of the `spotAdder` and the
			 * `detectionCreatorFactory`, directly using the `Model` class. But
			 * we wanted to make it possible to reuse any of the detector we
			 * built for Mastodon with other application type.
			 * 
			 * For instance, if someone wants to make a new application with the
			 * Mastodon code, but that does not use the Mamut classes, they can
			 * reuse directly the all the code under the
			 * `org.mastodon.tracking.detection` package for detection. They
			 * will have to implement a `DetectionCreatorFactory` specific to
			 * their application, that will know how to add detections to their
			 * specific model. But the actual detector code can be reused as is
			 * with this design.
			 * 
			 * We already use the flexibility of this design in the Mamut
			 * application, with several implementations of the
			 * `DetectionCreatorFactory` interface. The "Advanced DoG detector"
			 * offers a special option to configure the behavior of the
			 * detector, when a spot is found where another already exists. The
			 * user can specify whether they want to add it on top of the
			 * existing one, skip adding the new spot, replace the existing one,
			 * or remove all existing spots before detection (the default). This
			 * is simply done with several implementations of
			 * `DetectionCreatorFactory` that know how to perform each of these
			 * behaviors.
			 */

			/*
			 * Also: the detections are have now are just points. Their position
			 * is stored in pixel units, in the reference frame of the source.
			 * But each source might be rotated, translated, etc. And this might
			 * change for every time-point and source. So before creating spots
			 * we need to transform these points in the global coordinate
			 * system.
			 * 
			 * This is done with an `AffineTransform3D`, also stored in the
			 * source. There is utility method to extract it for the current
			 * source, time-point and level:
			 */
			final AffineTransform3D transform = DetectionUtil.getTransform( sources, tp, setup, level );

			/*
			 * Coming back to the `spotAdder`. Adding spots must be done within
			 * a block calling the following methods in order:
			 */

			/**
			 * <pre>
			 * spotAdder.preAddition();
			 * try {
			 * 		// Detect objects and transform their position.
			 * 		...
			 * 		spotAdder.createDetection( pos, radius, quality );
			 * }
			 * finally
			 * {
			 * 		spotAdder.postAddition();
			 * }
			 * </pre>
			 */

			/*
			 * `preAddition()` and `postAddition()` contain any task that must
			 * be performed before and after adding detection to the model in
			 * batch. In our case this yields:
			 */
			spotAdder.preAddition();
			try
			{
				for ( final double[] point : points )
				{
					/*
					 * The transform "goes" from the pixel coordinate to the
					 * global coordinate system. We can get the "world"
					 * coordinates of our detection this way:
					 */
					final double[] worldCoords = new double[ 3 ];
					transform.apply( point, worldCoords );

					/*
					 * `worldCoords` now contains the coordinate of the
					 * detection, in physical units, in the global reference
					 * frame. We can now create the `Spot` object from these
					 * coordinates, using the `spotAdder` we created above. It
					 * requires a `quality` value, for which we use a dummy
					 * random value as well.
					 */
					final double quality = ran.nextDouble();
					spotAdder.createDetection( worldCoords, radius, quality );
				}
			}
			finally
			{
				spotAdder.postAddition();
			}
		}

		/*
		 * We are done! Gracefully exit, stating we are ok.
		 */
		ok = true;
	}
}
