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
 * http://www.netbeans.org/cddl-gplv2.html
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
 * Sandip V. Chitale (sandipchitale@netbeans.org)
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
(function() {
    // XPCOM Utilities
    var _CI = Components.interfaces;
    var _CC = Components.classes;

    // Return Components class by Contract ID
    this.CC = function(cName)
    {
        return _CC[cName];
    };

    // Return Components interface
    this.CI = function(ifaceName)
    {
        return _CI[ifaceName];
    };

    // Return a service given the Contract ID and the interface
    this.CCSV = function(cName, iface)
    {
        return _CC[cName].getService(iface);
    };

    // Create and return an instance given the Contract ID and the interface
    this.CCIN = function(cName, iface)
    {
        return _CC[cName].createInstance(iface);
    };

    //  Query and return the interface from the given object
    this.QI = function(obj, iface)
    {
        return obj.QueryInterface(iface);
    };

    // General Utilities
    this.ObjectToString = function(o) {
        if (o && o instanceof Object) {
            return Object.prototype.toString.apply(o);
        }
        return '' + o;
    }
    
    // ************************************************************************************************

    const nsIIOService = Components.interfaces.nsIIOService;
    const nsIRequest = Components.interfaces.nsIRequest;
    const nsICachingChannel = Components.interfaces.nsICachingChannel;
    const nsIScriptableInputStream = Components.interfaces.nsIScriptableInputStream;
    const nsIXULRuntime = Components.interfaces.nsIXULRuntime;
    const nsIXULAppInfo = Components.interfaces.nsIXULAppInfo;
    const nsIShellService = Components.interfaces.nsIShellService;
    const nsIWebProgressListener = Components.interfaces.nsIWebProgressListener;
    const nsISupportsWeakReference = Components.interfaces.nsISupportsWeakReference;
    const nsISupports = Components.interfaces.nsISupports;
    const nsIExtensionManager = Components.interfaces.nsIExtensionManager;
    const nsIAddonUpdateCheckListener = Components.interfaces.nsIAddonUpdateCheckListener;

    const NS_NOINTERFACE = Components.results.NS_NOINTERFACE;

    // ************************************************************************************************

    this.keys = function(o)
    {
        var rv = new Array();
        for (var p in o)
            rv.push(p);
        return rv;  
    }

    this.sliceArray = function(array, index)
    {
        var slice = [];
        for (var i = index; i < array.length; ++i)
            slice.push(array[i]);
        return slice;
    }

    this.removeFromArray = function(array, item)
    {
        for (var i = 0; i < array.length; ++i) {
            if (array[i] == item)
            {
                array.splice(i, 1);
                return true;
            }
        }
        return false;
    }

    this.findInArray = function(array, item)
    {
        for (var i = 0; i < array.length; ++i) {
            if (array[i] == item) {
                return array[i];
            }
        }
        return null;
    }

    this.cloneArray = function(array, fn)
    {
        var newArray = [];
        if (fn) {
            for (var i = 0; i < array.length; ++i)
                newArray.push(fn(array[i]));
        } else {
            for (var j = 0; j < array.length; ++i)
                newArray.push(array[j]);
        }
        return newArray;
    }

    this.arrayInsert = function(array, index, other)
    {
        for (var i = 0; i < other.length; ++i)
            array.splice(i+index, 0, other[i]);
        return array;
    }

    this.arrayMerge = function(array, index, other)
    {
        return this.arrayInsert(this.cloneArray(array),index,other);
    }
    
    this.getSource = function(url)
    {
        var service = Components.classes["@mozilla.org/network/io-service;1"].
            getService(nsIIOService);

        var channel = service.newChannel(url, null, null);
        channel.loadFlags |= nsIRequest.LOAD_FROM_CACHE | nsIRequest.VALIDATE_NEVER | nsICachingChannel.LOAD_ONLY_FROM_CACHE;
	
        //CLEANUP
        var instream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(nsIScriptableInputStream);
        instream.init(channel.open());
	
        var data = "";
        var count;
        while ((count = instream.available()) > 0) {
            data += instream.read(count);
        }
        return data;
    }

    this.getSourceAsync = function(url, onComplete)
    {
        var service = Components.classes["@mozilla.org/network/io-service;1"].
            getService(nsIIOService);

	var channel = service.newChannel(url, null, null);
	channel.loadFlags |= nsIRequest.LOAD_FROM_CACHE | nsIRequest.VALIDATE_NEVER | nsICachingChannel.LOAD_ONLY_FROM_CACHE;
        
	var listener = {
		data: "",
		onStartRequest: function(request, context) {},
		onStopRequest: function(request, context, status)
		{
                    onComplete(this.data, Components.isSuccessCode(status));
		},
		onDataAvailable: function(request, context, inStr, sourceOffset, count)
		{
                    var stream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(nsIScriptableInputStream);
                    stream.init(inStr);
                    this.data += stream.read(count);
		}
	};
	return channel.asyncOpen (listener, null);
    }

    this.getBrowserByWindow = function(win)
    {
        var doc = document;
        if ( arguments.length > 1 && arguments[1] ) {
            doc = arguments[1];
        }
        var tabBrowser = doc.getElementById("content");
        for (var i = 0; i < tabBrowser.browsers.length; ++i) {
            var browser = tabBrowser.browsers[i];
            if (browser.contentWindow == win) {
                return browser;
            }
        }
        return null;
    }
    
    this.convertUnicodeToUTF8 = function(unicode) {
          var converter = this.CCIN(NetBeans.Constants.ScriptableUnicodeConverterCID,
                                    NetBeans.Constants.ScriptableUnicodeConverterIF);
          converter.charset = "UTF-8";
          return converter.ConvertFromUnicode(unicode) + converter.Finish();
    }
    
    this.convertUTF8ToUnicode = function(utf8) {
          var converter = this.CCIN(NetBeans.Constants.ScriptableUnicodeConverterCID,
                                    NetBeans.Constants.ScriptableUnicodeConverterIF);
          converter.charset = "UTF-8";
          return converter.ConvertToUnicode(utf8) + converter.Finish();
    }    
    
    this.isFF2 = function() {
        return getFirefoxVersion() == 2;
    }
    
    this.isFF3 = function() {
        return getFirefoxVersion() == 3;
    }
    
    const ffUserAgentRegExp = /Firefox[\/\s](\d+\.\d+)/;
    function getFirefoxVersion() {
        if (this.firefoxVersion) {
            return this.firefoxVersion;
        }
        
        if (ffUserAgentRegExp.test(navigator.userAgent)) {
            var version = new Number(RegExp.$1);
            if (version >= 3) {
                this.firefoxVersion = 3;
                return 3;
            } else if (version >= 2) {
                this.firefoxVersion = 2;
                return 2;
            }
        }
        NetBeans.Logger.logMessage("Could not detect Firefox version");
        
        return null;
    }
    
    // ************************************************************************************************
}).apply(NetBeans.Utils);
