/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.NetBeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


(function() {
    const ignoreThese = /about:|javascript:|resource:|chrome:|jar:/;
    const DEBUG = false;

    //Should we move this to constants.js?
    const STATE_IS_WINDOW = NetBeans.Constants.WebProgressListenerIF.STATE_IS_WINDOW;
    const STATE_IS_DOCUMENT = NetBeans.Constants.WebProgressListenerIF.STATE_IS_DOCUMENT;
    const STATE_IS_NETWORK = NetBeans.Constants.WebProgressListenerIF.STATE_IS_NETWORK;
    const STATE_IS_REQUEST = NetBeans.Constants.WebProgressListenerIF.STATE_IS_REQUEST;

    const STATE_START = NetBeans.Constants.WebProgressListenerIF.STATE_START;
    const STATE_STOP = NetBeans.Constants.WebProgressListenerIF.STATE_STOP;
    const STATE_TRANSFERRING = NetBeans.Constants.WebProgressListenerIF.STATE_TRANSFERRING;
    const STORE_ANYWHERE = NetBeans.Constants.CacheIF.STORE_ANYWHERE;
    const ACCESS_READ = NetBeans.Constants.CacheIF.ACCESS_READ;
    const SEEK_SET = NetBeans.Constants.SeakableStreamIF.NS_SEEK_SET;


    const observerService = NetBeans.Utils.CCSV(
        NetBeans.Constants.ObserverServiceCID,
        NetBeans.Constants.ObserverServiceIF);

    const NOTIFY_ALL= NetBeans.Constants.WebProgressIF.NOTIFY_ALL;

    const mimeExtensionMap =
    {
        "txt": "text/plain",
        "html": "text/html",
        "htm": "text/html",
        "xhtml": "text/html",
        "xml": "text/xml",
        "css": "text/css",
        "js": "application/x-javascript",
        "json": "application/json",
        "jpg": "image/jpeg",
        "jpeg": "image/jpeg",
        "gif": "image/gif",
        "png": "image/png",
        "bmp": "image/bmp",
        "swf": "application/x-shockwave-flash"
    };

    const mimeCategoryMap =
    {
        "text/plain": "txt",
        "application/octet-stream": "bin",
        "text/html": "html",
        "text/xml": "html",
        "text/css": "css",
        "application/x-javascript": "js",
        "text/javascript": "js",
        "application/javascript" : "js",
        "application/json" : "json",
        "image/jpeg": "image",
        "image/gif": "image",
        "image/png": "image",
        "image/bmp": "image",
        "application/x-shockwave-flash": "flash"
    };

    var cacheSession = null;
    var socket;
    var topWindow;
    var myContext;
    var myBrowser;
    var contexts = [];

    function Requests() {
        this.ids = {};
        this.getId = function( element ) {
            for ( var i in this.ids ){
                if( this.ids[i] instanceof NetBeans.Constants.HttpChannelIF) {
                  if ( this.ids[i] == element ){
                      return i;
                  }
                }
            }
            return null;
        }

        this.setId = function(i, element) {
            this.ids[i] = element;
        }
    }
    
    var requestsId = new Requests();

    this.initMonitor = function  (context, browser, _socket) {
        myContext = context;
        myBrowser = browser;
        var index = contexts.indexOf(context);
        if (DEBUG) NetBeans.Logger.log("net.initMonitor Turning on Monitor");
        if(  index == -1 ){
            contexts.push(context);
            if (context.window) {
                topWindow = context.window;
            } else {
                topWindow = getBrowser().contentWindow;
            }
            monitorContext(context, browser);
            if( !_socket )
                NetBeans.Logger.log("net.initMonitor - Socket is null");
            socket = _socket;
            if (DEBUG) NetBeans.Logger.log("net.initMonitor Monitor turned on.");
        } //else {  NetBeans.Logger.log("No need to do anything because it is already on."); }
    }

    this.destroyMonitor = function(context, browser) {
        myContext = null;
        var index = contexts.indexOf(context);
        if (DEBUG) NetBeans.Logger.log("net.initMonitor Turning off Monitor");
        if(  index != -1 ){
            contexts.splice(index, 1);
            unmonitorContext(context, browser);
            socket = null;
            topWindow = null;
            if (DEBUG) NetBeans.Logger.log("net.initMonitor Monitor turned off.");
        } //else {  NetBeans.Logger.log("No need to do anything because it is already off.");}
    }

    this.destroyAllMonitors = function(browser) {
        if (!browser) {
            browser = getBrowser();
        }

        for (var i = 0; i < contexts.length; i++) {
            var nextContext = contexts[i];
            unmonitorContext(nextContext, browser);
        }

        contexts = [];
    }

    var NetObserver =
    {
        QueryInterface: function(iid)
        {
            if( iid.equals(NetBeans.Constants.SupportsIF) ||
                iid.equals(NetBeans.Constants.ObserverIF))
                {
                return this;
            }

            throw NetBeans.Constants.NS_NOINTERFACE;
        },

        // nsIObserver
        //@type {nsIHttpChannel} channel
        observe: function(aNsISupport, topic, data)
        {

            if (topic == "http-on-modify-request") {
                this.onModifyRequest(aNsISupport);
            } else if (topic == "http-on-examine-response") {
                this.onExamineResponse(aNsISupport);
            } else {
                if (DEBUG) {
                    NetBeans.Logger.log("netmonitor.observer: topic is: " + topic);
                }
            }

        },
        /*
         * @param {nsISupport} aNsISupport
         * @type {nsIHttpChannel} request
         * @type {NetActivity} activity
         */
        onModifyRequest: function (aNsISupport) {
            var DEBUG_METHOD = (true & DEBUG);
            var request = aNsISupport.QueryInterface(NetBeans.Constants.HttpChannelIF);

            if ( isRelevantWindow(request) ){
                if (DEBUG_METHOD) {
                    NetBeans.Logger.log("netmonitor.onModifyRequest: push " + request.URI.asciiSpec);
                }
                if ( request.loadFlags & request.LOAD_INITIAL_DOCUMENT_URI ){
                    if (DEBUG_METHOD) NetBeans.Logger.log("netmonitor.onModifyRequest: reset on request.loadFlags");
                    requestsId = new Requests();
                }

                if (DEBUG_METHOD) NetBeans.Logger.log("netmonitor.onModifyRequest: processing modifyRequest");
                if (requestsId.getId(request)) {
                    return;
                }
                var id = uuid();
                var activity = createRequestActivity(request, id);
                if ( activity ){
                  requestsId.setId(id, request);
                  sendNetActivity(activity);
                } else if( DEBUG_METHOD ){
                    NetBeans.Logger.log("net.onModifyRequest - activity is null");
                }
            }

        },


        onExamineResponse: function( aNsISupport ){
            var DEBUG_METHOD = (true & DEBUG);
            var request = aNsISupport.QueryInterface(NetBeans.Constants.HttpChannelIF);
            if (DEBUG_METHOD) { NetBeans.Logger.log("<-----  netmonitor.onExamineResponse: " + request.URI.asciiSpec);}
            var id = requestsId.getId(request)
            if ( id ) {
              var notificationCallbacks = request.notificationCallbacks;
              if (!notificationCallbacks && request.loadGroup) {
                  notificationCallbacks = request.loadGroup.notificationCallbacks;
              }

              var xhrRequest;
              if (notificationCallbacks) {
                  try {
                    xhrRequest = notificationCallbacks.getInterface(NetBeans.Constants.XMLHttpRequestIF);
                  } catch (exc) {}
              }

              if( xhrRequest ){
                    xhrRequest.onreadystatechange = function () {
                      if (xhrRequest.readyState == 4) {
                         if(xhrRequest.status == 200) {
                            if ( DEBUG_METHOD ) { NetBeans.Logger.log("net.onExamineResponse - id:" + id);}
                            if ( DEBUG_METHOD ) { NetBeans.Logger.log("net.onExamineResponse - request:" + request);}
                            processExamineResponse(request, id, xhrRequest);
                         } else {if ( DEBUG_METHOD ) { NetBeans.Logger.log("net.onExamineResponse - failure to load responseText \n");}}
                       }
                   };
              } else {
                  processExamineResponse(request, id, null);
              }
              delete requestsId[id];
              return;
            } else if (DEBUG_METHOD){
                  NetBeans.Logger.log("net.onExamineResponse - Did not recognize response for: " + request.URI.asciiSpec);
            }
        }
    }
        
    function processExamineResponse( request, id, xhrRequest){
          var DEBUG_METHOD = (true & DEBUG);
          if (DEBUG_METHOD){ NetBeans.Logger.log("net.processExaminResponse: ");}
          var activity = createResponseActivity(request, id, xhrRequest);
          if (DEBUG_METHOD){ NetBeans.Logger.log("net.processExaminResponse: activity created");}
          if ( activity ) {
            sendExamineNetResponse(activity);
          } else if (DEBUG_METHOD){ NetBeans.Logger.log("net.onExamineResponse - activity is null"); }

    }

    function createRequestActivity(request, id){
        var DEBUG_METHOD = (true & DEBUG);
        if (DEBUG_METHOD) NetBeans.Logger.log("netmonitor.createRequestActivity: Start");
        var activity = new NetActivity();
        activity.uuid = id;
        activity.name = request.name;
        activity.method = request.requestMethod;
        activity.requestHeaders = getHttpRequestHeaders(request);
        activity.time = nowTime();
        activity.url = request.URI.asciiSpec;
        activity.category = getRequestCategory(request);
        activity.load_init = request.loadFlags & request.LOAD_INITIAL_DOCUMENT_URI;
        if ( activity.method == "post" || activity.method == "POST") {
            activity.postText = getPostText(activity, request, myContext, activity.requestHeaders);
        } else {
            activity.urlParams = parseURLParams(request.name);
            if (DEBUG_METHOD){
                NetBeans.Logger.log("netBeans.onModifyRequest - request.name:" + request.name);
                NetBeans.Logger.log("netBeans.onModifyRequest - urlParams:" + activity.urlParams);
            } 
        }
        return activity;
    }

    function createResponseActivity (request, id, xhrRequest) {
        var DEBUG_METHOD = (true & DEBUG);

        if( !request || !id){
            throw new Error("net.createResponseActivity - Something is null request:" + request + " id:" + id);
        }

        var activity = new NetActivity();
        activity.time = nowTime();
        activity.uuid = id;
        activity.name = request.name;
        activity.responseHeaders = getHttpResponseHeaders(request);
        activity.url = request.URI.asciiSpec;
        if (!activity.mimeType) {
            activity.mimeType = getMimeType(request);
           // if( DEBUG_METHOD && ! activity.mimeType ){ NetBeans.Logger.log("Activity mime type is null for:" + activity.url); }
        }
        activity.size = request.contentLength;
        if( xhrRequest){
            activity.category = "xhr"; // Temporary using this string since getRequestCategory returns html if called a second time.  Still not sure why.
            activity.responseText = xhrRequest.responseText;
            activity.status = request.responseStatus;
            if (DEBUG_METHOD){
                NetBeans.Logger.log("net.createResponseActivity: xhrRequest:" + xhrRequest.responseText);
                NetBeans.Logger.log("net.createResponseActivity: responseStatus:" + request.responseStatus);
                NetBeans.Logger.log("net.createResponseActivity: xhrRequest.status:" + xhrRequest.status);
            }
        } else {
            activity.category = getRequestCategory(request);
            activity.responseText = getResponseText(request);
            activity.status = request.responseStatus;
        }
        //if ( DEBUG_METHOD ){ NetBeans.Logger.log("Response Status:" + request.responseStatus);}
         /* Response File Loaded: */
        if (activity.status == "304") {
            NetBeans.Logger.log("CACHED ENTRY");
              activity.fromCache = true;
//            getCacheEntry(activity);
        } else {
            activity.fromCache = false;
        }

        return activity;
    }


//    function initCacheSession()
//    {
//        if (!cacheSession)
//        {
//            var cacheService = NetBeans.Utils.CCSV(NetBeans.Constants.CacheServiceCID, NetBeans.Constants.CacheServiceIF);
//            cacheSession = cacheService.createSession("HTTP", STORE_ANYWHERE, true);
//            cacheSession.doomEntriesIfExpired = false;
//        }
//    }


//    function getCacheEntry(activity) {
//            try
//            {
//                NetBeans.Logger.log("Initizlizing Cache Session");
//                initCacheSession();
//                cacheSession.asyncOpenCacheEntry(activity.url, ACCESS_READ, {
//                    onCacheEntryAvailable: function(descriptor, accessGranted, status)
//                    {
//                        if (descriptor)
//                        {
//                            if(activity.size == -1)
//                            {
//                                activity.size = descriptor.dataSize;
//                            }
//                            if(descriptor.lastModified && descriptor.lastFetched &&
//                                descriptor.lastModified < Math.floor(activity.time/1000)) {
//                                activity.fromCache = true;
//                            }
//                            activity.cacheEntry = [
//                              { name: "Last Modified",
//                                value: getDateFromSeconds(descriptor.lastModified)
//                              },
//                              { name: "Last Fetched",
//                                value: getDateFromSeconds(descriptor.lastFetched)
//                              },
//                              { name: "Expires",
//                                value: getDateFromSeconds(descriptor.expirationTime)
//                              },
//                              { name: "Data Size",
//                                value: descriptor.dataSize
//                              },
//                              { name: "Fetch Count",
//                                value: descriptor.fetchCount
//                              },
//                              { name: "Device",
//                                value: descriptor.deviceID
//                              }
//                            ];
//
//                            // Get contentType from the cache.
//                            descriptor.visitMetaData({
//                                visitMetaDataElement: function(key, value) {
//                                    if (key == "response-head")
//                                    {
//                                        var contentType = getContentTypeFromResponseHead(value);
////                                        activity.mimeType = getMimeType2(contentType, activity.name);
//                                        return false;
//                                    }
//
//                                    return true;
//                                }
//                            });
//
//                            // Update file category.
//                            if (activity.mimeType)
//                            {
//                                activity.category = getRequestCategoryFromMime(activity.mimeType);
//                            }
//                        }
//                    }
//                });
//            } catch (exc) {
//                if (DEBUG) NetBeans.Logger.log(exc);
//            }
//    }

    function getDateFromSeconds(s)
    {
        var d = new Date();
        d.setTime(s*1000);
        return d;
    }

    function getContentTypeFromResponseHead(value)
    {
        var values = value.split("\r\n");
        for (var i=0; i<values.length; i++)
        {
            var option = values[i].split(": ");
            if (option[0] == "Content-Type")
                return option[1];
        }
        return null;
    }

    function getResponseText ( aRequest ) {
        var DEBUG_METHOD = false & DEBUG;
        var responseText;
        var category = getRequestCategory(aRequest);

        if (category == "image"){
            return "IMAGE";
        }
        
        //Initiates a Get Request to Determine Response Text

        // XXX create a context (before we get one) hack
        if (myContext && !myContext.sourceCache) {
            myContext.sourceCache = new SourceCache(myContext);
            myContext.window = topWindow;
            myContext.browser = myBrowser;
        }
        try {
            responseText = myContext.sourceCache.loadText(aRequest.URI.asciiSpec, aRequest.requestMethod);
        } catch (exc) {}

        if (!responseText) {
            responseText = "BINARY";
        }
        if (DEBUG_METHOD){NetBeans.Logger.log("net.getResponseText - RESPONSE TEXT: " + responseText); }
        return responseText;
    }


    function getRequestCategory(aRequest)
    {
        try
        {
            if ((aRequest.notificationCallbacks && aRequest.notificationCallbacks instanceof XMLHttpRequest) ||
                (aRequest.loadGroup && aRequest.loadGroup.notificationCallbacks && aRequest.loadGroup.notificationCallbacks instanceof XMLHttpRequest)) {
                return "xhr";
            }
        }
        catch (exc) {}
        var mimeType = getMimeType(aRequest);
        return getRequestCategoryFromMime(mimeType);
    }

    function getRequestCategoryFromMime(mimeType){
        var DEBUG_METHOD = true & DEBUG;
        var category = mimeCategoryMap[mimeType];
        if (DEBUG_METHOD){ NetBeans.Logger.log("net.getRequestCategoryFromMime.category: " + category);}
        return category;
    }


    /*
     * isRelevantWindow - is the window a subclass of the window we are debugging?
     * @param {nsIHttpChannel} aRequest
     * @type {nsIDOMWindow} win
     * @return {bool}
     */
    function isRelevantWindow(aRequest) {
        var DEBUG_METHOD = (true & DEBUG);

        var webProgress = getRequestWebProgress(aRequest);
        var win = null;
        if( !webProgress){
            if (DEBUG_METHOD) NetBeans.Logger.log("net.isRelevantWindow - Your webprogress value is no good.");
            return false;
        }

        win = safeGetWindow(webProgress)
        //var win = webProgress ? safeGetWindow(webProgress) : null;
        if( !win || !( win instanceof NetBeans.Constants.DOMWindowIF)){
            if( DEBUG_METHOD ) NetBeans.Logger.log("ERROR: net.isRelevantWindow - null or not a DOMWINDOW");
            return false;
        }

        var result = isContainedWindow(win, topWindow);
        if (DEBUG_METHOD) NetBeans.Logger.logMessage("net.isRelevantWindow: isContainedWindow()=" + result);
        return result;
    }

    function isContainedWindow(win, top)
    {
        var currentTabId = getTabIdForWindow(win);
        var topTabId = getTabIdForWindow(top);
        if (!currentTabId) {
            if (DEBUG) NetBeans.Logger.logMessage("no currentTabId");
        }

        if (!topTabId) {
            if (DEBUG) NetBeans.Logger.logMessage("no tabId for topWindow");
        }

        return currentTabId && (!top || currentTabId == topTabId);
    }

    function getRootWindow(win)
    {
        for (; win; win = win.parent)
        {
            if (!win.parent || win == win.parent || !(win.parent instanceof Window) )
                return win;
        }
        return null;
    }

    function getTabIdForWindow(aWindow)
    {
        var topBrowser = window.getBrowser();
        aWindow = getRootWindow(aWindow);

        if (!aWindow || !topBrowser.getBrowserIndexForDocument)
            return null;

        try {
            var targetDoc = aWindow.document;

            var tab = null;
            var targetBrowserIndex = topBrowser.getBrowserIndexForDocument(targetDoc);

            if (targetBrowserIndex != -1)
            {
                tab = topBrowser.tabContainer.childNodes[targetBrowserIndex];
                return tab.linkedPanel;
            }
        } catch (ex) {}

        return null;
    }

    function NetProgressListener(context)
    {
        this.context = context;
    }

    NetProgressListener.prototype = {
        QueryInterface: function(iid)
        {
            if (iid.equals(NetBeans.Constants.WebProgressListenerIF)
                || iid.equals(NetBeans.Constants.SupportsWeakReferenceIF)
                || iid.equals(NetBeans.Constants.SupportsIF))
                {
                return this;
            }

            throw NetBeans.Constants.NS_NOINTERFACE;
        },
        //void onProgressChange ( nsIWebProgress webProgress , nsIRequest request , PRInt32 curSelfProgress , PRInt32 maxSelfProgress , PRInt32 curTotalProgress , PRInt32 maxTotalProgress )
        onProgressChange : function(progress, request, current, max, total, maxTotal )
        {
            if ( requestsId.getId(request) ){
                sendProgressUpdate(progress, request, current, max, total, maxTotal, nowTime());
            }
        },
        //void onLocationChange ( nsIWebProgress webProgress , nsIRequest request , nsIURI location )
        onLocationChange: function() {
        //NetBeans.Logger.log("On Location Change");
        },
        //void onSecurityChange ( nsIWebProgress webProgress , nsIRequest request , PRUint32 state )
        onSecurityChange : function() {
        //NetBeans.Logger.log("On Security Change");
        },
        //void onStatusChange ( nsIWebProgress webProgress , nsIRequest request , nsresult status , PRUnichar* message )
        onStatusChange : function() {
        //NetBeans.Logger.log("On Status Change");
        },
        //void onStateChange ( nsIWebProgress webProgress , nsIRequest request , PRUint32 stateFlags , nsresult status )
        onStateChange : function() {
        //NetBeans.Logger.log("On State Change");
        }
    }


    function monitorContext(aContext, browser)
    {

        if (!aContext.netProgressListener)
        {
            var netProgressListener = aContext.netProgressListener = new NetProgressListener(aContext);
            //Listening to the progress of the request
            browser.addProgressListener(netProgressListener, NOTIFY_ALL);

            observerService.addObserver(NetObserver, "http-on-modify-request", false);
            observerService.addObserver(NetObserver, "http-on-examine-response", false);
        }
    }

    // Maybe we should store browser inside context like firebug.
    function unmonitorContext(aContext,browser)
    {

        if (aContext.netProgressListener)
        {
            if (browser.docShell) {
                browser.removeProgressListener(aContext.netProgressListener, NOTIFY_ALL);
            }

            // XXXjoe We also want to do this when the context is hidden, so that
            // background files are only logged in the currently visible context
            observerService.removeObserver(NetObserver, "http-on-modify-request", false);
            observerService.removeObserver(NetObserver, "http-on-examine-response", false);

            delete aContext.netProgressListener;
        }
    }

    /*
     * @param {String} href
     * @return {String}
     */
    function parseURLParams( href ){

        if (!href){
            return "";
        }

        var hrefPieces = href.split("?");
        if ( hrefPieces.length != 2 ) {
            return null;
        }

        var searchString = hrefPieces[1];
        var nvPairs = searchString.split("&");
        return nvPairs;
    }

    function NetActivity (){
    }

    /*
     * @param {nsISupport} aRequest
     * @type {nsIHttpChannel} http
     * @return {NetActivity} activity
     */
    function getHttpResponseHeaders(aRequest) {
        var responseHeaders = [];
        try
        {
            aRequest.visitResponseHeaders({
                visitHeader: function(name, value)
                {
                    responseHeaders.push({
                        name: name,
                        value: value
                    });
                }
            });
        }
        catch (exc)
        {
            NetBeans.Logger.log("netmonitor.getHttpResponseHeaders: exception" + exc);
        } finally {
            return responseHeaders;
        }
    }


    /*
     * @param {nsISupport} aRequest
     * @type {nsIHttpChannel} http
     * @type {NetActivity} activity
     */
    function getHttpRequestHeaders( aRequest )
    {
        var requestHeaders = [];
        try
        {
            aRequest.visitRequestHeaders({
                visitHeader: function(name, value)
                {
                    requestHeaders.push({
                        name: name,
                        value: value
                    });
                }
            });
        }
        catch (exc) {
            NetBeans.Logger.log("netmonitor.getHttpRequestHeaders: exception" + exc);
        } finally {
            return requestHeaders;
        }

    }

//    function getMimeType(aRequest)
    function getMimeType(aRequest)
    {
        var DEBUG_METHOD = (true & DEBUG);
        if( DEBUG_METHOD ) NetBeans.Logger.log("net.getMimeType");
        if( !aRequest ){
            throw new Error("netmonitor.getMimeType - Invalid argument. Request is null");
        }

        try {
            var mimeType = aRequest.contentType;
            if ( mimeType && mimeCategoryMap.hasOwnProperty(mimeType) ){
                 if( DEBUG_METHOD ) NetBeans.Logger.log("net.getMimeType - File Extension:" + mimeType);
                return mimeType;
            }
        } catch (exc){ }
        var ext = getFileExtension(aRequest.name);
        if( DEBUG_METHOD ) NetBeans.Logger.log("net.getMimeType - File Extension:" + ext);
        if (ext) {
            var extMimeType = mimeExtensionMap[ext.toLowerCase()];
            return extMimeType ? extMimeType : null;
        }
        return null;
    }

   
    /*
     * @param {string} uri
     */
     function getFileExtension(url)
     {
        var lastDot = url.lastIndexOf(".");
        return url.substr(lastDot+1);
     }

    /*
     * On Observe when topic is "http-on-modify-request"
     * @param {NetActivity} aActivity
     */
    function sendNetActivity ( aActivity ){
        var netActivity = <http/>;
        netActivity.type="request";
        netActivity.id=aActivity.uuid;
        netActivity.method=aActivity.method;
        netActivity.timestamp=aActivity.time;
        netActivity.urlParams=aActivity.urlParams;
        netActivity.url = aActivity.url;
        netActivity.postText = aActivity.postText;
        netActivity.load_init = aActivity.load_init;
//        netActivity.category = aActivity.category;
        var headers = aActivity.requestHeaders;
        for( var header in headers ){
            var tmp = headers[header];
            netActivity.header[tmp.name] =  tmp.value;
        }
        if (DEBUG){
            NetBeans.Logger.log(netActivity.toXMLString());
        }

        socket.send(netActivity);
    }
    
    function loadXmlActivityResponse ( netActivity, activity ){
        if( !netActivity || !activity ){
            throw new Error("net.loadXmlActivity - NetAcitivity or Activity is null");
        }
        
        netActivity.name = activity.name;
        netActivity.status = activity.status;
        netActivity.size = activity.size;
        netActivity.contentType = activity.contentType;
        netActivity.url = activity.url;
        netActivity.mimeType = activity.mimeType;
        netActivity.category = activity.category;
        if( activity.responseText ){
            try {
              netActivity.responseText = window.btoa(activity.responseText);
            } catch ( exc ){ 
                NetBeans.Logger.log("netMonitor.sendProgresUpdate Exception:" + exc );
                netActivity.responseText = window.btoa("TEXT DISTORTED");
            }
        }
        var headers = activity.responseHeaders;
        for( var header in headers ){
            var tmp = headers[header];
            netActivity.header[tmp.name] =  tmp.value;
        }
        if( activity.fromCache ){
           var cacheEntries = activity.cacheEntry;
           for ( var entry in cacheEntries ){
              var valuePair = cacheEntries[entry];
              netActivity.cacheEntry[valuePair.name] = valuePair.value;
           }
        }
    }


    /*
     * On Observe when topic is "http-on-examine-request"
     * @param {NetActivity} aActivity;
     */
    function sendExamineNetResponse ( aActivity ){

        var netActivity = <http/>;
        netActivity.type = "response";
        netActivity.id = aActivity.uuid;
        netActivity.name = aActivity.name;
        netActivity.timestamp  = aActivity.time
        loadXmlActivityResponse( netActivity, aActivity);
        if(DEBUG){
            NetBeans.Logger.log(netActivity.toXMLString());
        }
        socket.send(netActivity);
    }

    function sendProgressUpdate(progress, aRequest, current, max, total, maxTotal, time) {
        var DEBUG_METHOD = true & DEBUG;

        if ( DEBUG_METHOD ){NetBeans.Logger.log("net.sendProgressUpdate"); }
        var request = aRequest.QueryInterface(NetBeans.Constants.HttpChannelIF);
        var id = requestsId.getId(request);
        if (!id) {
            throw new Error("Progress Request not found:" + request.URI.asciiSpec);
        }
        if ( DEBUG_METHOD ){NetBeans.Logger.log("net.sendProgressUpdate createResponseActivity"); }
        var activity = createResponseActivity(request,id);
        if( !activity ){
            throw new Error("net.sendProgressUpdate - activity is null");
        }

        var netActivity = <http />;
        netActivity.timestamp = time;
        netActivity.type ="progress";
        netActivity.id = id;
        netActivity.current = current;
        netActivity.max = max;
        netActivity.total = total;
        netActivity.maxTotal = maxTotal;
        loadXmlActivityResponse(netActivity, activity);
        if( DEBUG_METHOD ){
            NetBeans.Logger.log(netActivity.toXMLString());
        }
        socket.send(netActivity);
    }


    /*
     * getRequestWebProgress
     * @param {nsIHttpChannel} aRequest
     * @return {nsIWebProgress}
     */
    function getRequestWebProgress(aRequest) {
        try
        {
            var DEBUG_METHOD = (true & DEBUG);
            if(DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress: - aRequest:" + aRequest);

            var i = 0;
            var myInterface = null;
            var notificationCallbacks;

            if (aRequest.notificationCallbacks) {
                if(DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress: - got notificationCallback from request");
                notificationCallbacks = aRequest.notificationCallbacks;
            }

            if (!notificationCallbacks && aRequest.loadGroup && aRequest.loadGroup.notificationCallbacks) {
                if(DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress: - got notificationCallback from request.loadGroup");
                notificationCallbacks = aRequest.loadGroup.notificationCallbacks;
            }

            if (notificationCallbacks)
            {
                if(DEBUG_METHOD)  NetBeans.Logger.log("net.getRequestWebProgress: Notification Callback does exist #2.:" + notificationCallbacks);
                var bypass = false;
                if (getRequestCategory(aRequest) == "xhr")
                {
                    if(DEBUG_METHOD)  NetBeans.Logger.log("net.getRequestWebProgress: - begin visit requestHeaders notificationCallbacks.channel: " + notificationCallbacks.channel);
                    notificationCallbacks.channel.visitRequestHeaders(
                    {
                        visitHeader: function(header, value)
                        {
                            if (DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress.visitHeader header: " + header + " value:" + value);
                            if (header == "X-Moz" && value == "microsummary") {
                                if(DEUBG_METHOD) NetBeans.Logger.log("net.getRequestWebPRogress.visitHeader MATCH");
                                bypass = true;
                            }
                        }
                    });
                }
                if (!bypass) {
                    try {
                        myInterface = notificationCallbacks.getInterface(NetBeans.Constants.WebProgressIF);
                    } catch (exc) {}
                    if(myInterface && DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress - myInterface: "+ myInterface);
                    if (myInterface) {
                        return myInterface;
                    }
                }

            }
        } catch (exc) {
            if (DEBUG_METHOD) NetBeans.Logger.log("XXXX. net.getRequestWebProgress - Exception occurred: #1" + exc);
        }

        try {
            if (DEBUG_METHOD) NetBeans.Logger.log("net.getRequestWebProgress - loadGroup:" + aRequest.loadGroup);
            if (aRequest.loadGroup && aRequest.loadGroup.groupObserver) {
                myInterface = aRequest.loadGroup.groupObserver.QueryInterface(NetBeans.Constants.WebProgressIF);
                if( DEBUG && DEBUG_METHOD ) NetBeans.Logger.log("net.getRequestWebProgress - myInterface: "+ myInterface);
                return myInterface;
            } else if( DEBUG_METHOD ) {
                NetBeans.Logger.log("net.getRequestWebProgress does not have loadGropu or groupObserver properties.")
            }
        }
        catch (exc) {
            if (DEBUG_METHOD) NetBeans.Logger.log(i++ + "XXXX. net.getRequestWebProgress - Exception occurred: #2" + exc);
        }
        return null;

    }


    function isURLEncodedFile(request, text, headers)
    {
        try {
            if ( !request ) {
                return false;
            }
            if (text && text.indexOf("Content-Type: application/x-www-form-urlencoded") != -1){
                return true;
            }

            var headerValue = null;
            try {
                headerValue = request.contentType;
            } catch(exc) {
                if (DEBUG)NetBeans.Logger.log("netmonitor.isURLEncodedFile: request:" + request + " text:" + text + " Exception:" + exc);
            }
            if ( !headerValue ){
                headerValue = findHeader(headers, "Content-Type");
            }
            // The header value doesn't have to be alway exactly "application/x-www-form-urlencoded",
            // there can be even charset specified. So, use indexOf rather than just "==".
            return (headerValue && headerValue.indexOf("application/x-www-form-urlencoded") == 0);
        } catch (exc) {
            return undefined;
        }
    }

    function findHeader(headers, name) {
        for (var i = 0; i < headers.length; ++i)
        {
            if (headers[i].name == name)
                return headers[i].value;
        }
        return null;
    }

    function convertToUnicode (text, charset)
    {
        if (!text)
            return "";
        try
        {
            var conv = NetBeans.Utils.CCSV(
                NetBeans.Constants.ScriptableUnicodeConverterServiceCID,
                NetBeans.Constants.ScriptableUnicodeConverterIF);

            // if( DEBUG ) NetBeans.Logger.log("netmonitor.convertToUnicode: convertSErvice" + conv);
            conv.charset = charset ? charset : "UTF-8";
            return conv.ConvertToUnicode(text);
        }
        catch (exc) {
            NetBeans.Logger.log("netmonitor.convertToUnicode: " + exc);
        }
        return text;
    }

    function readFromStream(stream, charset)
    {
        try
        {
            var binaryInputStream = NetBeans.Utils.CCSV(
                NetBeans.Constants.BinaryInputStreamCID,
                NetBeans.Constants.BinaryInputStreamIF);

            //if ( DEBUG ){NetBeans.Logger.log("netmonitor.readFromStream - binaryInputStream: " + binaryInputStream); }

            binaryInputStream.setInputStream(stream);

            var segments = [];
            for (var count = stream.available(); count; count = stream.available()){
                //if (DEBUG) NetBeans.Logger.log("netmonitor.readFromStream - count: " + count);
                var bytes = binaryInputStream.readBytes(count);
                //if (DEBUG) NetBeans.Logger.log("netmonitor.readFromStream - bytes: " + bytes);
                segments.push(bytes);
            }
            var text = segments.join("");
            var convertedText = convertToUnicode(text, charset);
            //if (DEBUG) NetBeans.Logger.log("netmonitor.readFromStream - convertedText:" + convertedText);

            return convertedText;
        }
        catch(exc) { }
    }

    function parseURLEncodedText (text)
    {
        const maxValueLength = 25000;

        var params = [];

        var args = text.split("&");
        for (var i = 0; i < args.length; ++i)
        {
            var parts = args[i].split("=");
            if (parts.length == 2)
            {
                if (parts[1].length > maxValueLength)
                    parts[1] = this.$STR("LargeData");

                params.push({
                    name: unescape(parts[0]),
                    value: unescape(parts[1])
                });
            }
            else
                params.push({
                    name: unescape(parts[0]),
                    value: ""
                });
        }
        params.sort(function(a, b) {
            return a.name < b.name ? -1 : 1;
        });

        return params;
    };



    function getPostText(activity, request, context, headers)
    {
//        if ( DEBUG )  NetBeans.Logger.log("  netmonitor.getPostText href:" + activity.url );

        var postText;

        if( !postText) {
//            if ( DEBUG ) NetBeans.Logger.log("  netmonitor.getPostText - using getPostTextFromUploadStream");
            var uploadChannel = request.QueryInterface(NetBeans.Constants.UploadChannelIF);
            //if( DEBUG ){NetBeans.Logger.log(" netmonitor.getPostText - Upload:" + uploadChannel);}
            var uploadStream = uploadChannel.uploadStream;

            //if( DEBUG ){NetBeans.Logger.log(" netmonitor.getPostText - uploadStream:" + uploadStream);}
            var text = getPostTextFromUploadStream(uploadStream, context);
            //if( DEBUG ){NetBeans.Logger.log(" netmonitor.getPostText - text:" + text);}

            if (isURLEncodedFile(request, text, headers)) {
                //if(DEBUG) NetBeans.Logger.log(" netmonitor.getPostText -  URL ENCODED");
                var lines = text.split("\n");
                var params = parseURLEncodedText(lines[lines.length-1]);
                //if(DEBUG) NetBeans.Logger.log(" netmonitor.getPostText -  params:" + params);

                postText = "";
                var pair;
                for( pair in params ){
                    postText += params[pair].name  + "=" +  params[pair].value + " ";
                //if (DEBUG) NetBeans.Logger.log( params[pair].name + ":" + params[pair].value);
                }

            }
            else
            {
                if(DEBUG) NetBeans.Logger.log(" netmonitor.getPostText -  not URL ENCODED");
                postText = text;
                NetBeans.Logger.log("TEXT: " + text);
            /*  var postText = formatPostText(text);
                  if (postText)
                      insertWrappedText(postText, postTextBox);*/
            }


        }

        return postText;
    }
//

    function getPostTextFromUploadStream ( uploadStream, context ){
        if (uploadStream)
        {
            //if( DEBUG ){  NetBeans.Logger.log("netmonitor.getPostTextFromUploadStream - uploadStream:" + uploadStream);     }
            var seekableStream = uploadStream.QueryInterface(NetBeans.Constants.SeakableStreamIF);
            //if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromUploadStream seekableStream: " + seekableStream);
            if (seekableStream) seekableStream.seek(SEEK_SET, 0);
            var charset = context.window.document.characterSet;
            //if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromUploadStream charset: " + charset);
            var text = readFromStream(uploadStream, charset);
            //if (DEBUG) NetBeans.Logger.log("  netmonitor.getPostTextFromUploadStream text: " + text);
            if (seekableStream) seekableStream.seek(SEEK_SET, 0); //Not sure why firebug does this as well?

            return text;
        } //else { if( DEBUG ){ NetBeans.Logger.log(" netmonitor.getPostTextFromUploadStream - uploadStream is null"); } }
        return null;
    }




    /*
     * @param {nsIWebProgress} aWebProgress
     * @return {nsIDOMWindow}
     */
    function safeGetWindow(aWebProgress)
    {
        var SAFE_GET_WINDOW_DEBUG = false;
        var win = null;
        if (DEBUG && SAFE_GET_WINDOW_DEBUG) NetBeans.Logger.log("net.safeGetWindow");
        try
        {
            if ( !aWebProgress || !aWebProgress.DOMWindow){
                if (DEBUG && SAFE_GET_WINDOW_DEBUG)  NetBeans.Logger.log("net.safeGetWindow - aWebProgress:" + aWebProgress + " & its DOMWindow:" + aWebProgress.DOMWindow);
                return;
            } else {
                win = aWebProgress.DOMWindow;
                if( !win )  NetBeans.Logger.log("net.safeGetWindow - window is null");
            }
        }
        catch (exc){
            NetBeans.Logger.log("net.safeGetWindow - Exception: " + exc);
        }
        return win;
    }



    function safeGetName(request)
    {
        try
        {
            return request.name;
        }
        catch (exc)
        {
            return null;
        }
    }


    function nowTime()
    {
        return (new Date()).getTime();
    }


    function S4() {
        return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    }
    function uuid() {
        return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
    }



}).apply(NetBeans.NetMonitor);