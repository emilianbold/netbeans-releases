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
#include "stdafx.h"
#include "ScriptDebugger.h"
#include "DebugDocument.h"
#include "Utils.h"
#include <tlhelp32.h>

BOOL ScriptDebugger::alreadyStoppedOnFirstLine = FALSE;

HRESULT ScriptDebugger::FinalConstruct() {
    m_pBreakpointMgr = new BreakpointManager(this);
    m_dwThreadID = GetCurrentThreadId();
    m_dwRemoteDebugAppCookie = 0;
    m_dwRemoteDebugAppThreadCookie = 0;
    state = STATE_STOPPED;
    statesMap.insert(pair<State, tstring>(STATE_STARTING, STATE_STARTING_STR));
    statesMap.insert(pair<State, tstring>(STATE_STOPPING, STATE_STOPPING_STR));
    statesMap.insert(pair<State, tstring>(STATE_STOPPED, STATE_STOPPED_STR));
    statesMap.insert(pair<State, tstring>(STATE_RUNNING, STATE_RUNNING_STR));
    statesMap.insert(pair<State, tstring>(STATE_FIRST_LINE, STATE_FIRST_LINE_STR));
    statesMap.insert(pair<State, tstring>(STATE_BREAKPOINT, STATE_BREAKPOINT_STR));
    statesMap.insert(pair<State, tstring>(STATE_STEP, STATE_STEP_STR));
    statesMap.insert(pair<State, tstring>(STATE_DEBUGGER, STATE_DEBUGGER_STR));
    statesMap.insert(pair<State, tstring>(STATE_ERROR, STATE_ERROR_STR));
    statesMap.insert(pair<State, tstring>(STATE_EXCEPTION, STATE_EXCEPTION_STR));
    m_hDebugExprCallBackEvent = CreateEvent(NULL, false, false, NULL);
    breakRequested = FALSE;
    documentLoaded = FALSE;
    featureSet = 0;
	return S_OK;
}

void ScriptDebugger::FinalRelease() {
    if(m_pBreakpointMgr != NULL) {
        delete m_pBreakpointMgr;
    }
    CloseHandle(m_hDebugExprCallBackEvent);
    if(m_dwRemoteDebugAppCookie) {
        Utils::revokeInterfaceFromGlobal(m_dwRemoteDebugAppCookie);
    }
    if(m_dwRemoteDebugAppThreadCookie) {
        Utils::revokeInterfaceFromGlobal(m_dwRemoteDebugAppThreadCookie);
    }
}

void ScriptDebugger::cleanup() {
    //if(m_spDebugAppNodeEventsConnectionPoint != NULL) {
    //    m_spDebugAppNodeEventsConnectionPoint->Unadvise(m_dwDebugAppCookie);
    //}
    unregisterForDebugAppNodeEvents();
    m_pBreakpointMgr->removeAllBreakpoints();
    map<tstring, DebugDocument *>::iterator iter = debugDocumentsMap.begin();
    while(iter != debugDocumentsMap.end()) {
        DebugDocument *pDebugDoc = iter->second;
        DWORD cookie = pDebugDoc->getCookie();
        CComPtr<IDebugDocument> spDebugDocument;
        Utils::getInterfaceFromGlobal(cookie, IID_IDebugDocument, (void **)&spDebugDocument);
        if(spDebugDocument != NULL) {
            CComQIPtr<IDebugDocumentText> spDebugDocText = spDebugDocument;
            unregisterForDebugDocTextEvents(spDebugDocText, pDebugDoc->getEventCookie());
        }
        Utils::revokeInterfaceFromGlobal(cookie);
        pDebugDoc->Release();
        ++iter;
    }
    debugDocumentsMap.clear();
}

// ScriptDebugger
STDMETHODIMP ScriptDebugger::QueryAlive(void) {
    return S_OK;
}

void ScriptDebugger::changeState(State state, tstring reason) {
    if(this->state != state) {
        if(this->state == STATE_BREAKPOINT) {
            m_pCurrentBreakpoint = NULL;
        }
        this->state = state;
        Utils::log(4, _T("Debugger state - %s, reason - %s\n"), statesMap.find(state)->second.c_str(), reason.c_str());
        //send message to IDE to indicate the state
        if(state == STATE_BREAKPOINT) {
            StackFrame stackFrame;
            getTopStackFrame(&stackFrame);
            m_pDbgpConnection->sendBreakpointMessage(&stackFrame, m_pCurrentBreakpoint->getID(), reason);
        }else {
            m_pDbgpConnection->sendStatusMessage(statesMap.find(state)->second, reason);
        }
    }
}

STDMETHODIMP ScriptDebugger::CreateInstanceAtDebugger(REFCLSID rclsid, 
    IUnknown __RPC_FAR *pUnkOuter, DWORD dwClsContext, REFIID riid, 
    IUnknown __RPC_FAR *__RPC_FAR *ppvObject) {
    return E_NOTIMPL;
}

STDMETHODIMP ScriptDebugger::onDebugOutput(LPCOLESTR pstr) {
    return E_NOTIMPL;
}

