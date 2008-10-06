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
#include "resource.h"       // main symbols
#include <shlguid.h>
#include <exdispid.h>
#include "NetBeansExtension.h"
#include "ScriptDebugger.h"
#include "DbgpConnection.h"

using namespace std;

#if defined(_WIN32_WCE) && !defined(_CE_DCOM) && !defined(_CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA)
#error "Single-threaded COM objects are not properly supported on Windows CE platform, such as the Windows Mobile platforms that do not include full DCOM support. Define _CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA to force ATL to support creating single-thread COM object's and allow use of it's single-threaded COM object implementations. The threading model in your rgs file was set to 'Free' as that is the only threading model supported in non DCOM Windows CE platforms."
#endif

// NetbeansBHO
class ATL_NO_VTABLE CNetBeansBHO :
    public CComObjectRootEx<CComSingleThreadModel>,
    public CComCoClass<CNetBeansBHO, &CLSID_NetBeansBHO>,
    public IObjectWithSiteImpl<CNetBeansBHO>,
    public IDispatchImpl<INetBeansBHO, &IID_INetBeansBHO, &LIBID_NetBeansExtensionLib, /*wMajor =*/ 1, /*wMinor =*/ 0>,
    public IDispEventImpl<1, CNetBeansBHO, &DIID_DWebBrowserEvents2, &LIBID_SHDocVw, 1, 1> {
public:
	CNetBeansBHO() {
	}

DECLARE_REGISTRY_RESOURCEID(IDR_NETBEANSBHO)

DECLARE_NOT_AGGREGATABLE(CNetBeansBHO)

BEGIN_COM_MAP(CNetBeansBHO)
    COM_INTERFACE_ENTRY(INetBeansBHO)
    COM_INTERFACE_ENTRY(IDispatch)
    COM_INTERFACE_ENTRY(IObjectWithSite)
END_COM_MAP()

BEGIN_SINK_MAP(CNetBeansBHO)
    SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_NAVIGATECOMPLETE2, OnNavigateComplete)
    SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_DOCUMENTCOMPLETE, OnDocumentComplete)
END_SINK_MAP()

    DECLARE_PROTECT_FINAL_CONSTRUCT()

    HRESULT FinalConstruct();
    void FinalRelease(){
    }

public:
    //IObjectWithSite
    STDMETHOD(SetSite)(IUnknown *pUnkSite);

    //Browser events
    void STDMETHODCALLTYPE OnNavigateComplete(IDispatch *pDisp, VARIANT *pvarURL);
    void STDMETHODCALLTYPE OnDocumentComplete(IDispatch *pDisp, VARIANT *pvarURL);
    void OpenURI(string url);
    DbgpConnection *getDbgpConnection() {
        return m_pDbgpConnection;
    }
 
private:
    CComPtr<IWebBrowser2>  m_spWebBrowser;
    DWORD m_dwWebBrowserCookie;
    DWORD m_dwThreadID;
    BOOL m_bAdvised;
    DbgpConnection *m_pDbgpConnection;
    BOOL debuggerStarted;

    void checkAndInitNetbeansDebugging(BSTR bstrURL);
    void initializeNetbeansDebugging(tstring port, tstring sessionId);
    static DWORD WINAPI DebuggerProc(LPVOID param);
    HRESULT getWebBrowser(IWebBrowser2 **ppRemoteDebugApp);
    void setDebuggerStopped();
};

OBJECT_ENTRY_AUTO(__uuidof(NetBeansBHO), CNetBeansBHO)
