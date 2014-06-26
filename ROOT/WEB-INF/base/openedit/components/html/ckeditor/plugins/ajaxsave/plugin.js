/*
Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

/**
 * @file entermedia plugin
 */
(function(){
	CKEDITOR.plugins.add( 'ajaxsave',
	{
		init : function( editor )
		{
			var command = editor.addCommand( 'ajaxsave',
			{
			    exec : function( editor )
			    {
			    	var savepath = CKEDITOR.config.saveSubmitURL;
		    	  	var data = event.editor.getData();
                    var request = jQuery.ajax({
                        url: savepath,
                        type: "POST",
                        data: {
                            content : data
                        },
                        dataType: "html"
                    });
			    },
			    async : true   
			});
			
			command.modes={wysiwyg:1,source:0};
			command.canUndo=false;
			editor.ui.addButton("ajaxsave",{
					label:"Save",
					command:"ajaxsave",
					icon:this.path+"save.gif"
			});
		}
	});
})();




/**
 * The minimum height to which the editor can reach using entermedia.
 * @name CKEDITOR.config.entermedia_minHeight
 * @type Number
 * @default 200
 * @since 3.4
 * @example
 * config.entermedia_minHeight = 300;
 */

/**
 * The maximum height to which the editor can reach using entermedia. Zero means unlimited.
 * @name CKEDITOR.config.entermedia_maxHeight
 * @type Number
 * @default 0
 * @since 3.4
 * @example
 * config.entermedia_maxHeight = 400;
 */

/**
 * Fired when the entermedia plugin is about to change the size of the editor.
 * @name CKEDITOR#entermedia
 * @event
 * @param {Number} data.currentHeight The current height of the editor (before the resizing).
 * @param {Number} data.newHeight The new height of the editor (after the resizing). It can be changed
 *				to determine another height to be used instead.
 */