STDMETHODIMP ScriptDebugger::onHandleBreakPoint(IRemoteDebugApplicationThread __RPC_FAR *pDebugAppThread, 
    BREAKREASON br, IActiveScriptErrorDebug __RPC_FAR *pScriptErrorDebug) {
    CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    HRESULT hr = pDebugAppThread->GetApplication(&spRemoteDebugApp);
    //if(m_dwRemoteDebugAppCookie == 0 && hr == S_OK) {
    //    Utils::registerInterfaceInGlobal(spRemoteDebugApp, IID_IRemoteDebugApplication, 
    //                                        &m_dwRemoteDebugAppCookie);
    //}
    if(m_dwRemoteDebugAppThreadCookie) {
        Utils::revokeInterfaceFromGlobal(m_dwRemoteDebugAppThreadCookie);
    }
    Utils::registerInterfaceInGlobal(pDebugAppThread, IID_IRemoteDebugApplicationThread, 
                                        &m_dwRemoteDebugAppThreadCookie);
    StackFrame frame;
    getTopStackFrame(pDebugAppThread, &frame);
    if(br == BREAKREASON_DEBUGGER_HALT) {
        if(documentLoaded) {
            m_pBreakpointMgr->processBreakpoints(frame.fileName);
            documentLoaded = FALSE;
        }
        
        //Check for first line stop request or first line breakpoint or pause request
        if((featureSet & SUSPEND_ON_FIRSTLINE) && !alreadyStoppedOnFirstLine) {
            changeState(STATE_FIRST_LINE, OK);
            alreadyStoppedOnFirstLine = TRUE;
            return S_OK;
        }else if(handleBreakpoint(frame)) {
            return S_OK;
        }else if(breakRequested) {
            changeState(STATE_DEBUGGER, OK);
            breakRequested = false;
            return S_OK;
        }
    }else if(br == BREAKREASON_BREAKPOINT) {
        if(handleBreakpoint(frame)) {
            return S_OK;
        }
    }else if(br == BREAKREASON_STEP) {
        changeState(STATE_STEP, OK);
        return S_OK;
    }else if(br == BREAKREASON_ERROR && (featureSet & SUSPEND_ON_EXCEPTIONS)) {
        EXCEPINFO excepInfo;
        pScriptErrorDebug->GetExceptionInfo(&excepInfo);
        tstring errorMsg;
        if(excepInfo.bstrSource != NULL) {
            errorMsg = (TCHAR *)excepInfo.bstrSource;
        }
        if(excepInfo.bstrDescription != NULL) {
            if(excepInfo.bstrSource != NULL) {
                errorMsg.append(_T(": "));
            }
            errorMsg.append((TCHAR *)excepInfo.bstrDescription);
        }
        m_pDbgpConnection->sendErrorMessage(errorMsg);
        changeState(STATE_EXCEPTION, OK);
        return S_OK;
    }
    spRemoteDebugApp->ResumeFromBreakPoint(pDebugAppThread, BREAKRESUMEACTION_CONTINUE, 
                                           ERRORRESUMEACTION_AbortCallAndReturnErrorToCaller);
    return S_OK;
}


BOOL ScriptDebugger::handleBreakpoint(StackFrame frame) {
    Breakpoint *pBreakpoint = m_pBreakpointMgr->findMatchingBreakpoint(frame.fileName, frame.line);
    if(pBreakpoint != NULL) {
        m_pCurrentBreakpoint = pBreakpoint;
        //We cannot evaluate the conditional expression before we return from
        //this method. Therefore creating a thread which evaluates the expression
        //and communicates back to IDE appropriately
        DWORD threadID;
        CreateThread(NULL, 0, Breakpoint::handle, this, 0, &threadID);
        return true;
    }else {
        if(frame.code == STATE_DEBUGGER_STR && (featureSet & SUSPEND_ON_DEBUGGER_KEYWORD)) {
            changeState(STATE_DEBUGGER, OK);
            return true;
        }
    }
    return false;
}

STDMETHODIMP ScriptDebugger::onComplete(void) {
    SetEvent(m_hDebugExprCallBackEvent);
    return S_OK;
}

void ScriptDebugger::getTopStackFrame(IRemoteDebugApplicationThread *pDebugAppThread, 
                                          StackFrame *pStackFrame) {
    CComPtr<IEnumDebugStackFrames> spDebugStackFrames;    
    DebugStackFrameDescriptor frameDescriptor;
    ULONG frameCount = 1;
    HRESULT hr = pDebugAppThread->EnumStackFrames(&spDebugStackFrames);
    if(hr == S_OK) {
        hr = spDebugStackFrames->Next(1, &frameDescriptor, &frameCount);
        if(frameCount > 0) {
            getStackFrame(&frameDescriptor, pStackFrame);
        }
    }
}

void ScriptDebugger::getTopStackFrame(StackFrame *pStackFrame) {
    CComPtr<IRemoteDebugApplicationThread> spRemoteDebugAppThread;
    HRESULT hr = Utils::getInterfaceFromGlobal(m_dwRemoteDebugAppThreadCookie, IID_IRemoteDebugApplicationThread, 
                                                (void **)&spRemoteDebugAppThread);
    CComPtr<IEnumDebugStackFrames> spDebugStackFrames;    
    DebugStackFrameDescriptor frameDescriptor;
    ULONG frameCount = 1;
    hr = spRemoteDebugAppThread->EnumStackFrames(&spDebugStackFrames);
    if(hr == S_OK) {
        hr = spDebugStackFrames->Next(1, &frameDescriptor, &frameCount);
        if(frameCount > 0) {
            getStackFrame(&frameDescriptor, pStackFrame);
        }
    }
}

