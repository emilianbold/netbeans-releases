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
#include <stdafx.h>
#include "DbgpCommand.h"
#include "Utils.h"
#include <exdisp.h>
#include "Breakpoint.h"
#include "wininet.h"
#include <list>

map<tstring, DbgpCommand *> DbgpCommand::commandResponseMap;

void DbgpCommand::initializeMap() {
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STATUS, new StatusCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(FEATURE_GET, new FeatureGetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(FEATURE_SET, new FeatureSetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(RUN,  new RunCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STEP_INTO, new StepIntoCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STEP_OVER, new StepOverCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STEP_OUT, new StepOutCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STOP, new StopCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(DETACH, new DetachCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(PAUSE, new PauseCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(BREAKPOINT_SET, new BreakpointSetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(BREAKPOINT_GET, new BreakpointGetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(BREAKPOINT_UPDATE, new BreakpointUpdateCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(BREAKPOINT_REMOVE, new BreakpointRemoveCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(BREAKPOINT_LIST, new BreakpointListCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STACK_DEPTH, new StackDepthCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STACK_GET, new StackGetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(CONTEXT_NAMES, new ContextNamesCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(CONTEXT_GET, new ContextGetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(TYPEMAP_GET, new TypemapGetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(PROPERTY_GET, new PropertyGetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(PROPERTY_SET, new PropertySetCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(PROPERTY_VALUE, new PropertyValueCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(SOURCE, new SourceCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STDOUT, new StdoutCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(STDERR, new StderrCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(EVAL, new EvalCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(BREAK, new BreakCommand()));
    commandResponseMap.insert(pair<tstring, DbgpCommand *>(OPEN_URI, new OpenUriCommand()));
}

//STATUS command
//status -i <tx_id>
//<response command="status" status=""  reason="" transaction_id=xxx/>
DbgpResponse *StatusCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    tstring status = pScriptDebugger->getStatusString();

    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(STATUS, argsMap.find('i')->second);
    pDbgpResponse->addAttribute(REASON, OK);
    pDbgpResponse->addAttribute(STATUS, status);
    return pDbgpResponse;
}

//FEATURE_GET command
//feature_get -i <tx_id> -n <feature>
//<response command="feature_get" supported=<0|1> transaction_id=xxx/>
DbgpResponse *FeatureGetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    tstring feature = argsMap.find('n')->second;
    BOOL result = FALSE;
    if(feature == _T("showFunctions")) {
        result = pScriptDebugger->isFeatureSet(SHOW_FUNCTIONS);
    }else if(feature == _T("showConstants")) {
        result = pScriptDebugger->isFeatureSet(SHOW_CONSTANTS);
    }else if(feature == _T("bypassConstructors")) {
        result = pScriptDebugger->isFeatureSet(BY_PASS_CONSTRUCTORS);
    }else if(feature == _T("stepFiltersEnabled")) {
        result = pScriptDebugger->isFeatureSet(STEP_FILTERS_ENABLED);
    }else if(feature == _T("suspendOnFirstLine")) {
        result = pScriptDebugger->isFeatureSet(SUSPEND_ON_FIRSTLINE);
    }else if(feature == _T("suspendOnExceptions")) {
        result = pScriptDebugger->isFeatureSet(SUSPEND_ON_EXCEPTIONS);
    }else if(feature == _T("suspendOnErrors")) {
        result = pScriptDebugger->isFeatureSet(SUSPEND_ON_ERRORS);
    }else if(feature == _T("suspendOnDebuggerKeyword")) {
        result = pScriptDebugger->isFeatureSet(SUSPEND_ON_DEBUGGER_KEYWORD);
    }else if(feature == _T("ignoreQueryStrings")) {
        result = pScriptDebugger->isFeatureSet(IGNORE_QUERY_STRINGS);
    }
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(FEATURE_GET, argsMap.find('i')->second);
    pDbgpResponse->addAttribute(SUPPORTED, result ? 1 : 0);
    pDbgpResponse->addAttribute(FEATURE, feature);
    return pDbgpResponse;
}

//FEATURE_SET command
//feature_set -i <tx_id> -n <feature> -v <value>
//<response command="feature_set" success=<0|1> transaction_id=xxx/>          
DbgpResponse *FeatureSetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(FEATURE_SET, argsMap.find('i')->second);
    tstring feature = argsMap.find('n')->second;
    pDbgpResponse->addAttribute(SUCCESS, 0);
    pDbgpResponse->addAttribute(FEATURE, feature);
    if(pScriptDebugger != NULL) {
        BOOL value = argsMap.find('v')->second == _T("true") ? TRUE : FALSE;
        if(value) {
            if(feature == _T("showFunctions")) {
                pScriptDebugger->setFeature(SHOW_FUNCTIONS);
            }else if(feature == _T("showConstants")) {
                pScriptDebugger->setFeature(SHOW_CONSTANTS);
            }else if(feature == _T("bypassConstructors")) {
                pScriptDebugger->setFeature(BY_PASS_CONSTRUCTORS);
            }else if(feature == _T("stepFiltersEnabled")) {
                pScriptDebugger->setFeature(STEP_FILTERS_ENABLED);
            }else if(feature == _T("suspendOnFirstLine")) {
                pScriptDebugger->setFeature(SUSPEND_ON_FIRSTLINE);
            }else if(feature == _T("suspendOnExceptions")) {
                pScriptDebugger->setFeature(SUSPEND_ON_EXCEPTIONS);
            }else if(feature == _T("suspendOnErrors")) {
                pScriptDebugger->setFeature(SUSPEND_ON_ERRORS);
            }else if(feature == _T("suspendOnDebuggerKeyword")) {
                pScriptDebugger->setFeature(SUSPEND_ON_DEBUGGER_KEYWORD);
            }else if(feature == _T("ignoreQueryStrings")) {
                pScriptDebugger->setFeature(IGNORE_QUERY_STRINGS);
            }
        }
        pDbgpResponse->addAttribute(SUCCESS, 1);
    }

    return pDbgpResponse;
}

//RUN command(one-way)
//run -i <tx_id>
DbgpResponse *RunCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pScriptDebugger->run();
    return NULL;
}

//STEP_INTO command(one-way)
//step_into -i <tx_id>
DbgpResponse *StepIntoCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pScriptDebugger->stepInto();
    return NULL;
}

//STEP_OVER command(one-way)
//step_over -i <tx_id>
DbgpResponse *StepOverCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pScriptDebugger->stepOver();
    return NULL;
}

//STEP_OUT command(one-way)
//step_out -i <tx_id>
DbgpResponse *StepOutCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pScriptDebugger->stepOut();
    return NULL;
}

//STOP command(one-way)
//stop -i <tx_id>
DbgpResponse *StopCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pDbgpConnection->close();
    return NULL;
}

//DETACH command(one-way)
//detach -i <tx_id>
DbgpResponse *DetachCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pDbgpConnection->close();
    return NULL;
}

//PAUSE command(one-way)
//pause -i <tx_id>
DbgpResponse *PauseCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    pScriptDebugger->pause();
    return NULL;
}

//BREAKPOINT_SET command
//breakpoint_set -i <tx_id> -f <uri> -n <lineNo> -r <temporary> -h <hitValue> -o <hitFilter> -- <expression> -s <state>
//<response command="breakpoint_set" state="enabled/disabled" id=xxx transaction_id=xxx/>          
DbgpResponse *BreakpointSetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    Breakpoint *pBreakpoint = NULL;
    if(pScriptDebugger != NULL) {
        BreakpointManager *pMgr = pScriptDebugger->getBreakpointManager();
        tstring fileURI = argsMap.find('f')->second;
        int lineNo = _ttoi(argsMap.find('n')->second.c_str());
        pBreakpoint = pMgr->createBreakpoint(fileURI, lineNo);
        BreakpointUpdateCommand::setUpdatableValues(pBreakpoint, argsMap);
        pMgr->setBreakpoint(pBreakpoint);

        //check for run to cursor request
        map<char, tstring>::iterator iter = argsMap.find('r');
        if(iter != argsMap.end() && (_ttoi(iter->second.c_str()) == 1)) {
            pBreakpoint->setTemporary(TRUE);
            pScriptDebugger->run();
        }
    }

    //Generate response
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(BREAKPOINT_SET, argsMap.find('i')->second);
    pDbgpResponse->addAttribute(_T("id"), pBreakpoint != NULL ? pBreakpoint->getID() : _T(""));
    pDbgpResponse->addAttribute(_T("state"), argsMap.find('s')->second);
    return pDbgpResponse;
}

DbgpResponse *BreakpointGetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

//BREAKPOINT_UPDATE command
//breakpoint_update -i <tx_id> -d <breakpoint_id> -s <state> -n <lineNo> -h <hitValue> -o <hitFilter>
//<response command="breakpoint_update" transaction_id=xxx/>         
DbgpResponse *BreakpointUpdateCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    BreakpointManager *pMgr = pScriptDebugger->getBreakpointManager();
    Breakpoint *pBreakpoint = pMgr->getUpdatableBreakpoint(argsMap.find('d')->second);
    if(pBreakpoint != NULL) {
        map<char, tstring>::iterator iter = argsMap.find('n');
        if(iter != argsMap.end()) {
            int line = _ttoi(iter->second.c_str());
            pBreakpoint->setLineNumber(line);
        }
        setUpdatableValues(pBreakpoint, argsMap);
        pMgr->setBreakpoint(pBreakpoint);
    }
    //Generate response
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(BREAKPOINT_UPDATE, argsMap.find('i')->second);
    return pDbgpResponse;
}

void BreakpointUpdateCommand::setUpdatableValues(Breakpoint *pBreakpoint, map<char, tstring> argsMap) {
    map<char, tstring>::iterator iter = argsMap.find('s');
    if(iter != argsMap.end()) {
        BOOL state = (iter->second == _T("enabled")) ? TRUE : FALSE;
        pBreakpoint->setState(state);
    }

    iter = argsMap.find('h');
    if(iter != argsMap.end()) {
        int hitValue = _ttoi(iter->second.c_str());
        if(hitValue > 0) {
            pBreakpoint->setHitValue(hitValue);
        }
    }

    iter = argsMap.find('o');
    if(iter != argsMap.end()) {
        tstring filter = iter->second;
        HitFilter hitFilter;
        if(filter == HIT_FILTER_EQUAL) {
            hitFilter = EQUAL;
        }else if(filter == HIT_FILTER_GREATER_OR_EQUAL) {
            hitFilter = GREATER_OR_EQUAL;
        }else if(filter == HIT_FILTER_MULTIPLE) {
            hitFilter = MULTIPLE;
        }
        pBreakpoint->setHitFilter(hitFilter);
    }

    iter = argsMap.find('-');
    if(iter != argsMap.end()) {
        pBreakpoint->setExpression(iter->second);
    }
}



//BREAKPOINT_REMOVE command
//breakpoint_remove -i <tx_id> -d <breakpoint_id>
//<response command="breakpoint_remove" transaction_id=xxx/>          
DbgpResponse *BreakpointRemoveCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    BreakpointManager *pMgr = pScriptDebugger->getBreakpointManager();
    pMgr->removeBreakpoint(argsMap.find('d')->second);
    //Generate response
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(BREAKPOINT_REMOVE, argsMap.find('i')->second);
    return pDbgpResponse;
}

DbgpResponse *BreakpointListCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}
DbgpResponse *StackDepthCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

//STACK_GET command
//stack_get -i <tx_id>
//<response command="stack_get" transaction_id="xxx">
//  <stack level=<depth> type=<"file"|"eval"> filename=<file_name> lineno=<line_num> where=<location>/>
//  .
//  .
//</response>          
DbgpResponse *StackGetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    list<StackFrame *> frames = pScriptDebugger->getStackFrames();
    list<StackFrame *>::iterator iter = frames.begin();
    StackGetResponse *pDbgpResponse = new StackGetResponse(STACK_GET, argsMap.find('i')->second);
    for(int i=0; iter!=frames.end(); i++) {
        StackFrame *pInfo = *iter;
        DbgpStackTag &stackTag = pDbgpResponse->addStack();
        stackTag.addAttribute(_T("level"), i);
        stackTag.addAttribute(_T("type"), _T("file"));
        stackTag.addAttribute(_T("filename"), pInfo->fileName);
        stackTag.addAttribute(_T("lineno"), pInfo->line);
        stackTag.addAttribute(_T("where"), pInfo->location);
        ++iter;
    }
    return pDbgpResponse;
}

DbgpResponse *ContextNamesCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

DbgpResponse *ContextGetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

DbgpResponse *TypemapGetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

//PROPERTY_SET command
//property_set -i <tx_id> -n <name> -d <stack_depth> -- <value> (-l <data_length> -- {DATA})
//<response command="property_set" success="0|1" transaction_id="xxx">
//</response>
DbgpResponse *PropertySetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    int depth = _ttoi(argsMap.find('d')->second.c_str());
    tstring name = argsMap.find('n')->second;
    tstring value = argsMap.find('-')->second;
    BOOL result = pScriptDebugger->setProperty(name, depth, value);
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(PROPERTY_SET, argsMap.find('i')->second);
    pDbgpResponse->addAttribute(SUCCESS, result);
    return pDbgpResponse;
}

//PROPERTY_GET command
//property_get -i <tx_id> -n <name> -d <stack_depth>
//<response command="property_get" transaction_id="xxx">
//  <property name="" fullname="" type="" numchildren=<0|-1> encoding=""><value>
//      <property name="" fullname="" type="" numchildren=<0|-1> encoding="">"<value>"</property>
//  </property>
//  .
//  .
//</response>
DbgpResponse *PropertyGetCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    int depth = _ttoi(argsMap.find('d')->second.c_str());
    tstring name = argsMap.find('n')->second;
    Property *pProp = pScriptDebugger->getProperty(name, depth);
    PropertyGetResponse *pDbgpResponse = new PropertyGetResponse(PROPERTY_GET, argsMap.find('i')->second);
    return fillInProperties(pDbgpResponse, pProp);
}

DbgpResponse *PropertyGetCommand::fillInProperties(PropertyGetResponse *pDbgpResponse, Property *pProp) {
    if(pProp != NULL) {
        DbgpPropertyTag &propTag = pDbgpResponse->addProperty();
        propTag.addAttribute(_T("fullname"), pProp->fullName);
        propTag.addAttribute(_T("name"), pProp->name);
        propTag.addAttribute(_T("type"), pProp->type);
        propTag.addAttribute(_T("numchildren"), pProp->childrenCount);
        propTag.addAttribute(_T("encoding"), _T("none"));
        propTag.addAttribute(_T("classname"), pProp->classname);
        propTag.setValue(pProp->value);
        list<Property *>::iterator iter = pProp->children.begin();
        while(iter != pProp->children.end()) {
            Property *pChildProperty = *iter;
            if(pChildProperty != NULL) {
                DbgpPropertyTag &chidlPropTag = propTag.addPropertyTag();
                chidlPropTag.addAttribute(_T("fullname"), pChildProperty->fullName);
                chidlPropTag.addAttribute(_T("name"), pChildProperty->name);
                chidlPropTag.addAttribute(_T("type"), pChildProperty->type);
                chidlPropTag.addAttribute(_T("numchildren"), pChildProperty->childrenCount);
                chidlPropTag.addAttribute(_T("encoding"), _T("none"));
                chidlPropTag.addAttribute(_T("classname"), pChildProperty->classname);
                chidlPropTag.setValue(pChildProperty->value);
                delete pChildProperty;
            }
            ++iter;
        }
        delete pProp;
    }
    return pDbgpResponse;
}

DbgpResponse *PropertyValueCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

//SOURCE command
//source -i <tx_id> -f <file_uri> -b <begin_line> -e <end_line>
//<response command="source"  success="0|1" transaction_id="xxx">
//  file data
//</response> 
DbgpResponse *SourceCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    tstring fileURI = argsMap.find('f')->second;
    int beginLine = 0;
    int endLine = 0;
    map<char, tstring>::iterator iter = argsMap.find('b');
    if(iter != argsMap.end()) {
        beginLine = _ttoi(argsMap.find('b')->second.c_str());
    }
    iter = argsMap.find('e');
    if(iter != argsMap.end()) {
        endLine = _ttoi(argsMap.find('e')->second.c_str());
    }
    TCHAR *buffer = pScriptDebugger->getSourceText(fileURI, beginLine, endLine);
    StandardDbgpResponse *pDbgpResponse = new StandardDbgpResponse(SOURCE, argsMap.find('i')->second);
    int success = 0;
    if(buffer == NULL || buffer[0] == 0) {
        //Get the source using WinInet APIs
        USES_CONVERSION;
        HINTERNET hSession = InternetOpen(L"Source Reader", PRE_CONFIG_INTERNET_ACCESS, L"", 
                                            NULL, INTERNET_INVALID_PORT_NUMBER);
        if (hSession != NULL) {
            HINTERNET hUrlFile = InternetOpenUrl(hSession, fileURI.c_str(), NULL, 0, 0, 0);
            DWORD bufSize;
            if (hUrlFile != NULL && InternetQueryDataAvailable(hUrlFile, &bufSize, 0, 0)) {
                char *pBytes = new char[bufSize+1];
                DWORD dwBytesRead = 0;
                BOOL read = InternetReadFile(hUrlFile, pBytes, bufSize, &dwBytesRead);
                if(read) {
                    pBytes[dwBytesRead] = 0;
                    pDbgpResponse->setValue(A2W(pBytes));
                    success = 1;
                    delete []pBytes;
                }
            }
        }
    }else{
        pDbgpResponse->setValue(buffer);
        success = 1;
        delete []buffer;
    }
    pDbgpResponse->addAttribute(SUCCESS, success);
    return pDbgpResponse;

}

