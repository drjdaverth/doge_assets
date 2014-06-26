var runtimer = true;
filefinished = function(noarg)
{
	//tell all img links to reload
	jQuery('#emresultscontent img').each(
		function(index)
		{
			var src = jQuery(this).attr("src");
			src = src + "?cache=false";
			jQuery(this).attr("src",src);
		}
	);
}
uploadsComplete = function(noarg)
{
	//set the link to be a check box
	runtimer = false;
	jQuery("#emsyncstatus").html("");
	//tell all img links to reload?
	jQuery('img').each(
		function(index)
		{
			var src = jQuery(this).attr("src");
			//TODO: Add a ?reload=true on there
			jQuery(this).attr("src",src);
		}
	);
}