BOOL ScriptDebugger::getStackFrame(DebugStackFrameDescriptor *pFrameDescriptor, 
                                           StackFrame *pStackFrame) {
    CComPtr<IDebugCodeContext> spDebugCodeCtxt; 
    HRESULT hr = pFrameDescriptor->pdsf->GetCodeContext(&spDebugCodeCtxt);
    CComBSTR description;
    pFrameDescriptor->pdsf->GetDescriptionString(TRUE, &description);
    CComPtr<IDebugDocumentContext> spDebugDocCtxt;
    spDebugCodeCtxt->GetDocumentContext(&spDebugDocCtxt);
    if(spDebugDocCtxt != NULL) {
        CComPtr<IDebugDocument> spDebugDoc;
        hr = spDebugDocCtxt->GetDocument(&spDebugDoc);
        CComBSTR docName;
        hr = spDebugDoc->GetName(DOCUMENTNAMETYPE_URL, &docName);
        if(docName != NULL) {
            CComQIPtr<IDebugDocumentText> spDebugDocText = spDebugDoc;
            ULONG position, numChars, col, line;
            hr = spDebugDocText->GetPositionOfContext(spDebugDocCtxt, &position, &numChars);
            hr = spDebugDocText->GetLineOfPosition(position, &line, &col);
            SOURCE_TEXT_ATTR *attrs = new SOURCE_TEXT_ATTR[numChars+1];
            TCHAR *buffer = new TCHAR[numChars+1];
            ULONG actualSize = 0;
            hr = spDebugDocText->GetText(position, buffer, attrs, &actualSize, numChars);
            if(hr == S_OK && actualSize > 0) {
                buffer[actualSize] = 0;
                pStackFrame->code = buffer;
                delete[] buffer;
                delete[] attrs;
            }
            pStackFrame->fileName = (TCHAR *)(docName);
            pStackFrame->line = line+1;
            pStackFrame->col = col;
            if(description != NULL) {
                pStackFrame->location = (TCHAR *)(description);
            }
            return TRUE;
        }
    }
    return FALSE;
}

list<StackFrame *> ScriptDebugger::getStackFrames() {
    CComPtr<IRemoteDebugApplicationThread> spRemoteDebugAppThread;
    HRESULT hr = Utils::getInterfaceFromGlobal(m_dwRemoteDebugAppThreadCookie, IID_IRemoteDebugApplicationThread, 
                                                (void **)&spRemoteDebugAppThread);
    list<StackFrame *> frames;
    CComPtr<IEnumDebugStackFrames> spDebugStackFrames;    
    DebugStackFrameDescriptor frameDescriptor;
    ULONG frameCount = 1;
    hr = spRemoteDebugAppThread->EnumStackFrames(&spDebugStackFrames);
    if(hr == S_OK) {
        while(frameCount > 0) {
            hr = spDebugStackFrames->Next(1, &frameDescriptor, &frameCount);
            if(hr == S_OK) {
                StackFrame *pInfo = new StackFrame();
                if(getStackFrame(&frameDescriptor, pInfo)) {
                    frames.push_back(pInfo);
                }
            }
        }
    }
    return frames;
}

BOOL ScriptDebugger::getStackFrameDescriptor(int stackDepth, DebugStackFrameDescriptor *pDescriptor) {
    CComPtr<IRemoteDebugApplicationThread> spRemoteDebugAppThread;
    HRESULT hr = Utils::getInterfaceFromGlobal(m_dwRemoteDebugAppThreadCookie, IID_IRemoteDebugApplicationThread, 
                                                (void **)&spRemoteDebugAppThread);
    CComPtr<IEnumDebugStackFrames> spDebugStackFrames;    
    ULONG frameCount = 1;
    int depth = 0;
    hr = spRemoteDebugAppThread->EnumStackFrames(&spDebugStackFrames);
    while(hr == S_OK && frameCount > 0) {
        hr = spDebugStackFrames->Next(1, pDescriptor, &frameCount);
        if(depth == stackDepth){
            return TRUE;
        }
        depth++;
    }
    return FALSE;
}

TCHAR *ScriptDebugger::getSourceText(tstring fileURI, int beginLine, int endLine) {
    TCHAR *buffer = NULL;
    map<tstring, DebugDocument *>::iterator iter = debugDocumentsMap.find(fileURI);
    if(iter != debugDocumentsMap.end()) {
        DebugDocument *pDebugDoc = iter->second;
        DWORD cookie = pDebugDoc->getCookie();
        CComPtr<IDebugDocument> spDebugDocument;
        Utils::getInterfaceFromGlobal(cookie, IID_IDebugDocument, (void **)&spDebugDocument);
        if(spDebugDocument != NULL) {
            CComQIPtr<IDebugDocumentText> spDebugDocText = spDebugDocument;
            ULONG lines, numChars;
            spDebugDocText->GetSize(&lines, &numChars);
            SOURCE_TEXT_ATTR *attrs = new SOURCE_TEXT_ATTR[numChars+1];
            buffer = new TCHAR[numChars+1];
            ULONG actualSize = 0;
            HRESULT hr = spDebugDocText->GetText(0, buffer, attrs, &actualSize, numChars);
            if(hr == S_OK && actualSize >= 0) {
                buffer[actualSize] = 0;
                delete[] attrs;
            }
        }
    }
    return buffer;
}

Property *ScriptDebugger::eval(tstring expression, int stackDepth) {
    Property *pArgsProp = NULL;
    CComPtr<IDebugProperty> spDebugProperty = evalToDebugProperty(expression, stackDepth);
    if(spDebugProperty != NULL) {
        pArgsProp = getProperty(spDebugProperty, EMPTY, stackDepth);
        //Special handling for function arguments
        if(pArgsProp != NULL && expression == ARGUMENTS) {
            pArgsProp->type = TYPE_OBJECT;
            tstring lengthString = evalToString(ARGUMENTS_LENGTH, stackDepth);
            int length = _ttoi(lengthString.c_str());
            TCHAR buffer[64];
            for(int i=0;i<length;i++) {
                _stprintf_s(buffer, 64, _T("%s[%d]"), ARGUMENTS.c_str(), i);
                CComPtr<IDebugProperty> spElementsDebugProp = evalToDebugProperty(buffer, stackDepth);
                Property *pElemProp = getProperty(spElementsDebugProp, EMPTY, stackDepth);
                if(pElemProp != NULL) {
                    _stprintf_s(buffer, 64, _T("[%d]"), i);
                    pElemProp->name = buffer;
                    pArgsProp->children.push_back(pElemProp);
                }
            }
        }
    }
    return pArgsProp;
}

