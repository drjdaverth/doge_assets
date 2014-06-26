/* ------------------------------------------------------------------------
	Pretty Comments
	
	Developped By: Stephane Caron (http://www.no-margin-for-errors.com)
	Inspired By: The facebook textarea :)
	Version: 1.4
	
	Copyright: Feel free to redistribute the script/modify it, as
			   long as you leave my infos at the top.
------------------------------------------------------------------------- */

	jQuery.fn.prettyComments = function(settings) {
		settings = jQuery.extend({
					animate: false, /* If you set it to true, cursor will dissapear in FF3 */
					animationSpeed: 'fast', /* fast/slow/normal */
					maxHeight : 500,
					alreadyAnimated: false, /* DONT CHANGE */
					init: true /* DONT CHANGE */
				}, settings);

		// Create the div in which the content will be copied
		jQuery('body').append('<div id="comment_hidden"></div>');

		var setCSS = function(which){
			// Init the div for the current textarea
			jQuery("#comment_hidden").css({
				'position':'absolute',
				'top': -10000,
				'left': -10000,
				'width': jQuery(which).width(),
				'min-height': jQuery(which).height(),
				'font-family': jQuery(which).css('font-family'),
				'font-size': jQuery(which).css('font-size'),
				'line-height': jQuery(which).css('line-height')
			});
			
			if(jQuery.browser.msie && parseFloat(jQuery.browser.version) < 7){
				jQuery("#comment_hidden").css('height',jQuery(which).height());
			};
		};
		
		var copyContent = function(which){
			// Convert the line feeds into BRs
			theValue = jQuery(which).attr('value') || "";
			theValue = theValue.replace(/\n/g,'<br />');
			
			jQuery("#comment_hidden").html(theValue + '<br />');
			
			if(!settings.init){
				if(jQuery("#comment_hidden").height() > jQuery(which).height()){
					if(jQuery('#comment_hidden').height() > settings.maxHeight){
						jQuery(which).css('overflow-y','scroll');
					}else{
						jQuery(which).css('overflow-y','hidden');
						expand(which);
					};
				}else if(jQuery("#comment_hidden").height() < jQuery(which).height()){
					if(jQuery('#comment_hidden').height() > settings.maxHeight){
						jQuery(which).css('overflow-y','scroll');
					}else{
						jQuery(which).css('overflow-y','hidden');
						shrink(which);
					};
				};
			};
		};
		
		var expand = function(which){
			if(settings.animate && !settings.alreadyAnimated){
				settings.alreadyAnimated = true;
				jQuery(which).animate({'height':jQuery("#comment_hidden").height()},settings.animationSpeed,function(){
					settings.alreadyAnimated = false;
				});
			}else if(!settings.animate && !settings.alreadyAnimated){
				jQuery(which).height(jQuery("#comment_hidden").height());
			};
		};
		
		var shrink = function(which){
			if(settings.animate && !settings.alreadyAnimated){
				settings.alreadyAnimated = true;
				jQuery(which).animate({'height':jQuery("#comment_hidden").height()},settings.animationSpeed,function(){
					settings.alreadyAnimated = false;
				});
			}else{
				jQuery(which).height(jQuery("#comment_hidden").height());
			};
		};
		
		jQuery(this).each(function(){
			jQuery(this).css({
				'overflow':'hidden'
			})
			.bind('keyup',function(){
				copyContent(jQuery(this));
			});
			
			var h = jQuery("#comment_hidden").height();
			
			// Make sure all the content in the textarea is visible
			setCSS(this);
			copyContent(jQuery(this));
			
			if(h > settings.maxHeight){
				jQuery(this).css({
					'overflow-y':'scroll',
					'height':settings.maxHeight
				});
			}
			else if ( h > 0)
			{
				jQuery(this).height(h);
			};
			
			settings.init = false;
		});
	};