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
    const DEBUG = true;
    
    //Should we move this to constants.js?
    const STATE_IS_WINDOW = NetBeans.Constants.WebProgressListenerIF.STATE_IS_WINDOW;
    const STATE_IS_DOCUMENT = NetBeans.Constants.WebProgressListenerIF.STATE_IS_DOCUMENT;
    const STATE_IS_NETWORK = NetBeans.Constants.WebProgressListenerIF.STATE_IS_NETWORK;
    const STATE_IS_REQUEST = NetBeans.Constants.WebProgressListenerIF.STATE_IS_REQUEST;

    const STATE_START = NetBeans.Constants.WebProgressListenerIF.STATE_START;
    const STATE_STOP = NetBeans.Constants.WebProgressListenerIF.STATE_STOP;
    const STATE_TRANSFERRING = NetBeans.Constants.WebProgressListenerIF.STATE_TRANSFERRING;


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
        "jss": "application/x-javascript",
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
        "image/jpeg": "image",
        "image/gif": "image",
        "image/png": "image",
        "image/bmp": "image",
        "application/x-shockwave-flash": "flash"
    };


    
    var netFeatures = {
        netFilterCategory: null,
        disableNetMonitor: false,
        collectHttpHeaders: false
    };
    var socket;
    
    this.initMonitor = function  (context, browser, _socket) {
        if( !netFeatures.disableNetMonitor ){
            monitorContext(context, browser);
            if( !_socket ) 
                NetBeans.Logger.log("net.initMonitor - Socket is null");
            socket = _socket;
        }
    }
    
    this.destroyMonitor = function(context, browser) {
        if (context.networkListener) {
            unmonitorContext(context, browser);
            socket = null;
        }
            
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
            }

        },
        /*
         * @param {nsISupport} aNsISupport
         * @type {nsIHttpChannel} request
         * @type {NetActivity} activity
         */
        onModifyRequest: function (aNsISupport) {
            var request = aNsISupport.QueryInterface(NetBeans.Constants.HttpChannelIF);
            
            var activity = getHttpRequestHeaders(request);
            activity.time = nowTime();
            activity.webProgress = getRequestWebProgress(request, this);
            activity.category = getRequestCategory(request);
            //activity.win = webProgress ? safeGetWindow(webProgress) : null;

            sendNetActivity(activity);
        },
        
        onExamineResponse: function( request ){
            var activity = getHttpResponseHeaders(request);
            activity.time = nowTime();
            sendExamineNetResponse(activity);
        }
        
        
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
        onProgressChange : function(progress, request, current, max, total, maxTotal)
        {
        //NetBeans.Logger.log("On ProgressChange: " + Object.prototype.toString.apply(progress.wrappedJSObject) + " Request: " + Object.prototype.toString.apply(request));
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
    
    
    function monitorContext(context, browser)
    {
        if (!context.netProgressListener)
        {
            var netProgressListener = context.netProgressListener = new NetProgressListener(context);
            
            //Listening to the progress of the request
            browser.addProgressListener(netProgressListener, NOTIFY_ALL);

            observerService.addObserver(NetObserver, "http-on-modify-request", false);
            observerService.addObserver(NetObserver, "http-on-examine-response", false);
        }
    }

    // Maybe we should store browser inside context like firebug.
    function unmonitorContext(context,browser)
    {
        if (context.netProgressListener)
        {
            if (browser.docShell)
                browser.removeProgressListener(context.netProgressListener, NOTIFY_ALL);

            // XXXjoe We also want to do this when the context is hidden, so that
            // background files are only logged in the currently visible context
            observerService.removeObserver(NetObserver, "http-on-modify-request", false);
            observerService.removeObserver(NetObserver, "http-on-examine-response", false);

            delete context.netProgressListener;
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
    function getHttpResponseHeaders(aRequest)
    {
        if ( DEBUG ) {
            NetBeans.Logger.log("GetHttpResponseHeaders: ");
        }
        var activity = new NetActivity();
        try
        {
            //var http = QI(request, nsIHttpChannel);
            var http = aRequest.QueryInterface(NetBeans.Constants.HttpChannelIF);
            var href = aRequest.name;
            activity.method = http.requestMethod;
            activity.status = aRequest.responseStatus;
            activity.urlParams = parseURLParams(href);

            if (!activity.mimeType)
                activity.mimeType = getMimeType(aRequest.contentType, href);

            var responseHeaders = [];

            http.visitResponseHeaders({
                visitHeader: function(name, value)
                {
                    responseHeaders.push({
                        name: name, 
                        value: value
                    });
                }
            });
            activity.responseHeaders = responseHeaders;
        }
        catch (exc)
        {
            NetBeans.Logger.log("netmonitor.getHttpResponseHeaders: exception" + exc);
            activity = null;
        } finally {
            return activity;
        }
    }


    /*
     * @param {nsISupport} aRequest
     * @type {nsIHttpChannel} http
     * @type {NetActivity} activity
     */
    function getHttpRequestHeaders( aRequest )
    {
        if( DEBUG ){
            NetBeans.Logger.log("GetHttpRequestHeaders: ");
        }
        var activity = new NetActivity();
        try
        {
            //var http = QI(request, nsIHttpChannel);
            var http = aRequest.QueryInterface(NetBeans.Constants.HttpChannelIF);
            activity.method = http.requestMethod;
            //activity.status = aRequest.responseStatus;
            activity.urlParams = parseURLParams(activity.href);

            //if (!activity.mimeType && aRequest.contentType )
           //     activity.mimeType = getMimeType(aRequest.contentType, aRequest.name);

            var requestHeaders = [];

            http.visitRequestHeaders({
                visitHeader: function(name, value)
                {
                    requestHeaders.push({
                        name: name, 
                        value: value
                    });
                }
            });
            activity.requestHeaders = requestHeaders;
        }
        catch (exc)
        {
            NetBeans.Logger.log("netmonitor.getHttpRequestHeaders: exception" + exc);
            activity = null;
        } finally {
            return activity;
        }
        
    }
  
    function getMimeType(mimeType, uri)
    {
        if (!mimeType || !(mimeCategoryMap.hasOwnProperty(mimeType)))
        {
            var ext = getFileExtension(uri);
            if( DEBUG ) {
                NetBeans.Logger.log("netmonitor - getFileExtension: " + ext);
            }
            if (!ext)
                return mimeType;
            else
            {
                var extMimeType = mimeExtensionMap[ext.toLowerCase()];
                return extMimeType ? extMimeType : mimeType;
            }
        }
        else
            return mimeType;
    }
    
    /*
     * @param {string} uri
     */
    function getFileExtension( uri ){
        var ext = "";
        var index = uri.indexOf('.');
        if ( index > -1 ) {
            ext = uri.substr(index,uri.length());
        }
        return ext;
    }

    /*
     * On Observe when topic is "http-on-modify-request"
     * @param {NetActivity} aActivity
     */
    function sendNetActivity ( aActivity ){
        if (DEBUG){
            for( var key in aActivity ){
                NetBeans.Logger.log("Item: " + key + "Value: " + aActivity[key]);
            }
        }
    }
    /*
     * On Observe when topic is "http-on-examine-request"
     * @param {NetActivity} aActivity;
     */
    function sendExamineNetResponse ( aActivity ){
//        var href = aRequest.name;
//        var referrer = aRequest.referrer;
//        var status = aRequest.responseStatus;
//        var statusText = aRequest.responseStatusText;
//        
//        var method = null;
//        var params = null;
//        if( aRequest.requestMethod  ){
//            method = aRequest.requestMethod;
//            if( method != "POST"){
//                params = parseURLParmas(href);
//            }
//        }
//        
//        var loadFlags = null;
//        if( aRequest.loadFlags){
//            loadFlags = aRequest.loadFlags;
//        }
        if (DEBUG){
            for( var key in aActivity ){
                NetBeans.Logger.log("Item: " + key + "Value: " + aActivity[key]);
            }
//            NetBeans.Logger.log("   <-- net.netprogress.sendNetResponse");
//            NetBeans.Logger.log("   < Request: " + aRequest);
//            NetBeans.Logger.log("   < Time " + aTime);
//            NetBeans.Logger.log("   < Win " + aWin);
//            NetBeans.Logger.log("   < Category " + aCategory);
//            NetBeans.Logger.log("   < Method " + method);
//            NetBeans.Logger.log("   < LoadFlags " + loadFlags);
//            NetBeans.Logger.log("   < Href " + href);
//            NetBeans.Logger.log("   < Referrer " + referrer);
//            NetBeans.Logger.log("   < Params " + params);
//            NetBeans.Logger.log("   < Status " + status);
//            NetBeans.Logger.log("   < StatusText " + statusText);
        }
    }
    
    /*
     * On State Change when State is STATE_STOP
     */
    function sendNetStopRequest() {
        if (DEBUG){
            NetBeans.Logger.log("   <-- net.netprogress.sendNetStopResponse");
        }
    }
    
    function sendNetMainWindowResponded() {
        if (DEBUG){
            NetBeans.Logger.log("net.netprogress.sendNetMainWindowResponded");
        }
    }
    
    function sendProgressUpdate() {
        if( DEBUG ){
            NetBeans.Logger.log("net.netprogress.sendProgressUpdate")
        }
    }
    
    
    function getRequestWebProgress(request)
    {
        try
        {
            if (request.notificationCallbacks)
            {
                var bypass = false;
                if (request.notificationCallbacks instanceof XMLHttpRequest)
                {
                    request.notificationCallbacks.channel.visitRequestHeaders(
                    {
                        visitHeader: function(header, value)
                        {
                            if (header == "X-Moz" && value == "microsummary")
                                bypass = true;
                        }
                    });
                }
                if (!bypass)
                    return GetInterface( request.notificationCallbacks, NetBeans.Constants.WebProgressIF);
            } else if (request.loadGroup && request.loadGroup.groupObserver) {
                return QueryInterface(request.loadGroup.groupObserver, NetBeans.Constants.WebProgressIF);
            } 
            return null;
        }
        catch (exc) {}
    }
    
    function getRequestCategory(request)
    {
        try
        {
            if (request.notificationCallbacks && request.notificationCallbacks instanceof XMLHttpRequest){
                return "xhr";
            }
            return null;
        }
        catch (exc) {}
    }
    
    
    function safeGetWindow(webProgress)
    {
        try
        {
            return webProgress.DOMWindow;
        }
        catch (exc)
        {
            return null;
        }
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
    
    function GetInterface(obj, aInterface)
    {
        try
        {
            return obj.getInterface(aInterface);
        }
        catch (e)
        {
            if (e.name == NetBeans.Constants.NS_NOINTERFACE)
            {
                if (DEBUG)
                    NetBeans.Logger.Log("net.GetInterface - obj has no interface: ", aInterface, obj);
            }
        }

        return null;
    }



    
}).apply(NetBeans.NetMonitor);