IDebugProperty *ScriptDebugger::evalToDebugProperty(tstring expression, int stackDepth) {
    CComPtr<IDebugExpression> spDebugExpr = getDebugExpression(expression, stackDepth);
    if(spDebugExpr != NULL) {
        HRESULT evalRetValue;
        CComPtr<IDebugProperty> spDebugProperty;
        HRESULT hr = spDebugExpr->GetResultAsDebugProperty(&evalRetValue, &spDebugProperty);
        if(hr == S_OK) {
            return spDebugProperty.Detach();
        }
    }
    return NULL;
}

IDebugExpression *ScriptDebugger::getDebugExpression(tstring expression, int stackDepth) {
    DebugStackFrameDescriptor frameDescriptor;
    if(getStackFrameDescriptor(stackDepth, &frameDescriptor)) {
        CComQIPtr<IDebugExpressionContext> spDebugExprCtxt = frameDescriptor.pdsf;
        CComPtr<IDebugExpression> spDebugExpr;
        LPCOLESTR pOleStr = T2BSTR(expression.c_str());
        HRESULT hr = spDebugExprCtxt->ParseLanguageText(pOleStr, 10, L"", DEBUG_TEXT_RETURNVALUE, &spDebugExpr);
        if(hr == S_OK) {
            CComPtr<IDebugExpressionCallBack> spDebugExprCallBack;
            this->QueryInterface(IID_IDebugExpressionCallBack, (void **)&spDebugExprCallBack);
            hr = spDebugExpr->Start(spDebugExprCallBack);

            //Wait for the event which is signalled when the evaluation is complete
            AtlWaitWithMessageLoop(m_hDebugExprCallBackEvent);
            return spDebugExpr.Detach();
        }
    }
    return NULL;
}

tstring ScriptDebugger::evalToString(tstring expression, int stackDepth) {
    //DebugBreak();
    CComPtr<IDebugExpression> spDebugExpr = getDebugExpression(expression, stackDepth);
    if(spDebugExpr != NULL) {
        HRESULT evalRetValue;
        CComPtr<IDebugProperty> spDebugProperty;
        CComBSTR result;
        HRESULT hr = spDebugExpr->GetResultAsString(&evalRetValue, &result);
        if(hr == S_OK) {
            return (TCHAR *)(result);
        }
    }
    return NULL;
}

Property *ScriptDebugger::getProperty(tstring name, int stackDepth){
    CComPtr<IDebugProperty> spDebugProperty;
    if(name.substr(0, 1) == DOT) {
		DebugStackFrameDescriptor frameDescriptor;
		if(getStackFrameDescriptor(stackDepth, &frameDescriptor)) {
			CComPtr<IDebugStackFrame> spDebugStackFrame = frameDescriptor.pdsf;
			CComPtr<IDebugProperty> spLocalDebugProperty;
			spDebugStackFrame->GetDebugProperty(&spLocalDebugProperty);
			spDebugProperty = resolveProperty(spLocalDebugProperty, name.substr(1));
		}
	}else {
		if(name == ARGUMENTS) {
			return eval(name, stackDepth);
		}
		spDebugProperty = evalToDebugProperty(name, stackDepth);
	}
    Property *pProp = getProperty(spDebugProperty, name, stackDepth, TRUE);
    if(name == DOT && pProp != NULL) {
        tstring childProps[] = {ARGUMENTS, ARGUMENTS_LENGTH, ARGUMENTS_CALLEE_LENGTH};
        for(int i=0; i<3; i++) {
            Property *pChildProp = eval(childProps[i], stackDepth);
            if(pChildProp != NULL) {
                pProp->children.push_back(pChildProp);
            }
        }
    }
    return pProp;
}

IDebugProperty *ScriptDebugger::resolveProperty(IDebugProperty *pDebugProperty, tstring relativeName) {
    //fullName will be either of the form .name1.name2.name3 or .name1.name2
    size_t pos = relativeName.find(DOT);
    if (pos == tstring::npos && relativeName.length() == 0) {
        return pDebugProperty;
    }
    tstring firstPart = relativeName;
    tstring remainingPart;
    if(pos != tstring::npos) {
        firstPart = relativeName.substr(0, pos);
        remainingPart = relativeName.substr(pos+1, relativeName.length());
    }
    return resolveProperty(getChildDebugProperty(pDebugProperty, firstPart), remainingPart); 
}

IDebugProperty *ScriptDebugger::getChildDebugProperty(IDebugProperty *pDebugProperty, tstring name) {
    if(pDebugProperty == NULL) {
        return NULL;
    }
    DebugPropertyInfo propertyInfo;
    HRESULT hr = pDebugProperty->GetPropertyInfo(DBGPROP_INFO_ALL, 10, &propertyInfo);
    CComPtr<IEnumDebugPropertyInfo> spEnumDebugPropInfo;
    hr = pDebugProperty->EnumMembers(DBGPROP_INFO_ALL, 10, IID_IDebugPropertyEnumType_LocalsPlusArgs, 
                                                &spEnumDebugPropInfo);
    ULONG count = 1;
    DebugPropertyInfo childPropInfo;
    if(hr == S_OK && spEnumDebugPropInfo != NULL) {
        while(count > 0) {
            hr = spEnumDebugPropInfo->Next(1, &childPropInfo, &count);
            if(hr == S_OK) {
                if((TCHAR *)(childPropInfo.m_bstrName) == name) {
                    return childPropInfo.m_pDebugProp;
                }
            }
        }
    }
}

