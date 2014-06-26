importPackage( Packages.java.lang );

var destinationPath = context.getRequestParameter( "destinationPath" );
var forcedDestinationPath = context.getRequestParameter( "forcedDestinationPath" );

if ( forcedDestinationPath != null )
{
	context.redirect(forcedDestinationPath);
}
else
{
	context.redirect(destinationPath);
}
