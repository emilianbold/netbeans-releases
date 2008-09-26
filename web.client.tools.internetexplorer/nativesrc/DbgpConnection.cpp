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
#include "DbgpConnection.h"
#include "DbgpCommand.h"
#include "XMLTag.h"
#include "Exdisp.h"
#include "Utils.h"

DbgpConnection::DbgpConnection(tstring port, tstring sessionId, DWORD dwWebBrowserCookie) {
    m_port = port;
    m_sessionId = sessionId;
    // Initialize Winsock
    WSADATA wsaData;
    int iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
    if (iResult != 0) {
        Utils::log(1, _T("WSAStartup failed: %d\n"), iResult);
    }
    m_dwWebBrowserCookie = dwWebBrowserCookie;
}

void DbgpConnection::close() {
    closesocket(m_socket);
    WSACleanup();   
    m_socket = NULL;
}

BOOL DbgpConnection::connectToIDE() {
    BOOL connected = FALSE;
    //Create a SOCKET for connecting to server
    m_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (m_socket != INVALID_SOCKET) {
        struct sockaddr_in clientService; 
        clientService.sin_family = AF_INET;
        clientService.sin_addr.s_addr = inet_addr("127.0.0.1");
        clientService.sin_port = htons(_ttoi(m_port.c_str()));
        // Connect to server.
        int iResult = connect(m_socket, (SOCKADDR*)&clientService, sizeof(clientService));
        if (iResult == SOCKET_ERROR) {
            Utils::log(1, _T("Connect failed with error: %d\n"), WSAGetLastError());
        }else {
            connected = TRUE;
        }
    }
    return connected;
}

void DbgpConnection::sendResponse(tstring xmlString) {
    const TCHAR *xmlResponse = xmlString.c_str();
    if(xmlResponse != NULL) {
        char *messageData = NULL;
        int messageDataLen;
        if(unicodeToUTF8(xmlString, &messageData, &messageDataLen)) {
            //Format of message: <length of data><null><XML response><null>
            int approxDataLen = messageDataLen + 16;
            char *data = new char[approxDataLen]; //16 bytes for length of data
            sprintf_s(data, approxDataLen, "%d%c%s", messageDataLen-1, '\0', messageData);
            int dataLen = (int)strlen(data) + 1 + messageDataLen;
            int iResult = send(m_socket, data, dataLen, 0);
            //Utils::log(4, _T("Response: %s\n"), data);
            delete []messageData;
            delete []data;
        }
    }
}

BOOL DbgpConnection::unicodeToUTF8(tstring str, char **ppBytes, int *pBytesLen) {
    char *data = NULL;
    int dataLen = WideCharToMultiByte(CP_UTF8, 0, str.c_str(), -1, data, 0, 0, 0);
    if(dataLen) {
        data = new char[dataLen];
        dataLen = WideCharToMultiByte(CP_UTF8, 0, str.c_str(), -1, data, dataLen, 0, 0);
        if(dataLen) {
            *ppBytes = data;
            *pBytesLen = dataLen;
            return TRUE;
        }
    }
    return FALSE;
}

BOOL DbgpConnection::UTF8toUnicode(char *str, TCHAR **ppChars) {
    TCHAR *out = NULL;
    int len = MultiByteToWideChar(CP_UTF8, 0, str, -1, out, 0);
    if(len) {
        out = new TCHAR[len];
        len = MultiByteToWideChar(CP_UTF8, 0, str, -1, out, len);
        if(len) {
            *ppChars = out;
            return TRUE;
        }
    }
    return FALSE;
}

void DbgpConnection::sendInitMessage() {
    DbgpMessage message;
    message.setTagName(_T("init"));
    message.addAttribute(_T("appid"), _T("netBeans-ie-extension"));
    message.addAttribute(_T("idekey"), _T("6.5"));
    message.addAttribute(_T("session"), m_sessionId);
    message.addAttribute(_T("thread"), _T("1"));
    message.addAttribute(_T("parent"), _T("IE"));
    message.addAttribute(_T("language"), _T("JavaScript"));
    message.addAttribute(_T("protocol_version"), _T("1.0"));
    message.addAttribute(FILE_URI, _T("about:blank"));
    sendResponse(message.toString());
}