Property *ScriptDebugger::getProperty(IDebugProperty *pDebugProperty, tstring name, int stackDepth, BOOL recurse) {
    if(pDebugProperty == NULL) {
        return NULL;
    }
    DebugPropertyInfo propertyInfo;
    HRESULT hr = pDebugProperty->GetPropertyInfo(DBGPROP_INFO_ALL, 10, &propertyInfo);

    //Special handling of arguments will be done for local scope
    if(name == ARGUMENTS && (TCHAR *)propertyInfo.m_bstrName == ARGUMENTS) {
        return NULL;
    }

    Property *pProp = new Property();
    pProp->name = (TCHAR *)(propertyInfo.m_bstrName);
    if(name == DOT) {
		pProp->name = LOCAL_SCOPE;
	}

	if(propertyInfo.m_bstrFullName != NULL) {
		pProp->fullName = (TCHAR *)(propertyInfo.m_bstrFullName);
	}else {
		pProp->fullName = (name != EMPTY) ? name : pProp->name;
	}
    pProp->value = (TCHAR *)(propertyInfo.m_bstrValue);
    pProp->childrenCount = 0;
    pProp->type = (TCHAR *)(propertyInfo.m_bstrType);
    if(pProp->type == TYPE_ERROR) {
        return NULL;
    }else if(pProp->type == TYPE_OBJECT) {
        pProp->childrenCount = -1;
    }
    if(pProp->type == TYPE_USER_DEFINED) {
        pProp->type = TYPE_VOID;
    }else if(pProp->type == TYPE_SINGLE || pProp->type == TYPE_VARIANT) {
        pProp->type = TYPE_OBJECT;
    }else if(pProp->type == TYPE_LONG || pProp->type == TYPE_INTEGER) {
        pProp->type = TYPE_INT;
    }

	if(pProp->fullName.length() > 1) {
        if(pProp->type == TYPE_OBJECT) {
			tstring fullName = pProp->fullName;
			if(fullName.substr(0, 1) == DOT) {
				fullName = fullName.substr(1);
			}
            pProp->classname = getObjectType(fullName, stackDepth);
            tstring toString = fullName;
            if(pProp->classname == TYPE_FUNCTION) {
                if(!(featureSet & SHOW_FUNCTIONS)) {
                    return NULL;
                }
                toString.append(TO_STRING);
                tstring value = evalToString(toString, stackDepth);
                pProp->value = value.substr(1, value.length()-2);
                pProp->classname = value.find(NATIVE_CODE) != tstring::npos ? NATIVE_FUNCTION : SCRIPT_FUNCTION;
                pProp->type = TYPE_FUNCTION;
            }
        }
    }

    CComPtr<IEnumDebugPropertyInfo> spEnumDebugPropInfo;
    if( pDebugProperty != NULL && recurse) {
        HRESULT hr = pDebugProperty->EnumMembers(DBGPROP_INFO_ALL, 10, IID_IDebugPropertyEnumType_LocalsPlusArgs, 
                                                    &spEnumDebugPropInfo);
        if(spEnumDebugPropInfo != NULL) {
            ULONG count = 1;
            spEnumDebugPropInfo->GetCount(&count);
            pProp->childrenCount = count;
            while(count > 0) {
                DebugPropertyInfo childPropInfo;
                hr = spEnumDebugPropInfo->Next(1, &childPropInfo, &count);
                if(hr == S_OK) {
					tstring childFullName;
					if(pProp->name != LOCAL_SCOPE) {
						childFullName.append(pProp->fullName);
						childFullName.append(DOT);
					}
					childFullName.append((TCHAR *)childPropInfo.m_bstrName);
					Property *pChildProp = getProperty(childPropInfo.m_pDebugProp, childFullName, stackDepth);
                    if(pChildProp != NULL) {
                        pProp->children.push_back(pChildProp);
                    }
                }
            }
        }
    }
    return pProp;
}

tstring ScriptDebugger::getObjectType(tstring fullName, int stackDepth) {
    fullName.append(CTOR_TO_STRING);
    tstring value = evalToString(fullName, stackDepth).substr(1);
    size_t pos = value.find(FUNCTION);
    if(pos != tstring::npos && value.length() > TYPE_FUNCTION.length()+1) {
        value = value.erase(0, value.find_first_not_of (' '));
        tstring ctor = value.substr(TYPE_FUNCTION.length()+1);
        ctor = ctor.erase(0, ctor.find_first_not_of(' '));
        pos = ctor.find(_T("("));
        if(pos != tstring::npos) {
            return ctor.substr(0, pos);
        }
    }
    return TYPE_OBJECT;
}

BOOL ScriptDebugger::setProperty(tstring name, int stackDepth, tstring value) {
    USES_CONVERSION;
    CComPtr<IDebugProperty> spDebugProperty;
    spDebugProperty = evalToDebugProperty(name, stackDepth);
    tstring evalValue = evalToString(value, stackDepth);
    HRESULT hr = E_FAIL;
    if(evalValue.c_str() != NULL) {
        hr = spDebugProperty->SetValueAsString(T2COLE(evalValue.c_str()), 10);
    }
    return hr == S_OK ? TRUE : FALSE;
}


STDMETHODIMP ScriptDebugger::onClose(void) {
    cleanup();
    return S_OK;
}

STDMETHODIMP ScriptDebugger::onDebuggerEvent(REFIID riid, IUnknown __RPC_FAR *punk) {
    return E_NOTIMPL;
}

STDMETHODIMP ScriptDebugger::BringDocumentToTop(IDebugDocumentText __RPC_FAR *pddt) {
    HRESULT hr = S_OK;
    return hr;
}
        
STDMETHODIMP ScriptDebugger::BringDocumentContextToTop(IDebugDocumentContext __RPC_FAR *pddc) {
    HRESULT hr = S_OK;
    return hr;
}

