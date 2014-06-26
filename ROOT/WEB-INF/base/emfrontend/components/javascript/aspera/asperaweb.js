// Aspera Web JavaScript Library
// Revision: 2006-10-17
//
// http://www.asperasoft.com/

function AsperaWeb(id) {
  this.id = id;
}

new AsperaWeb();

AsperaWeb.prototype = {
  isInstalled: function() {
    if (navigator.userAgent.indexOf('MSIE') != -1) {
      try {
        var activex = new ActiveXObject('Aspera.AsperaWebCtrl.1');
        return true;
      }
      catch (error) {
        return false;
      }
    }
    else {
      for (var ix = 0; ix < navigator.plugins.length; ix++) 
      {
    	var plugin = navigator.plugins[ix];
    	//alert(plugin + " at " + ix + " from " + navigator.plugins.length);
    	if (typeof plugin  == "undefined") 
    	{
    		continue;
    	}
        if (plugin.name.substring(0,10) == "Aspera Web") 
        {
		  return true;
        }
      }
	}
	
	return false;
  },

  download: function(sourceURL) {
    document.getElementById(this.id).startDownload(sourceURL);
  },

  downloadToPath: function(sourceURL, destinationPath) {
    document.getElementById(this.id).startDownloadToPath(sourceURL, destinationPath);
  },

  downloadURLArray: function(sourceURLArray) {
    var plugin = document.getElementById(this.id);

    for (var i = 0; i < sourceURLArray.length; i++) {
      plugin.addToDownloadList(sourceURLArray[i]);
    }
    plugin.startListDownloadToPath("");
    plugin.clearDownloadList();
  },

  downloadURLArrayToPath: function(sourceURLArray, destinationPath) {
    var plugin = document.getElementById(this.id);

    for (var i = 0; i < sourceURLArray.length; i++) {
      plugin.addToDownloadList(sourceURLArray[i]);
    }
    plugin.startListDownloadToPath(destinationPath);
    plugin.clearDownloadList();
  },

  uploadToURL: function(sourcePath, destinationURL) {
    document.getElementById(this.id).startUploadToURL(sourcePath, destinationURL)
  },

  uploadPathArrayToURL: function(sourcePathArray, destinationURL) {
    var plugin = document.getElementById(this.id);

    for (var i = 0; i < sourcePathArray.length; i++) {
      plugin.addToUploadList(sourcePathArray[i]);
    }
    plugin.startListUploadToURL(destinationURL);
    plugin.clearUploadList();
  },

  runOpenFileDialog: function(allowDirs, allowMulti) {
    var pathSet = document.getElementById(this.id).runOpenFileDialog(allowDirs, allowMulti);
    if (pathSet.length == 0) {
      return;
    }
    return this.unescapePathSet(pathSet);
  },

  runOpenFolderDialog: function(allowMulti) {
    var pathSet = document.getElementById(this.id).runOpenFolderDialog(allowMulti);
    if (pathSet.length == 0) {
       return;
    }
    return this.unescapePathSet(pathSet);
  },
  runSaveFileDialog: function(suggestedName) {
    var path = document.getElementById(this.id).runSaveFileDialog(suggestedName);
    if (path.length == 0) {
      return;
    }
    return path;
  },

  promptAndDownload: function(sourceURL, suggestedName) {
    var path = this.runSaveFileDialog(suggestedName);
    if (path) {
      this.downloadToPath(sourceURL, path);
    }
  },

  promptAndDownloadURLArray: function(sourceURLArray, suggestedName) {
    var path = this.runSaveFileDialog(suggestedName);
    if (path) {
      this.downloadURLArrayToPath(sourceURLArray, path);
    }
  },

  promptAndUploadToURL: function(destinationURL, allowDirs, allowMulti) {
    var pathArray = this.runOpenFileDialog(allowDirs, allowMulti);
    if (pathArray) {
      this.uploadPathArrayToURL(pathArray, destinationURL);
    }
  },
  promptAndUploadFolderToURL: function(destinationURL, allowMulti) {
    var pathArray = this.runOpenFolderDialog(allowMulti);
    if (pathArray) {
       this.uploadPathArrayToURL(pathArray, destinationURL);
    }
  },

  displayTransferManager: function() {
    return document.getElementById(this.id).displayTransferManager();
  },

  displayPreferencesWindow: function() {
    return document.getElementById(this.id).displayPreferencesWindow();
  },

  displayAboutWindow: function() {
    return document.getElementById(this.id).displayAboutWindow();
  },
  
  getName: function() {
    return document.getElementById(this.id).name;
  },
  
  setName: function(value) {
    document.getElementById(this.id).name = value;
  },

  queryVersion: function() {
    return document.getElementById(this.id).queryVersion();
  },
  
  queryBuildVersion: function() {
    return document.getElementById(this.id).queryBuildVersion();
  },

  queryBrowserEngine: function() {
    return document.getElementById(this.id).queryBrowserEngine();
  },

  unescapePathSet: function(string) {
    var pathArray = new Array();
    var path = "";
    var idx = 0;

    idx = string.search(/[^\\]\:/);
    while (idx != -1) {
      path = string.slice(0, idx + 1);
      path = path.replace(/\\\\/g, "\\");
      path = path.replace(/\\\:/g, "\:");
      pathArray.push(path);

      string = string.slice(idx + 2);
      idx = string.search(/[^\\]\:/);
    }

    string = string.replace(/\\\\/g, "\\");
    string = string.replace(/\\\:/g, "\:");
    pathArray.push(string);

    return pathArray;
  }
}