void DbgpConnection::handleDocumentComplete(IHTMLDocument2 *pHTMLDocument) {
    sendWindowsMessage(pHTMLDocument);
    sendSourcesMessage(pHTMLDocument);
    if(m_pScriptDebugger != NULL && m_pScriptDebugger->getStatus() != STATE_RUNNING) {
        m_pScriptDebugger->changeState(STATE_RUNNING, OK);
    }
}

void DbgpConnection::sendWindowsMessage(IHTMLDocument2 *pHTMLDocument) {
    CComBSTR bstrURL;
    DbgpWindowsMessage message;
    message.addAttribute(ENCODING, BASE64);
    DbgpWindowTag &windowTag = message.addWindow();
    HRESULT hr = pHTMLDocument->get_URL(&bstrURL);
    windowTag.addAttribute(FILE_URI, encodeToBase64((TCHAR *)(bstrURL)));

    set<tstring> frameURLs = getFrameURLs(pHTMLDocument, FALSE);
    set<tstring>::iterator iter = frameURLs.begin();
    while(iter != frameURLs.end()) {
        DbgpWindowTag &frameTag = windowTag.addWindowTag();
        frameTag.addAttribute(FILE_URI, encodeToBase64(*iter));
        ++iter;
    }
    sendResponse(message.toString());
}

tstring DbgpConnection::encodeToBase64(tstring value) {
    USES_CONVERSION;
    char *data = NULL;
    int dataLen;
    if(unicodeToUTF8(value, &data, &dataLen)) {
        int destLen = Base64EncodeGetRequiredLength(dataLen, ATL_BASE64_FLAG_NOCRLF);
        char *dest = new char[destLen+1];
        if(Base64Encode((BYTE *)data, dataLen, dest, &destLen, ATL_BASE64_FLAG_NOCRLF)) {
            dest[destLen] = 0;
            return tstring(A2T(dest));
        }
    }
    return NULL;
}


set<tstring> DbgpConnection::getFrameURLs(IHTMLDocument2 *pHTMLDocument, BOOL scriptOnly) {
    CComPtr<IDispatch> spDisp = pHTMLDocument;
    set<tstring> frameURLs;
    if(spDisp != NULL) {
        CComQIPtr<IOleContainer> spContainer = spDisp;
        CComPtr<IEnumUnknown> spEnumerator;
        // Get an enumerator for the frames
        HRESULT hr = spContainer->EnumObjects(OLECONTF_EMBEDDINGS, &spEnumerator);
        if (SUCCEEDED(hr)) {
            CComPtr<IUnknown> spUnk;
            ULONG uFetched;
            // Enumerate and add to the list
            for (int i = 0; S_OK == spEnumerator->Next(1, &spUnk, &uFetched); i++) {
                CComQIPtr<IWebBrowser2> spWebBrowser = spUnk;
                if (spWebBrowser != NULL) {
                    CComBSTR bstrURL;
                    if(scriptOnly) {
                        CComPtr<IDispatch> spDispHtmlDoc;
                        spWebBrowser->get_Document(&spDispHtmlDoc);
                        CComQIPtr<IHTMLDocument2> spHtmlDocument = spDispHtmlDoc;
                        if(spHtmlDocument != NULL) {
                            CComPtr<IHTMLElementCollection> spHTMLElementCollection;
                            hr = spHtmlDocument->get_scripts(&spHTMLElementCollection);
                            long items;
                            spHTMLElementCollection->get_length(&items);
                            if(items > 0) {
                                spHtmlDocument->get_URL(&bstrURL);
                            }
                        }
                    } else {
                        spWebBrowser->get_LocationURL(&bstrURL);
                    }
                    if(bstrURL != NULL) {
                        tstring location = (TCHAR *)(bstrURL);
                        size_t pos = location.find(_T("http://"));
                        if(pos != string::npos && bstrURL != NULL) {
                            frameURLs.insert(location);
                        }
                    }
                }
                spUnk.Release();
            }
        }
    }
    return frameURLs;
}

