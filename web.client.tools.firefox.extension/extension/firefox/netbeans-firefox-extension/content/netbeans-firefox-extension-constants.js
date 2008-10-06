/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR this.EADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this.ile are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this.ile except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this.icense Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this.ode. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this.ile to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this.oftware in this.istribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this.ile under either the CDDL, the GPL Version 2 or
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

// Constants
// NOTE: We have to use the object literal syntax to be able to use the
// get operator to define read-only properties




NetBeans.Constants = { 
    // Version
    // This should be compatible with the spec version of
    // JavaScript Debugger API module version
    get VERSION() { return "0.6"; },


    // static constants
    get NULL_TERMINATOR() {  return String.fromCharCode(0); },
    
    get NS_OK() { return 0; },
    get NS_ERROR_FAILURE() { return -2147467259; },
    get NS_ERROR_MODULE_NETWORK() { return 2152398848; },
    get NS_NET_STATUS_READ_FROM() { return  JSDBGP.Constants.NS_ERROR_MODULE_NETWORK + 8; },
    get NS_NET_STATUS_WROTE_TO() { return  JSDBGP.Constants.NS_ERROR_MODULE_NETWORK + 9; },
    get NS_NET_STATUS_WROTE_TO() { return  JSDBGP.Constants.NS_ERROR_MODULE_NETWORK + 9; },
    get NS_ERROR_NOT_CONNECTED() { return  JSDBGP.Constants.NS_ERROR_MODULE_NETWORK + 12; },

    // XPCOM Base
    // static constants
    get NS_ERROR_NO_INTERFACE() { return Components.results.NS_ERROR_NO_INTERFACE; },

    // Contract IDs and Interfaces
    get SupportsIF() { return Components.interfaces.nsISupports; },
    
    get NS_NOINTERFACEIF() { return Components.results.NS_NOINTERFACE; },
    
    // Console
    get ConsoleServiceCID() { return '@mozilla.org/consoleservice;1'; },

    get ConsoleServiceIF() { return Components.interfaces.nsIConsoleService; },

    get ScriptErrorIF() { return Components.interfaces.nsIScriptError; },
    
    //Observer
    get ObserverServiceCID() { return '@mozilla.org/observer-service;1';},

    get ObserverServiceIF() { return Components.interfaces.nsIObserverService; },
    
    get ObserverIF() { return Compontents.interfaces.nsIObserver; },

    // Shell
    get ShellServiceCID() { return '@mozilla.org/browser/shell-service;1'; },

    get ShellServiceIF() { return Components.interfaces.nsIShellService; },

    get PreferencesServiceCID() { return '@mozilla.org/preferences-service;1'; },

    get PrefBranch2ServiceIF() { return Components.interfaces.nsIPrefBranch2; },

    // Browser
    get WebProgressIF() { return Components.interfaces.nsIWebProgress; },

    get WebProgressListenerIF() { return Components.interfaces.nsIWebProgressListener; },

    get WebNavigationIF() { return Components.interfaces.nsIWebNavigation; },

    get DOMWindowIF() { return Components.interfaces.nsIDOMWindow;},

    //Net Monitor Http Request
    get HttpChannelIF() { return Components.interfaces.nsIHttpChannel;},
    
    get XMLHttpRequestIF() { return Components.interfaces.nsIXMLHttpRequest;},

    get SeakableStreamIF() { return Components.interfaces.nsISeekableStream;},

    get WebPageDescriptorIF() { return Components.interfaces.nsIWebPageDescriptor; },

    get SHEntryIF() { return Components.interfaces.nsISHEntry; },

    get UploadChannelIF() { return Components.interfaces.nsIUploadChannel;},
    
    get InterfaceRequestorIF() { return Components.interfaces.nsIInterfaceRequestor;},

    get ScriptableUnicodeConverterServiceCID() { return '@mozilla.org/intl/scriptableunicodeconverter'; },

    get ScriptableUnicodeConverterIF() { return Components.interfaces.nsIScriptableUnicodeConverter;},
    
    get BinaryInputStreamCID() { return '@mozilla.org/binaryinputstream;1'; },

    get BinaryInputStreamIF() { return Components.interfaces.nsIBinaryInputStream;},
    
    get CacheServiceIF() { return Components.interfaces.nsICacheService;},

    get CacheIF() { return Components.interfaces.nsICache;},

    get CacheServiceCID() { return "@mozilla.org/network/cache-service;1"; },

    // Windows
    get WindowWatcherServiceCID() { return '@mozilla.org/embedcomp/window-watcher;1'; },

    get WindowWatcherServiceIF() { return Components.interfaces.nsIWindowWatcher; },

    get WindowMediatorServiceCID() { return '@mozilla.org/appshell/window-mediator;1'; },

    get WindowMediatorService() { return Components.interfaces.nsIWindowMediator; },

    get BrowserDOMWindowIF() { return Components.interfaces.nsIBrowserDOMWindow; },

    get DOMChromeWindowIF() { return Components.interfaces.nsIDOMChromeWindow; },

    get ThreadManagerServiceCID() { return '@mozilla.org/thread-manager;1'; },

    get ThreadManagerService() { return Components.interfaces.nsIThreadManager; },

    get EventQueueServiceCID() { return '@mozilla.org/event-queue-service;1'; },

    get EventQueueServiceIF() { return Components.interfaces.nsIEventQueueService; },

    get SupportsWeakReferenceIF() { return Components.interfaces.nsISupportsWeakReference; },

    // IO
    get IOServiceIF() { return Components.interfaces.nsIIOService; },

    get AsyncInputStreamIF() { return Components.interfaces.nsIAsyncInputStream; },

    get InputStreamCallbackIF() { return Components.interfaces.nsIInputStreamCallback; },

    get ScriptableInputStreamCID() { return '@mozilla.org/scriptableinputstream;1'; },

    get ScriptableInputStreamIF() { return Components.interfaces.nsIScriptableInputStream; },

    get BinaryInputStreamCID() { return '@mozilla.org/binaryinputstream;1'; },

    get BinaryInputStreamIF() { return Components.interfaces.nsIBinaryInputStream; },

    get StringInputStreamCID() { return '@mozilla.org/io/string-input-stream;1'; },
    
    get StringInputStreamIF() { return Components.interfaces.nsIStringInputStream; },
    
    get FileOutputStreamIF() { return Components.interfaces.nsIFileOutputStream; },

    get SocketTransportServiceCID() { return '@mozilla.org/network/socket-transport-service;1'; },

    get SocketTransportServiceIF() { return Components.interfaces.nsISocketTransportService; },

    get SocketTransportIF() { return Components.interfaces.nsISocketTransport; },

    get TransportIF() { return Components.interfaces.nsITransport; },

    get ServerSocketIF() { return Components.interfaces.nsIServerSocket; },

    get ServerSocketListenerIF() { return Components.interfaces.nsIServerSocketListener; },

    get CachingChannelIF() { return Components.interfaces.nsICachingChannel; },
    
    // Convertor
    get ScriptableUnicodeConverterCID() { return '@mozilla.org/intl/scriptableunicodeconverter'; },

    get ScriptableUnicodeConverterIF() { return Components.interfaces.nsIScriptableUnicodeConverter; },
    
    // JavaScript debugger
    get jsdIDebuggerServiceCID() { return '@mozilla.org/js/jsd/debugger-service;1'; },

    get jsdIDebuggerServiceIF() { return Components.interfaces.jsdIDebuggerService; },

    get jsdIErrorHookIF() { return Components.interfaces.jsdIErrorHook; },

    get jsdIExecutionHookIF() { return Components.interfaces.jsdIExecutionHook; },

    get jsdIPropertyIF() { return Components.interfaces.jsdIProperty; },

    get jsdIValueIF() { return Components.interfaces.jsdIValue; },
    
    // Firebug
    get FirebugCID() { return '@joehewitt.com/firebug;1'; },
    
    get FirebugIF() { return Components.interfaces.nsIFireBug; },
    
    get FirebugDebuggerIF() { return Components.interfaces.nsIFireBugDebugger; },
    
    get FirebugNetworkDebuggerIF() { return Components.interfaces.nsIFireBugNetworkDebugger; },
    
    get FirebugScriptListenerIF() { return Components.interfaces.nsIFireBugScriptListener; }
}
