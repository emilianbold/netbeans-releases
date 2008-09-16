
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
#include "NetbeansBHO.h"
#include "ScriptDebugger.h"
#include "DbgpConnection.h"
#include "Utils.h"

// NetbeansBHO

HRESULT CNetBeansBHO::FinalConstruct() {
    m_dwThreadID = GetCurrentThreadId();
    setDebuggerStopped();
    return S_OK;
}

void CNetBeansBHO::setDebuggerStopped() {
    m_pDbgpConnection = NULL;
    debuggerStarted = FALSE;
}

STDMETHODIMP CNetBeansBHO::SetSite(IUnknown* pUnkSite) {
    HRESULT hr = E_FAIL;
    if (pUnkSite != NULL) {
        hr = pUnkSite->QueryInterface(IID_IWebBrowser2, (void**)&m_spWebBrowser);
        if (SUCCEEDED(hr)) {
            // Register DWebBrowserEvents2
            hr = DispEventAdvise(m_spWebBrowser);
            if (SUCCEEDED(hr)) {
                m_bAdvised = TRUE;
            }
        }
        Utils::registerInterfaceInGlobal(m_spWebBrowser, IID_IWebBrowser2, &m_dwWebBrowserCookie);
    } else {
        // Unregister DWebBrowserEvents2
        if (m_bAdvised) {
            DispEventUnadvise(m_spWebBrowser);
            m_bAdvised = FALSE;
        }
        Utils::revokeInterfaceFromGlobal(m_dwWebBrowserCookie);
        m_spWebBrowser.Release();
        if(m_pDbgpConnection != NULL) {
            m_pDbgpConnection->close();
        }
    }

    return IObjectWithSiteImpl<CNetBeansBHO>::SetSite(pUnkSite);
}

HRESULT CNetBeansBHO::getWebBrowser(IWebBrowser2 **ppWebBrowser) {
    HRESULT hr = S_OK;
    if(m_dwThreadID == GetCurrentThreadId()) {
        *ppWebBrowser = m_spWebBrowser;
    }else {
        hr = Utils::getInterfaceFromGlobal(m_dwWebBrowserCookie, IID_IWebBrowser2, 
                                            (void **)ppWebBrowser);
    }
    return hr;
}

void STDMETHODCALLTYPE CNetBeansBHO::OnNavigateComplete(IDispatch *pDisp, VARIANT *pvarURL) {
    if(!debuggerStarted) {
        checkAndInitNetbeansDebugging(pvarURL->bstrVal);
    }
}

void STDMETHODCALLTYPE CNetBeansBHO::OnDocumentComplete(IDispatch *pDisp, VARIANT *pvarURL) {
    if(debuggerStarted) {
        CComPtr<IDispatch> spDisp;
        m_spWebBrowser->get_Document(&spDisp);
        CComQIPtr<IHTMLDocument2> spHtmlDocument = spDisp;
        if(spHtmlDocument != NULL) {
            CComBSTR bstrState;
            spHtmlDocument->get_readyState(&bstrState);
            if(bstrState == "complete") {
                m_pDbgpConnection->handleDocumentComplete(spHtmlDocument);
            }
        }
    }else {
        if(m_pDbgpConnection != NULL) {
            debuggerStarted = TRUE;
        }
    }
}

void CNetBeansBHO::checkAndInitNetbeansDebugging(BSTR bstrURL) {
    tstring str = _bstr_t(bstrURL);
    tstring portArgName(L"--netbeans-debugger-port=");
    tstring sessionArgName(L"--netbeans-debugger-session-id=");

    size_t portArgPos = str.find(portArgName);
    if (portArgPos == string::npos) {
        return;
    }
    size_t sessionIdArgPos = str.find(sessionArgName, portArgPos);
    if (sessionIdArgPos == std::string::npos) {
        return;
    }
    size_t argsEndPos = str.find(L".html", sessionIdArgPos);
    if (argsEndPos == string::npos) {
        return;
    }

    size_t pos = portArgPos+portArgName.length();
    tstring port = str.substr(pos, sessionIdArgPos-pos);
    pos = sessionIdArgPos+sessionArgName.length();
    tstring sessionId = str.substr(pos, argsEndPos-pos);
    initializeNetbeansDebugging(port, sessionId);
}

void CNetBeansBHO::initializeNetbeansDebugging(tstring port, tstring sessionId) {
    DWORD threadID;
    m_pDbgpConnection = new DbgpConnection(port, sessionId, m_dwWebBrowserCookie);
    //DebugBreak();
    if(m_pDbgpConnection->connectToIDE()) {
        //Create thread for debugger
        CreateThread(NULL, 0, CNetBeansBHO::DebuggerProc, this, 0, &threadID);
    }else {
        Utils::log(1, _T("Unable to connect back to Netbeans IDE\n"));
    }
}

DWORD WINAPI CNetBeansBHO::DebuggerProc(LPVOID param) {
    ::CoInitializeEx(NULL, COINIT_MULTITHREADED);
    CNetBeansBHO *pNetbeansBHO = (CNetBeansBHO*)param;
    pNetbeansBHO->AddRef();
    ScriptDebugger *pScriptDebugger = ScriptDebugger::createScriptDebugger();
    DbgpConnection *pDbgpConnection = pNetbeansBHO->m_pDbgpConnection;
    if(pDbgpConnection != NULL) {
        pDbgpConnection->setScriptDebugger(pScriptDebugger);
        if(pScriptDebugger != NULL) {
            pScriptDebugger->SetDbgpConnection(pDbgpConnection);
            pScriptDebugger->startSession();
        }
        DWORD threadID;
        //Thread for DBGP command and responses
        HANDLE hThread = CreateThread(NULL, 0, DbgpConnection::commandHandler, 
                            pNetbeansBHO->m_pDbgpConnection, 0, &threadID);
        AtlWaitWithMessageLoop(hThread);

        //cleanup
        if(pScriptDebugger != NULL) {
            pScriptDebugger->endSession();
        }
        delete pDbgpConnection;
        pNetbeansBHO->setDebuggerStopped();
    }
    pNetbeansBHO->Release();
    ::CoUninitialize();
    return 0;
}