//IDebugApplicationNodeEvents implementation
STDMETHODIMP ScriptDebugger::onAddChild(IDebugApplicationNode __RPC_FAR *prddpChild) {
    CComPtr<IDebugDocument> spDebugDocument;
    HRESULT hr = prddpChild->GetDocument(&spDebugDocument);
    if(hr == S_OK) {
        CComBSTR name;
        hr = spDebugDocument->GetName(DOCUMENTNAMETYPE_URL, &name);
        if(name != NULL) {
            TCHAR *docName = (TCHAR *)name;
            Utils::log(4, _T("Loaded document: %s\n"), docName);
            DWORD cookie;
            Utils::registerInterfaceInGlobal(spDebugDocument, IID_IDebugDocument, &cookie);
            CComObject<DebugDocument> *pComDebugDoc;
            CComObject<DebugDocument>::CreateInstance(&pComDebugDoc);
            DebugDocument *pDebugDoc = (DebugDocument *)pComDebugDoc;
            pDebugDoc->setDocumentName(docName);
            pDebugDoc->setScriptDebugger(this);
            pDebugDoc->setCookie(cookie);
            CComQIPtr<IDebugDocumentText> spDebugDocText = spDebugDocument;
            cookie = registerForDebugDocTextEvents(spDebugDocText, pComDebugDoc);
            pDebugDoc->setEventCookie(cookie);
            debugDocumentsMap.insert(pair<tstring, DebugDocument *>(docName, pDebugDoc));
            documentLoaded = TRUE;
            pauseImpl();
        }
    }
    return S_OK;
}
    
STDMETHODIMP ScriptDebugger::onRemoveChild(IDebugApplicationNode __RPC_FAR *prddpChild) {
    CComPtr<IDebugDocument> spDebugDocument;
    HRESULT hr = prddpChild->GetDocument(&spDebugDocument);
    if(hr == S_OK) {
        CComBSTR name;
        hr = spDebugDocument->GetName(DOCUMENTNAMETYPE_URL, &name);
        if(name != NULL) {
            TCHAR *docName = (TCHAR *)name;
            Utils::log(4, _T("Removing document: %s\n"), docName);
            map<tstring, DebugDocument *>::iterator iter = debugDocumentsMap.find(docName);
            if(iter != debugDocumentsMap.end()) {
                DebugDocument *pDebugDoc = iter->second;
                Utils::revokeInterfaceFromGlobal(pDebugDoc->getCookie());
                CComQIPtr<IDebugDocumentText> spDebugDocText = spDebugDocument;
                unregisterForDebugDocTextEvents(spDebugDocText, pDebugDoc->getEventCookie());
                debugDocumentsMap.erase(iter);
            }
        }
    }
    return S_OK;
}
    
STDMETHODIMP ScriptDebugger::onDetach(void) {
    return S_OK;
}
    
STDMETHODIMP ScriptDebugger::onAttach(IDebugApplicationNode __RPC_FAR *prddpParent) {
    return S_OK;
}

/*
void ScriptDebugger::setAllBreakpoints(BREAKPOINT_STATE state) {
    map<tstring, DWORD>::iterator iter = debugDocumentsMap.begin();
    while(iter != debugDocumentsMap.end()) {
        CComPtr<IDebugDocument> spDebugDocument;
        Utils::getInterfaceFromGlobal(iter->second, IID_IDebugDocument, (void **)&spDebugDocument);
        setBreakpointsForDocument(spDebugDocument, state);
        ++iter;
    }
}

void ScriptDebugger::setBreakpointsForDocument(IDebugDocument *pDebugDocument, BREAKPOINT_STATE state) {
    CComBSTR name;
    pDebugDocument->GetName(DOCUMENTNAMETYPE_URL, &name);
    list<Breakpoint *> *pBreakpoints = getBreakpointManager()->getBreakpoints((TCHAR *)(name));
    if(pBreakpoints != NULL)
    list<Breakpoint *>::iterator bpIter = pBreakpoints->begin();
    while(bpIter != pBreakpoints->end()) {
        setBreakpoint(pDebugDocument, *bpIter, state);
        ++bpIter;
    }
}
*/

BOOL ScriptDebugger::setBreakpoint(IDebugDocument *pDebugDocument, Breakpoint *pBreakpoint, 
                                    BREAKPOINT_STATE state) {
    CComQIPtr<IDebugDocumentText> spDebugDocumentText = pDebugDocument;
    int line = pBreakpoint->getLineNumber();
    ULONG position;
    HRESULT hr = spDebugDocumentText->GetPositionOfLine(line-1, &position);
    CComPtr<IDebugDocumentContext> spDebugDocumentContext;
    hr = spDebugDocumentText->GetContextOfPosition(position, 0, &spDebugDocumentContext);
    CComPtr<IEnumDebugCodeContexts> spEnumDebugCtxts; 
    hr = spDebugDocumentContext->EnumCodeContexts(&spEnumDebugCtxts);
    ULONG count = 1;
    if(spEnumDebugCtxts != NULL) {
        do {
            CComPtr<IDebugCodeContext> spDebugCodeCtxt;
            hr = spEnumDebugCtxts->Next(1, &spDebugCodeCtxt, &count);
            if(SUCCEEDED(hr) && count > 0) {
                hr = spDebugCodeCtxt->SetBreakPoint(state);
            }
        }while(count > 0);
    }
    return SUCCEEDED(hr) ? TRUE : FALSE;
}

BOOL ScriptDebugger::setBreakpoint(Breakpoint *pBreakpoint, BOOL remove) {
    BOOL result = false;
    tstring fileURI = pBreakpoint->getFileURI();
    map<tstring, DebugDocument *>::iterator iter = debugDocumentsMap.find(fileURI);
    if (iter == debugDocumentsMap.end()) {
        if(isFeatureSet(IGNORE_QUERY_STRINGS)) {
            for (iter = debugDocumentsMap.begin(); iter != debugDocumentsMap.end();++iter) {
                size_t pos = iter->first.find(fileURI);
                if (pos != std::string::npos) {
                    result = setBreakpoint(pBreakpoint, iter->first, remove);
                }
            }
        }
        return result;
    }else {
        return setBreakpoint(pBreakpoint, fileURI, remove);
    }
}

