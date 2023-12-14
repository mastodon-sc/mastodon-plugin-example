import org.mastodon.mamut.launcher.MastodonLauncherCommand;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class PluginExampleTestDrive
{

	public static void main( final String[] args )
	{
		@SuppressWarnings( "resource" )
		final Context context = new Context();
		final UIService uiService = context.service( UIService.class );
		uiService.showUI();
		final CommandService commandService = context.service( CommandService.class );
		commandService.run( MastodonLauncherCommand.class, true );
	}
}
