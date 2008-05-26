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
    
    const observerService = NetBeans.Utils.CCSV(
      NetBeans.Constants.ObserverServiceCID,
      NetBeans.Constants.ObserverServiceIF);
      
    const NOTIFY_ALL= NetBeans.Constants.WebProgressIF.NOTIFY_ALL;
    
    var netFeatures = {
        netFilterCategory: null,
        disableNetMonitor: true,
        collectHttpHeaders: false
    };
    
    this.initMonitor = function  (context, browser) {
        if( !netFeatures.disableNetMonitor ){
            monitorContext(context, browser);
        }
    }
    
    this.destroyMonitor = function(context, browser) {
        if (context.networkListener)
            unmonitorContext(context, browser);
    }
    
    
    function NetworkListener(context)
    {
        this.context = context;
    }
    
    NetworkListener.prototype = {
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
        onProgressChange : function(progress, request, current, max, total, maxTotal)
        {
            NetBeans.Logger.log("On ProgressChange: " + Object.prototype.toString.apply(progress) + " Request: " + Object.prototype.toString.apply(request));
        },
        onLocationChange: function() {NetBeans.Logger.log("On Location Change");},
        onSecurityChange : function() {NetBeans.Logger.log("On Security Change");},
        onStateChange : function() {NetBeans.Logger.log("On State Change");},
        onStatusChange : function() {NetBeans.Logger.log("On Status Change");},
        
        
        // nsIObserver
        observe: function(request, topic, data)
        {
           NetBeans.Logger.log("** Observe Request=" + Object.prototype.toString.apply(request) + " Topic=" + topic + " Data" +  Object.prototype.toString.apply(data));
        } 
        

        /*
         *  void onLocationChange ( nsIWebProgress webProgress , nsIRequest request , nsIURI location )   
         *  void onProgressChange ( nsIWebProgress webProgress , nsIRequest request , PRInt32 curSelfProgress , PRInt32 maxSelfProgress , PRInt32 curTotalProgress , PRInt32 maxTotalProgress )   
         *  void onSecurityChange ( nsIWebProgress webProgress , nsIRequest request , PRUint32 state )   
         *  void onStateChange ( nsIWebProgress webProgress , nsIRequest request , PRUint32 stateFlags , nsresult status )   
         *  void onStatusChange ( nsIWebProgress webProgress , nsIRequest request , nsresult status , PRUnichar* message )
         */
            
    }
    
    function monitorContext(context, browser)
    {
        if (!context.networkListener)
        {
            var networklistener = context.networkListener = new NetworkListener(context);

            browser.addProgressListener(networklistener, NOTIFY_ALL);

            observerService.addObserver(networklistener, "http-on-modify-request", false);
            observerService.addObserver(networklistener, "http-on-examine-response", false);
        }
    }

    // Maybe we should store browser inside context like firebug.
    function unmonitorContext(context,browser)
    {
        if (context.networkListener)
        {
            if (browser.docShell)
                browser.removeProgressListener(context.networkListener, NOTIFY_ALL);

            // XXXjoe We also want to do this when the context is hidden, so that
            // background files are only logged in the currently visible context
            observerService.removeObserver(context.networkListener, "http-on-modify-request", false);
            observerService.removeObserver(context.networkListener, "http-on-examine-response", false);

            delete context.networkListener;
        }
    }
    
}).apply(NetBeans.NetMonitor);