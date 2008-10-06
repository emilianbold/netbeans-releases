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
#include "ScriptDebugger.h"
#include <string>
#include <map>
#include <list>
#include <set>
#include "Mshtml.h"

using namespace std;

class ScriptDebugger;
struct StackFrame;
struct Property;

class DbgpConnection {
public:
    DbgpConnection(tstring port, tstring sessionId, DWORD dwWebBrowserCookie);
    BOOL connectToIDE();
    void close();
    static DWORD WINAPI commandHandler(LPVOID param);
    void sendInitMessage();
    ScriptDebugger *getScriptDebugger() {
        return m_pScriptDebugger;
    }
    void setScriptDebugger(ScriptDebugger *pScriptDebugger) {
        m_pScriptDebugger = pScriptDebugger;
    }
    DWORD getWebBrowserCookie() {
        return m_dwWebBrowserCookie;
    }
    void handleDocumentComplete(IHTMLDocument2 *pHTMLDocument);
    void sendBreakpointMessage(StackFrame *pStackFrame, tstring breakPointID, tstring reason);
    void sendStatusMessage(tstring status, tstring reason);
    void sendErrorMessage(tstring message);
    void sendReloadSourcesMessage(tstring docName);

private:
    SOCKET m_socket;
    tstring m_port, m_sessionId;
    SOCKET getSocket() {
        return m_socket;
    }
    ScriptDebugger *m_pScriptDebugger;
    DWORD m_dwWebBrowserCookie;
    BOOL readCommand(char *cmdString);
    void processCommand(char *cmdString, DbgpConnection *pDbgpConnection);
    void sendResponse(tstring xmlString);
    void sendWindowsMessage(IHTMLDocument2 *pHTMLDocument);
    void sendSourcesMessage(IHTMLDocument2 *pHTMLDocument);
    set<tstring> getFrameURLs(IHTMLDocument2 *pHTMLDocument, BOOL scriptOnly);
    BOOL unicodeToUTF8(tstring str, char **ppBytes, int *pBytesLen);
    BOOL UTF8toUnicode(char *str, TCHAR **ppChars);
    tstring encodeToBase64(tstring value);
};


