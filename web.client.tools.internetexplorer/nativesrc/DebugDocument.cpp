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
#include "DebugDocument.h"
#include "Utils.h"


// CDebugDocTextEvents
STDMETHODIMP DebugDocument::onDestroy(void) {
    return E_NOTIMPL;
}

STDMETHODIMP DebugDocument::onInsertText(ULONG cCharacterPosition, ULONG cNumToInsert) {
    Utils::log(4, _T("Insert pos - %d, num - %d\n"), cCharacterPosition, cNumToInsert);
    return handleSourceChange();
}
    
STDMETHODIMP DebugDocument::onRemoveText(ULONG cCharacterPosition, ULONG cNumToRemove) {
    Utils::log(4, _T("Remove pos - %d, num - %d\n"), cCharacterPosition, cNumToRemove);
    return handleSourceChange();
}

STDMETHODIMP DebugDocument::onReplaceText(ULONG cCharacterPosition, ULONG cNumToReplace) {
    Utils::log(4, _T("Replace pos - %d, num - %d\n"), cCharacterPosition, cNumToReplace);
    return handleSourceChange();
}

STDMETHODIMP DebugDocument::onUpdateTextAttributes(ULONG cCharacterPosition, ULONG cNumToUpdate) {
    return E_NOTIMPL;
}

STDMETHODIMP DebugDocument::onUpdateDocumentAttributes(TEXT_DOC_ATTR textdocattr) {
    return E_NOTIMPL;
}

HRESULT DebugDocument::handleSourceChange() {
    Utils::log(4, _T("Text modified - %s\n"), name.c_str());
    if(pScriptDebugger != NULL) {
        BreakpointManager *pMgr = pScriptDebugger->getBreakpointManager();
        if(pMgr != NULL) {
            pMgr->processBreakpoints(name);
        }
        DbgpConnection *pDbgpConnection = pScriptDebugger->getDbgpConnection();
        if(pDbgpConnection != NULL) {
            pDbgpConnection->sendReloadSourcesMessage(name);
        }
    }
    return S_OK;
}