tstring SourceCommand::getDOMText(DbgpConnection *pDbgpConnection, tstring fileURI) {
    CComPtr<IWebBrowser2> spWebBrowser;
    tstring result;
    HRESULT hr = Utils::getInterfaceFromGlobal(pDbgpConnection->getWebBrowserCookie(), 
                                                IID_IWebBrowser2, (void **)&spWebBrowser);
    if(hr == S_OK) {
        CComBSTR bstrURL;
        spWebBrowser->get_LocationURL(&bstrURL);
        tstring location = (TCHAR *)(bstrURL);
        CComPtr<IWebBrowser2> spFrameWebBrowser;
        if(location != fileURI) {
            spFrameWebBrowser = getWebBrowserForFrame(fileURI, spWebBrowser);
        }else {
            spFrameWebBrowser = spWebBrowser;
        }
        if(spFrameWebBrowser != NULL) {
            CComPtr<IDispatch> spDisp;
            spFrameWebBrowser->get_Document(&spDisp);
            CComQIPtr<IHTMLDocument2> spHtmlDocument = spDisp;
            CComPtr<IHTMLElementCollection> spHTMLElementCollection;
            if(spHtmlDocument != NULL) {
                HRESULT hr = spHtmlDocument->get_all(&spHTMLElementCollection);
                long items;
                if(spHTMLElementCollection != NULL) {
                    spHTMLElementCollection->get_length(&items);
                    //Track the visited elements to prevent duplication of text
                    list<IHTMLElement *> visitedElements;
                    for (long i=0; i<items; i++) {
                        CComVariant index = i;
                        CComPtr<IDispatch> spDisp1;
                        hr = spHTMLElementCollection->item(index, index, &spDisp1);
                        CComQIPtr<IHTMLElement> spHTMLElement = spDisp1;
                        if(spHTMLElement != NULL) {
                            //Ignore if element is child of an visited element for which source is 
                            //already generated
                            CComPtr<IHTMLElement> spParentHTMLElement;
                            spHTMLElement->get_parentElement(&spParentHTMLElement);
                            list<IHTMLElement *>::iterator iter = visitedElements.begin();
                            boolean ignore = false;
                            while(iter != visitedElements.end()) {
                                IHTMLElement *pHTMLElementTemp = *iter;
                                if(pHTMLElementTemp == spParentHTMLElement) {
                                    ignore = true;
                                }
                                ++iter;
                            }
                            IHTMLElement *pHTMLElement = spHTMLElement.Detach();
                            visitedElements.push_front(pHTMLElement);
                            if(!ignore) {
                                CComBSTR bstr;
                                pHTMLElement->get_outerHTML(&bstr);
                                if(bstr != NULL) {
                                    result.append((TCHAR *)(bstr));
                                }
                            }
                        }
                     }
                    list<IHTMLElement *>::iterator iter = visitedElements.begin();
                    while(iter != visitedElements.end()) {
                        IHTMLElement *pHTMLElement = *iter;
                        pHTMLElement->Release();
                        ++iter;
                    }
                }
            }
        }
        Utils::log(1, _T("Source from DOM - %s\n"), fileURI.c_str());
    }
    return result;
}

