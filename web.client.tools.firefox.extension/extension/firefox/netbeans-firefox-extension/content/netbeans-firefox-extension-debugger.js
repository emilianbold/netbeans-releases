/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR this HEADER.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

// NetBeans Debugger
(function() {
    const DEBUG = true;

    // Version
    this.VERSION = NetBeans.Constants.VERSION;
    this.DEBUG = DEBUG;

    // jsdI stop types
    const TYPE_INTERRUPTED = NetBeans.Constants.jsdIExecutionHookIF.TYPE_INTERRUPTED;
    const TYPE_BREAKPOINT = NetBeans.Constants.jsdIExecutionHookIF.TYPE_BREAKPOINT;
    const TYPE_DEBUG_REQUESTED = NetBeans.Constants.jsdIExecutionHookIF.TYPE_DEBUG_REQUESTED;
    const TYPE_DEBUGGER_KEYWORD = NetBeans.Constants.jsdIExecutionHookIF.TYPE_DEBUGGER_KEYWORD;
    const TYPE_THROW = NetBeans.Constants.jsdIExecutionHookIF.TYPE_THROW;

    // jsdI return types
    const RETURN_CONTINUE = NetBeans.Constants.jsdIExecutionHookIF.RETURN_CONTINUE;
    const RETURN_CONTINUE_THROW = NetBeans.Constants.jsdIExecutionHookIF.RETURN_CONTINUE_THROW;
    const RETURN_RET_WITH_VAL = NetBeans.Constants.jsdIExecutionHookIF.RETURN_RET_WITH_VAL;
    const RETURN_THROW_WITH_VAL = NetBeans.Constants.jsdIExecutionHookIF.RETURN_THROW_WITH_VAL;

    // jsdI report types
    const REPORT_ERROR = NetBeans.Constants.jsdIErrorHookIF.REPORT_ERROR;
    const REPORT_EXCEPTION = NetBeans.Constants.jsdIErrorHookIF.REPORT_EXCEPTION;
    const REPORT_WARNING = NetBeans.Constants.jsdIErrorHookIF.REPORT_WARNING;

    // jsdIValue types
    const TYPE_VOID     = NetBeans.Constants.jsdIValueIF.TYPE_VOID;
    const TYPE_NULL     = NetBeans.Constants.jsdIValueIF.TYPE_NULL;
    const TYPE_BOOLEAN  = NetBeans.Constants.jsdIValueIF.TYPE_BOOLEAN;
    const TYPE_INT      = NetBeans.Constants.jsdIValueIF.TYPE_INT;
    const TYPE_DOUBLE   = NetBeans.Constants.jsdIValueIF.TYPE_DOUBLE;
    const TYPE_STRING   = NetBeans.Constants.jsdIValueIF.TYPE_STRING;
    const TYPE_FUNCTION = NetBeans.Constants.jsdIValueIF.TYPE_FUNCTION;
    const TYPE_OBJECT   = NetBeans.Constants.jsdIValueIF.TYPE_OBJECT;

    // display types
    const CONST_TYPE_VOID       = "void";
    const CONST_TYPE_NULL       = "null";
    const CONST_TYPE_BOOLEAN    = "boolean";
    const CONST_TYPE_INT        = "int";
    const CONST_TYPE_DOUBLE     = "double";
    const CONST_TYPE_STRING     = "string";
    const CONST_TYPE_OBJECT     = "object";
    const CONST_TYPE_FUNCTION   = "function";
    const CONST_NATIVE_FUNCTION = "Native Function";
    const CONST_SCRIPT_FUNCTION = "Script Function";
    const CONST_CLASS_XPCOBJ    = "class_xpcobj";
    const CONST_CLASS_CONST_XPCOBJ = "class_const_xpcobj";

    //jsdI property flags
    const PROP_ENUMERATE    = NetBeans.Constants.jsdIPropertyIF.FLAG_ENUMERATE;
    const PROP_READONLY     = NetBeans.Constants.jsdIPropertyIF.FLAG_READONLY;
    const PROP_PERMANENT    = NetBeans.Constants.jsdIPropertyIF.FLAG_PERMANENT;
    const PROP_ALIAS        = NetBeans.Constants.jsdIPropertyIF.FLAG_ALIAS;
    const PROP_ARGUMENT     = NetBeans.Constants.jsdIPropertyIF.FLAG_ARGUMENT;
    const PROP_VARIABLE     = NetBeans.Constants.jsdIPropertyIF.FLAG_VARIABLE;
    const PROP_EXCEPTION    = NetBeans.Constants.jsdIPropertyIF.FLAG_EXCEPTION;
    const PROP_ERROR        = NetBeans.Constants.jsdIPropertyIF.FLAG_ERROR;
    const PROP_HINTED       = NetBeans.Constants.jsdIPropertyIF.FLAG_HINTED;
    const PROP_CONST        = 0x8000;

    // patterns
    const CONSTANTS_FILTER    = new RegExp("^[A-Z][A-Z_]*$");
    const JAVA_OBJECT_PATTERN = new RegExp("^Java(Array|Member|Object|Package)$");
    const WATCH_SRCIPT = '[Watch-script]';

    var topWindow;
    var browser;

    var currentUrl;
    var loadedSources = {};

    var releaseFirebugContext = false;
    var currentFirebugContext = null;

    var DebuggerListener;

    var features = {
        showFunctions: false,
        showConstants: true,
        bypassConstructors: false,
        stepFiltersEnabled: false,
        suspendOnFirstLine: false,
        suspendOnExceptions: false,
        suspendOnErrors: false,
        suspendOnDebuggerKeyword: true,
        suspendOnAssert: false,
        http_monitor:false
    };

    var jsDebuggerService;
    var firebugDebuggerService;

    var socket = null;

    var enabled = false;

    var transactionId;

    var hookReturn;

    var debugState = {};
    var stepping = false;
    var debugging = false;

    var port;
    var sessionId;

    // FirebugDebugger
    const firebugDebugger = {
        
        QueryInterface : function(iid)
        {
            if (iid.equals(NetBeans.Constants.FirebugDebuggerIF)||
                iid.equals(NetBeans.Constants.FirebugNetworkDebuggerIF)||
                iid.equals(NetBeans.Constants.FirebugScriptListenerIF)||
                iid.equals(NetBeans.Constants.SupportsIF)) {
                return this;
            }
            throw NetBeans.Constants.NS_NOINTERFACE;
        },

        // nsIFireBugDebugger
        supportsWindow: function(win)
        {
            return false;
        },

        supportsGlobal: function(global) 
        {
            return false;
        },
        
        onBreak: function(frame, type)
        {
            return RETURN_CONTINUE;
        },

        onHalt: function(frame)
        {
            return RETURN_CONTINUE;
        },

        onThrow: function(frame, rv)
        {
            return RETURN_CONTINUE;
        },

        onCall: function(frame){},
        onError: function(frame, error){},
        onLock: function(state){},
        onTopLevel: function(frame){},
        onEvalScript: function(url, lineNo, script){},
        onTopLevelScript: function(url, lineNo, script){},
        onToggleBreakpoint: function(url, lineNo, isSet, props){},
        onToggleErrorBreakpoint: function(url, lineNo, isSet){},
        onToggleMonitor: function(url, lineNo, isSet){},

        // nsIFireBugNetworkDebugger implementation
        suspendActivity: function()
        {
            if ( socket ) {
                socket.stopProcessing();
            }
        },

        resumeActivity: function()
        {
            if ( socket ) {
                socket.startProcessing();
            }
        },

        // nsIFireBugScriptListener
        onScriptCreated: function(script, url, lineNo)
        {
            if ( !enabled ) {
                return;
            }
            var fileName = script.fileName;
            if ((fileName == WATCH_SRCIPT) || (fileName.substr(0,11) == 'javascript:')) {
                return;
            }
            var sourceKey = "" + fileName;
            if(sourceKey in loadedSources) {
                return;
            }
            loadedSources[sourceKey] = true;
            sendSourcesMessage(loadedSources);
        },

        onScriptDestroyed: function(script){}

    };
    firebugDebugger.wrappedJSObject = firebugDebugger;

    this.initDebugger = function(_port, _sessionId) {
        port = _port;
        sessionId = _sessionId;

        jsDebuggerService = FBL.jsd;

        firebugDebuggerService = FBL.fbs;

        const socketListener = {
            onDBGPCommand: function(command) {
                try {
                    processCommand(command);
                } catch (e) {
                    window.NetBeans.Logger.logException(e);
                }
            },
            onDBGPClose: function() {
                window.NetBeans.Debugger.shutdown();
            }
        };
        
        // create DBGP socket
        socket = NetBeans.SocketUtils.createSocket("127.0.0.1", port, socketListener);

        fbsRegisterDebugger(firebugDebugger);

        initialize(this);
        sendInitMessage();
    }

    // Initalize Firebug.Extension and Firebug.DebuggerListener and register accordingly
    function initialize (netBeansDebugger)
    {
        const NetBeansDebuggerExtension = FBL.extend(Firebug.Extension,
        {
            // #1 Accept Context / Decline Context
            acceptContext: function(win,uri)
            {
                if ( !topWindow ) {
                    topWindow = win;
                    browser = NetBeans.Utils.getBrowserByWindow(win);
                    wrapBrowserDestroy();
                    currentUrl = uri.prePath+uri.path;
                    return true;
                } else if ( topWindow == win && browser == NetBeans.Utils.getBrowserByWindow(win) ) {
                    if ( currentFirebugContext != null && releaseFirebugContext ) {
                        currentUrl = uri.prePath+uri.path;
                        return true;
                    }
                }
                return false;
            },

            declineContext: function(win,uri)
            {
                if ( topWindow ) {
                    return true;
                }
                return false;
            },

            // #2 Unwatch Window ( For Reset )
            unwatchWindow: function(context, win)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    netBeansDebugger.detachFromWindow(win);
                    if (features["http_monitor"]  ) {
                        NetBeans.NetMonitor.destroyMonitor(context, browser);
                    }

                }

            },

            // #3 Destroy Context ( For Reset )
            destroyContext: function(context)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    releaseFirebugContext = true;
                    netBeansDebugger.onDestroy(netBeansDebugger);


                }
            },

            // #4 Init Context
            initContext: function(context)
            {
                if ( topWindow && context.window == topWindow ) {
                    currentFirebugContext = context;
                    releaseFirebugContext = false;
                    netBeansDebugger.onInit(netBeansDebugger);
                }
            },

            // #5 Show Current Context - we didn't need this.'
            showContext: function(browser, context) {

                if (features.suspendOnFirstLine) {
                    features.suspendOnFirstLine = false;
                    suspend("firstline");
                }
            },

            // #6 Watch Window ( attachToWindow )
            watchWindow: function(context, win)
            {

                if ( context == currentFirebugContext && currentFirebugContext ) {
                    netBeansDebugger.attachToWindow(win);
                    // We would be better off using the Firefox preferences so we can observe and turn on and off
                    // http monitor as needed rather than only at the beginning.

                    //Joelle: Did you do this == true for a reason?  Double check later when you get time.
                    if (features["http_monitor"] ) {
                        NetBeans.NetMonitor.initMonitor(context, browser, socket);
                    }
                }

            },

            // #7 Loaded Context
            loadedContext: function(context)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    netBeansDebugger.onLoaded(currentUrl);
                    delete currentUrl;
                }
            }

        }
        );


        DebuggerListener = FBL.extend(Firebug.DebuggerListener,
        {
            onJSDActivate: function(jsd)
            {
            },
            
            // function signature changed between firebug 1.1 and 1.2
            onStop: function(context, frame /*type*/, type /*rv*/, rv)
            {
                if (NetBeans.Utils.isFF2()) {
                    rv = type;
                    type = frame;
                    frame = context.debugFrame;
                }
                
                if ( context == currentFirebugContext ) {
                    // XXX hideDebuggerUI is not used in firebug 1.2; needs to be replaced?
                    context.hideDebuggerUI = true;
                    return netBeansDebugger.onStop(frame, type, rv);
                }
            },

            onResume: function(context)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    if ( releaseFirebugContext ) {
                        currentFirebugContext = null;
                    }
                    netBeansDebugger.onResume();
                    // XXX hideDebuggerUI is not used in firebug 1.2; needs to be replaced?
                    context.hideDebuggerUI = false;
                }
            },

            onThrow: function(context, frame, rv)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    return netBeansDebugger.onThrow(frame,rv);
                }
            },

            onError: function(context, frame, error)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    return netBeansDebugger.onError(frame,error);
                }
            },

            onTopLevel: function(context, frame)
            {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    netBeansDebugger.onTopLevel();
                }
            },
            
            // XXX replacement for onTopLevel in firebug 1.2
            onTopLevelScriptCreated: function(context, frame, url) {
                if ( context == currentFirebugContext && currentFirebugContext ) {
                    netBeansDebugger.onTopLevel();
                }                
            },
            
            onEventScriptCreated: function(context, frame, url)
            {
            },
            
            onFunctionConstructor: function(context, frame, ctor_script, url)
            {
                // XXX Not active as of firebug 1.2b06
            }
        }
        );

        // Make sure we our shutdown when the browser is destroyed.
        function wrapBrowserDestroy()
        {
            browser._destroy = browser.destroy;
            browser.destroy = function() {
                netBeansDebugger.shutdown();
                if(this._destroy) {
                    this.destroy = this._destroy;
                    this.destroy();
                }
            }
        }

        function unwrapBrowserDestroy()
        {
            if (browser && browser._destroy)
            {
                browser.destroy = browser._destroy;
                delete browser._destroy;
            }
        }

        Firebug.registerExtension(NetBeansDebuggerExtension);
        
        Firebug.Debugger.isHostEnabled = function(context) {
            return topWindow && context.window == topWindow;
        }
        
    }

    function disableFirebugDebugger()
    {
        Firebug.Debugger.removeListener(DebuggerListener);
        if ( topWindow && currentFirebugContext && !releaseFirebugContext ) {
            TabWatcher.unwatchTopWindow(topWindow);
        }
        topWindow = null;
        unwrapBrowserDestroy();
        browser = null;
    }

    function abortFirebugDebugger()
    {
        Firebug.Debugger.abort(currentFirebugContext);
    }

    this.isInitialized = function()
    {

        return socket != null;
    }

    this.isEnabled = function()
    {

        return enabled;
    }

    this.isSuspendEnabled = function()
    {

        return enabled && !stepping && !debugging;
    }

    this.sendClientAction = function(action){}

    function hook(name, args)
    {
        if ( name in hooks ) {
            return hooks[name].apply(this,args);
        }
    }

    this.contextHook = hook;

    this.setHook = function(name, hook)
    {

        hooks[name] = hook;
    }

    this.shutdown = function()
    {

        this.shutdownInProgress = true;
        if ( !jsDebuggerService )
            return;
        if ( !((currentFirebugContext == null) || releaseFirebugContext) )
            return;
        disable();

        if ( socket ) {
            socket.close();
            socket = null;
        }
        if ( firebugDebuggerService ) {
            fbsUnregisterDebugger(firebugDebugger);
        }
        firebugDebuggerService = null;
        jsDebuggerService = null;
    }

    function onTryCloseWindow(event,prevOnClose)
    {

        if ( typeof(prevOnClose) == 'function' ) {
            return prevOnClose(event);
        }
        return true;
    }

    // command handling
    const commandRegularExpression = /^\s*(\w+)\s*-i\s*(\d+)\s*(.*)$/;

    function processCommand(command) {
        command = command.replace(/\r\n|\r|\n/g, '');

        var matches = commandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a command out of [" + command + "]");
        }
        var cmd = matches[1];
        transactionId = matches[2];

        //NetBeans.Logger.log("debugger.commandRegularExpress cmd:" +cmd);

        if (cmd == "feature_set") {
            feature_set(matches[3]);
        } else if (cmd == "breakpoint_set") {
            breakpoint_set(transactionId, matches[3]);
        } else if (cmd == "breakpoint_remove") {
            breakpoint_remove(transactionId, matches[3]);
        } else if (cmd == "breakpoint_update") {
            breakpoint_update(transactionId, matches[3]);
        } else if (cmd == "open_uri") {
            open_uri(matches[3]);
        } else if (cmd == "run") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is already running");
                return;
            }
            resume("resume");
        } else if (cmd == "pause") {
            if ( debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is already suspended");
                return;
            }
            suspend("suspend");
        } else if (cmd == "step_into" || cmd == "step_over" || cmd == "step_out") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is already running");
                return;
            }
            step(cmd);
        } else if (cmd == "stack_depth") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is running");
                return;
            }
            sendStackDepthResponse(transactionId);
        } else if (cmd == "stack_get") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is running");
                return;
            }
            sendStackGetResponse(transactionId);
        } else if (cmd == "property_get") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is running");
                return;
            }
            property_get(transactionId, matches[3]);
        } else if (cmd == "property_set") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is running");
                return;
            }
            property_set(transactionId, matches[3]);
        } else if (cmd == "eval") {
            if ( !debugging ) {
                NetBeans.Logger.log("Invalid Command: " + cmd  + " - debuggee is running");
                return;
            }
            onEval(transactionId, matches[3]);
        } else if (cmd == "source") {
            source(transactionId, matches[3]);
        }   else if (cmd == "stop") {
            terminate();
        }
        transactionId = -1;
    }

    // TODO: Call Shutdown
    function onClose()  {
    }


    // 1. feature_set
    const feature_setCommandRegularExpression = /^\s*-n\s*(\w+)\s*-v\s*(.+)\s*$/;
    function feature_set(command) {
        var matches = feature_setCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a feature_set command arguments out of [" + command + "]");
        }
        if (matches[1] == 'enable') {
            try {
                if (matches[2] == 'true') {
                    enable();
                } else {
                    disable();
                }
                sendFeatureSetResponse(matches[1], '1', transactionId);
                sendOnResumeMessage();
            } catch (ex) {
                sendFeatureSetResponse(matches[1], '0', transactionId);
            }
            return;
        }
        if (matches[1] in features) {
            //NetBeans.Logger.log("debugger.feature_set - Feature: " + matches[1]);
            if ( typeof(features[matches[1]]) == 'boolean' ) {
                //NetBeans.Logger.log("debugger.feature_set - Boolean:" + matches[2] );
                if ( matches[1] == 'http_monitor' && features['http_monitor'] != matches[2] ){
                    setHttpMonitor(matches[2]);
                }
                features[matches[1]] = (matches[2] == 'true');
            } else {
                features[matches[1]] = matches[2];
            }
            sendFeatureSetResponse(matches[1], '1', transactionId);
        } else {
            sendFeatureSetResponse(matches[1], '0', transactionId);
        }
    }

    function setHttpMonitor(isEnabled) {
        if ( currentFirebugContext ){
            if ( isEnabled == 'true' ){  //looking for the string "true"
                NetBeans.NetMonitor.initMonitor(currentFirebugContext, browser, socket);
            } else {
                NetBeans.NetMonitor.destroyMonitor(currentFirebugContext, browser);
            }
        }
    }


    // 2. Breakpoints
    const breakpoint_setCommandRegularExpression = /^\s*-t\s*(line)\s*-s\s*(enabled|disabled)\s*-r\s*(0|1)\s*-f\s*(\S+)\s*-n\s*(\d+)\s*-h\s*(\d+)\s*-o\s*(==|>=|%)\s*--\s+(.*)$/;

    function breakpoint_set(transaction_id, command) {
        var matches = breakpoint_setCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a breakpoint_set command arguments out of [" + command + "]");
        }

        var disabled = matches[2] == "disabled";
        var isTemporary = matches[3] == "1";
        var href = matches[4];
        var line = matches[5];
        var hitValue = matches[6];
        var hitCondition = matches[7];
        var condition = matches[8];

        if (!isTemporary) {
            setBreakpoint(href, line,
            {
                disabled: disabled,
                hitCount: hitValue,
                hitCondition: hitCondition,
                condition: condition,
                onTrue: true
            });
        }

        var breakpointSetResponse =
        <response command="breakpoint_set"
        state={
        disabled ? "disabled" : "enabled"
        }
        id={
        href + ":" + line
        }
        transaction_id={
        transaction_id
        } />;

        socket.send(breakpointSetResponse);

        if (isTemporary) {
            runUntil(href, line);
        }
    }

    const breakpoint_updateCommandRegularExpression = /^\s*-d\s*(\S+)\s*(.*)$/;
    // Line number change is treated as remove and an add
    const breakpoint_updateSubCommandRegularExpression = /^\s*-s\s*(enabled|disabled)\s*-r\s*(0|1)\s*-h\s*(\d+)\s*-o\s*(==|>=|%)\s*--\s+(.*)$/;
    const breakpointIdRegularExpression = /^\s*(\S+):(\d+)\s*$/;
    function breakpoint_update(transaction_id, command)
    {
        var breakpointUpdateResponse =
        <response command="breakpoint_update"
        transaction_id={
        transaction_id
        } />;

        var matches = breakpoint_updateCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a breakpoint_update command arguments out of [" + command + "]");
        }

        var breakpointId = matches[1];
        var subcommand = matches[2];

        matches = breakpointIdRegularExpression.exec(breakpointId);
        if (!matches) {
            throw new Error("Can't split breakpointId " + breakpointId);
        }

        var href = matches[1];
        var line = matches[2];

        matches = breakpoint_updateSubCommandRegularExpression.exec(subcommand);
        if (!matches) {
            throw new Error("Can't get a breakpoint_update sub command arguments out of [" + subcommand + "]");
        }

        var disabled = ("disabled" ==  matches[1]);
        var hitValue = matches[3];
        var hitCondition = matches[4];
        var condition = matches[5];

        // Remove and add with new properties
        removeBreakpointId(breakpointId);
        setBreakpoint(href, line,
        {
            disabled: disabled,
            hitCount: hitValue,
            hitCondition: hitCondition,
            condition: condition,
            onTrue: true
        });

        socket.send(breakpointUpdateResponse);
    }

    const breakpoint_removeCommandRegularExpression = /^\s*-d\s*(\S+)\s*$/;
    function breakpoint_remove(transaction_id, command)
    {
        var breakpointRemoveResponse =
        <response command="breakpoint_remove"
        transaction_id={
        transaction_id
        } />;

        var matches = breakpoint_removeCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a breakpoint_remove command arguments out of [" + command + "]");
        }

        var breakpointId = matches[1];
        removeBreakpointId(breakpointId);

        socket.send(breakpointRemoveResponse);
    }

    function removeBreakpointId(breakpointId)
    {
        var matches = breakpointIdRegularExpression.exec(breakpointId);
        if (!matches) {
            throw new Error("Can't split breakpointId " + breakpointId);
        }

        var href = matches[1];
        var line = matches[2];
        clearBreakpoint(href, line);

        return true;
    }

    function enableDisableBreakpointId(breakpointId, enable)
    {
        var matches = breakpointIdRegularExpression.exec(breakpointId);
        if (!matches) {
            throw new Error("Can't split breakpointId " + breakpointId);
        }

        var href = matches[1];
        var line = matches[2];

        if (hasBreakpoint(href, line)) {
            if (enable) {
                fbsEnableBreakpoint(href, line);
            } else {
                fbsDisableBreakpoint(href, line);
            }
            return true;
        }
        return false;
    }

    function setConditionOfBreakpointId(breakpointId, condition)
    {
        var matches = breakpointIdRegularExpression.exec(breakpointId);
        if (!matches) {
            throw new Error("Can't split breakpointId " + breakpointId);
        }

        var href = matches[1];
        var line = matches[2];

        if (hasBreakpoint(href, line)) {
            fbsSetBreakpointCondition(href, line, condition);
            return true;
        }
        return false;
    }

    var breakpoints = {};

    function hasBreakpoint(href,line)
    {
        if ( href in breakpoints ) {
            var list = breakpoints[href];
            for( var i = 0; i < list.length; ++i ) {
                if ( list[i] == line )
                    return true;
            }
        }
        return false;
    }

    function clearBreakpoint(href,line)
    {
        if ( removeBreakpoint(href,line) ) {
            fbsClearBreakpoint(href,line);
            return true;
        }
        return false;
    }

    function clearAllBreakpoints()
    {
        var hrefs = [];
        for( var href in breakpoints ) {
            if ( breakpoints[href].length > 0 )
                hrefs.push(href);
        }
        fbsClearAllBreakpoints(hrefs.length,hrefs);
    }

    function setBreakpoint(href,line,props)
    {
        if ( fbsSetBreakpoint(href,line,props) ) {
            addBreakpoint(href,line);
            return true;
        }
        return false;
    }

    function addBreakpoint(href,line)
    {
        if ( hasBreakpoint(href,line)){
            return false;
        }
        if ( href in breakpoints ) {
            breakpoints[href].push(line);
        } else {
            breakpoints[href] = [line];
        }
        return true;
    }

    function removeBreakpoint(href,line)
    {
        if ( !hasBreakpoint(href,line)){
            return false;
        }
        if ( href in breakpoints ) {
            var list = breakpoints[href];
            for( var i = 0; i < list.length; ++i ) {
                if ( list[i] == line ) {
                    if ( list.length > 1 ) {
                        list.slice(i,1);
                    } else {
                        delete breakpoints[href];
                    }
                    return true;
                }
            }
        }
        return false;
    }


    // 3. enable/disable
    function enable()
    {
        if (enabled)
            return;
        enabled = true;

        Firebug.Debugger.addListener(DebuggerListener);
    }

    function disable()
    {
        if (!enabled)
            return;
        enabled = false;

        clearAllBreakpoints();
        disableFirebugDebugger();
    }

    // 4. open_uri
    const open_uriCommandRegularExpression = /^-f\s*(\S+)\s*$/;
    function open_uri(command) {

        var matches = open_uriCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a open_uri command arguments out of [" + command + "]");
        }
        var debugURI = matches[1];
        var headers = "Cache-Control: no-cache, must-revalidate\r\nPragma: no-cache\r\n";
        var headersStream = NetBeans.Utils.CCIN(
            NetBeans.Constants.StringInputStreamCID,
            NetBeans.Constants.StringInputStreamIF);

        headersStream.setData(headers, headers.length);
        window.getWebNavigation().loadURI(debugURI,
            NetBeans.Constants.WebNavigationIF.LOAD_FLAGS_BYPASS_PROXY|NetBeans.Constants.WebNavigationIF.LOAD_FLAGS_BYPASS_CACHE,
            null,
            null,
            headersStream);
    }

    // 5. resume/suspend
    function resume(reason)
    {
        Firebug.Debugger.resume(currentFirebugContext);
    }

    // 7. run until
    function runUntil(url, lineno) {
        var src = url;
        if (NetBeans.Utils.isFF2()) {
            if (currentFirebugContext) {
                src = currentFirebugContext.sourceFileMap[href];
            }
            if (!src) {
                src = new FBL.NoScriptSourceFile(currentFirebugContext, href);
            }
        }
        
        Firebug.Debugger.runUntil(currentFirebugContext, src, lineno);
    }

    function suspend(reason)
    {
        if ( reason )
            debugState.suspendReason = reason;
        stepping = true;
        Firebug.Debugger.suspend(currentFirebugContext);
    }

    // 6. Step
    function step(reason)
    {
        debugState.suspendReason = reason;
        stepping = true;
        switch(reason)
        {
            case "step_into":
                Firebug.Debugger.stepInto(currentFirebugContext);
                break;
            case "step_over":
                Firebug.Debugger.stepOver(currentFirebugContext);
                break;
            case "step_out":
                Firebug.Debugger.stepOut(currentFirebugContext);
                break;
            case "stepToFrame":
                // TODO
                break;
        }
    }

    // property_get
    const property_getCommandRegularExpression = /^\s*-n\s*(\S+)\s*-d\s*(\d+)\s*$/;

    function property_get(transaction_id, command) {

        var propertyGetResponse =
        <response
        command="property_get"
        transaction_id={
        transaction_id
        } ></response>;

        if (! debugging ) {
            socket.send(propertyGetResponse);
            return;
        }

        var matches = property_getCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a property_get command arguments out of [" + command + "]");
        }

        var propertyFullName = matches[1];
        var frameIndex = parseInt(matches[2]);

        getLocalVariables(frameIndex, propertyFullName, propertyGetResponse);

        socket.send(propertyGetResponse);
    }
    
    // property_get
    const property_setCommandRegularExpression = /^\s*-n\s*(\S+)\s*-d\s*(\d+)\s*--\s*(.+)$/;

    function property_set(transaction_id, command) {
        var propertySetResponse =
        <response
        command="property_set"
        transaction_id={
        transaction_id
        } ></response>;

        if (! debugging ) {
            socket.send(propertySetResponse);
            return;
        }

        var matches = property_setCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a property_set command arguments out of [" + command + "]");
        }

        var propertyFullName = matches[1];
        var frameIndex = parseInt(matches[2]);
        var value = matches[3];
        var frame = debugState.frames[frameIndex];
        var parent = null;
        if(propertyFullName.indexOf(".") == 0) {
            while (propertyFullName.indexOf(".") == 0) {
                parent = ((parent == null) ? frame.scope : parent.jsParent);
                propertyFullName = propertyFullName.substring(1);
            }
        }
        var nameParts = propertyFullName.split(".");
        var propertyName = nameParts[nameParts.length-1];
        if(nameParts.length > 1) {
            nameParts.splice(nameParts.length-1,1);
            if(nameParts == "this") {
                parent = frame.thisValue;
                nameParts.splice(0,1);
            }else if(nameParts == "[exception]") {
                parent = debugState.currentException;
                nameParts.splice(0,1);
            }
        }
        var relativeParent = parent;
        if(nameParts.length > 1) {
            var relativeParentName = nameParts.join('.');
            relativeParent = resolveVariable(parent, relativeParentName);
        }
        var success = "0";
        if(relativeParent != null) {
            var obj = relativeParent.getWrappedValue();
            var rval = new Object();
            if(frame.eval(value, WATCH_SRCIPT,1,rval)) {
                rval = ("value" in rval) ? rval.value : null;
                if(rval != null) {
                    try {
                        obj[propertyName] = rval.getWrappedValue();
                        success = "1";
                    }catch(e) {
                        NetBeans.Logger.log("NetbeansDebugger: Unable to set value: " + e, "err");
                    }     
                }
            }
        }
        
        propertySetResponse =
        <response
        command="property_set"
        transaction_id={transaction_id}
        success={success}>
        </response>;               

        socket.send(propertySetResponse);
    }
    
    // Eval
    const evalCommandRegularExpression = /^\s*-d\s*(\d+)\s*-e\s*(.+)$/;

    function onEval(transaction_id, command)
    {
        var evalResponse =
        <response
        command="eval"
        success="0"
        transaction_id={
        transaction_id
        } ></response>;

        if (! debugging ) {
            socket.send(evalResponse);
            return;
        }

        var matches = evalCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a onEvel command arguments out of [" + command + "]");
        }

        var frameIndex = parseInt(matches[1]);
        var expression = matches[2];

        var frame = debugState.frames[frameIndex];
        var rval = new Object();
        if ( frame.eval(expression, WATCH_SRCIPT,1,rval) )
        {
            rval = ("value" in rval) ? rval.value : null;
            if (rval != null ) {
                var val = getPropertyValue(rval);
                evalResponse.property =
                <property
                name={
                expression
                }
                fullname={
                expression
                }
                type={
                val.type
                }
                numchildren="0"
                encoding="none">{
                val.displayValue
                }</property>;
                if (val.type == "object" || val.type == "function" || val.type == "array") {
                    evalResponse.property.@classname = val.displayType;
                    evalResponse.property.property = buildPropertiesList(expression, rval);
                    evalResponse.property.@numchildren = evalResponse.property.property.length();
                }
                // Indicate success
                evalResponse.@success ="1";
            }
        }
        socket.send(evalResponse);
    }

    // source
    const sourceCommandRegularExpression = /^-f\s*(\S+)\s*$/;

    function source(transaction_id, command)
    {
        var matches = sourceCommandRegularExpression.exec(command);
        if (!matches) {
            throw new Error("Can't get a source command arguments out of [" + command + "]");
        }
        var sourceURI = matches[1];
        var data = NetBeans.Utils.getSourceAsync(sourceURI,
            function(data, succeeds) {
                var sourceResponse =
                    <response command="source"
                        success={(succeeds ? "1" : "0")}
                        transaction_id={transaction_id}>{data}</response>;
                socket.send(sourceResponse);
            }
        );
    }



    // responses to ide
    function sendInitMessage()
    {
        var initMessage =
        <init appid="netBeans-firefox-extension"
        idekey="6.1TP"
        session={
        sessionId
        }
        thread="1"
        parent="Firefox"
        language="JavaScript"
        protocol_version="1.0"
        fileuri="about:blank"/>;

        socket.send(initMessage);
    }

    // TODO: Do we need this?
    function sendOnloadMessage(uri)
    {
        var onloadMessage =
        <onload appid="netBeans-firefox-extension"
        idekey="6.1TP"
        session={
        sessionId
        }
        thread="1"
        parent="Firefox"
        language="JavaScript"
        protocol_version="1.0"
        fileuri={
        uri
        }/>;

        socket.send(onloadMessage);
    }

    function sendFeatureSetResponse(optionName, success, transaction_id)
    {
        var featureSetResponse =
        <response command="feature_set"
        feature={
        optionName
        }
        success={
        success
        }
        transaction_id={
        transaction_id
        } />;

        socket.send(featureSetResponse);
    }

    function sendOnResumeMessage()
    {
        var onResumeMessage =
        <response
        command="status"
        status="running"
        reason="ok" />;
        socket.send(onResumeMessage);
    }

    function sendOnDebuggerMessage()
    {
        var onDebuggerMessage =
        <response
        command="status"
        status="debugger"
        reason="ok" />;
        socket.send(onDebuggerMessage);
    }

    function sendOnFirstLineMessage()
    {
        var onFirstLineMessage =
        <response
        command="status"
        status="first_line"
        reason="ok" />;
        socket.send(onFirstLineMessage);
    }

    function sendOnExceptionMessage()
    {
        var onExceptionMessage =
        <response
        command="status"
        status="exception"
        reason="ok" />;
        socket.send(onExceptionMessage);
    }

    function sendOnBreakpointMessage(breakpoint_id, uri, line)
    {
        var onBreakpointMessage =
        <response
        command="status"
        status="breakpoint"
        reason="ok">
        <message
        filename={
        uri
        }
        lineno={
        line
        }
        id={
        breakpoint_id
        } />
        </response>;
        socket.send(onBreakpointMessage);
    }

    function sendOnStepMessage()
    {
        var onStepMessage =
        <response
        command="status"
        status="step"
        reason="ok" />;
        socket.send(onStepMessage);
    }

    function sendStackDepthResponse(transaction_id)
    {
        var stackDepthRespose =
        <response command="stack_depth"
        depth={
        (debugState.frames ? debugState.frames.length : -1)
        }
        transaction_id={
        transaction_id
        } />;
        socket.send(stackDepthRespose);
    }

    function sendStackGetResponse(transaction_id)
    {
        var stackGetRespose =
        <response command="stack_get" transaction_id={
        transaction_id
        } />;
        if (debugging && debugState.frames && debugState.frames.length > 0) {
            for (var level = 0; level < debugState.frames.length; level++ ) {
                var stackElement =
                <stack
                level={
                level
                }
                type={
                (debugState.frames[level].isNative ? "native" : "file")
                }
                filename={
                debugState.frames[level].script.fileName
                }
                lineno={
                debugState.frames[level].line
                }
                where={
                getFunctionName(debugState.frames[level].script)
                }
                pc={
                debugState.frames[level].pc
                }
                />;
                stackGetRespose.stack += stackElement;
            }
        }
        socket.send(stackGetRespose);
    }

    function getFunctionName(script)
    {
        var functionName = script.functionName;
        if ( !functionName )
            functionName = '';
        return functionName;
    }


    // Format of the message is:
    // <windows>
    //   <window fileuri="http://..." />
    //   <window fileuri="http://..." />
    //   :
    // </windows>
    function sendWindowsMessage(windows)
    {
        var windowsMessage =
        <windows encoding="base64"></windows>;
        for each (var aWindow in windows) {
            if (aWindow.top == aWindow) {
                buildWindowsMessage([aWindow], windowsMessage);
            }
        }
        socket.send(windowsMessage);
    }

    function buildWindowsMessage(windows, windowsMessage)
    {
        if (windows.length == 0) {
            return;
        }
        for each (var aWindow in windows) {
            var windowMessage = <window fileuri={
            window.btoa(aWindow.document.location.href)
            } />;

            var frameWindows = aWindow.frames;
            if (frameWindows) {
                for (var i = 0; i < frameWindows.length; i++) {
                    buildWindowsMessage([frameWindows[i]], windowMessage);
                }
            }
            windowsMessage.window += windowMessage;
        }
    }

    // Format of the message is:
    // <sources>
    //   <source fileuri="http://..." />
    //   <source fileuri="http://..." />
    //   :
    // </sources>
    function sendSourcesMessage(sources)
    {
        var sourcesMessage =
        <sources encoding="base64"></sources>;

        for (var source in sources) {
            sourcesMessage.source +=
            <source fileuri={
            window.btoa(source)
            } />;
        }

        socket.send(sourcesMessage);
    }

    function terminate()
    {
        const delayShutdownIfDebugging = function() {
            disable();
            NetBeans.Debugger.shutdown();
            window.close();
        };

        if (debugging) {
            debugState.shutdownHook = delayShutdownIfDebugging;
            abortFirebugDebugger();
        } else {
            delayShutdownIfDebugging();
        }
    }

    // Stepping
    this.onStop = function(frame, type, rv)
    {
        if (debugging)
            return RETURN_CONTINUE;

        hookReturn = RETURN_CONTINUE;
        switch( type )
        {
            case TYPE_INTERRUPTED:
                if ( frame.isConstructing && features.bypassConstructors ) {
                    return hookReturn;
                }
                break;
            case TYPE_BREAKPOINT:
                debugState.suspendReason = "breakpoint";
                break;
            case TYPE_DEBUG_REQUESTED:
                if ( debugState.currentException ) {
                    delete debugState.currentException;
                    return hookReturn;
                }
                debugState.suspendReason = "requested";
                break;
            case TYPE_DEBUGGER_KEYWORD:
                if ( !features.suspendOnDebuggerKeyword )
                    return hookReturn;
                debugState.suspendReason = "debugger";
                break;
            case TYPE_THROW:
                hookReturn = RETURN_CONTINUE_THROW;
                debugState.suspendReason = "exception";
                debugState.currentException = rv.value;
                break;
            default:
                message = "onStop() - Unknown type: " + type;
                NetBeans.Logger.logException(message);
        }
        debugState.hookReturnValue = rv;
        debugging = true;
        stepping = false;

        // TODO update status bar

        debugState.frames = new Array();
        var prevFrame = frame;
        while (prevFrame)
        {
            var fileName = prevFrame.script.fileName;
            if ( fileName.indexOf("chrome:") != 0 ) {
                debugState.frames.push(prevFrame);
            }
            prevFrame = prevFrame.callingFrame;
        }

        if (debugState.suspendReason == "debugger") {
            sendOnDebuggerMessage();
        } else if (debugState.suspendReason == "firstline") {
            sendOnFirstLineMessage();
        } else if (debugState.suspendReason == "exception") {
            sendOnExceptionMessage();
        } else if (debugState.suspendReason == "step_into"  ||
            debugState.suspendReason == "step_over" ||
            debugState.suspendReason == "step_out") {
            sendOnStepMessage();
        } else if (debugState.suspendReason == "breakpoint") {
            sendOnBreakpointMessage(0, frame.script.fileName, frame.line);
        } else {
            sendOnStepMessage();
        }

        delete debugState.suspendReason;
    }

    this.onResume = function()
    {
        if ( debugState.currentException && (hookReturn == RETURN_CONTINUE_THROW) ) {
            if ( debugState.hookReturnValue.value != debugState.currentException ) {
                debugState.hookReturnValue.value = debugState.currentException;
                if ( debugState.currentException.getWrappedValue() == null ) {
                    hookReturn = RETURN_RET_WITH_VAL;
                    debugState.hookReturnValue.value = null;
                } else {
                    hookReturn = RETURN_THROW_WITH_VAL;
                }
            }
        }

        delete debugState.hookReturnValue;
        delete debugState.evalResults;
        delete debugState.frames;
        delete debugState.currentException;

        sendOnResumeMessage();
        delete debugState.suspendReason;

        debugging = false;

        if ( debugState.shutdownHook ) {
            debugState.shutdownHook();
            delete debugState.shutdownHook;
        }
        return hookReturn;
    }

    // Exceptions
    this.onError = function(frame, error)
    {
        var logType = "out";
        var message = error.message;
        if ( error.flags & REPORT_ERROR ) {
            logType = "err";
        } else if ( error.flags & REPORT_EXCEPTION ) {
            logType = "exception";
        } else if ( error.flags & REPORT_WARNING ) {
            logType = "warn";
        }
        if ( error.exc ) {
            message = getExceptionTypeName(error.exc)+": "+message;
        }
        if ( frame ) {
            var frames = [];
            for (; frame; frame = frame.callingFrame) {
                var fileName = frame.script.fileName;
                if ( fileName.indexOf("chrome:") != 0 ) {
                    frames.push({
                        functionName: getFunctionName(frame.script),
                        fileName: fileName,
                        lineNumber: frame.line
                    });
                }
            }
            NetBeans.Logger.log(message + " Frames: " + frames, logType);
        } else {
            NetBeans.Logger.log(message + " fileName: " + error.fileName + " lineNumber: + " + error.line, logType);
        }
        if ( features.suspendOnErrors )
            return -1;
        return RETURN_CONTINUE;
    }

    this.onThrow = function(frame, rv)
    {
        var needSuspend = features.suspendOnExceptions;
        return needSuspend;
    }

    function getExceptionTypeName(exc)
    {
        if ( exc.jsType == TYPE_STRING ) {
            return "String";
        }else if ( exc.jsType == TYPE_OBJECT ) {
            var type = exc.jsClassName;
            if ( exc.isNative && type == "Error" )
                type = exc.jsConstructor.jsFunctionName;
            return type;
        }
        NetBeans.Logger.log("Exception type=" + exc.jsType + " className=" + exc.jsClassName);
        return "";
    }

    this.onTopLevel = function() {}

    this.onInit = function(debuggr)
    {
        if ( this == debuggr ) {
    // TODO socket.send(...);
    }
    }

    this.onLoaded = function(url)
    {
        if (!enabled) {
            sendOnloadMessage(encodeData(url));
            enable();
        }
    }

    var attachedWindows = [];

    this.attachToWindow = function(win)
    {
        if ( NetBeans.Utils.findInArray(attachedWindows,win) != null ) {
            return;
        }
        attachedWindows.push(win);

        var self = this;
        var onUnload = function(event) {
            self.detachFromWindow(win);
        }
        win.addEventListener("unload", onUnload, true);
        sendWindowsMessage(attachedWindows);
    }

    this.detachFromWindow = function(win)
    {
        if ( !NetBeans.Utils.removeFromArray(attachedWindows,win) )
            return;

        sendWindowsMessage(attachedWindows);
    }

    this.onDestroy = function(debuggr)
    {
        if ( !debuggr.shutdownInProgress ) {
            if (debugging) {
                abortFirebugDebugger();
            }
            return;
        }

        const delayShutdownIfDebugging = function() {
            try {
                disable();
            } catch(e) {
                NetBeans.Logger.log("NetbeansDebugger: onDestroy - disable() throws error: " + e,"err");
            }
            debuggr.shutdown();
        };

        if (debugging) {
            debugState.shutdownHook = delayShutdownIfDebugging;
            abortFirebugDebugger();
        } else {
            delayShutdownIfDebugging();
        }
    }

    // Variables
    function getLocalVariables(frameIndex, variableFullName, propertyGetResponse)
    {
        var frame = debugState.frames[frameIndex];
        if ( frame ) {
            switch(variableFullName)
            {
                case ".":
                    var rval = frame.scope;
                    if (rval) {
                        var val = getPropertyValue(rval);
                        var scopeFullName = "."
                        propertyGetResponse.property =
                        <property
                        name="scope"
                        fullname={
                        scopeFullName
                        }
                        type={
                        val.type
                        }
                        classname={
                        val.displayType
                        }
                        numchildren="0"
                        encoding="none">scope</property>;
                        // Add exception
                        if (frameIndex == 0 && debugState.currentException) {
                            var exceptionVal = getPropertyValue(debugState.currentException);
                            propertyGetResponse.property.property =
                            <property
                            name="[exception]"
                            fullname="[exception]"
                            type={
                            exceptionVal.type
                            }
                            classname={
                            exceptionVal.displayType
                            }
                            numchildren="-1"
                            encoding="none">{
                            exceptionVal.displayValue
                            }</property>;
                        }
                        propertyGetResponse.property.property += buildPropertiesList(".", rval);
                        if (!frame.isNative) {
                            // Add arguments properties
                            var argumentsVariable = resolveVariable(rval, "arguments");
                            if (argumentsVariable) {
                                var argumentsVal = getPropertyValue(argumentsVariable);
                                propertyGetResponse.property.property +=
                                <property
                                name="arguments"
                                fullname="arguments"
                                type={
                                argumentsVal.type
                                }
                                classname={
                                argumentsVal.displayType
                                }
                                numchildren="-1"
                                encoding="none">{
                                argumentsVal.displayValue
                                }</property>;
                            }
                            var argumentsLengthVariable = resolveVariable(rval, "arguments.length");
                            if (argumentsLengthVariable) {
                                var argumentsLengthVal = getPropertyValue(argumentsLengthVariable);
                                propertyGetResponse.property.property +=
                                <property
                                name="arguments.length"
                                fullname="arguments.length"
                                type={
                                argumentsLengthVal.type
                                }
                                classname={
                                argumentsLengthVal.displayType
                                }
                                numchildren="-1"
                                encoding="none">{
                                argumentsLengthVal.displayValue
                                }</property>;
                            }
                            var functionLengthVariable = resolveVariable(rval, "arguments.callee.length");
                            if (functionLengthVariable) {
                                var functionLengthVal = getPropertyValue(functionLengthVariable);
                                propertyGetResponse.property.property +=
                                <property
                                name="arguments.callee.length"
                                fullname="arguments.callee.length"
                                type={
                                functionLengthVal.type
                                }
                                classname={
                                functionLengthVal.displayType
                                }
                                numchildren="-1"
                                encoding="none">{
                                functionLengthVal.displayValue
                                }</property>;
                            }

                            var parentScope = propertyGetResponse.property;
                            rval = rval.jsParent;
                            while (rval) {
                                scopeFullName += ".";
                                var name = "parent scope";
                                val = getPropertyValue(rval);
                                var parentScopeProperty =
                                <property
                                name={
                                name
                                }
                                fullname={
                                scopeFullName
                                }
                                type={
                                val.type
                                }
                                classname={
                                val.displayType
                                }
                                numchildren="0"
                                encoding="none">{
                                name
                                }</property>;
                                parentScopeProperty.property = buildPropertiesList(scopeFullName, rval );
                                rval = rval.jsParent;
                                parentScopeProperty.@numchildren =
                                parentScopeProperty.property.length() + (rval ? 1 : 0);
                                parentScope.property += parentScopeProperty;
                                parentScope = parentScope.property[parentScope.property.length() - 1];
                            }
                        }
                        propertyGetResponse.property.@numchildren = propertyGetResponse.property.property.length();
                    }
                    break;
                case "this":
                    var rval = frame.thisValue;
                    var val = getPropertyValue(rval);
                    propertyGetResponse.property =
                    <property
                    name="this"
                    fullname="this"
                    type={
                    val.type
                    }
                    classname ={
                    val.displayType
                    }
                    numchildren="0"
                    encoding="none">{
                    val.displayValue
                    }</property>;
                    if( rval ) {
                        propertyGetResponse.property.property = buildPropertiesList("this", rval);
                        propertyGetResponse.property.@numchildren = propertyGetResponse.property.property.length();
                    }
                    break;
                case "[exception]":
                    if (frameIndex == 0 && debugState.currentException) {
                        var rval = debugState.currentException;
                        var val = getPropertyValue(rval);
                        propertyGetResponse.property =
                        <property
                        name="[exception]"
                        fullname="[exception]"
                        type={
                        val.type
                        }
                        classname ={
                        val.displayType
                        }
                        numchildren="0"
                        encoding="none">{
                        val.displayValue
                        }</property>;
                        if( rval ) {
                            propertyGetResponse.property.property = buildPropertiesList("[exception]", rval);
                            propertyGetResponse.property.@numchildren = propertyGetResponse.property.property.length();
                        }
                    }
                    break;

                default:
                    var value = null;
                    var processedVariableFullName = variableFullName;
                    if (variableFullName.indexOf("this.") == 0) {
                        if ( !frame.thisValue ) {
                            break;
                        }
                        processedVariableFullName = variableFullName.substring(5);
                        value = resolveVariable(frame.thisValue, processedVariableFullName);
                    } else if (variableFullName.indexOf(".") == 0) {
                        var scope = frame.scope;
                        processedVariableFullName = processedVariableFullName.substring(1);
                        while (processedVariableFullName.indexOf(".") == 0) {
                            scope = scope.jsParent;
                            processedVariableFullName = processedVariableFullName.substring(1);
                        }
                        value = resolveVariable(scope, processedVariableFullName);
                    } if (variableFullName.indexOf("[exception].") == 0) {
                        if (frameIndex == 0 && debugState.currentException) {
                            if ( !frame.thisValue ) {
                                break;
                            }
                            processedVariableFullName = variableFullName.substring(12);
                            value = resolveVariable(debugState.currentException, processedVariableFullName);
                        }
                    } else {
                        // first try parameters and local variables case
                        value = resolveVariable(frame.scope, processedVariableFullName);
                        if (value == null) {
                            var parentScope = frame.scope.jsParent;
                            while (parentScope != null) {
                                value = resolveVariable(parentScope, processedVariableFullName);
                                if (value != null) {
                                    break;
                                }
                                parentScope = parentScope.jsParent;
                            }
                        }
                    }
                    if(value != null) {
                        var name = processedVariableFullName;
                        var lastDot = processedVariableFullName.lastIndexOf(".");
                        if (lastDot != -1) {
                            name = processedVariableFullName.substring(lastDot+1);
                        }
                        var val = getPropertyValue(value);
                        propertyGetResponse.property =
                        <property
                        name={
                        name
                        }
                        fullname={
                        variableFullName
                        }
                        type={
                        val.type
                        }
                        numchildren="0"
                        encoding="none">{
                        val.displayValue
                        }</property>;
                        if (val.type == "object" || val.type == "function" || val.type == "array") {
                            propertyGetResponse.property.@classname = val.displayType;
                            propertyGetResponse.property.property = buildPropertiesList(variableFullName, value);
                            propertyGetResponse.property.@numchildren = propertyGetResponse.property.property.length();
                        }
                    }
                    break;
            }
        }
    }

    function buildPropertiesList(prefix, value)
    {
        var props = getPropertiesArray(value);
        var names = new XMLList();

        for (var i = 0; i < props.length; ++i)
        {
            var name = props[i].name;
            try {
                var val = getPropertyValue(props[i].value);
                var property =
                <property
                name={
                name
                }
                fullname={
                (prefix.match(/^\.+$/) ? prefix : prefix + ".") + name
                }
                type={
                val.type
                }
                numchildren="0"
                encoding="none">{
                val.displayValue
                }</property>;
                if (val.type == "object" || val.type == "function" || val.type == "array") {
                    property.@classname = val.displayType;
                    property.@numchildren = "-1";
                }
                names += property;
            } catch (e) {
                NetBeans.Logger.logException(e);
            }
        }
        return names;
    }

    // resolve a "."-separated variable name path in the parent.
    function resolveVariable(parent, variableName)
    {
        var nameParts = variableName.split(".");
        var obj = parent;
        for(var i = 0; obj != null && i < nameParts.length; ++i)
        {
            var part = nameParts[i];
            var prop = obj.getProperty(part);
            if ( prop != null ) {
                obj = prop.value;
            } else {
                var jsobj = obj.getWrappedValue();
                obj = null;
                if ( part in jsobj ) {
                    obj = jsDebuggerService.wrapValue(jsobj[part]);
                }
            }
        }
        return obj;
    }

    /**
     * @param  jsdIValue variable
     **/
    function getPropertiesArray(variable)
    {
        var props = {};
        var value;

        // get the enumerable properties
        var jsvalue = variable.getWrappedValue();
        try {
            for( var name in jsvalue )
            {
                if ( CONSTANTS_FILTER.test(name) ) {
                    if ( features.showConstants )
                        continue;
                }
                try {
                    try {
                        value = jsvalue[name];
                    } catch( e ) {
                        value = null;
                    }
                    value = jsDebuggerService.wrapValue(value);
                    if ( (value.jsType == TYPE_FUNCTION) && !features.showFunctions )
                        continue;
                    if ( (value.jsType == TYPE_OBJECT) && filterObjectValue(value) )
                        continue;

                    props[':'+name] = {
                        name: name,
                        value: value
                    };
                } catch( e ) {
                    NetBeans.Logger.logException(e);
                }
            }
        } catch( e ) {
            NetBeans.Logger.logException(e);
        }

        // get the local properties, may or may not be enumerable
        var propertiesArrayHolder = {
            value: null
        }, numPropertiesHolder = {
            value: 0
        };
        variable.getProperties(propertiesArrayHolder, numPropertiesHolder);
        for (var i = 0; i < numPropertiesHolder.value; ++i)
        {
            var prop = propertiesArrayHolder.value[i];
            var name = prop.name.stringValue;
            if ( (prop.value.jsType == TYPE_FUNCTION) && !features.showFunctions )
                continue;
            if ( (prop.value.jsType == TYPE_OBJECT) && filterObjectValue(prop.value) )
                continue;
            if ( CONSTANTS_FILTER.test(name) )
            {
                if ( !features.showConstants )
                {
                    if ( props[':'+name] )
                        delete props[':'+name];
                    continue;
                }
            }

            props[':'+name] = {
                name: name,
                value: prop.value
            };
        }

        // sort the property list
        var nameList = NetBeans.Utils.keys(props);
        nameList.sort();
        var propList = [];
        for (i = 0; i < nameList.length; ++i)
        {
            var name = nameList[i];
            propList.push(props[name]);
        }
        return propList;
    }

    // Filtering objects of special class which cause dereference problems.
    function filterObjectValue(value)
    {
        var className = value.jsClassName;
        return className == 'Constructor' || className == 'nsXPCComponents'
        || className == 'XULControllers' || className.substr(0,9) == 'chrome://';
    }

    function getPropertyValue(value)
    {
        var val = new Object();
        var strval;
        var jsType = value.jsType;
        switch (jsType)
        {
            case TYPE_VOID:
                val.type = "void";
                val.displayType  = CONST_TYPE_VOID;
                val.displayValue = CONST_TYPE_VOID
                break;
            case TYPE_NULL:
                val.type = "null";
                val.displayType  = CONST_TYPE_NULL;
                val.displayValue = CONST_TYPE_NULL;
                break;
            case TYPE_BOOLEAN:
                val.type = "boolean";
                val.displayType  = CONST_TYPE_BOOLEAN;
                val.displayValue = value.stringValue;
                break;
            case TYPE_INT:
                val.type = "int";
                val.displayType  = CONST_TYPE_INT;
                val.displayValue = value.intValue;
                break;
            case TYPE_DOUBLE:
                val.type = "double";
                val.displayType  = CONST_TYPE_DOUBLE;
                val.displayValue = value.doubleValue;
                break;
            case TYPE_STRING:
                val.type = "string";
                val.displayType  = CONST_TYPE_STRING;
                strval = value.stringValue;
                val.displayValue = strval.quote();
                break;
            case TYPE_FUNCTION:
            case TYPE_OBJECT:
                val.type = "object";
                if (jsType == TYPE_FUNCTION) {
                    val.type = "function";
                }
                val.displayType = value.jsClassName;

                var ctor = value.jsClassName;
                switch (ctor)
                {
                    case "Function":
                        val.displayType  = (value.isNative ? CONST_NATIVE_FUNCTION : CONST_SCRIPT_FUNCTION );
                        val.displayValue = value.getWrappedValue().toString();
                        break;

                    case "Object":
                        val.displayType  = CONST_TYPE_OBJECT;
                        if (value.jsConstructor) {
                            val.displayType = value.jsConstructor.jsFunctionName;
                        }
                        val.displayValue = value.stringValue;
                        if ( val.displayValue == 'null' ) {
                            NetBeans.Logger.log('!null value was found','err');
                        }
                        break;

                    case "String":
                        strval = value.stringValue;
                        val.displayValue = strval.quote();
                        break;

                    case "Call":
                        val.displayValue = "["+val.displayType+"]";
                        break;

                    case "Date":
                        break;

                    case "Number":
                    case "Boolean":
                        val.displayValue = value.stringValue;
                        break;

                    default:
                        val.displayValue = value.stringValue;
                        if ( val.displayValue == 'null' ) {
                            NetBeans.Logger.log('!null value was found','err');
                        }
                        break;
                }
                break;
        }

        return val;
    }
    
     function encodeData(data)
    {
        if( typeof data != "string")
            data = ""+data;
        return data.replace(/#/g, "#0").replace(/\|/g, "#1").replace(/\*/g, "#2");
    }

    function decodeData(data)
    {
        return data.replace(/#2/g, "*").replace(/#1/g, "|").replace(/#0/g, "#");
    }
    
    function fbsRegisterDebugger(debuggr) {
        firebugDebuggerService.registerDebugger(debuggr);
    }
    
    function fbsUnregisterDebugger(debuggr) {
        firebugDebuggerService.unregisterDebugger(debuggr);
    }
    
    function fbsEnableBreakpoint(href, line) {
        line = parseInt(line);
        firebugDebuggerService.enableBreakpoint(href, line);
    }
    
    function fbsDisableBreakpoint(href, line) {
        line = parseInt(line);
        firebugDebuggerService.disableBreakpoint(href, line);
    }
    
    function fbsClearBreakpoint(href, line) {
        line = parseInt(line);
        firebugDebuggerService.clearBreakpoint(href, line);
    }
    
    function fbsClearAllBreakpoints(hrefs) {
        if (NetBeans.Utils.isFF2()) {
            firebugDebuggerService.clearAllBreakpoints(hrefs.length, hrefs);
            return;
        }
        
        var sourceFiles = [];
        var sourceFile;
        
        for (var i = 0; i < hrefs.length; i++) {
            sourceFile = currentFirebugContext.sourceFileMap[hrefs[i]];
            if (sourceFile) {
                sourceFiles.push(sourceFile);
            } else {
                NetBeans.Logger.logMessage("clearAllBreakpoints() - Could not find source: " + hrefs[i]);
                sourceFile = new FBL.NoScriptSourceFile(currentFirebugContext, hrefs[i]);
                sourceFiles.push(sourceFile);
            }
        }
        
        firebugDebuggerService.clearAllBreakpoints(sourceFiles);
    }
    
    function fbsSetBreakpointCondition(href, line, condition) {
        line = parseInt(line);
        
        if (NetBeans.Utils.isFF2()) {
            firebugDebuggerService.setBreakpointCondition(href, line, condition);
            return;
        }
        
        var sourceFile;
        if (currentFirebugContext) {
            sourceFile = currentFirebugContext.sourceFileMap[href];
        }
        if (!sourceFile) {
            sourceFile = new FBL.NoScriptSourceFile(currentFirebugContext, href);
        }
        
        if (sourceFile) {
            firebugDebuggerService.setBreakpointCondition(sourceFile, line, condition, Firebug.Debugger);
        } else {
            NetBeans.Logger.logMessage("setBreakpointCondition() - No source file found for: " + href);
        }
    }
    
    function fbsSetBreakpoint(href, line, props) {
        line = parseInt(line);
        if (NetBeans.Utils.isFF2()) {
            return firebugDebuggerService.setBreakpoint(href, line, props);
        }
        
        var sourceFile;
        if (currentFirebugContext) {
            sourceFile = currentFirebugContext.sourceFileMap[href];
        }
        if (!sourceFile) {
            sourceFile = new FBL.NoScriptSourceFile(currentFirebugContext, href);
        }
        
        if (sourceFile) {
            return firebugDebuggerService.setBreakpoint(sourceFile, line, props, Firebug.Debugger);
        } else {
            NetBeans.Logger.logMessage("setBreakpoint() - No source file found for: " + href);
            return false;
        }
    }

}).apply(NetBeans.Debugger);
