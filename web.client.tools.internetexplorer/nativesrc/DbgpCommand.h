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
#include <string>
#include <exdisp.h>
#include "DbgpResponse.h"
#include "ScriptDebugger.h"

using namespace std;

//static const tstring STATUS =            _T("status");
static const tstring FEATURE_GET =       _T("feature_get");
static const tstring FEATURE_SET =       _T("feature_set");
static const tstring RUN =               _T("run");
static const tstring STEP_INTO =         _T("step_into");
static const tstring STEP_OVER =         _T("step_over");
static const tstring STEP_OUT =          _T("step_out");
static const tstring STOP =              _T("stop");
static const tstring DETACH =            _T("detach");
static const tstring PAUSE =             _T("pause");
static const tstring BREAKPOINT_SET =    _T("breakpoint_set");
static const tstring BREAKPOINT_GET =    _T("breakpoint_get");
static const tstring BREAKPOINT_UPDATE = _T("breakpoint_update");
static const tstring BREAKPOINT_REMOVE = _T("breakpoint_remove");
static const tstring BREAKPOINT_LIST =   _T("breakpoint_list");
static const tstring STACK_DEPTH =       _T("stack_depth");
static const tstring STACK_GET =         _T("stack_get");
static const tstring CONTEXT_NAMES =     _T("context_names");
static const tstring CONTEXT_GET =       _T("context_get");
static const tstring TYPEMAP_GET =       _T("typemap_get");
static const tstring PROPERTY_GET =      _T("property_get");
static const tstring PROPERTY_SET =      _T("property_set");
static const tstring PROPERTY_VALUE =    _T("property_value");
static const tstring SOURCE =            _T("source");
static const tstring STDOUT =            _T("stdout");
static const tstring STDERR =            _T("stderr");
static const tstring EVAL =              _T("eval");
static const tstring BREAK =             _T("break");
static const tstring OPEN_URI =          _T("open_uri");
static const tstring SOURCE_GET =        _T("source_get");
static const tstring WINDOW_GET =        _T("window_get");

/*
 * DbgpCommand is the base class for the classes responsible for processing the DBGP commands from IDE. 
 * It usually delegates to ScriptDebugger for appropriate action. 
 *
 * The process() method should be over-riden by the derived classes. The argument switch and values are
 * passed in as a map to this method.
 */

class DbgpCommand {
public:
    static void initializeMap();
    virtual DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap) = 0;
    virtual BOOL needsResponse() {
        return TRUE;
    }
    static map<tstring, DbgpCommand *> commandResponseMap;
};

/*
 * OneWayCommand is the base class for the command classes which do not need response back to IDE
 */
class OneWayCommand : public DbgpCommand {
    BOOL needsResponse() {
        return FALSE;
    }
};

typedef map<tstring, DbgpCommand *>::iterator CommandResponseIterator;

class StatusCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class FeatureGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class FeatureSetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class RunCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StepIntoCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StepOverCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StepOutCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StopCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class PauseCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class DetachCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class BreakpointSetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class BreakpointGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class BreakpointUpdateCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
public:
    static void setUpdatableValues(Breakpoint *pBreakpoint, map<char, tstring> argsMap);
};

class BreakpointRemoveCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class BreakpointListCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StackDepthCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StackGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class ContextNamesCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class ContextGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class TypemapGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class PropertyGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
public:
    static DbgpResponse *fillInProperties(PropertyGetResponse *pDbgpResponse, Property *pProp);
};

class PropertySetCommand : public DbgpCommand {
public:
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class PropertyValueCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class SourceCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
    tstring getDOMText(DbgpConnection *pDbgpConnection, tstring fileURI);
    IWebBrowser2 *getWebBrowserForFrame(tstring fileURI, IWebBrowser2 *parent);
};

class StdoutCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class StderrCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class EvalCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class BreakCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class OpenUriCommand : public OneWayCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class SourceGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};

class WindowGetCommand : public DbgpCommand {
    DbgpResponse *process(DbgpConnection *pDbgpConnection, map<char, tstring> argsMap);
};