IWebBrowser2 *SourceCommand::getWebBrowserForFrame(tstring fileURI, IWebBrowser2 *parent) {
    CComPtr<IDispatch> spDisp;
    parent->get_Document(&spDisp);
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
                    spWebBrowser->get_LocationURL(&bstrURL);
                    tstring location = (TCHAR *)(bstrURL);
                    if(location == fileURI) {
                        return spWebBrowser.Detach();
                    }
                }
                spUnk.Release();
            }
        }
    }
    return NULL;
}

DbgpResponse *StdoutCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

DbgpResponse *StderrCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

//EVAL command
//eval -i <tx_id> -e <expression> -d <stack_depth>
//<response command="eval"  success=<0|1> transaction_id="xxx">
//  <property name="" fullname="" type="" numchildren=<0|-1> encoding=""><value>
//      <property name="" fullname="" type="" numchildren=<0|-1> encoding="">"<value>"</property>
//  </property>
//  .
//  .
//</response>
DbgpResponse *EvalCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    ScriptDebugger *pScriptDebugger = pDbgpConnection->getScriptDebugger();
    int depth = _ttoi(argsMap.find('d')->second.c_str());
    Property *pProp = pScriptDebugger->eval(argsMap.find('e')->second, depth);
    PropertyGetResponse *pDbgpResponse = new PropertyGetResponse(EVAL, argsMap.find('i')->second);
    pDbgpResponse->addAttribute(SUCCESS, 1);
    PropertyGetCommand::fillInProperties(pDbgpResponse, pProp);
    return pDbgpResponse;
}

