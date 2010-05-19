
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
#include <atlstr.h>
#include "ProtocolCF.h"
#include "HttpMonitoringApp.h"
#include <AtlBase.h>
#include <AtlConv.h> 
#include "HttpMonitoringApp.h"
#include "base64.h"

#include "wininet.h"
// NetbeansBHO

extern char* gPostText;

typedef PassthroughAPP::CMetaFactory<PassthroughAPP::CComClassFactoryProtocol,
	CHttpMonitoringApp> MetaFactory;


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
        // initialize HTTP Request Monitoring
        CComPtr<IInternetSession> spSession;
        CoInternetGetSession(0, &spSession, 0);

        // HTTP Support (for HTTP Client Monitor)
        // Uncomment the following 4 lines to enable experimental
        // HTTP Client Monitor support for IE.
//        MetaFactory::CreateInstance(CLSID_HttpProtocol, &m_spCFHTTP);
//        spSession->RegisterNameSpace(m_spCFHTTP, CLSID_NULL, L"http", 0, 0, 0);
        // HTTPS Support ---> (for HTTP Client Monitor)
//        MetaFactory::CreateInstance(CLSID_HttpSProtocol, &m_spCFHTTPS);
//        spSession->RegisterNameSpace(m_spCFHTTPS, CLSID_NULL, L"https", 0, 0, 0);
        // HTTPS Support <---
        // end initialize HTTP Request Monitoring

        // initialize HTTP debugging
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
        // Uninitialize HTTP Monitoring code
        CComPtr<IInternetSession> spSession;
        CoInternetGetSession(0, &spSession, 0);
        // HTTP Support
		// Uncomment the following 4 lines to enable experimental
		// HTTP Client Monitor support for IE.
//		spSession->UnregisterNameSpace(m_spCFHTTP, L"http");
//        m_spCFHTTP.Release();
        // HTTPS Support --->
//        spSession->UnregisterNameSpace(m_spCFHTTPS, L"https");
//        m_spCFHTTPS.Release();
        // HTTPS Support <---
        // End Uninitialize HTTP Monitoring code

        // Unitialize Javascript Debugging code
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
    //USES_CONVERSION;

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

			// begin changes - this section somewhat works, but is not 
			// received well by Netbeans, because we're sending the 
			// same http message split into two parts, so this (second)
			// part wipes out the first part that was already received.
			/*
			//Get the source using WinInet APIs
			USES_CONVERSION;
			HINTERNET hSession = InternetOpen(L"Source Reader", PRE_CONFIG_INTERNET_ACCESS, L"", 
												NULL, INTERNET_INVALID_PORT_NUMBER);
			if (hSession != NULL) {
				CString URL_string;
				if ((pvarURL != NULL) && (V_VT(pvarURL) == VT_BSTR)) {
					URL_string = V_BSTR(pvarURL);
				}

				HINTERNET hUrlFile = InternetOpenUrl(hSession, URL_string.GetString(), NULL, 0, 0, 0);
				DWORD bufSize;
				if (hUrlFile != NULL && InternetQueryDataAvailable(hUrlFile, &bufSize, 0, 0)) {
					char *pBytes = new char[bufSize+1];
					DWORD dwBytesRead = 0;
					BOOL read = InternetReadFile(hUrlFile, pBytes, bufSize, &dwBytesRead);
					if(read) {
						pBytes[dwBytesRead] = 0;

						HttpDbgpResponse msg;
						msg.addChildTagWithValue(_T("type"), _T("response"));
						msg.addChildTagWithValue(_T("url"), URL_string.GetString());
						msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());
						msg.addChildTagWithValue(_T("id"), 123);
						msg.addChildTagWithValue(_T("name"),URL_string.GetString());
						msg.addChildTagWithValue(_T("responseText"), encodeToBase64(A2CT(pBytes)));
						if (DbgpConnection::lastInstance != NULL) {
							ScriptDebugger* sdbg = DbgpConnection::lastInstance->getScriptDebugger();
							//if (sdbg!= NULL && sdbg->isHttpMonitorEnabled()) {
								DbgpConnection::lastInstance->sendResponse(msg.toString());
							//} 
						}
					    delete []pBytes;
						
					}
				}

            }
			*/
            // end new changes
        }
    }else {
        if(m_pDbgpConnection != NULL) {
            debuggerStarted = TRUE;
        }
    }
}

void STDMETHODCALLTYPE CNetBeansBHO::OnBeforeNavigate2(IDispatch *pDisp,
		VARIANT *pUrl, VARIANT *pFlags, VARIANT *pTargetFrameName,
		VARIANT *pPostData, VARIANT *pHeaders, VARIANT_BOOL *pCancel) 
{
    USES_CONVERSION; // to use ATL string conversion macros

	//char *post_data = NULL;
	int	post_data_size = 0;

	CString URL_string;
    if ((pUrl != NULL) && (V_VT(pUrl) == VT_BSTR)) {
        URL_string = V_BSTR(pUrl);
    }

	CString headersStr;
    if ((pHeaders != NULL) && (V_VT(pHeaders) == VT_BSTR)) {
        headersStr = V_BSTR(pHeaders);
    }

	if ((pPostData != NULL) && (V_VT(pPostData) == (VT_VARIANT | VT_BYREF))) {
        VARIANT *PostData_variant = V_VARIANTREF(pPostData);
        

		if ((PostData_variant != NULL) &&(V_VT(PostData_variant) != VT_EMPTY)) {
            SAFEARRAY *PostData_safearray = V_ARRAY(PostData_variant);
            if (PostData_safearray != NULL) {
                // Copy Post Data into a global so it can be read back when 
                // BeginningTransaction() in HttpMonitoringApp.cpp to be 
                // included with the rest of the Http Request message sent
                // to Netbeans.

				char *post_data_array = NULL;

				SafeArrayAccessData(PostData_safearray,(void HUGEP **)&post_data_array);

				long lower_bound = 1;
				long upper_bound = 1;

				SafeArrayGetLBound(PostData_safearray,1,&lower_bound);
				SafeArrayGetUBound(PostData_safearray,1,&upper_bound);

				post_data_size = (int)(upper_bound - lower_bound + 1);

                //post_data = new char[post_data_size];
                gPostText = new char[post_data_size];
				//memcpy(post_data,post_data_array,post_data_size);
                memcpy(gPostText,post_data_array,post_data_size);
				SafeArrayUnaccessData(PostData_safearray);

            } 
        }
    } 

    *pCancel = VARIANT_FALSE;
    //delete[] post_data;
}

void CNetBeansBHO::checkAndInitNetbeansDebugging(BSTR bstrURL) {
    tstring str = _bstr_t(bstrURL);
    tstring portArgName(_T("--netbeans-debugger-port="));
    tstring sessionArgName(_T("--netbeans-debugger-session-id="));

    size_t portArgPos = str.find(portArgName);
    if (portArgPos == string::npos) {
        return;
    }
    size_t sessionIdArgPos = str.find(sessionArgName, portArgPos);
    if (sessionIdArgPos == std::string::npos) {
        return;
    }
    size_t argsEndPos = str.find(_T(".html"), sessionIdArgPos);
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