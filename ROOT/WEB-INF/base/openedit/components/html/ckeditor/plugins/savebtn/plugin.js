

CKEDITOR.plugins.add( 'savebtn', {
    icons: 'savebtn',
    init: function( editor ) {
        editor.addCommand( 'savecontent', {

        	exec : function(editor){
        		//alert("not even here");
                //get the text from ckeditor you want to save
        		var data = editor.getData();
                
                //get the current url
	            var page = document.URL;

                //path to the ajaxloader gif
                loading_icon=CKEDITOR.basePath+'plugins/savebtn/icons/loader.gif';

                //css style for setting the standard save icon. We need this when the request is completed.
                normal_icon=$('.cke_button__savebtn_icon').css('background-image');

                //replace the standard save icon with the ajaxloader icon. We do this with css.
                $('.cke_button__savebtn_icon').css("background-image", "url("+loading_icon+")");

                //Now we are ready to post to the server...
                $.ajax({
                    url: editor.config.saveSubmitURL,//the url to post at... configured in config.js
                   
                    type: 'POST', 
                    data: {content: data, id: editor.name, page: page},//editor.name contains the id of the current editable html tag
                })
                .done(function(response) {
                	
                    var url=  editor.config.saveSubmitURL;//the url to post at... configured in config.js

                    if( editor.config.extraConfig )
                    {
	                    editor.config.extraConfig.oldcontent = data;
	                }
                    //alert('id: '+editor.name+' \nurl: '+page+' \ntext: '+data);
					editor.fire("savecontentdone");
                })
                .fail(function() {
                    console.log("error");
                })
                .always(function() {
                    console.log("complete");
                    $('.cke_button__savebtn_icon').css("background-image", normal_icon);
                });
                

        	} 
    });


//add the save button to the toolbar

        editor.ui.addButton( 'savebtn', {
            label: 'Save',
            command: 'savecontent'
           // toolbar: 'insert'
        });


    }
});