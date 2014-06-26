jQuery(document).ready(function() 
{ 
	$('.emtree-widget ul li div span.arrow').livequery('click', function(event){
			event.stopPropagation();
			var tree = $(this).closest(".emtree");
			var node = $(this).closest('.noderow');
			var nodeid = node.data('nodeid');
			var depth = node.data('depth');
			
			var home = $(this).closest(".emtree").data("home");
			
			if ( $(this).find('.arrow').hasClass('down') )
			{
				$(this).find('.arrow').removeClass('down');				
			}
			else 
			{ 
				//Open it. add a UL
				$(this).find('.arrow').addClass('down');				
			}
			
			tree.find(nodeid + "_add").remove();
			node.load(home + "/components/emtree/tree.html?toggle=true&tree-name=" + tree.data("treename") + "&nodeID=" + nodeid + "&depth=" + depth, doResize);
			
			
	});

	$('.emtree-widget ul li div').livequery('click', function(event) {
		event.stopPropagation();
		$('.emtree ul li div').removeClass('selected');
		$(this).addClass("selected");
		var tree = $(this).closest(".emtree");
		var node = $(this).closest('.noderow');
		var nodeid = node.data('nodeid');
		var depth = node.data('depth');
		var home = tree.data("home");
		var prefix = $(this).closest(".emtree").data("url-prefix");
		if( prefix)
		{
			var treeholder = $("div#categoriescontent");
			var toplocation =  parseInt( treeholder.scrollTop() );
			var leftlocation =  parseInt( treeholder.scrollLeft() );
		
			//$("#right-col").load(); 'clearfilters':true,
				jQuery.get(prefix + nodeid + ".html",
						{
							'oemaxlevel':3,
							'tree-name':tree.data("treename"),
							'nodeID':nodeid,							
							'treetoplocation':toplocation,
							'treeleftlocation':leftlocation,
							'depth': depth
						},	
						function(data) 
						{
							var cell = jQuery("#searchlayout"); //view-picker-content
							cell.html(data);
							//window.location.hash="TOP";
						}
				);
		}
		else
		{
			tree.find(nodeid + "_add").remove();
			node.load(home + "/components/emtree/tree.html?toggle=true&tree-name=" + tree.data("treename") + "&nodeID=" + nodeid + "&depth=" + depth);
		}
});
	
	var field = $('.emtree .field'); 
	
	field.livequery('click', function(event) 	{
			event.stopPropagation();
	});
	
	$('.emtree-widget .field.text').livequery('focusin', function(event) 	{
		if ($(this).val() == this.defaultValue ) { 
			$(this).val(''); 
		}
	});
	
	$('.emtree-widget .field.text').livequery('focusout', function(event)	{
		if ($(this).val() == "") { 
			$(this).val(this.defaultValue); 
		}
	});
	
	$(".emtree-widget .add").livequery('click', function(event) {
				event.stopPropagation();
				var tree = $(this).closest(".emtree");
				tree = $(tree);
				var home = tree.data("home");
				var node = $(this).closest('.noderow');
				var nodeid = node.data('nodeid');
				var depth = node.data('depth');
				node.load(home + "/components/emtree/tree.html?adding=true&tree-name=" + tree.data("treename") + "&nodeID=" + nodeid + "&depth=" + depth);
	} );
	
	
	$(".emtree-widget .cancel").livequery('click', function(event) { 
			
			event.stopPropagation();

			$("#newarrow").remove();
	
			var id = $(this).attr("id");	
			$("#" + id + "_add").hide("fast");
			$("#" + id + "_row > div").removeClass('selected');
	} );
	
	$(".emtree-widget .delete").livequery('click', function(event) {
			event.stopPropagation();

			var id = $(this).data('parent');
			
			var agree=confirm("Are you sure you want to delete?");
			if (agree)
			{
				var tree = $(this).closest(".emtree");
				var home = tree.data("home");

				jQuery.get(home + "/components/emtree/deletecategory.html", {
					categoryid: id,
					'tree-name': tree.data("treename"),
					} ,function () {
						tree.find("#" + id + "_row").hide( 'fast', function(){
							repaintEmTree(tree); 
						} );
						
					});
			} else {
				return false;
			}
	} );
			
	$(".emtree-widget .save").livequery('click', function(event) {
					event.stopPropagation();
					var id = $(this).data("parent");
					//alert("parent category was: " + id);
					var newname = $("#" + id + "_new").attr("value");
					var tree = $(this).closest(".emtree");

					//alert("New name: " + newname);
					var home = tree.data("home");

					jQuery.get(home + "/components/emtree/addcategory.html", {
						categoryid: id,
						'newname': newname,
						'tree-name': tree.data("treename")
						} , function () {
							tree.find("#" + id + "_add").hide("fast", function(){
								repaintEmTree(tree); 
							});
						}
					
					);
	} );
	
	
	$(".emtree-widget .edit").livequery('click', function(event) {
				event.stopPropagation();
				var id = $(this).parents('.noderow:first').data('nodeid');

				var tree = $(this).closest(".emtree");
				tree.find("#" + id +"_display").hide("fast");
				tree.find("#" + id +"_edit").show("fast");		//TODO: Load edit dialogs using Ajax
				
				return false;
	} );
	

	$(".emtree-widget .editcancel").livequery('click', function(event) {
				event.stopPropagation();
				var id = $(this).parents('.noderow:first').data('nodeid');

				var tree = $(this).closest(".emtree");
				
				tree.find("#" + id +"_display").show("fast");
				tree.find("#" + id +"_edit").hide("fast");		
				return false;
	} );

	$(".emtree-widget .editsave").livequery('click', function(event) {
				event.stopPropagation();
				var id = $(this).parents('.noderow:first').data('nodeid');


				var tree = $(this).closest(".emtree");
				var home = tree.data("home");
				
				var newname = tree.find("#" + id + "_edit_field").attr("value");
				jQuery.get( home + "/components/emtree/savecategory.html", {
					'id': id,
					categoryid: id,
					'name': newname,
					'tree-name': tree.data("treename")
					} , function () {						
						tree.find("#" + id +"_edit").hide("fast");		
						tree.find("#" + id +"_display").show("fast");
						tree.find("#" + id + "_display").html(newname);
						repaintEmTree(tree);
					}
				
				);
				return false;
	} );
	
	$('.emtree-widget .checkbox input').livequery('click', function(event){
		event.stopPropagation();
	});

	//need to init this with the tree
	
	$("div#treeholder").livequery( function()
	{	
		var treeholder = $(this);
		var top = treeholder.data("treetoplocation");
		if( top )
		{
			var left = treeholder.data("treeleftlocation");
			var catcontent = $("div#categoriescontent");
			catcontent.scrollTop(parseInt(top));
			catcontent.scrollLeft(parseInt(left));
		}
	});
	
	//end document ready
	
});

repaintEmTree = function (tree) {
	var home = tree.data("home");

	tree.closest("#treeholder").load(home +  "/components/emtree/tree.html?tree-name=" + tree.data("treename") );
}