BOOL ScriptDebugger::setBreakpoint(Breakpoint *pBreakpoint, tstring fileURI, BOOL remove) {
    map<tstring, DebugDocument *>::iterator iter = debugDocumentsMap.find(fileURI);
    if(iter != debugDocumentsMap.end()) {
        DebugDocument *pDebugDoc = iter->second;
        DWORD cookie = pDebugDoc->getCookie();
        CComPtr<IDebugDocument> spDebugDocument;
        Utils::getInterfaceFromGlobal(cookie, IID_IDebugDocument, (void **)&spDebugDocument);
        if(spDebugDocument != NULL && isDocumentReady(spDebugDocument)) {
            BREAKPOINT_STATE state = pBreakpoint->getState() ? BREAKPOINT_ENABLED : BREAKPOINT_DISABLED;
            if(remove){
                state = BREAKPOINT_DELETED;
            }
            return setBreakpoint(spDebugDocument, pBreakpoint, state);
        }
    }
    return FALSE;
}

void ScriptDebugger::pauseImpl() {
    CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    getRemoteDebugApplication(&spRemoteDebugApp);
    if(spRemoteDebugApp != NULL) {
        spRemoteDebugApp->CauseBreak();
    }
}

void ScriptDebugger::resume(BREAKRESUMEACTION resumeAction) {
    CComPtr<IRemoteDebugApplicationThread> spRemoteDebugAppThread;
    HRESULT hr = Utils::getInterfaceFromGlobal(m_dwRemoteDebugAppThreadCookie, IID_IRemoteDebugApplicationThread, 
                                                (void **)&spRemoteDebugAppThread);
    if(hr == S_OK) {
        CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
        spRemoteDebugAppThread->GetApplication(&spRemoteDebugApp);
        spRemoteDebugApp->ResumeFromBreakPoint(spRemoteDebugAppThread, resumeAction, 
                                                ERRORRESUMEACTION_AbortCallAndReturnErrorToCaller);
        changeState(STATE_RUNNING, OK);
    }
}

BOOL ScriptDebugger::isDocumentReady(IDebugDocument *pDebugDocument) {
    CComQIPtr<IDebugDocumentText> spDebugDocumentText = pDebugDocument;
    ULONG numLines, numChars;
    HRESULT hr = spDebugDocumentText->GetSize(&numLines, &numChars);
    return hr == S_OK && numLines && numChars ? TRUE : FALSE;
}

BOOL ScriptDebugger::isCurrentprocessThread(DWORD dwThreadID) {
    DWORD dwProcessID = GetCurrentProcessId();
    HANDLE hThreadSnap = INVALID_HANDLE_VALUE; 
    THREADENTRY32 threadEntry;
    BOOL result = false;

    //Take a snapshot of all running threads  
    hThreadSnap = CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, 0); 
    if(hThreadSnap != INVALID_HANDLE_VALUE) {
        threadEntry.dwSize = sizeof(THREADENTRY32); 
        //Walk through threads and check if it belongs to the process
        if(Thread32First(hThreadSnap, &threadEntry)) {
            do { 
                if(threadEntry.th32ThreadID == dwThreadID && threadEntry.th32OwnerProcessID == dwProcessID) {
                    result = true;
                    break;
                }
            } while(Thread32Next(hThreadSnap, &threadEntry));
        }

        CloseHandle(hThreadSnap);
    }
    return result;
}

void ScriptDebugger::setDebugApplication(IRemoteDebugApplication *pRemoteDebugApplication) {
    //pRemoteDebugApplication->AddRef();
    if(m_dwRemoteDebugAppCookie == 0) {
        Utils::registerInterfaceInGlobal(pRemoteDebugApplication, IID_IRemoteDebugApplication, 
                                            &m_dwRemoteDebugAppCookie);
    }
}

ScriptDebugger *ScriptDebugger::createScriptDebugger() {
	HRESULT hr = E_FAIL;
	CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    CComPtr<IMachineDebugManager> spMachineDebugManager;
	hr = spMachineDebugManager.CoCreateInstance(CLSID_MachineDebugManager, NULL, CLSCTX_ALL); 
    CComPtr<IEnumRemoteDebugApplications> spEnumDebugApps;
    ULONG count = 0;
	hr = spMachineDebugManager->EnumApplications(&spEnumDebugApps);
    if(hr == S_OK) {
        do {
            CComPtr<IRemoteDebugApplication> spCurrentRemoteDebugApp;
            //Enumerate debuggable applications and select the one corresponding
            //to the current process
   	        hr = spEnumDebugApps->Next(1, &spCurrentRemoteDebugApp, &count);
	        if(hr == S_OK && count > 0) {
                CComBSTR name;
                spCurrentRemoteDebugApp->GetName(&name);
                Utils::log(1, _T("Debuggable application - %s\n"), (TCHAR *)name);
                if(name != NULL) {
                    spRemoteDebugApp = spCurrentRemoteDebugApp;
                    CComPtr<IEnumRemoteDebugApplicationThreads> spThreads;
                    hr = spRemoteDebugApp->EnumThreads(&spThreads);
				    if(spThreads != NULL) {
					    CComPtr<IRemoteDebugApplicationThread> spRemoteDebugAppThreads;
					    ULONG threadCount;
					    hr = spThreads->Next(1, &spRemoteDebugAppThreads, &threadCount);
					    if(hr == S_OK && threadCount > 0) {
                            DWORD dwThreadID;
						    hr = spRemoteDebugAppThreads->GetSystemThreadId(&dwThreadID);
                            Utils::log(1, _T("Debuggable application thread - %d\n"), dwThreadID);
						    if(hr == S_OK && isCurrentprocessThread(dwThreadID)) {
							    break;
						    }
					    }else if(hr == S_FALSE) {
 						    break;
					    }
                    }else {
                        Utils::log(1, _T("Threads enumeration completed, error code - %x\n"), hr);
                    }
                }
            }else {
                Utils::log(1, _T("Debuggable applications enumeration completed, error code - %x\n"), hr);
            }
        }while(count > 0);
    } else {
        Utils::log(1, _T("Did not find debuggable application, error code - %x\n"), hr);
    }

    if(spRemoteDebugApp != NULL) {
        //create our application debugger
        CComObject<ScriptDebugger> *pScriptDebugger;
        hr = CComObject<ScriptDebugger>::CreateInstance(&pScriptDebugger);
        if(SUCCEEDED(hr)){
            pScriptDebugger->setDebugApplication(spRemoteDebugApp);
            return pScriptDebugger;
        }
    }
    return NULL;
}

