
	jQuery(document).ready(function() 
	{ 
		jQuery(".metadatadroppable").livequery(
				function()
				{
					jQuery(this).draggable({
						start: function(){
							var width = jQuery(this).width();
							var id = jQuery(this).attr('id');
							
					        jQuery(this).hide();
					        jQuery(this).attr('id', 'old-' + id);
					        jQuery('#' + id).width(width);
					        jQuery(this).attr('id', id);
					        
						}
							
					});
					jQuery(this).droppable(
						{
							drop: function(event, ui) 
							{
								var source = ui.draggable.attr("id");
								var destination = this.id;

								var ul = ui.draggable.closest("ul");
								var viewpath = ul.attr("viewpath");
								var seachtype = ul.attr("searchtype");
								var assettype = ul.attr("assettype");
								var viewid = ul.attr("viewid");
								var path = ul.attr("path");
								
								jQuery("#workarea").load(path,
									{
									"source":source,
									"destination":destination,
									"viewpath": viewpath,
									"searchtype": seachtype,
									"assettype": assettype,
									"viewid" : viewid
									});
							},
							tolerance: 'pointer',
							over: outlineSelectionRow,
							out: unoutlineSelectionRow
						}
					);
				}
			);
		
		
		
		 
		jQuery('.listsort').sortable({
			  
			axis: 'y',
		    stop: function (event, ui) {
		  
				var path = jQuery(this).data("path");
				
		    	
		        var data = jQuery(this).sortable('serialize');
		        
		        // POST to server using $.post or $.ajax
		        jQuery.ajax({
		            data: data,
		            type: 'POST',
		            url: path 		            
		        });
		    }
		});
		
		
		
		
	}); 
