function deleteGroups()
{
	for ( var i = 0; i < document.groups.length; i++ )
	{
		if ( document.groups.elements[ i ].checked )
		{
			gotoPage( 'deletegroups.html', null, document.groups );
			break;
		}
	}
}