void DbgpConnection::sendSourcesMessage(IHTMLDocument2 *pHTMLDocument) {
    CComPtr<IHTMLElementCollection> spHTMLElementCollection;
    HRESULT hr = pHTMLDocument->get_scripts(&spHTMLElementCollection);

    CComBSTR bstrURL;
    pHTMLDocument->get_URL(&bstrURL);
    DbgpSourcesMessage message(SOURCES);
    message.addAttribute(ENCODING, BASE64);
    DbgpSourceTag &sourceTag = message.addSource();
    sourceTag.addAttribute(FILE_URI, encodeToBase64((TCHAR *)(bstrURL)));

    long items;
    spHTMLElementCollection->get_length(&items);
    for (long i=0; i<items; i++) {
        CComVariant index = i;
        CComPtr<IDispatch> spDisp;
        hr = spHTMLElementCollection->item(index, index, &spDisp);
        CComQIPtr<IHTMLScriptElement> spScriptElement = spDisp;
        CComBSTR bstrSrc;
        hr = spScriptElement->get_src(&bstrSrc);
        if(hr == S_OK && bstrSrc != NULL) {
            DbgpSourceTag &sourceTag = message.addSource();
            tstring location = (TCHAR *)(bstrSrc);
            size_t pos = location.find(_T("http://"));
            tstring uri = location;
            if (pos == string::npos) {
                tstring docURL = (TCHAR *)(bstrURL);
                pos = docURL.find_last_of(_T("/\\"));
                if (pos != string::npos) {
                    uri = docURL.substr(0, pos+1).append(location);
                }
            }
            sourceTag.addAttribute(FILE_URI, encodeToBase64(uri));
        }
    }

    set<tstring> frameURLs = getFrameURLs(pHTMLDocument, TRUE);
    set<tstring>::iterator iter = frameURLs.begin();
    while(iter != frameURLs.end()) {
        DbgpSourceTag &sourceTag = message.addSource();
        sourceTag.addAttribute(FILE_URI, encodeToBase64(*iter));
        ++iter;
    }

    sendResponse(message.toString());

    /*
    fillSources(spHTMLElementCollection, &sources);
    CComPtr<IDispatch> spDisp = pHTMLDocument;
    if(spDisp != NULL) {
        CComQIPtr<IOleContainer> spContainer = spDisp;
        CComPtr<IEnumUnknown> spEnumerator;
        // Get an enumerator for the frames
        hr = spContainer->EnumObjects(OLECONTF_EMBEDDINGS, &spEnumerator);
        if (SUCCEEDED(hr)) {
            CComPtr<IUnknown> spUnk;
            ULONG uFetched;
            // Enumerate and add the child tags
            for (int i = 0; S_OK == spEnumerator->Next(1, &spUnk, &uFetched); i++) {
                CComQIPtr<IHTMLDocument2> spHtmlDocument = spUnk;
                if (spHtmlDocument != NULL) {
                    hr = spHtmlDocument->get_scripts(&spHTMLElementCollection);
                    fillSources(spHTMLElementCollection, &sources);
                }
                spUnk.Detach();
            }
        }
    }
    */
}

void DbgpConnection::sendReloadSourcesMessage(tstring docName) {
    DbgpSourcesMessage message(RELOAD_SOURCES);
    message.addAttribute(ENCODING, BASE64);
    DbgpSourceTag &sourceTag = message.addSource();
    sourceTag.addAttribute(FILE_URI, encodeToBase64(docName.c_str()));
    sendResponse(message.toString());
}

void DbgpConnection::sendBreakpointMessage(StackFrame *pStackFrame, tstring breakPointID, tstring reason) {
    DbgpBreakpointMessage message;
    message.addAttribute(STATUS, BREAKPOINT);
    message.addAttribute(REASON, reason);
    DbgpMessageTag &messageTag = message.addMessage();
    messageTag.addAttribute(FILE_NAME, pStackFrame->fileName);
    messageTag.addAttribute(LINE_NO, pStackFrame->line);
    messageTag.addAttribute(ID, breakPointID);
    sendResponse(message.toString());
}

