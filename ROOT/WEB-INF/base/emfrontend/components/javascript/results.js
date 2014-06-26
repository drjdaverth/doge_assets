jQuery(document).ready(function() 
{ 

jQuery("input.selectionbox").livequery( function() 
{
	jQuery(this).change(function() 
	{
		var home = $('#application').data('home') + $('#application').data('apphome');
		var hitssessionid = $('#resultsdiv').data('hitssessionid');
		var dataid = jQuery(this).data('dataid');
		
		jQuery(this).load( home + "/components/results/toggle.html", {dataid:dataid, searchtype: "asset", hitssessionid: hitssessionid });
			//jQuery(this).load( home + apphome + "/components/results/togglepage.html", {oemaxlevel:1, hitssessionid: hitsessionid });
	});
});

jQuery("a.selectpage").livequery( 'click', function() 
{
	jQuery('input[name=pagetoggle]').attr('checked','checked');
	jQuery('.selectionbox').attr('checked','checked');
   // jQuery("#select-dropdown-open").click();

});
	
jQuery("a.deselectpage").livequery( 'click', function() 
{
	jQuery('input[name=pagetoggle]').removeAttr('checked');
	jQuery('.selectionbox').removeAttr('checked');
	//jQuery("#select-dropdown-open").click();

});

jQuery("input[name=pagetoggle]").livequery( 'click', function() 
{
	  var home = $('#application').data('home');
	  var apphome = $('#application').data('apphome');
	  var hitssessionid = $('#resultsdiv').data('hitssessionid');
	   
	   var status = jQuery('input[name=pagetoggle]').is(':checked');
	   if(status)
	   {
		   jQuery(this).load( home + apphome + "/components/results/togglepage.html", {oemaxlevel:1, hitssessionid: hitssessionid, action:"page"});
		   jQuery('.selectionbox').attr('checked','checked');
       }
       else
       {
   	       jQuery(this).load( home + apphome + "/components/results/togglepage.html", {oemaxlevel:1, hitssessionid: hitssessionid, action:"none"});         
   	       jQuery('.selectionbox').removeAttr('checked');  
   	   }
	   //jQuery("#select-dropdown-open").click();
});


jQuery(".gallery-checkbox input").livequery( 'click', function() 
{
	if ( jQuery(this).is(':checked') ) {
		jQuery(this).closest(".emthumbbox").addClass("selected");
	} else {
		jQuery(this).closest(".emthumbbox").removeClass("selected");
	}
});




jQuery(".moduleselectionbox").livequery("click", function(e) {
	
	
	e.stopPropagation();
	
	var searchhome = $('#resultsdiv').data('searchhome');
	  
	var dataid = jQuery(this).data("dataid");
	var sessionid = jQuery(this).data("hitssessionid");
	
	
	jQuery.get(searchhome + "/selections/toggle.html", {dataid:dataid, hitssessionid:sessionid});
	
		
	return;
	
});

});        //document ready
        

//TODO: remove this. using ajax
togglehits =  function(action)
{
	var searchhome = $('#resultsdiv').data('searchhome');
	var sessionid = jQuery('#resultsdiv').data("hitssessionid");

	jQuery.get(searchhome + "/selections/togglepage.html", {oemaxlevel:1, hitssessionid:sessionid, action:action});         
       if(action == 'all' || action== 'page'){
    	   jQuery('.moduleselectionbox').attr('checked','checked');
        }else{
        	jQuery('.moduleselectionbox').removeAttr('checked');  
        }
       return false;       

}


