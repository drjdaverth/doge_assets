jQuery(document).ready(function() {
	// Add highlight logic for mouse overs
	jQuery('.highlightMe').mouseover(function(){
		jQuery(this).addClass('highlight');
	});
	jQuery('.highlightMe').mouseout(function(){
		jQuery(this).removeClass('highlight');
	});

});

Array.prototype.index = function(val) {
  for(var i = 0, l = this.length; i < l; i++) {
    if(this[i] == val) return i;
  }
  return null;
}

Array.prototype.include = function(val) {
  return this.index(val) !== null;
}

var DetailEditor = {
	find_field_from_row: function(detail){
		
		if(detail.constructor == String) detail = jQuery('.detaileditor #inputarea_' + detail);
		var result = detail.find('input[type!="hidden"], textarea, select').filter('input:not(.nodependsondefault),select:not(.nodependsondefault),textarea:not(.nodependsondefault)'); 

		return 	result;
	},
	
	toggle_dependency_row: function(row,dependant,on,default_value,delimiter){
		if(default_value == '') default_value = 'N/A';
		if(delimiter == '') delimiter = ',';
		
		e = jQuery('.detaileditor #inputarea_' + row);
		i = DetailEditor.find_field_from_row(e);
		currentval = dependant.val();
		
		if(dependant.is(":checkbox")) {
			if(!dependant.attr('checked')){
				currentval = "";
			}
			
		}
		
		if(on.split(delimiter).include(currentval)){
			if(i.val() == default_value) 
				i.val('');
				
			e.show();
		}
		else{
			e.hide();
			
		  if(i.val() == '') 
				i.val(default_value);
		}		
	}
};
	
	
jQuery.fn.extend({
	bind_and_run: function(trigger,fn){		
		e = jQuery(this);

		e.bind(trigger + '_and_run',fn);
		e.bind(trigger,function(){jQuery(this).trigger(trigger + '_and_run');})
		e.trigger(trigger + '_and_run');
	}
});