void DbgpConnection::sendStatusMessage(tstring status, tstring reason) {
    DbgpMessage message;
    message.addAttribute(COMMAND, STATUS);
    message.addAttribute(STATUS, status); 
    message.addAttribute(REASON, reason);
    sendResponse(message.toString());
}

void DbgpConnection::sendErrorMessage(tstring error) {
    DbgpStreamMessage message;
    message.addAttribute(TYPE, STD_ERR);
    message.setValue(error);
    sendResponse(message.toString());
}


BOOL DbgpConnection::readCommand(char *cmdString) {
    int result;
    int index = 0;
    do {
        result = recv(m_socket, cmdString+index, 1, 0);
        if (result > 0) {
            //check for end of command
            if(*(cmdString+index) == '\0') {
                return TRUE;
            }
            index++;
        }else {
            return FALSE;
        }
    } while(result > 0);
    return FALSE;
}

void DbgpConnection::processCommand(char *cmdString, DbgpConnection *pDbgpConnection) {
    TCHAR *str;

    if(!UTF8toUnicode(cmdString, &str)) {
        return;
    }
    Utils::log(4, _T("Processing command - %s\n"), str);
    //Utils::log(4, _T("Command: %s\n"), cmdString);
    tstring cmdStr = str;
    size_t firstSpacePos = cmdStr.find(_T(" "));
    tstring command = cmdStr.substr(0, firstSpacePos);
    tstring args = cmdStr.substr(firstSpacePos+1, cmdStr.length());

    //Parse the arguments and prepare a map of argument switch and value
    map<char, tstring> argsMap;
    unsigned int index = 0;
    const TCHAR *data = args.c_str();
    bool argSwitchFound = false;
    int argValueIndex = -1;
    while(index <= args.length()) {
        TCHAR argSwitch;
        if(data[index] == _T(' ') || data[index] == _T('\0')){
            if(argValueIndex != -1) {
                tstring argValue = args.substr(argValueIndex, index-argValueIndex);
                argsMap.insert(pair<char, tstring>(argSwitch, argValue.c_str()));
                argValueIndex = -1;
            }
        }else if(data[index] == _T('-')) {
              if(data[index+2] == _T(' ')) {
                argSwitchFound = true;
                argSwitch = data[++index];
                if(argSwitch == _T('-') || argSwitch == _T('e')) {
                    //- arg switch indicates data
                    //no more options are expected after this
                    argValueIndex = index+2;
                    index = args.length()-1;
                }else {
                    index++;
                }
            }
        }else {
            if(argSwitchFound) {
                argValueIndex = index;
                argSwitchFound = false;
            }
        }
        index++;
    }

    CommandResponseIterator iterator = DbgpCommand::commandResponseMap.find(command.c_str());
    DbgpCommand *pDbgpCommand = (DbgpCommand *)iterator->second;
    DbgpResponse *pDbgpResponse = pDbgpCommand->process(pDbgpConnection, argsMap);
    if(pDbgpCommand->needsResponse() && pDbgpResponse != NULL) {
        sendResponse(pDbgpResponse->toString());
        delete pDbgpResponse;
    }
}

DWORD WINAPI DbgpConnection::commandHandler(LPVOID param) {
    ::CoInitialize(NULL);
    DbgpConnection *pDbgpConnection = (DbgpConnection *)param;        
    if(pDbgpConnection != NULL) {
        //Send init message to IDE, to establish communication with Netbeans IDE
        pDbgpConnection->sendInitMessage();

        //Setup the command response map
        DbgpCommand::initializeMap();

        char cmdString[256];
        //Tx and Rx messages
        while(pDbgpConnection->readCommand(cmdString)) {
            pDbgpConnection->processCommand(cmdString, pDbgpConnection);
        }
    }
    
    ::CoUninitialize();
    return 0;
}