DbgpResponse *BreakCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    return NULL;
}

//OPEN_URI is a one-way command, no response required
//Format: open_uri -i <tx_id> -f <uri> 
DbgpResponse *OpenUriCommand::process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) {
    CComBSTR bstrURL = argsMap.find('f')->second.c_str();
    CComVariant emptyVar;
    CComPtr<IWebBrowser2> spWebBrowser;
    HRESULT hr = Utils::getInterfaceFromGlobal(pDbgpConnection->getWebBrowserCookie(), 
                                                IID_IWebBrowser2, (void **)&spWebBrowser);
    if(hr == S_OK) {
        hr = spWebBrowser->Navigate(bstrURL, &emptyVar, &emptyVar, &emptyVar, &emptyVar);
        if(pDbgpConnection->getScriptDebugger() == NULL) {
            HWND hWnd;
            spWebBrowser->get_HWND(reinterpret_cast<SHANDLE_PTR*>(&hWnd));
            MessageBox(hWnd, _T("Netbeans JavaScript client side debugging is disabled because of not able \
to connect to Internet Explorer Script Debugger.\n\nPlease shutdown mdm.exe by using Windows Task Manager \
and restart the debugging session"), 
            _T("Netbeans Internet Explorer Extension"), MB_OK);
            pDbgpConnection->close();
        }
    }
    return NULL;
}