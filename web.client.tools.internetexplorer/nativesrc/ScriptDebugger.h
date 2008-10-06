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
 *      jdeva <deva@neteans.org>
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
#pragma once
#include "stdafx.h"
#include "resource.h"       // main symbols
#include "NetBeansExtension.h"
#include "Breakpoint.h"
#include "DbgpConnection.h"
#include "DebugDocument.h"
#include <map>

using namespace std;

class DbgpConnection;
class BreakpointManager;
class Breakpoint;
class DebugDocument;

struct StackFrame {
    tstring fileName;
    tstring location;
    ULONG line;
    ULONG col;
    tstring type;
    tstring code;
};

struct Property {
    tstring name;
    tstring fullName;
    tstring type;
    int childrenCount;
    tstring encoding;
    tstring classname;
    tstring value;
    list<Property *> children;
};

enum State {
    STATE_STARTING, 
    STATE_STOPPING,    
    STATE_STOPPED,    // Stopped or disconnected
    STATE_RUNNING,    // Running
    STATE_FIRST_LINE, // firts line of javascript executed
    STATE_BREAKPOINT, // Breakpoint
    STATE_STEP,       // Stopped due to Step 
    STATE_DEBUGGER,   // debugger keyword encountered
    STATE_ERROR,
    STATE_EXCEPTION
};

enum Scope {
    SCOPE_LOCAL,
    SCOPE_NONE
};

static const tstring STATE_STARTING_STR =       _T("starting");
static const tstring STATE_STOPPING_STR =       _T("stopping");
static const tstring STATE_STOPPED_STR =        _T("stopped");
static const tstring STATE_RUNNING_STR =        _T("running");
static const tstring STATE_FIRST_LINE_STR =     _T("first_line");
static const tstring STATE_BREAKPOINT_STR =     _T("breakpoint");
static const tstring STATE_STEP_STR =           _T("step");
static const tstring STATE_DEBUGGER_STR =       _T("debugger");
static const tstring STATE_ERROR_STR =          _T("error");
static const tstring STATE_EXCEPTION_STR =      _T("exception");

static const tstring OK =                       _T("ok");

static const tstring TYPE_FUNCTION =            _T("Function");
static const tstring TYPE_ARRAY =               _T("Array");
static const tstring TYPE_OBJECT =              _T("Object");
static const tstring TYPE_VOID =                _T("Void");
static const tstring TYPE_USER_DEFINED =        _T("User-defined Type");
static const tstring TYPE_VARIANT =             _T("Variant");
static const tstring TYPE_SINGLE =              _T("Single");
static const tstring TYPE_INTEGER =             _T("Integer");
static const tstring TYPE_INT =                 _T("int");
static const tstring TYPE_LONG =                _T("Long");
static const tstring TYPE_ERROR =               _T("Error");
static const tstring TYPE_NUMBER =              _T("Number");
static const tstring TYPE_STRING =              _T("String");

static const tstring FUNCTION =                 _T("function");
static const tstring ARGUMENTS =                _T("arguments");
static const tstring ARGUMENTS_LENGTH =         _T("arguments.length");
static const tstring ARGUMENTS_CALLEE_LENGTH =  _T("arguments.callee.length");
static const tstring DOT =                      _T(".");
static const tstring LOCAL_SCOPE =              _T("scope");
static const tstring EMPTY =                    _T("");

static const tstring UNDEFINED =                _T("Undefined");
static const tstring SCRIPT_FUNCTION =          _T("Script Function");
static const tstring NATIVE_FUNCTION =          _T("Native Function");
static const tstring NATIVE_CODE =              _T("native code");
static const tstring TO_STRING =                _T(".toString()");
static const tstring CTOR_TO_STRING =           _T(".constructor.toString()");


static const unsigned int SHOW_FUNCTIONS = 0x1;
static const unsigned int SHOW_CONSTANTS = SHOW_FUNCTIONS << 1;
static const unsigned int BY_PASS_CONSTRUCTORS = SHOW_CONSTANTS << 1;
static const unsigned int STEP_FILTERS_ENABLED = BY_PASS_CONSTRUCTORS << 1;
static const unsigned int SUSPEND_ON_FIRSTLINE = STEP_FILTERS_ENABLED << 1;
static const unsigned int SUSPEND_ON_EXCEPTIONS = SUSPEND_ON_FIRSTLINE << 1;
static const unsigned int SUSPEND_ON_ERRORS = SUSPEND_ON_EXCEPTIONS << 1;
static const unsigned int SUSPEND_ON_DEBUGGER_KEYWORD = SUSPEND_ON_ERRORS << 1;
static const unsigned int IGNORE_QUERY_STRINGS = SUSPEND_ON_DEBUGGER_KEYWORD << 1;

