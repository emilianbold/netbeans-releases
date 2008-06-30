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
    var requestsId = {};
    var requests = [];
    var topWindow;
    this.initMonitor = function  (context, browser, _socket) {
        topWindow = context.window;
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
            topWindow = null;
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

            if ( isRelevantWindow(request) ){
                requests.push(request);
                var id = uuid();
                var activity = getHttpRequestHeaders(request);
                activity.uuid = uuid();
                requestsId[request] = activity.uuid;
                activity.time = nowTime();
                activity.category = getRequestCategory(request);
                sendNetActivity(activity);
            }
        },
       
        onExamineResponse: function( aNsISupport ){
            
            var request = aNsISupport.QueryInterface(NetBeans.Constants.HttpChannelIF);
            //The bug is here.. Figure it out.
            //  if( isRelevantWindow(request) ){
            if( requests.indexOf(request) != -1 ){
                var activity = getHttpResponseHeaders(request);

                if ( activity ) {
                    activity.time = nowTime();
                    sendExamineNetResponse(activity);
                }
            }
        }
        
        
    }
    
    /*
     * isRelevantWindow - is the window a subclass of the window we are debugging?
     * @param {nsIHttpChannel} aRequest
     * @type {nsIDOMWindow} win
     * @return {bool}
     */
    function isRelevantWindow(aRequest) {
        
        var webProgress = getRequestWebProgress(aRequest);
        var win = null;
        if( webProgress){
            win = safeGetWindow(webProgress)
        } else if (DEBUG) {
            NetBeans.Logger.log("net.isRelevantWindow - Your webprogress value is no good.");
            return false;
        }
        
        //var win = webProgress ? safeGetWindow(webProgress) : null;
        if( !win || !( win instanceof NetBeans.Constants.DOMWindowIF)){
            if( DEBUG )
                NetBeans.Logger.log("ERROR: net.isRelevantWindow - null or not a DOMWINDOW");
            return false;
        }
        

        if ( topWindow == win){
            return true;
        } else if ( !win.parent ) {
            return false;
        } else {
            return isRelevantWindow(win.parent);  //Hmm, sh
        }
        
    //return ( topWindow == win || win.top == topWindow )
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
            if ( requests.indexOf(request) != -1){
                sendProgressUpdate(progress, request, current, max, total, maxTotal);
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
//        if ( DEBUG ) {
//            NetBeans.Logger.log("GetHttpResponseHeaders: ");
//        }
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
//        if( DEBUG ){
//            NetBeans.Logger.log("GetHttpRequestHeaders: ");
//        }
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
        if ( index > -1 && uri.length) {
            ext = uri.substr(index,uri.length());
        }
        return ext;
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
        netActivity.mimeType=aActivity.mimeType;
        var headers = aActivity.requestHeaders;
        for( var header in headers ){
            var tmp = headers[header];
            netActivity.header[tmp.name] =  tmp.value;
        }
        if (DEBUG){
            NetBeans.Logger.log(netActivity.toXMLString());
        }

        //socket.send(breakpointSetResponse);
    }
    /*
     * On Observe when topic is "http-on-examine-request"
     * @param {NetActivity} aActivity;
     */
    function sendExamineNetResponse ( aActivity ){

//        <http type="response" id="1209013880362" method="GET" timestamp="1209013880362" ipaddress="127.0.0.1" method="GET" protocol="HTTP/1.1" queryString="&user=Name?password=Pass" uri="/WebApplication2/">
//<header
//date="Sun, 08 Jun 2008 19:32:56 GMT"
//cache-control="max-age=315360000"
//expires"Wed, 19 Mar 2008 21:11:32 GMT"
//last-modified="We, 19 Mar 2008 21:11:32 GMT"
//accept-ranges="bytes"
//x-yahoo-compressed="true"
//vary="Accept-Encoding"
//connect-type="application/x-javascript"
//connection="keep-alive"
//Server="YTS/1.16.0"
///> //header
///> //http

       var netActivity = <http/>;
       netActivity.type = "response";
       netActivity.id = 0;
       netActivity.method = aActivity.method;
       netActivity.timestamp=aActivity.time;
       netActivity.status = aActivity.status;
       netActivity.urlParams = aActivity.urlParams;

        var headers = aActivity.responseHeaders;
        for( var header in headers ){
            var tmp = headers[header];
            netActivity.header[tmp.name] =  tmp.value;
        }
        if(DEBUG){
            NetBeans.Logger.log(netActivity.toXMLString());
        }
        //socket.send(netActivity);

//        if (DEBUG){
//            for( var key in aActivity ){
//                if ( key == "responseHeaders"){
//                    var headers = aActivity[key];
//                    NetBeans.Logger.log("   < Response Headers");
//                    for( var header in headers ){
//                        NetBeans.Logger.log("       " + headers[header].name + " : " + headers[header].value);
//                    }
//                } else {
//                    NetBeans.Logger.log("   <" + key + " : " + aActivity[key]);
//                }
//            }
//        }
    }
    
    function sendProgressUpdate(progress, request, current, max, total, maxTotal) {
        var uuid = requestsId[request];
        if( DEBUG ){
            NetBeans.Logger.log("UUID: " + uuid + " On ProgressChange: " + Object.prototype.toString.apply(progress.wrappedJSObject) + " Request: " + Object.prototype.toString.apply(request) + " current: " + current +" max: " + max + " total: " + total + " maxTotal: " + maxTotal);
        }
    }
    
    
    /*
     * getRequestWebProgress
     * @param {nsIHttpChannel} aRequest
     * @return {nsIWebProgress}
     */
    function getRequestWebProgress(aRequest)
    {
        try
        {
            var i = 0;
            var myInterface = null;
            if (aRequest.notificationCallbacks)
            {
                //NetBeans.Logger.log(i++ + "   a. net.getRequestWebProgress request has notificationCallBacks");
                var bypass = false;
                //if (aRequest.notificationCallbacks instanceof XMLHttpRequest)
                if (getRequestCategory(aRequest) == "xhr")
                {
                    //                    NetBeans.Logger.log(i++ + "a. net.getRequestWebProgress    the notificationCallbacks is a XMLHttpRequest");
                    aRequest.notificationCallbacks.channel.visitRequestHeaders(
                    {
                        visitHeader: function(header, value)
                        {
                            if (header == "X-Moz" && value == "microsummary")
                                bypass = true;
                        }
                    });
                }
                if (!bypass){
                    //var myInterface = aRequest.notificationCallbacks.getInterface(NetBeans.Constants.WebProgressIF);
                    myInterface = GetInterface( aRequest.notificationCallbacks, NetBeans.Constants.WebProgressIF);
                    return myInterface;
                }
            }
        } catch (exc) {
            NetBeans.Logger.log("1XXXX. net.getRequestWebProgress - Exception occurred: " + exc);
        }
            
        try {
            // NetBeans.Logger.log(i++ + "     b. net.getRequestWebProgress request has loadGroup and loadGroup.GroupObserver");
            if (aRequest.loadGroup && aRequest.loadGroup.groupObserver) {
                myInterface = aRequest.loadGroup.groupObserver.QueryInterface(NetBeans.Constants.WebProgressIF);
                return myInterface;
            }
            if( DEBUG )
                NetBeans.Logger.log("net.getRequestWebProgress does not have loadGropu or groupObserver properties.")
        }
        catch (exc) {
            NetBeans.Logger.log(i++ + "2XXXX. net.getRequestWebProgress - Exception occurred: " + exc);
        }
        
    //        if( !myInterface ){
    //            NetBeans.Logger.log(i++ + ". net.getRequestWebProgress - myInterface is null");
    //        } else if ( !( myInterface instanceof NetBeans.Constants.WebProgressIF) ) {
    //            NetBeans.Logger.log(i++ + ". net.getRequestWebProgress - myInterface is not an instance of nsIWebProgress")
    //        }
    //        return myInterface;
    }
    
    /*
     * @param {nsIWebProgress} aWebProgress
     * @return {nsIDOMWindow}
     */
    function safeGetWindow(aWebProgress)
    {
        var win = null;
        //NetBeans.Logger.log("net.safeGetWindow");
        try
        {
            if ( !aWebProgress || !aWebProgress.DOMWindow){
                if (DEBUG)
                    NetBeans.Logger.log("net.safeGetWindow - netProgress is null or is not a DOM Window");
                return;
            } else {
                win = aWebProgress.DOMWindow;
                if( !win ){
                    NetBeans.Logger.log("net.safeGetWindow - window is null");
                }
            }
        }
        catch (exc)
        {
            NetBeans.Logger.log("net.safeGetWindow - Exception: " + exc);
        }
        return win;
    }
    function getRequestCategory(aRequest)
    {
        try
        {
            if (aRequest.notificationCallbacks && aRequest.notificationCallbacks instanceof XMLHttpRequest){
                return "xhr";
            }
            return null;
        }
        catch (exc) {}
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
        if(!obj || !aInterface ){
            NetBeans.Logger.log("net.GetInterface - you are passing null params");
        }
        try
        {
            return obj.getInterface(aInterface);
        }
        catch (e)
        {
            if (e.name == NetBeans.Constants.NS_NOINTERFACE)
            {
                //if (DEBUG)
                NetBeans.Logger.Log("net.GetInterface - obj has no interface: ", aInterface, obj);
            }
        }

        return null;
    }

    function S4() {
        return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    }
    function uuid() {
        return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
    }

    
}).apply(NetBeans.NetMonitor);