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
package org.mastodon.mamut.example.feature;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

/*
 * In this example we show an example of a simple custom numerical feature in 
 * Mastodon. We take the simple example of returning the angle of a link in
 * the XY plane.
 * 
 * As an introduction, we would like to give some context about the feature 
 * system in Mastodon, unrelated to the programming tutorial, but maybe 
 * useful to understand the complexity we will deal with, and apologize a 
 * bit for it. TrackMate and MaMuT - our previous works and the most similar 
 * to Mastodon - were focused on tracking. But we already had recognized 
 * the utility of 'numerical features' for basic analysis and exploration, 
 * and the ability to extend analysis by creating and adding new ones  
 * to these platforms. In TrackMate and MaMuT, the numerical feature 
 * system has some strong limitations: you could only have features 
 * that returned one scalar floating point number for a data item 
 * (spot or link). In Mastodon we wanted to remove this limitation
 * and have features returning any kind of objects, numbers, matrices
 * or text. We also wanted to avoid enforcing a specific implementation.
 * Finally, we wanted to have for Mastodon a reasonable approach when dealing 
 * with numerical feature for a very large number of data items. The price to 
 * pay for this generality is the length and verbosity of the code required 
 * to implement a feature. Now that we want to implement our own feature in 
 * Mastodon, we have to pay this price.
 * 
 * Somewhat fortunately, we also recognized this difficulty, and created a 
 * few facilities to accelerate feature development for certain cases.
 * For instance, in this example we want to develop a simple feature, that
 * returns the angle of a link in a XY plane, giving the instantaneous
 * direction of displacement for a cell. For this we simply need a feature
 * that returns a scalar real number for each link. We don't even need to
 * store them, and can compute it on the fly, avoiding the need to create 
 * a feature computer and storing the values it returns. We will start with
 * this simple case, that allows for introducing the main concepts of the 
 * numerical feature system. 
 * 
 * This class is heavily commented with many details. We tried to arrange 
 * methods and comments so that it can be read from top to bottom without 
 * jumping through the class to understand it. Let's go.
 */

/*
 * A Mastodon feature is class that associates a value (numerical or 
 * not) to a data item (a spot or a link). Such classes implement the
 * 'org.mastodon.feature.Feature' interface, with a type that specifies
 * what data item they are defined for. Because we want to compute 
 * something on links, our feature implements 'Feature < Link >'.
 */
public class LinkXYAngleExampleFeature implements Feature< Link >
{

	/*
	 * We start by defining a few constants, that are used to declare the
	 * 'signature' or specifications of our feature: its name, what type of
	 * values it returns, what is their units and dimension, etc.
	 */

	/*
	 * Our feature needs a name, and we will use it repeatedly. So we declare it
	 * now in a constant. The feature name also serves as a unique key, hence
	 * the name of the constant. It is a good idea to use something meaningful
	 * to the users.
	 */
	private static final String KEY = "Link angle in XY plane";

	/*
	 * We also have a means to pass some information to the user about what a
	 * feature is, and we also store this in a constant.
	 */
	public static final String INFO_STRING = "Example feature that computes the "
			+ "angle of a link in the XY plane.";

