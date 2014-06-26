/*
 * FancyBox - simple and fancy jQuery plugin
 * Examples and documentation at: http://fancy.klade.lv/
 * Version: 1.2.1 (13/03/2009)
 * Copyright (c) 2009 Janis Skarnelis
 * Licensed under the MIT License: http://en.wikipedia.org/wiki/MIT_License
 * Requires: jQuery v1.3+
*/

/**
 * Look for a sessision id. If you find it then increment the index
 */



;(function($) {

	jQuery.fn.fixPNG = function() {
		return this.each(function () {
			var image = jQuery(this).css('backgroundImage');

			if (image.match(/^url\(["']?(.*\.png)["']?\)$/i)) {
				image = RegExp.$1;
				jQuery(this).css({
					'backgroundImage': 'none',
					'filter': "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true, sizingMethod=" + (jQuery(this).css('backgroundRepeat') == 'no-repeat' ? 'crop' : 'scale') + ", src='" + image + "')"
				}).each(function () {
					var position = jQuery(this).css('position');
					if (position != 'absolute' && position != 'relative')
						jQuery(this).css('position', 'relative');
				});
			}
		});
	};

	var elem, opts, busy = false, cancelslideshow = false, imagePreloader = new Image, loadingTimer, loadingFrame = 1, imageRegExp = /\.(jpg|gif|png|bmp|jpeg|tiff|tif)(.*)?$/i, mediaViewerRegExp = /\.html/i;
	var isIE = (jQuery.browser.msie && parseInt(jQuery.browser.version.substr(0,1)) < 8);

	jQuery.fn.fancybox = function(settings) {
		settings = jQuery.extend({}, jQuery.fn.fancybox.defaults, settings);

		var matchedGroup = this;

		function _initialize() {
			elem = this;
			opts = settings;

			_start();

			return false;
		};

		function _start() {
			if (busy) return;
			cancelslideshow = false;
			opts.itemArray		= [];
			opts.itemCurrent	= 0;

			if (settings.itemArray.length > 0) 
			{
				//pre loaded
				opts.itemArray = settings.itemArray;
			}
			else if (!elem.rel || elem.rel == '') 
			{
				//This is the entire page
				var item = {href: elem.href, title: elem.title,pageturner: elem.getAttribute("pageturner")};
				if (jQuery(elem).children("img:first").length) {
					item.orig = jQuery(elem).children("img:first");
				}
				opts.itemArray.push( item );
//OLD				
			} else {				
				var subGroup = $(matchedGroup).filter("a[rel=" + elem.rel + "]");

				var item = {};

				for (var i = 0; i < subGroup.length; i++) {
					item = {href: subGroup[i].href, title: subGroup[i].title};

					if ($(subGroup[i]).children("img:first").length) {
						item.orig = $(subGroup[i]).children("img:first");
						opts.itemArray.push( item );
					}

				}
			}			

			//Load up the other items from the list
//			var pageturner = opts.itemArray[ opts.itemCurrent ].pageturner;
//			_load_pages(pageturner);
			
			if (jQuery.isFunction(opts.callbackOnStart)) {
				opts.callbackOnStart();
			}
			
			if (opts.overlayShow) {
				if (isIE) {
					jQuery('embed, object, select').css('visibility', 'hidden');
				}

				jQuery("#fancy_overlay").css('opacity', opts.overlayOpacity).show();
			}

			_change_item();  //This will start to pre-load the next page
		};
		function _load_pages(pageturner) 
		{
			if( pageturner)
			{
				opts.itemArray		= [];

				jQuery.ajax(
				{
					  type: "GET",
					  url: pageturner,
					  async: false,
					  dataType: "xml",
					  success: function(data)
					  {
							var parsed = jQuery(data);
							parsed.find("links").each(function()  //Loop one. How the heck to you select only one?
							{
								var current = this.getAttribute("current");
								opts.itemCurrent = parseInt(current);
							});
							parsed.find("link").each(function() 
							{
								var href = this.getAttribute("href");
								var name = this.getAttribute("name");
								var pageturner = this.getAttribute("pageturner");
								var item = {href: href, title: name,pageturner: pageturner};
								opts.itemArray.push( item );
							});
                   	  }
				});
								
			}
		}
		function _change_item() {
			jQuery("#fancy_right, #fancy_left, #fancy_close, #fancy_title").hide();

			var href = opts.itemArray[ opts.itemCurrent ].href;

			var queryString = href.replace(/^[^\?]+\??/,'');
			var params = tb_parseQuery( queryString );
			opts.frameWidth = (params['width']*1) + 30 || opts.frameWidth; //defaults to 630 if no paramaters were added to URL
			opts.frameHeight = (params['height']*1) + 40 || opts.frameHeight; //defaults to 440 if no paramaters were added to URL
			
			if (href.match(imageRegExp) && !href.match(mediaViewerRegExp)) 
			{
				imagePreloader = new Image; imagePreloader.src = href;

				if (imagePreloader.complete) {
					_proceed_image();

				} else {
					jQuery.fn.fancybox.showLoading();

					jQuery(imagePreloader).unbind().bind('load', function() {
						jQuery(".fancy_loading").hide();

						_proceed_image();
					});
				}
			}
			else if (href.match(/#/)) 
			{
				var target = window.location.href.split('#')[0]; target = href.replace(target, ''); target = target.substr(target.indexOf('#'));

				_set_content('<div id="fancy_div">' + jQuery(target).html() + '</div>', opts.frameWidth, opts.frameHeight);

			} else {
				//jQuery.get(href, function(data) {
				//	_set_content( '<div id="fancy_ajax">' + data + '</div>', opts.frameWidth, opts.frameHeight );
				//});
				_set_content('<iframe id="fancy_frame" onload="jQuery.fn.fancybox.showIframe()" name="fancy_iframe' + Math.round(Math.random()*1000) + '" frameborder="0" hspace="0" src="' + href + '"></iframe>', opts.frameWidth, opts.frameHeight);
			}
			
			if( opts.slideshowtime && !cancelslideshow)
			{
					//setTimeout( "jQuery('#fancy_right_ico').click();",parseInt(opts.slideshowtime));
					setTimeout( "jQuery.fn.fancybox.goNext();",parseInt(opts.slideshowtime));
			}
			
		};

		function _proceed_image() {
			if (opts.imageScale) {
				var w = jQuery.fn.fancybox.getViewport();

				var r = Math.min(Math.min(w[0] - 36, imagePreloader.width) / imagePreloader.width, Math.min(w[1] - 60, imagePreloader.height) / imagePreloader.height);

				var width = Math.round(r * imagePreloader.width);
				var height = Math.round(r * imagePreloader.height);

			} else {
				var width = imagePreloader.width;
				var height = imagePreloader.height;
			}

			_set_content('<img alt="" id="fancy_img" src="' + imagePreloader.src + '" />', width, height);
		};

		function _preload_neighbor_images() {
			if ((opts.itemArray.length -1) > opts.itemCurrent) {
				var href = opts.itemArray[opts.itemCurrent + 1].href;

				if (href.match(imageRegExp)) {
					objNext = new Image();
					objNext.src = href;
				}
			}

			if (opts.itemCurrent > 0) {
				var href = opts.itemArray[opts.itemCurrent -1].href;

				if (href.match(imageRegExp)) {
					objNext = new Image();
					objNext.src = href;
				}
			}
		};

		function _set_content(value, width, height) {
			busy = true;

			var pad = opts.padding;

			if (isIE) {
				jQuery("#fancy_content")[0].style.removeExpression("height");
				jQuery("#fancy_content")[0].style.removeExpression("width");
			}

			if (pad > 0) {
				width	+= pad * 2;
				height	+= pad * 2;

				jQuery("#fancy_content").css({
					'top'		: pad + 'px',
					'right'		: pad + 'px',
					'bottom'	: pad + 'px',
					'left'		: pad + 'px',
					'width'		: 'auto',
					'height'	: 'auto'
				});

				if (isIE) {
					jQuery("#fancy_content")[0].style.setExpression('height',	'(this.parentNode.clientHeight - 20)');
					jQuery("#fancy_content")[0].style.setExpression('width',		'(this.parentNode.clientWidth - 20)');
				}

			} else {
				jQuery("#fancy_content").css({
					'top'		: 0,
					'right'		: 0,
					'bottom'	: 0,
					'left'		: 0,
					'width'		: '100%',
					'height'	: '100%'
				});
			}

			if (jQuery("#fancy_outer").is(":visible") && width == jQuery("#fancy_outer").width() && height == jQuery("#fancy_outer").height()) {
				jQuery("#fancy_content").fadeOut("fast", function() {
					jQuery("#fancy_content").empty().append(jQuery(value)).fadeIn("normal", function() {
						_finish();
					});
				});

				return;
			}

			var w = jQuery.fn.fancybox.getViewport();

			var itemLeft	= (width + 36)	> w[0] ? w[2] : (w[2] + Math.round((w[0] - width - 36) / 2));
			var itemTop		= (height + 50)	> w[1] ? w[3] : (w[3] + Math.round((w[1] - height - 50) / 2));

			var itemOpts = {
				'left':		itemLeft,
				'top':		itemTop,
				'width':	width + 'px',
				'height':	height + 'px'
			};

			if (jQuery("#fancy_outer").is(":visible")) {
				jQuery("#fancy_content").fadeOut("normal", function() {
					jQuery("#fancy_content").empty();
					jQuery("#fancy_outer").animate(itemOpts, opts.zoomSpeedChange, opts.easingChange, function() {
						jQuery("#fancy_content").append(jQuery(value)).fadeIn("normal", function() {
							_finish();
						});
					});
				});

			} else {

				if (opts.zoomSpeedIn > 0 && opts.itemArray[opts.itemCurrent].orig !== undefined) {
					jQuery("#fancy_content").empty().append(jQuery(value));

					var orig_item	= opts.itemArray[opts.itemCurrent].orig;
					var orig_pos	= jQuery.fn.fancybox.getPosition(orig_item);

					jQuery("#fancy_outer").css({
						'left':		(orig_pos.left - 18) + 'px',
						'top':		(orig_pos.top  - 18) + 'px',
						'width':	jQuery(orig_item).width(),
						'height':	jQuery(orig_item).height()
					});

					if (opts.zoomOpacity) {
						itemOpts.opacity = 'show';
					}

					jQuery("#fancy_outer").animate(itemOpts, opts.zoomSpeedIn, opts.easingIn, function() {
						_finish();
					});

				} else {

					jQuery("#fancy_content").hide().empty().append(jQuery(value)).show();
					jQuery("#fancy_outer").css(itemOpts).fadeIn("normal", function() {
						_finish();
					});
				}
			}
		};

		function _set_navigation() {
			if (opts.itemCurrent != 0) {
				jQuery("#fancy_left, #fancy_left_ico").unbind().bind("click", function(e) {
					e.stopPropagation();
					cancelslideshow = true; //they went back so we stop
					if( busy)
					{
						return;
					}
					opts.itemCurrent--;
					_change_item();
					return false;
				});

				jQuery("#fancy_left").show();
			}

			if (opts.itemCurrent != ( opts.itemArray.length -1)) {
				jQuery("#fancy_right, #fancy_right_ico").unbind().bind("click", function(e) {
					e.stopPropagation();
					cancelslideshow = true; //they went back so we stop
					if( busy)
					{
						return;
					}
					
					if (!jQuery("#fancy_overlay").is(':visible'))
					{
						return; //late click or slideshow
					}
					opts.itemCurrent++;
					_change_item();

					return false;
				});

				jQuery("#fancy_right").show();

			}
		};
		jQuery.fn.fancybox.goNext = function()
		{
			if( !cancelslideshow )
			{
				opts.itemCurrent++;
				_change_item();
			}
		}

		function _finish() {
			if (jQuery.isFunction(opts.callbackOnFinish)) 
			{
				opts.callbackOnFinish();
			}
			
			jQuery(document).keydown(function(e) {
				if (e.keyCode == 27) {
					jQuery.fn.fancybox.close();
					jQuery(document).unbind("keydown");

				} else if(e.keyCode == 37 && opts.itemCurrent != 0) {
					opts.itemCurrent--;
					_change_item();
					jQuery(document).unbind("keydown");

				} else if(e.keyCode == 39 && opts.itemCurrent != (opts.itemArray.length - 1)) {
 					opts.itemCurrent++;
					_change_item();
					jQuery(document).unbind("keydown");
				}
			});

			if (opts.centerOnScroll) {
				jQuery(window).bind("resize scroll", jQuery.fn.fancybox.scrollBox);
			} else {
				jQuery("div#fancy_outer").css("position", "absolute");
			}

			if (opts.hideOnContentClick) {
				jQuery("#fancy_wrap").click(jQuery.fn.fancybox.close);
			}

			jQuery("#fancy_overlay, #fancy_close").bind("click", jQuery.fn.fancybox.close);

			jQuery("#fancy_close").show();

			if (opts.itemArray[ opts.itemCurrent ].title !== undefined && opts.itemArray[ opts.itemCurrent ].title.length > 0) {
				jQuery('#fancy_title div').html(opts.itemArray[ opts.itemCurrent ].title);
				jQuery('#fancy_title').show();
			}

			if (opts.overlayShow && isIE) {
				jQuery('embed, object, select', jQuery('#fancy_content')).css('visibility', 'visible');
			}

			if (jQuery.isFunction(opts.callbackOnShow)) {
				opts.callbackOnShow();
			}
			//load the array with the next images in them
			var pageturner = opts.itemArray[ opts.itemCurrent ].pageturner;
			_load_pages(pageturner);

			_preload_neighbor_images();

			_set_navigation();

			busy = false;
		};

		return this.unbind('click').click(_initialize);
	};

	jQuery.fn.fancybox.scrollBox = function() {
		var pos = jQuery.fn.fancybox.getViewport();

		jQuery("#fancy_outer").css('left', ((jQuery("#fancy_outer").width()	+ 36) > pos[0] ? pos[2] : pos[2] + Math.round((pos[0] - jQuery("#fancy_outer").width()	- 36)	/ 2)));
		jQuery("#fancy_outer").css('top',  ((jQuery("#fancy_outer").height()	+ 50) > pos[1] ? pos[3] : pos[3] + Math.round((pos[1] - jQuery("#fancy_outer").height()	- 50)	/ 2)));
	};

	jQuery.fn.fancybox.getNumeric = function(el, prop) {
		return parseInt(jQuery.curCSS(el.jquery?el[0]:el,prop,true))||0;
	};

	jQuery.fn.fancybox.getPosition = function(el) {
		var pos = el.offset();

		pos.top	+= jQuery.fn.fancybox.getNumeric(el, 'paddingTop');
		pos.top	+= jQuery.fn.fancybox.getNumeric(el, 'borderTopWidth');

		pos.left += jQuery.fn.fancybox.getNumeric(el, 'paddingLeft');
		pos.left += jQuery.fn.fancybox.getNumeric(el, 'borderLeftWidth');

		return pos;
	};

	jQuery.fn.fancybox.showIframe = function() {
		jQuery(".fancy_loading").hide();
		jQuery("#fancy_frame").show();
	};

	jQuery.fn.fancybox.getViewport = function() {
		return [jQuery(window).width(), jQuery(window).height(), jQuery(document).scrollLeft(), jQuery(document).scrollTop() ];
	};

	jQuery.fn.fancybox.animateLoading = function() {
		if (!jQuery("#fancy_loading").is(':visible')){
			clearInterval(loadingTimer);
			return;
		}

		jQuery("#fancy_loading > div").css('top', (loadingFrame * -40) + 'px');

		loadingFrame = (loadingFrame + 1) % 12;
	};

	jQuery.fn.fancybox.showLoading = function() {
		clearInterval(loadingTimer);

		var pos = jQuery.fn.fancybox.getViewport();

		jQuery("#fancy_loading").css({'left': ((pos[0] - 40) / 2 + pos[2]), 'top': ((pos[1] - 40) / 2 + pos[3])}).show();
		jQuery("#fancy_loading").bind('click', jQuery.fn.fancybox.close);

		loadingTimer = setInterval(jQuery.fn.fancybox.animateLoading, 66);
	};

	jQuery.fn.fancybox.allowclose = function() 
	{
		return true;
	}

	jQuery.fn.fancybox.close = function() {
	
		var ok = jQuery.fn.fancybox.allowclose();
		if( ok  == false)
		{
			return false;
		}
		busy = true;
		cancelslideshow = true;
		jQuery(imagePreloader).unbind();

		jQuery("#fancy_overlay, #fancy_close").unbind();

		if (opts.hideOnContentClick) {
			jQuery("#fancy_wrap").unbind();
		}

		jQuery("#fancy_close, .fancy_loading, #fancy_left, #fancy_right, #fancy_title").hide();

		if (opts.centerOnScroll) {
			jQuery(window).unbind("resize scroll");
		}

		__cleanup = function() {
			jQuery("#fancy_overlay, #fancy_outer").hide();

			if (opts.centerOnScroll) {
				jQuery(window).unbind("resize scroll");
			}

			if (isIE) {
				jQuery('embed, object, select').css('visibility', 'visible');
			}

			if (jQuery.isFunction(opts.callbackOnClose)) {
				opts.callbackOnClose();
			}
			busy = false;
		};

		if (jQuery("#fancy_outer").is(":visible") !== false) {
			if (opts.zoomSpeedOut > 0 && opts.itemArray[opts.itemCurrent].orig !== undefined) {
				var orig_item	= opts.itemArray[opts.itemCurrent].orig;
				var orig_pos	= jQuery.fn.fancybox.getPosition(orig_item);

				var itemOpts = {
					'left':		(orig_pos.left - 18) + 'px',
					'top': 		(orig_pos.top  - 18) + 'px',
					'width':	jQuery(orig_item).width(),
					'height':	jQuery(orig_item).height()
				};

				if (opts.zoomOpacity) {
					itemOpts.opacity = 'hide';
				}

				jQuery("#fancy_outer").stop(false, true).animate(itemOpts, opts.zoomSpeedOut, opts.easingOut, __cleanup);

			} else {
				jQuery("#fancy_outer").stop(false, true).fadeOut("fast", __cleanup);
			}

		} else {
			__cleanup();
		}

		return false;
	};

	jQuery.fn.fancybox.build = function() {
		var html = '';

		html += '<div id="fancy_overlay"></div>';

		html += '<div id="fancy_wrap">';

		html += '<div class="fancy_loading" id="fancy_loading"><div></div></div>';

		html += '<div id="fancy_outer">';

		html += '<div id="fancy_inner">';

		html += '<div id="fancy_close"></div>';

		html +=  '<div id="fancy_bg"><div class="fancy_bg fancy_bg_n"></div><div class="fancy_bg fancy_bg_ne"></div><div class="fancy_bg fancy_bg_e"></div><div class="fancy_bg fancy_bg_se"></div><div class="fancy_bg fancy_bg_s"></div><div class="fancy_bg fancy_bg_sw"></div><div class="fancy_bg fancy_bg_w"></div><div class="fancy_bg fancy_bg_nw"></div></div>';

		html +=  '<a href="javascript:;" id="fancy_left"><span class="fancy_ico" id="fancy_left_ico"></span></a><a href="javascript:;" id="fancy_right"><span class="fancy_ico" id="fancy_right_ico"></span></a>';

		html += '<div id="fancy_content"></div>';

		html +=  '<div id="fancy_title"></div>';

		html += '</div>';

		html += '</div>';

		html += '</div>';

		jQuery(html).appendTo("body");

		jQuery('<table cellspacing="0" cellpadding="0" border="0"><tr><td class="fancy_title" id="fancy_title_left"></td><td class="fancy_title" id="fancy_title_main"><div></div></td><td class="fancy_title" id="fancy_title_right"></td></tr></table>').appendTo('#fancy_title');

		if (isIE) {
			jQuery("#fancy_inner").prepend('<iframe src="" class="fancy_bigIframe" scrolling="no" frameborder="0"></iframe>');
			jQuery("#fancy_close, .fancy_bg, .fancy_title, .fancy_ico").fixPNG();
		}
	};

	jQuery.fn.fancybox.defaults = {
		padding				:	10,
		imageScale			:	true,
		zoomOpacity			:	false,
		zoomSpeedIn			:	0,
		zoomSpeedOut		:	0,
		zoomSpeedChange		:	300,
		easingIn			:	'swing',
		easingOut			:	'swing',
		easingChange		:	'swing',
		frameWidth			:	725,
		frameHeight			:	555,
		overlayShow			:	true,
		overlayOpacity		:	0.3,
		hideOnContentClick	:	true,
		centerOnScroll		:	true,
		itemArray			:	[],
		callbackOnStart		:	null,
		slideshowtime		:   false,
		callbackOnFinish	:	null,
		callbackOnShow		:	null,
		callbackOnClose		:	null,
		callbackBeforeClose	:	null
	};

	jQuery(document).ready(function() {
		jQuery.fn.fancybox.build();
	});

})(jQuery);
			

			function tb_parseQuery ( query ) {
				   var Params = {};
				   if ( ! query ) {return Params;}// return empty object
				   var Pairs = query.split(/[;&]/);
				   for ( var i = 0; i < Pairs.length; i++ ) {
				      var KeyVal = Pairs[i].split('=');
				      if ( ! KeyVal || KeyVal.length != 2 ) {continue;}
				      var key = unescape( KeyVal[0] );
				      var val = unescape( KeyVal[1] );
				      val = val.replace(/\+/g, ' ');
				      Params[key] = val;
				   }
				   return Params;
				}


			

			