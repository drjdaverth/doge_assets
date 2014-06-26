// Aspera Connect Installer JavaScript Library
// Revision: 2009-09-21
//
// http://www.asperasoft.com/developer

function ConnectInstaller( aw_id, autoinstall_location ) {
    this.url = autoinstall_location;
    this.aspera_web_id = aw_id;

    var agent = navigator.userAgent;
    
    this.applet_supported = true;
    this.os = "";

    // change all of the windows lines to 
    // agent.indexOf("Windows") != -1) this.os = "windows-32";
    if (agent.indexOf("Windows NT 6") != -1) this.os = "windows-32"; //Vista
    if (agent.indexOf("Windows NT 7") != -1) this.os = "windows-32"; //Win7
    if (agent.indexOf("Windows NT 5") != -1) this.os = "windows-32"; //Win XP and 2003
    if (agent.indexOf("Intel Mac") != -1)    this.os = "mac-intel";
    if (agent.indexOf("PPC Mac") != -1)      this.os = "mac-ppc";
    if (agent.indexOf("Linux") != -1)        this.os = "linux-32";
    if (agent.indexOf("Linux x86_64") != -1) this.os = "linux-64";

    if (((agent.indexOf("Firefox/3.6.12") != -1) && (this.os === "mac-intel")) ||
        ((agent.indexOf("Firefox/3.6.11") != -1) && (this.os === "mac-intel")) ||
        (agent.indexOf("Firefox/2") != -1) ||
        (agent.indexOf("Chrome/7.0.517.44") != -1) ||
        (agent.indexOf("Chrome/8") != -1) ||
        (agent.indexOf("Chrome/9") != -1) ||
        (agent.indexOf("Linux x86_64") != -1) ||
        (agent.indexOf("Linux") != -1) ||
        (agent.indexOf("Windows NT 5.0") != -1) ||
        (agent.indexOf("PPC Mac") != -1)) {
        this.applet_supported = false;
    }
}

new ConnectInstaller();

function createInstallerEventCallback(state, description, percent) {
    e = new ConnectInstallerEvent();
    e.init(state, percent, description);
    aspera_installer_callback_function(e);
    if (state == "COMPLETE") {
        var date = new Date();
        var expire_time = new Date( date.getTime() + 30000 );
        var expiryString = expire_time.toGMTString();
        document.cookie = 'appletInstalled=yes;expires='+expiryString;
    }
}

// until we figure out how to call a prototyped function from the applet
// this global will suffice.
var aspera_installer_callback_function;

