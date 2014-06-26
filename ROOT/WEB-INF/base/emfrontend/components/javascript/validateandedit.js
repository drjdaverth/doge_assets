
var openDetail = "";
var app,home,apphome,themeprefix;



jQuery(document).ready(function() 
		{
			app = jQuery("#application");
			home =  app.data("home");
			apphome = home + app.data("apphome");
			themeprefix = app.data("home") + app.data("themeprefix");	
		}
);


showPicker = function(detailid)
{
	openDetail=detailid;
	if(!window.name)window.name='admin_parent';
	window.open( home + '/system/tools/newpicker/index.html?parentName='+window.name+'&detailid='+detailid, 'pickerwindow','alwaysRaised=yes,menubar=no,scrollbars=yes,width=1000,x=100,y=100,height=600,resizable=yes' );
	return false;
}

//TODO: Does this need to be defined on the page itself? 
SetPath = function( inUrl )
{
	var input = document.getElementById(openDetail + ".value");
	input.value = inUrl;
}
	
/*
validate = function(inCatalogId, inDataType, inView , inFieldName)
{
	var val = $("#list-" + inFieldName).val();
	var div = '#error_' + inFieldName;
	var params = {
			catalogid: inCatalogId,
			searchtype: inDataType,
			view: inView,
			field: inFieldName,
			value: val
		};
	//alert( params );
	jQuery(div).load(apphome + '/components/xml/validatefield.html', params);
}
*/

var listchangelisteners = []; //list nodes

findOrAddNode = function(inParentId)
{
	//alert("Looking now: " + inParentId + listchangelisteners.length );
	for (var i=0; i<listchangelisteners.length; i++) 
	{
		var node = listchangelisteners[i];
		if( node.parentid == inParentId )
		{
			return node;
		}
	}
	var node = new Object();
	node.parentid = inParentId;
	node.children = [];
	listchangelisteners.push(node);
	
	return node;
}

addListListener = function( inParentFieldName, inFieldName )
{
	//an array of array
	var node = findOrAddNode(inParentFieldName);
	node.children.push(inFieldName); //append the child
}

//When a field is changed we want to validate it and update any listeners
//Find all the lists in this form. Update all of them that are marked as a listener
//parent = businesscategory, child = lob, field = product
updatelisteners = function(catalogid, searchtype,view , fieldname)
{
	var val = $("#list-" + fieldname).val();

	//validate(catalogid, searchtype, view , fieldname);
	
	var node = findOrAddNode(fieldname);
	
	if( node.children )
	{
		for( var i=0;i< node.children.length;i++)
		{
			var childfieldname = node.children[i];
			var element = $("#list-" + childfieldname);
			if(element.options !== undefined) {
				var valueselection = element.options[element.selectedIndex].value;
			}
			var div="listdetail_" + childfieldname;
			//we are missing the data element of the children
			//required: catalogid, searchtype, fieldname, value
			//optional: query, foreignkeyid and foreignkeyvalue
			jQuery("#" + div).load(apphome + '/components/xml/list.html', {catalogid:catalogid, searchtype:searchtype, view:view, fieldname:childfieldname, foreignkeyid:fieldname, foreignkeyvalue:val, value:valueselection, oemaxlevel:1});
		}
	}
}


//searchtype parentname 

loadlist = function(indiv, catalogid, searchtype, inlabel, childfieldname, foreignkeyid, foreignkeyvalue, value )
{
	//what is this?
	jQuery(indiv).load(apphome + '/components/xml/types/simplelist.html', {catalogid:catalogid, searchtype:searchtype, fieldname:childfieldname, label:inlabel, foreignkeyid:foreignkeyid, foreignkeyvalue:foreignkeyvalue, value:value, oemaxlevel:1});
}
//Don't use any form inputs named 'name'!
postForm = function(inDiv, inFormId)
{
	var form = document.getElementById(inFormId);
	if( jQuery )
	{
		var targetdiv = inDiv.replace(/\//g, "\\/");
		jQuery(form).ajaxSubmit( 
			{
				target:"#" + targetdiv
			}
		);	
	}
	else
	{
		form = Element.extend(form);
		var oOptions = { 
		    method: 'post',
		    parameters: form.serialize(true), 
		    evalScripts: true,
			asynchronous: false,
	        onFailure: function (oXHR, oJson) {
	              alert("An error occurred: " + oXHR.status);
	        }
	     };
	
		jQuery("#"+inDiv).load( form.action, oOptions);
	}	
	return false;
}	

postPath = function(inCss, inPath, inMaxLevel)
{
	if( inMaxLevel == null )
	{
		inMaxLevel = 1;
	}
	jQuery("#"+inCss).load( inPath,{oemaxlevel: inMaxLevel });	
	return false;
}

toggleBox = function(inId, togglePath, inPath)
{
	jQuery("#"+inId).load( home + togglePath,{ pluginpath: inPath, propertyid: inId });
}	
