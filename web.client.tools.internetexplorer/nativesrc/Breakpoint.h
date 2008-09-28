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
#include <map>
#include <list>
#include "ScriptDebugger.h"

using namespace std;

enum HitFilter{
    EQUAL, GREATER_OR_EQUAL, MULTIPLE
};

static const tstring HIT_FILTER_EQUAL =                 _T("==");
static const tstring HIT_FILTER_GREATER_OR_EQUAL =      _T(">=");
static const tstring HIT_FILTER_MULTIPLE =              _T("%");

/*
 * Breakpoint class represents a JavaScript breakpoint. Each breakpoint is given a 
 * unique ID, which is required for updation and removal.
 */
class Breakpoint {
public:
    Breakpoint(tstring fileURI, int lineNo);

    void setExpression(tstring expression){
        this->expression = expression;
    }

    void setHitValue(int hitValue){
        this->hitValue = hitValue;
    }

    int getHitValue(){
        return hitValue;
    }

    void setHitFilter(HitFilter hitFilter) {
        this->hitFilter = hitFilter;
    }

    void setID(tstring ID) {
        this->ID = ID;
    }

    tstring getID() {
        return ID;
    }

    void setState(BOOL state) {
        this->state = state;
    }

    void setTemporary(BOOL temporary) {
        this->temporary = temporary;
    }

    BOOL isTemporary() {
        return temporary;
    }

    void incrementHitCount() {
        hitCount++;
    }

    void setHitCount(int hitCount){
        this->hitCount = hitCount;
    }

    int getHitCount(){
        return hitCount;
    }

    int getLineNumber(){
        return lineNo;
    }

   void setLineNumber(int line){
        this->lineNo = line;
    }

    tstring getFileURI(){
        return fileURI;
    }

    BOOL getState() {
        return state;
    }

    tstring getExpression(){
        return expression;
    }

    BOOL isExpressionSet() {
        return expression.length() > 0;
    }

    BOOL isValidHit();

    static DWORD WINAPI handle(LPVOID param);

private:
    int lineNo;
    tstring fileURI;
    tstring expression;
    int hitCount;
    int hitValue;
    HitFilter hitFilter;
    tstring ID;
    BOOL state;
    BOOL temporary;
};

class ScriptDebugger;


/* Breakpoint manager is responsible for creation and removal of breakpoint objects.
 * It delegates to script debugger to do the action. If script debugger fails to do
 * the action(for example when the document is not yet loaded), breakpoint manager 
 * adds/removes into an internal list and this list is processed when breakpoint 
 * manager is requested by script debugger to process the breakpoints for a given file
 *
 * The internal map fileToBreakpointsMap is used to find a breakpoint given a file 
 * name, this is mainly used to lookup breakpoint instance given a file name and
 * line number. This is useful when script debugger wants to decide to suspend or
 * continue. Another map idToBreakpointMap is used to lookup a breakpoint given
 * the breakpoint ID. This is used when we receive update/removal command from IDE
 */
class BreakpointManager {
public:
    BreakpointManager(ScriptDebugger *pScriptDebugger) {
        m_pScriptDebugger = pScriptDebugger;
        ID = 0;
    }
    ~BreakpointManager();
    Breakpoint *createBreakpoint(tstring fileURI, int lineNo);
    Breakpoint *findMatchingBreakpoint(tstring fileURI, int lineNo);
    void removeAllBreakpoints();
    void processBreakpoints(tstring fileURI);
    BOOL setBreakpoint(Breakpoint *pBreakpoint);
    BOOL removeBreakpoint(tstring id);
    Breakpoint *getUpdatableBreakpoint(tstring id);

private:
    map<tstring, list<Breakpoint *> *> fileToBreakpointsMap;
    map<tstring, Breakpoint *> idToBreakpointMap;
    unsigned int ID;
    ScriptDebugger *m_pScriptDebugger;
    void addToMaps(Breakpoint *pBreakpoint);
    void removeFromMaps(Breakpoint *pBreakpoint);
};