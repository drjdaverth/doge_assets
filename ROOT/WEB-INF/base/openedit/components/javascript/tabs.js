showtab = function(inHome , inTabName)
{	
	var elem = document.getElementById(inTabName);
	if( elem )
	{
		//the td itself
		elem.style.backgroundImage = "url(" + inHome + "/system/images/tabs/navlh.gif)";
		elem.style.backgroundPosition = "left top";		
		elem.style.backgroundRepeat = "no-repeat";
		elem.style.backgroundColor = "#31578d";

		//the span
		var spane = elem.childNodes[0]; //span tag
		spane.style.backgroundImage = "url(" + inHome + "/system/images/tabs/navrh.gif)";
		spane.style.backgroundPosition = "right top";
		spane.style.backgroundRepeat = "no-repeat";

		var atag = spane.childNodes[0]; //span tag
		atag.style.color = "#ffffff";


	}
}	

showpath = function(inDivId, inPath, inMaxLevel)
{
	jQuery("#"+inDivId).load(inPath, { oemaxlevel: inMaxLevel });	
}

