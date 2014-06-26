

CKEDITOR.plugins.add( 'closebtn', {
    icons: 'closebtn',
    init: function( editor ) {
        editor.addCommand( 'closeinline', {

        	exec : function(editor){
        		if(editor.checkDirty()){
        			var save = confirm("Are you sure?  You have unsaved changes.");
        			if(save){
                		location.reload();        				
        			}
        		}
        		
                

        	} 
    });




        editor.ui.addButton( 'closebtn', {
            label: 'Close',
            command: 'closeinline'
           // toolbar: 'insert'
        });


    }
});