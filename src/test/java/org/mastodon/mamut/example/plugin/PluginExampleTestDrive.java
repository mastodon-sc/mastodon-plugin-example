package org.mastodon.mamut.example.plugin;

import org.mastodon.mamut.launcher.MastodonLauncherCommand;
import org.scijava.Context;
import org.scijava.command.CommandService;

import ij.ImageJ;

public class PluginExampleTestDrive
{

	public static void main( final String[] args )
	{
		ImageJ.main( args );
		@SuppressWarnings( "resource" )
		final CommandService commandService = new Context().service( CommandService.class );
		commandService.run( MastodonLauncherCommand.class, true );
	}
}
