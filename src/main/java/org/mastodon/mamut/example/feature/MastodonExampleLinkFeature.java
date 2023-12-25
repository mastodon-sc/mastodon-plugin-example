package org.mastodon.mamut.example.feature;

import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Link;
import org.scijava.plugin.Plugin;

/*
 * In this example we show an example of a simple custom numerical feature in 
 * Mastodon. 
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
 * numerical feature system. Let's go.
 */

/*
 * A Mastodon feature is class that associates a value (numerical or 
 * not) to a data item (a spot or a link). Such classes implement the
 * 'org.mastodon.feature.Feature' interface, with a type that specifies
 * what data item they are defined for. Because we want to compute 
 * something on links, our feature implements 'Feature < Link >'.
 */
public class MastodonExampleLinkFeature implements Feature< Link >
{

	/*
	 * We start by defining a few constants, that are used to declare the
	 * 'signature' of our feature: its name, what type of values it returns,
	 * what is their units and dimension, etc.
	 */

	/*
	 * Our feature needs a name, and we will use it repeatedly. So we declare it
	 * now in a constant. The feature name also serves as a unique key, hence
	 * the name of the constant.
	 */
	private static final String KEY = "Example link feature";

	/*
	 * We also have a means to pass some information to the user about what a
	 * feature is, and we also store this in a constant.
	 */
	public static final String INFO_STRING = "Example feature that computes the "
			+ "angle of a link in the XY plane.";

	/*
	 * Now we need to speak a little bit about the notion of 'projections'.
	 * 
	 * Since a feature can return any type of data, we needed a way to make use
	 * of them in Mastodon in some specific situations, for instance to display
	 * a feature in a table, or use it in a color mode. For this we use
	 * *projections*. A feature can return a value of any type, but we ask it to
	 * be 'decomposable' into projections. A feature projection returns a scalar
	 * real number for each data item, and a feature can be expressed as a
	 * collection of projections. What we see in the Mastodon tables and what we
	 * use in color modes are the projections. Of course, it is only useful for
	 * numerical features (number, matrices), but for other feature types, they
	 * are specific enough to prompt for their own display. But in this example,
	 * we only deal with numerical feature.
	 * 
	 * The way a numerical feature is decomposed into projection is completely
	 * up to you. The decomposition does not have to be complete, and it can be
	 * redundant. For instance, here we will just compute the angle of a link in
	 * the XY plane, but in 3D we require 2 scalar angles. In such a feature,
	 * you could use the polar angle and the azimuthal angle as projections. Or
	 * the x, y and z coordinates a unit vector along the link. Or even return
	 * then 2 angles and the 3 components. To choose the projections you need to
	 * think of the most direct way to use it in your track analysis pipeline.
	 */

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.ANGLE );

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< MastodonExampleLinkFeature, Link >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					MastodonExampleLinkFeature.class,
					Link.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public static final Spec SPEC = new Spec();

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	public MastodonExampleLinkFeature( final RefPool< Link > pool )
	{
		super( KEY, Dimension.ANGLE, Dimension.ANGLE.getUnits( "", "" ), pool );
	}

	@Override
	public FeatureProjection< Link > project( final FeatureProjectionKey key )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set< FeatureProjection< Link > > projections()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidate( final Link obj )
	{
		// TODO Auto-generated method stub

	}

}
