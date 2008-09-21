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
#include "Breakpoint.h"

Breakpoint::Breakpoint(tstring fileURI, int lineNo) {
    this->fileURI = fileURI;
    this->lineNo = lineNo;
    hitValue = 0;
    hitCount = 0;
    hitFilter = EQUAL;
    state = FALSE;
    temporary = FALSE;
}

BOOL Breakpoint::isValidHit() {
    if(hitValue > 0) {
        ++hitCount;
        switch(hitFilter) {
            case EQUAL:
                return hitCount == hitValue;
            case GREATER_OR_EQUAL:
                return hitCount >= hitValue;
            case MULTIPLE:
                return (hitCount % hitValue) == 0;
        }
    }
    return TRUE;
}

DWORD WINAPI Breakpoint::handle(LPVOID param) {
    ScriptDebugger *pScriptDebugger = (ScriptDebugger *)param;
    Breakpoint *pBreakpoint = pScriptDebugger->getCurrentBreakpoint();
    if(pBreakpoint != NULL) {
        BOOL needToBreak = TRUE;
        if(pBreakpoint->isExpressionSet()) {
            tstring result = pScriptDebugger->evalToString(pBreakpoint->getExpression(), 0);
            if(result != _T("true")) {
                needToBreak = FALSE;
            }
        }
        if(needToBreak && pBreakpoint->isValidHit()) {
            pScriptDebugger->changeState(STATE_BREAKPOINT, OK);
            if(pBreakpoint->isTemporary()) {
                pScriptDebugger->removeBreakpoint(pBreakpoint);
            }
        }else {
            pScriptDebugger->run();
        }
    }
    return 0;
}


BreakpointManager::~BreakpointManager() {
    removeAllBreakpoints();
}

Breakpoint *BreakpointManager::createBreakpoint(tstring fileURI, int lineNo) {
    Breakpoint *pBreakpoint = new Breakpoint(fileURI, lineNo);
    int id = ID++;
    TCHAR buffer[32];
    _itot_s(id, buffer, 32, 10);
    tstring stringID = fileURI;
    stringID.append(_T(":"));
    stringID.append(buffer);
    pBreakpoint->setID(stringID);
    
    return pBreakpoint;
}

Breakpoint *BreakpointManager::findMatchingBreakpoint(tstring fileURI, int lineNo) {
    size_t queryPos = fileURI.find(_T("?"));
    if (queryPos != string::npos) {
        if(m_pScriptDebugger->isFeatureSet(IGNORE_QUERY_STRINGS)) {
            fileURI = fileURI.substr(0, queryPos);
        }
    }
    map<tstring, list<Breakpoint *> *>::iterator iter = fileToBreakpointsMap.find(fileURI);
    if(iter != fileToBreakpointsMap.end()) {
        list<Breakpoint *> *pList = iter->second;
        list<Breakpoint *>::iterator bpIter = pList->begin();
        while(bpIter != pList->end()) {
            if((*bpIter)->getLineNumber() == lineNo) {
                return *bpIter;
            }
            ++bpIter;
        }
    }
    return NULL;
}

Breakpoint *BreakpointManager::getUpdatableBreakpoint(tstring id) {
    map<tstring, Breakpoint *>::iterator iter = idToBreakpointMap.find(id);
    if(iter != idToBreakpointMap.end()) {
        //If already set in the engine, unset it, but do not delete in
        //our internal structures
        m_pScriptDebugger->removeBreakpoint(iter->second);
        return iter->second;
    }
    return NULL;
}

void BreakpointManager::removeAllBreakpoints() {
    map<tstring, Breakpoint *>::iterator idIter = idToBreakpointMap.begin();
    while(idIter != idToBreakpointMap.end()) {
        m_pScriptDebugger->removeBreakpoint(idIter->second);
        ++idIter;
    }
    fileToBreakpointsMap.clear();
    idToBreakpointMap.clear();
}

void BreakpointManager::processBreakpoints(tstring fileURI) {
    tstring origFileURI = fileURI;
    size_t queryPos = origFileURI.find(_T("?"));
    if (queryPos != string::npos) {
        if(m_pScriptDebugger->isFeatureSet(IGNORE_QUERY_STRINGS)) {
            fileURI = origFileURI.substr(0, queryPos);
        }
    }
    map<tstring, list<Breakpoint *> *>::iterator iter = fileToBreakpointsMap.find(fileURI);
    if(iter != fileToBreakpointsMap.end()) {
        list<Breakpoint *> *pList = iter->second;
        list<Breakpoint *>::iterator bpIter = pList->begin();
        while(bpIter != pList->end()) {
            m_pScriptDebugger->setBreakpoint(*bpIter, origFileURI);
            ++bpIter;
        }
    }
}

void BreakpointManager::addToMaps(Breakpoint *pBreakpoint) {
    tstring fileURI = pBreakpoint->getFileURI();
    //Add into id->Breakpoint map
    idToBreakpointMap.insert(pair<tstring, Breakpoint *>(pBreakpoint->getID(), pBreakpoint));

    //Add into file->list<Breakpoint> map
    map<tstring, list<Breakpoint *> *>::iterator iter = fileToBreakpointsMap.find(fileURI);
    if(iter == fileToBreakpointsMap.end()) {
        list<Breakpoint *> *pList = new list<Breakpoint *>();
        pList->push_back(pBreakpoint);
        fileToBreakpointsMap.insert(pair<tstring, list<Breakpoint *> *>(fileURI, pList));
    }else {
        iter->second->push_back(pBreakpoint);
    }
}

void BreakpointManager::removeFromMaps(Breakpoint *pBreakpoint) {
    tstring fileURI = pBreakpoint->getFileURI();
    //Remove from id->Breakpoint map
    map<tstring, Breakpoint *>::iterator idIter = idToBreakpointMap.find(pBreakpoint->getID());
    if(idIter != idToBreakpointMap.end()) {
        idToBreakpointMap.erase(idIter);
    }

    //Remove from file->list<Breakpoint> map
    map<tstring, list<Breakpoint *> *>::iterator fileIter = fileToBreakpointsMap.find(fileURI);
    if(fileIter != fileToBreakpointsMap.end()) {
        fileIter->second->remove(pBreakpoint);
    }

    delete pBreakpoint;
}

BOOL BreakpointManager::setBreakpoint(Breakpoint *pBreakpoint) {
    BOOL result = m_pScriptDebugger->setBreakpoint(pBreakpoint);
    addToMaps(pBreakpoint);
    return result;
}

BOOL BreakpointManager::removeBreakpoint(tstring id) {
    BOOL result = FALSE;
    map<tstring, Breakpoint *>::iterator iter = idToBreakpointMap.find(id);
    if(iter != idToBreakpointMap.end()) {
        Breakpoint *pBreakpoint = iter->second;
        result = m_pScriptDebugger->removeBreakpoint(pBreakpoint);
        removeFromMaps(pBreakpoint);
    }
    return result;
}