package org.mastodon.mamut.example.feature;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/*
 * This class is part of the first tutorial on writing your own numerical
 * feature for Mastodon. It discusses the computer logic, which will be very
 * simple in our example. You should read the code of the
 * 'LinkXYAngleExampleFeature' class first.
 * 
 * The separation of concerns in Mastodon is quite strong. In the previous
 * class, we have seen the classes that are in charge of *storing* the
 * values of a feature. Though in our example it also contained the logic to
 * compute these values, as we wanted to have a 'on the fly' feature.
 * Normally, the logic to compute a feature should be implemented in a
 * feature computer. Such a class is the one discovered by Mastodon, and is
 * in charge of creating the feature and adding values to it. It is normally
 * the important one. This added verbosity and multiplication of classes
 * gave us a wide berth as to how features are defined and calculated. But
 * again, we pay the price now.
 * 
 * However this class will be quite brief. Because for the XY angle the
 * computation is done in the feature class, we just have to instantiate it.
 * To return the feature class to Mastodon, and receive the data required, a
 * feature computer uses the SciJava modifiable private field, annotated
 * with the '@Parameter' tag, that we will explain below. If you never met
 * this technique before, it will feel strange.
 * 
 * The feature computer class itself needs to implement the
 * 'MamutFeatureComputer' interface. As for the plugin system, it has a
 * 'Mamut' in its name to indicate that it is specific to the cell lineaging
 * application. And as for the plugin system, you need to annotate it by add
 * the '@Plugin( type = MamutFeatureComputer.class )' line before the class
 * declaration, so that Mastodon can discover the computer at runtime.
 */
@Plugin( type = MamutFeatureComputer.class )
public class LinkXYAngleExampleFeatureComputer implements MamutFeatureComputer
{

	/*
	 * Let's start immediately with the part that might feel new or strange or
	 * like dark magic. A feature computer is expected to receive a set of
	 * parameters, such as the image, the track data, etc., and return an
	 * output, the feature it computed. But it does not have methods to do so.
	 * Everything is done via annotated private field, annotated with a special
	 * tag. This system comes from the SciJava core library, and is meant to
	 * simplify developing code that process things.
	 */
	
	/*
	 * First, we will specify the output of this computer. This is the feature
	 * instance we will create. It will be stored in a private field, and we add
	 * the '@Parameter' tag to signal Mastodon to look into this field. And we
	 * add the extra parameter 'type = ItemIO.OUTPUT' to signal that this is the
	 * output that Mastodon should take after computation.
	 * 
	 * Thanks to SciJava, Mastodon will be able to read this field, even if it
	 * is private. But very importantly: for this to work, the field must *not*
	 * be final. If you get a stange error related to it, this is most of the
	 * time because of this.
	 */
	@Parameter( type = ItemIO.OUTPUT )
	private LinkXYAngleExampleFeature feature;

	/*
	 * Second, we can declare the inputs we need in the exact same way. Mastodon
	 * will inspect the class and automatically set the input fields you
	 * declared with the adequate values. Of course, you have a limited choice
	 * in the list of fields you can add to your computer, but they should cover
	 * all cases. Our simple example does not need anything, so there is nothing
	 * there. The list of possible fields is documented elsewhere.
	 */

	/*
	 * In this method, the actual feature instance is typically instantiated,
	 * without any values.
	 */
	@Override
	public void createOutput()
	{
		feature = new LinkXYAngleExampleFeature();
	}

	/*
	 * This method is called after '#createOuput()'. This is where the
	 * computation logic should happen, storing the computed values in the
	 * feature instance. In our simple case, we have nothing to do.
	 */
	@Override
	public void run()
	{}

	/*
	 * That's it!
	 * 
	 * With this, a new feature should appear in the feature computation dialog.
	 * If you check it and trigger a computation, the new feature will appear
	 * and be usable throughout Mastodon.
	 */
}