HRESULT ScriptDebugger::startSession() {
    CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    HRESULT hr = getRemoteDebugApplication(&spRemoteDebugApp);
    if(SUCCEEDED(hr)) {
        CComPtr<IApplicationDebugger> spAppDebugger;
        hr = spRemoteDebugApp->GetDebugger(&spAppDebugger);
        if(SUCCEEDED(hr)) {
		    spRemoteDebugApp->DisconnectDebugger();
	    }
    	//Register for IDebugApplicationNodeEvents
        registerForDebugAppNodeEvents();

        hr = spRemoteDebugApp->ConnectDebugger(this);
        Utils::log(1, _T("Attempting to connect to debugger, error code - %x\n"), hr);
    }
    return hr;
}

HRESULT ScriptDebugger::endSession() {
    cleanup();
    CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    HRESULT hr = getRemoteDebugApplication(&spRemoteDebugApp);
    if(SUCCEEDED(hr)) {
        CComPtr<IApplicationDebugger> spAppDebugger;
        hr = spRemoteDebugApp->GetDebugger(&spAppDebugger);
        if(SUCCEEDED(hr)) {
            if(state != STATE_RUNNING) {
                run();
            }
		    hr = spRemoteDebugApp->DisconnectDebugger();
	    }
    }
    Utils::log(4, _T("Disconnected from debugger, error code-%x\n"), hr);
    return hr;
}

void ScriptDebugger::registerForDebugAppNodeEvents() {
    CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    HRESULT hr = getRemoteDebugApplication(&spRemoteDebugApp);
    CComPtr<IDebugApplicationNode> spDebugAppNode;
    hr = spRemoteDebugApp->GetRootNode(&spDebugAppNode);
    if(SUCCEEDED(hr)) {
        /*
        CComPtr<IConnectionPointContainer> spConnectionPoint;
        hr = spDebugAppNode->QueryInterface(IID_IConnectionPointContainer,(void **)&spConnectionPoint);
        if(SUCCEEDED(hr)) {
            hr = spConnectionPoint->FindConnectionPoint(IID_IDebugApplicationNodeEvents, 
                                                        &m_spDebugAppNodeEventsConnectionPoint);
            if(SUCCEEDED(hr)) {
                CComPtr<IDebugApplicationNodeEvents> spDebugAppNodeEvents;
                this->QueryInterface(IID_IDebugApplicationNodeEvents, (void **)&spDebugAppNodeEvents);
                hr = m_spDebugAppNodeEventsConnectionPoint->Advise(spDebugAppNodeEvents, &m_dwDebugAppCookie);
            }
        }
        */
        CComPtr<IDebugApplicationNodeEvents> spDebugAppNodeEvents;
        this->QueryInterface(IID_IDebugApplicationNodeEvents, (void **)&spDebugAppNodeEvents);
        hr = AtlAdvise(spDebugAppNode, spDebugAppNodeEvents, IID_IDebugApplicationNodeEvents, 
                        &m_dwDebugAppNodeEventsCookie);
    }
}

DWORD ScriptDebugger::registerForDebugDocTextEvents(IDebugDocumentText *pDebugDocText, CComObject<DebugDocument> *pDebugDoc) {
    CComPtr<IDebugDocumentTextEvents> spDebugDocTextEvents;
    DWORD cookie;
    HRESULT hr = pDebugDoc->QueryInterface(IID_IDebugDocumentTextEvents, (void **)&spDebugDocTextEvents);
    AtlAdvise(pDebugDocText, spDebugDocTextEvents, IID_IDebugDocumentTextEvents, &cookie);
    return cookie;
}

void ScriptDebugger::unregisterForDebugAppNodeEvents() {
    CComPtr<IRemoteDebugApplication> spRemoteDebugApp;
    HRESULT hr = getRemoteDebugApplication(&spRemoteDebugApp);
    if(hr == S_OK){
        CComPtr<IDebugApplicationNode> spDebugAppNode;
        hr = spRemoteDebugApp->GetRootNode(&spDebugAppNode);
        if(SUCCEEDED(hr)) {
            hr = AtlUnadvise(spDebugAppNode, IID_IDebugApplicationNodeEvents, m_dwDebugAppNodeEventsCookie);
        }
    }
}

void ScriptDebugger::unregisterForDebugDocTextEvents(IDebugDocumentText *pDebugDocText, DWORD cookie) {
    AtlUnadvise(pDebugDocText, IID_IDebugDocumentTextEvents, cookie);
}

HRESULT ScriptDebugger::getRemoteDebugApplication(IRemoteDebugApplication **ppRemoteDebugApp) {
    HRESULT hr = E_FAIL;
    if(m_dwRemoteDebugAppCookie > 0){
        hr = Utils::getInterfaceFromGlobal(m_dwRemoteDebugAppCookie, IID_IRemoteDebugApplication, 
                                            (void **)ppRemoteDebugApp);
    }
    return hr;
}