// ScriptDebugger
class ATL_NO_VTABLE ScriptDebugger :
	public CComObjectRootEx<CComMultiThreadModel>,
	public IApplicationDebugger,
    public IApplicationDebuggerUI,
    public IDebugApplicationNodeEvents,
    public IDebugExpressionCallBack {
public:
	ScriptDebugger() {
	}

BEGIN_COM_MAP(ScriptDebugger)
	COM_INTERFACE_ENTRY(IApplicationDebugger)
    COM_INTERFACE_ENTRY(IDebugApplicationNodeEvents)
//  COM_INTERFACE_ENTRY(IApplicationDebuggerUI)
    COM_INTERFACE_ENTRY(IDebugExpressionCallBack)
    COM_INTERFACE_ENTRY2(IUnknown, IApplicationDebugger)
END_COM_MAP()

	DECLARE_PROTECT_FINAL_CONSTRUCT()

    HRESULT FinalConstruct();
    void FinalRelease();

public:
    // IApplicationDebugger
    STDMETHOD(QueryAlive)(void);
    STDMETHOD(CreateInstanceAtDebugger)( 
        /* [in] */ REFCLSID rclsid,
        /* [in] */ IUnknown __RPC_FAR *pUnkOuter,
        /* [in] */ DWORD dwClsContext,
        /* [in] */ REFIID riid,
        /* [iid_is][out] */ IUnknown __RPC_FAR *__RPC_FAR *ppvObject);
    STDMETHOD(onDebugOutput)( 
        /* [in] */ LPCOLESTR pstr);
    STDMETHOD(onHandleBreakPoint)( 
        /* [in] */ IRemoteDebugApplicationThread __RPC_FAR *prpt,
        /* [in] */ BREAKREASON br,
        /* [in] */ IActiveScriptErrorDebug __RPC_FAR *pError);
    STDMETHOD(onClose)(void);
    STDMETHOD(onDebuggerEvent)( 
        /* [in] */ REFIID riid,
        /* [in] */ IUnknown __RPC_FAR *punk);

    // IApplicationDebuggerUI
    STDMETHOD(BringDocumentToTop)( 
        /* [in] */ IDebugDocumentText __RPC_FAR *pddt);
    STDMETHOD(BringDocumentContextToTop)( 
        /* [in] */ IDebugDocumentContext __RPC_FAR *pddc);

    // IDebugApplicationNodeEvents
    STDMETHOD(onAddChild)( 
        /* [in] */ IDebugApplicationNode __RPC_FAR *prddpChild);
    STDMETHOD(onRemoveChild)( 
        /* [in] */ IDebugApplicationNode __RPC_FAR *prddpChild);
    STDMETHOD(onDetach)(void);
    STDMETHOD(onAttach)( 
        /* [in] */ IDebugApplicationNode __RPC_FAR *prddpParent);

    // IDebugExpressionCallBack
    STDMETHOD(onComplete)(void);

    static ScriptDebugger *createScriptDebugger();
    void setDebugApplication(IRemoteDebugApplication *pRemoteDebugApplication);
    HRESULT startSession();
    HRESULT endSession();
    BreakpointManager *getBreakpointManager() {
        return m_pBreakpointMgr;
    }
    BOOL setBreakpoint(Breakpoint *pBreakpoint) {
        return setBreakpoint(pBreakpoint, false);
    }
   BOOL setBreakpoint(Breakpoint *pBreakpoint, tstring fileURI) {
        return setBreakpoint(pBreakpoint, fileURI, false);
    }
    BOOL removeBreakpoint(Breakpoint *pBreakpoint) {
        return setBreakpoint(pBreakpoint, true);
    }
    void SetDbgpConnection(DbgpConnection *pDbgpConnection) {
        m_pDbgpConnection = pDbgpConnection;
    }
    DbgpConnection *getDbgpConnection() {
        return m_pDbgpConnection;
    }
    list<StackFrame *> getStackFrames();
    void run() {
        resume(BREAKRESUMEACTION_CONTINUE);
    }
    void stop() {
        resume(BREAKRESUMEACTION_ABORT);
    }
    void stepInto() {
        resume(BREAKRESUMEACTION_STEP_INTO);
    }
    void stepOver() {
        resume(BREAKRESUMEACTION_STEP_OVER);
    }
    void stepOut() {
        resume(BREAKRESUMEACTION_STEP_OUT);
    }
    void pause() {
        breakRequested = TRUE;
        pauseImpl();
    }
    
    Breakpoint *getCurrentBreakpoint() {
        return m_pCurrentBreakpoint;
    }

    Property *getProperty(tstring name, int stackDepth);
    BOOL setProperty(tstring name, int stackDepth, tstring value);
    Property *eval(tstring expression, int stackDepth);
    TCHAR *getSourceText(tstring fileName,int  beginLine, int endLine);
    void changeState(State state, tstring reason);
    tstring evalToString(tstring expression, int stackDepth);
    void setFeature(const unsigned int feature) {
        featureSet |= feature;
    }
    BOOL isFeatureSet(const unsigned int feature) {
        return featureSet & feature;
    }
    tstring getStatusString() {
        return statesMap.find(state)->second;
    }
    State getStatus() {
        return state;
    }
private:
    DWORD m_dwRemoteDebugAppCookie, m_dwRemoteDebugAppThreadCookie, m_dwDebugAppCookie;
    DWORD m_dwThreadID;
    static BOOL isCurrentprocessThread(DWORD threadId);
    HRESULT getRemoteDebugApplication(IRemoteDebugApplication **ppRemoteDebugApp);
    void registerForDebugAppNodeEvents();
    void unregisterForDebugAppNodeEvents();
    void cleanup();
    BOOL setBreakpoint(IDebugDocument *pDebugDocument, Breakpoint *pBreakpoint, BREAKPOINT_STATE state);
    //void setAllBreakpoints(BREAKPOINT_STATE state);
    //void setBreakpointsForDocument(IDebugDocument *pDebugDocument, BREAKPOINT_STATE state);
    //CComPtr<IConnectionPoint> m_spDebugAppNodeEventsConnectionPoint;
    DWORD m_dwDebugAppNodeEventsCookie;
    BreakpointManager *m_pBreakpointMgr;
    map<tstring, DebugDocument *> debugDocumentsMap;
    BOOL isDocumentReady(IDebugDocument *pDebugDocument);
    DbgpConnection *m_pDbgpConnection;
    void getTopStackFrame(IRemoteDebugApplicationThread *pDebugAppThread, StackFrame *pStackFrame);
    void getTopStackFrame(StackFrame *pStackFrame);
    BOOL getStackFrame(DebugStackFrameDescriptor *pFrameDescriptor, StackFrame *pStackFrame);
    void resume(BREAKRESUMEACTION resumeAction);
    State state;
    map<State, tstring> statesMap;
    IDebugProperty *resolveProperty(IDebugProperty *pDebugProperty, tstring relativeName);
    Property *getProperty(IDebugProperty *pDebugProperty, tstring name, int stackDepth, BOOL recurse=FALSE);
    IDebugProperty *getChildDebugProperty(IDebugProperty *pDebugProperty, tstring name);
    BOOL getStackFrameDescriptor(int stackDepth, DebugStackFrameDescriptor *pDescriptor);
    HANDLE m_hDebugExprCallBackEvent;
    IDebugProperty *evalToDebugProperty(tstring expression, int stackDepth);
    IDebugExpression *getDebugExpression(tstring expression, int stackDepth);
    tstring getObjectType(tstring fullName, int stackDepth);
    BOOL setBreakpoint(Breakpoint *pBreakpoint, BOOL remove);
    BOOL setBreakpoint(Breakpoint *pBreakpoint, tstring fileURI, BOOL remove);
    BOOL handleBreakpoint(StackFrame frame);
    BOOL breakRequested;
    BOOL documentLoaded;
    void pauseImpl();
    Breakpoint *m_pCurrentBreakpoint;
    unsigned int featureSet;
    static BOOL alreadyStoppedOnFirstLine;
    DWORD registerForDebugDocTextEvents(IDebugDocumentText *pDebugDocText, CComObject<DebugDocument> *pDebugDoc);
    void unregisterForDebugDocTextEvents(IDebugDocumentText *pDebugDocText, DWORD cookie);
};