	/*
	 * Before going on, we need to speak a little bit about the notion of
	 * 'projections'.
	 * 
	 * Since a feature can return any type of data, we needed a way to make use
	 * of them in Mastodon in some specific situations, for instance to display
	 * feature values in a table, or use them in a color mode. For this we use
	 * *projections*. A feature can return a value of any type, but we ask it to
	 * be 'decomposable' into scalar, real projections. A feature projection
	 * returns a scalar real number for each data item, and a feature can be
	 * expressed as a collection of projections. What we see in the Mastodon
	 * tables and what we use in color modes are the projections. Of course, it
	 * is only useful for numerical features (number, matrices). Other feature
	 * types are specific enough to prompt for their own display. In the
	 * examples of this repository, we only deal with numerical features.
	 * 
	 * The way a numerical feature is decomposed into projection is completely
	 * up to you. The decomposition does not have to be complete, and it can be
	 * redundant. For instance, here we will just compute the angle of a link in
	 * the XY plane, but in 3D it requires 2 scalar angles. In such a feature,
	 * you could use the polar angle and the azimuthal angle as projections. Or
	 * the x, y and z coordinates a unit vector along the link. Or even return
	 * the 2 angles and the 3 components. To choose the projections you need to
	 * think of the most direct way to use it in your track analysis pipeline.
	 * 
	 * In concrete implementations, we therefore need to give the specifications
	 * of the projections we will use, and pass these projection specifications
	 * to the feature specifications (read below). In our case we just have one,
	 * and projection specifications just need a projection name, and the
	 * dimension of the scalar value they return (in our case, an angle). This
	 * is done with the 'FeatureProjectionSpec' class, as below. Our feature is
	 * made of one real value, so we use only one projection. For the projection
	 * name, we can reuse the feature name, since we have only one projection.
	 */
	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.ANGLE );

	/*
	 * In Mastodon, features are discovered via their specification class.
	 * 
	 * To be integrated into Mastodon, each feature must implement a
	 * specification class, that inherits from 'FeatureSpec< F, O >' where 'F'
	 * is the concrete feature class and 'O' is the type of the data item the
	 * feature is defined for. This can be done as below, using an inner public
	 * static class.
	 * 
	 * And as for the example plugin we have seen, we rely on the SciJava plugin
	 * mechanism for automatic runtime discovery. Concretely this means you need
	 * to add the '@Plugin( type = FeatureSpec.class )' line above the class
	 * declaration for Mastodon to discover and integrate your class.
	 */
	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkXYAngleExampleFeature, Link >
	{
		/*
		 * The specification class itself just need to pass a few arguments via
		 * the super constructor. In order:
		 * 
		 * 1. The name or key of the feature, which must be unique. By
		 * convention we choose user-friendly name, starting with the data item
		 * the feature is defined for, such as 'Link angle in XY plane'.
		 * 
		 * 2. The user information, as a String.
		 * 
		 * 3. The class of the feature. In our case it will just be
		 * 'LinkXYAngleExampleFeature.class'.
		 * 
		 * 4. The class of the data item the feature is defined for. In our
		 * case, 'Link.class'.
		 * 
		 * 5. The multiplicity. The multiplicity is an enum that specifies
		 * whether the feature is defined once or twice per channels in the
		 * image, or is independent from the the number of channels in the
		 * image.
		 * 
		 * For instance, for features the measure the mean intensity, we would
		 * use 'Multiplicity.ON_SOURCES', to signal that the feature needs to be
		 * computed once per channel. In our case, the feature is independent
		 * from the image data and the number of channels it contains. For this
		 * we use the 'Multiplicity.SINGLE' flag.
		 * 
		 * 6... A series or list of feature projection spec, one per projection
		 * defined in the feature. Since we have a scalar feature, we will just
		 * put the one projection spec we created.
		 */
		public Spec()
		{
			super(
					KEY, // 1. The feature name.
					INFO_STRING, // 2. The feature info.
					LinkXYAngleExampleFeature.class, // 3. The feature class.
					Link.class, // 4. The class of the data item.
					Multiplicity.SINGLE, // 5. The multiplicity.
					PROJECTION_SPEC ); // 6... The list of projection specs.
		}
	}

	/*
	 * Now we instantiate and keep an instance of this feature spec class.
	 */
	public static final Spec SPEC = new Spec();

	/*
	 * And we return it in this method, specified by the 'Feature<>' interface.
	 */
	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	/*
	 * This is the instance of the projection we will use to return the scalar
	 * real values. It is defined below and instantiated in the feature
	 * constructor.
	 */
	private final MyProjection projection;

	/*
	 * In the feature constructor, we should ask for the minimum required to
	 * compute values. In our case we do not need anything. We just use the
	 * constructor to instantiate the one projection that will actually do the
	 * computation on the fly. Its code is defined below.
	 */
	public LinkXYAngleExampleFeature()
	{
		this.projection = new MyProjection();
	}

	/*
	 * Here is the feature projection class. It implements 'FeatureProjection<
	 * Link >' that specifies a bunch of methods related to getting a scalar
	 * value for each link. Because it is compact, we chose to put it in an
	 * inner class within the feature class.
	 * 
	 * In our case, since we do the computation on the fly, this is where the
	 * computation logic is. We detail the methods in the class body.
	 */
	private static final class MyProjection implements FeatureProjection< Link >
	{

		/*
		 * This should return a key for 'projection'. We use a special class for
		 * this, used to facilitate manipulating a collection of projections
		 * elsewhere in Mastodon.
		 */
		@Override
		public FeatureProjectionKey getKey()
		{
			return FeatureProjectionKey.key( PROJECTION_SPEC );
		}

		/*
		 * This method below is used to indicate to Mastodon that a value exist
		 * for the specified data item, and *that this value is valid*. In our
		 * case, because we compute values on the fly, they always exist and are
		 * always valid.
		 */
		@Override
		public boolean isSet( final Link link )
		{
			return true; // Always set, always valid.
		}

		/*
		 * The logic that computes the value.
		 */
		@Override
		public double value( final Link link )
		{
			// The source of the link (where it starts from).
			final Spot source = link.getSource();
			final double xs = source.getDoublePosition( 0 );
			final double ys = source.getDoublePosition( 1 );

			// Its target (where it points to).
			final Spot target = link.getTarget();
			final double xt = target.getDoublePosition( 0 );
			final double yt = target.getDoublePosition( 1 );

			final double dx = xt - xs;
			final double dy = yt - ys;

			return Math.atan2( dy, dx );
		}

		/*
		 * The units.
		 */
		@Override
		public String units()
		{
			return Dimension.RADIANS_UNITS;
		}
	}

	/*
	 * This is it for the computation. In our case it is very simple. It is
	 * somewhat daunting that such a simple computation requires that many
	 * boilerplate code and specifications to be integrated in Mastodon. Again,
	 * the feature system ambitions to address a very wide range of situations
	 * and means of computations. This generality is the source of this
	 * verbosity.
	 */

	/*
	 * The method below is important for features that *store* values. These
	 * features have a 'computer' class that handles the computation logic. The
	 * computer class is called by the user (in the 'compute features' dialog),
	 * and when after it runs, the feature instance is created. It is then just
	 * used to store and return values.
	 * 
	 * But additionally, Mastodon has a mechanism to invalidate the values of
	 * the data items that are modified. Let's take for example the feature that
	 * stores the mean intensity in a spot. If the user moves a spot to another
	 * position, or change its radius, the mean intensity value stored for this
	 * spot becomes invalid. When a user modifies a data item in such a way,
	 * Mastodon automatically calls the method below, which is meant to
	 * *discard* the value the feature stores.
	 * 
	 * For our example where the computation is done on the fly, we don't have
	 * to do anything.
	 */
	@Override
	public void invalidate( final Link link )
	{}

	/*
	 * The rest of the feature class is dedicated to returning the projections
	 * contained in the feature.
	 */

	/*
	 * Here we should return the one projection we have if the specified
	 * projection key matches., or 'null' otherwise.
	 */
	@Override
	public FeatureProjection< Link > project( final FeatureProjectionKey key )
	{
		if ( projection.getKey().equals( key ) )
			return projection;

		return null;
	}

	/*
	 * Here we should return all the projections defined in the feature.
	 */
	@Override
	public Set< FeatureProjection< Link > > projections()
	{
		return Collections.singleton( projection );
	}

	/*
	 * We could think our work is done. Alas! We still need to give Mastodon a
	 * way to create the feature. And this is done in the feature computer. So
	 * the rest of the tutorial is in the 'LinkXYAngleExampleFeatureComputer'
	 * class, and here you will see that it is the class that calls the
	 * constructor of this feature.
	 */
}
