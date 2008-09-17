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
#include "Utils.h"
#include "ScriptDebugger.h"

class DbgpConnection;

// CDebugDocTextEvents
class ATL_NO_VTABLE DebugDocument :
    public CComObjectRootEx<CComMultiThreadModel>,
    public IDebugDocumentTextEvents {
public:
    DebugDocument() {
    }

BEGIN_COM_MAP(DebugDocument)
   COM_INTERFACE_ENTRY(IDebugDocumentTextEvents)
   COM_INTERFACE_ENTRY2(IUnknown, IDebugDocumentTextEvents)
END_COM_MAP()

    DECLARE_PROTECT_FINAL_CONSTRUCT()

    HRESULT FinalConstruct() {
        return S_OK;
    }

    void FinalRelease() {
        Utils::log(4, _T("Destructing DebugDocument - %s\n"), name.c_str());
    }

public:
    //IDebugDocumentTextEvents
    STDMETHOD(onDestroy)(void);
    STDMETHOD(onInsertText)( 
        /* [in] */ ULONG cCharacterPosition,
        /* [in] */ ULONG cNumToInsert);
    STDMETHOD(onRemoveText)( 
        /* [in] */ ULONG cCharacterPosition,
        /* [in] */ ULONG cNumToRemove);
    STDMETHOD(onReplaceText)( 
        /* [in] */ ULONG cCharacterPosition,
        /* [in] */ ULONG cNumToReplace);
    STDMETHOD(onUpdateTextAttributes)( 
        /* [in] */ ULONG cCharacterPosition,
        /* [in] */ ULONG cNumToUpdate);
    STDMETHOD(onUpdateDocumentAttributes)( 
        /* [in] */ TEXT_DOC_ATTR textdocattr);

    void setCookie(DWORD cookie) {
        this->cookie = cookie;
    }

    void setEventCookie(DWORD cookie) {
        this->eventCookie = eventCookie;
    }

    DWORD getCookie() {
        return this->cookie;
    }

    DWORD getEventCookie() {
        return this->eventCookie;
    }

    void setScriptDebugger(ScriptDebugger *pScriptDebugger) {
        this->pScriptDebugger = pScriptDebugger;
    }

    void setDocumentName(tstring name) {
        this->name = name;
    }
    
private:
    DWORD cookie, eventCookie;
    tstring name;
    ScriptDebugger *pScriptDebugger;
    HRESULT handleSourceChange();
};