ConnectInstaller.prototype = {

    asperaWebInstalled: function() {
        if (readCookie('appletInstalled') == 'yes') {
            return true;
        }
        var connect_installed=true;
        try {
            var asperaWeb = new AsperaWeb(this.aspera_web_id);
            if( !asperaWeb.isInstalled() ) {
                connect_installed=false;
            }
        } catch( error ) {
            connect_installed=false;
        }
        return connect_installed;
    },

    platformSupportsApplet: function() {
        return this.applet_supported && this.platformSupportsConnect() && this.javaAvailable();
    },

    platformSupportsConnect: function() {
        return (this.os != "");
    },

    javaAvailable: function() {
        var minVer = "1.5.0";
        jreList = deployJava.getJREs().toArray();
        for (var i=0; i<jreList.size(); i++){
            if (!this.versionLessThan(jreList[i],minVer))
                return true;
        }
        return false;
    },

    updateAvailable: function() {
        // if (readCookie('appletInstalled') == 'yes') {
        //     return false;
        // }
        this.latest_version = this.getLatestConnectVersion(this.os);
        var aw = new AsperaWeb(this.aspera_web_id);
        var awv = aw.queryBuildVersion();
        return this.versionLessThan(awv, this.latest_version);
    },

    getLatestConnectVersion: function( os ) {
        return connect_versions[os.replace('-','_')];
    },
    
    installLatest: function(callback) {
        aspera_installer_callback_function = callback;
        //if (this.updateAvailable()) {
            
        var codepath = "aspera/install/applet/InstallerApplet.class";
        var jar = "aspera-connect-installer-applet.jar";
        var cbk = "createInstallerEventCallback";
        //cbk = "call_this";

        var appletElement = document.createElement('div');
        appletElement.setAttribute('id','aph');
        document.body.appendChild(appletElement);
        
        if (/msie/i.test(navigator.userAgent)) {
            appletElement.innerHTML=
                '<object id="applet"' +
                'height="0"' +
                'width="0"' +
                'classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"' +
                'codebase= "">' +
                '<param name="codebase"' +
                'value="' + this.url + '"/>' +
                '<param name="code"' +
                'value="'+codepath+'"/>' +
                '<param name="archive"' +
                'value="'+jar+'"/>' +
                '<param name="mayscript"' +
                'value="true"/>' +
                '<param name="callback"'+
                'value="'+cbk+'"/>' +
                '<param name="useragent"'+
                'value="'+navigator.userAgent+'"/>' +
                '</object>';
        } else {
            appletElement.innerHTML=
                '<object id="applet"' +
                'height="0"' +
                'width="0"' +
                'classid="java:'+codepath+'"' +
                'type="application/x-java-applet"' +
                'archive="'+jar+'">' +
                '<param name="codebase"'+
                'value="'+ this.url +'"/>' +
                '<param name="code"'+
                'value="'+codepath+'"/>' +
                '<param name="mayscript"' +
                'value="true"/>' +
                '<param name="callback"' +
                'value="'+cbk+'"/>' +
                '<param name="useragent"'+
                'value="'+navigator.userAgent+'"/>' +
                '</object>';
        }
    },

    reloadPlugins: function() {
        if (/msie/i.test(navigator.userAgent)) {
            throw("Plugin reload is not yet supported in IE");
        } else {
            navigator.plugins.refresh();
        }
    },

    //Internal use only. Not part of the API.
    versionToArray: function( version ) {
        return version.split(".").map(function(s) {
                return parseInt(s);
            });
    },


    // Internal use only. Not part of the API.
    // Returns true if version string 'a' is less than version string 'b'
    //     "1.2.1" < "1.11.3" 
    //     "1.1"   < "2.1"
    //     "1"     = "1"
    //     "1.2"   < "2"
    // Note the following behavior:
    //     "1"     = "1.2"
    //     "1.2"   = "1"
    //   This helps with upgrade checks.  If at least version "4" is required, and 
    //   "4.4.2" is installed, versionLessThan("4.4.2","4") will return false.
    versionLessThan: function( a,b ) {
        var a_arr = this.versionToArray(a);
        var b_arr = this.versionToArray(b);
        var i;
        for ( i = 0; i < Math.min(a_arr.length, b_arr.length); i++ ) {
            // if i=2, a=[0,0,1,0] and b=[0,0,2,0]
            if( a_arr[i] < b_arr[i] ) {
                return true;
            } 
            // if i=2, a=[0,0,2,0] and b=[0,0,1,0]
            if( a_arr[i] > b_arr[i] ) {
                return false;
            } 
            // a[i] and b[i] exist and are equal:
            // move on to the next version number
        }
        // all numbers equal (or all are equal and we reached the end of a or b)
        return false;
    }

};

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

function ConnectInstallerEvent() {}

new ConnectInstallerEvent();

// An ConnectInstallerEvent is passed in the callback during the 
// java-applet-based installer.  Each event has these properties:
//
//   1) state: one of "START", "COMPLETE", "DOWNLOAD", "INSTALL",
//          or "ERROR"
//   2) description: A user-friendly description of the event.
//   3) percent: The overall progress toward completion.
//   4) exceptions: An Array of exceptional conditions that may
//          be important.  Currently, the only possible condition is
//            ConnectInstallerEvent.IE_PLUGIN_NOT_INSTALLED
//          which can only happen during the "COMPLETE" event.
//
ConnectInstallerEvent.prototype = {
    
    IE_PLUGIN_NOT_INSTALLED: "Plugin installation for Internet Explorer failed.",

    init: function(state, percent, description) {
        this.state = state;
        this.loadExceptions(); // modifies this.state
        this.description = this.convertStateToString(state,description);
        this.percent = this.inferPercent(state,percent);
    },


    //Internal use only. Not part of the API.
    loadExceptions: function() {
        this.exceptions = new Array();
        switch (this.state) {
          case "COMPLETE-NO-IE": 
            this.exceptions.push(this.IE_PLUGIN_NOT_INSTALLED);
            break;
          default:
            break;
        }        
        if (this.state == "COMPLETE-NO-IE") {
            // The lack of IE support should be communicated in something 
            // other than 'state'.  
            this.state = "COMPLETE";
        }
    },

    //Internal use only. Not part of the API.
    inferPercent: function(state,percent) {
        switch (state) {
        case "START":  return 0;

        case "COMPLETE":
        case "COMPLETE-NO-IE":  return 100;

        case "DOWNLOAD":
        case "INSTALL":
        case "ERROR":
        default:  return percent;
        }
    },
    //Internal use only. Not part of the API.
    convertStateToString: function(state,description) {
        s = "";
        var lstate = ""+state;
        
        switch (lstate) {
        case "START":          s="Starting the installation."; break;
        case "DOWNLOAD":       s="Downloading Aspera Connect."; break;
        case "COMPLETE":       s="Installation complete."; break;
        case "COMPLETE-NO-IE": s="Installation complete with no support for Internet Explorer.";  break;
        case "ERROR":          s=description; break;
        case "INSTALL":        s="Installing Aspera Connect."; break;
        default:               s="Installing Aspera Connect.";
        }
        return s;
    }
};
