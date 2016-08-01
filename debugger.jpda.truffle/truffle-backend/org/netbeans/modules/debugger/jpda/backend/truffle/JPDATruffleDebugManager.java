/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import com.oracle.truffle.api.debug.SuspendedCallback;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 *
 * @author martin
 */
class JPDATruffleDebugManager implements SuspendedCallback {
    
    private final Reference<Debugger> debugger;
    private final Reference<DebuggerSession> session;
    //private volatile boolean prepareStepInto;
    private volatile SuspendedEvent currentSuspendedEvent;

    public JPDATruffleDebugManager(Debugger debugger, boolean doStepInto) {
        this.debugger = new WeakReference<>(debugger);
        DebuggerSession debuggerSession = debugger.startSession(this);
        if (doStepInto) {
            debuggerSession.suspendNextExecution();
        }
        this.session = new WeakReference<>(debuggerSession);
    }
    
    /*static JPDATruffleDebugManager setUp(Debugger debugger) {
        //System.err.println("JPDATruffleDebugManager.setUp()");
        JPDATruffleDebugManager debugManager = new JPDATruffleDebugManager(debugger);
        //System.err.println("SET UP of JPDATruffleDebugManager = "+debugManager+" for "+engine+" and prober to "+jsContext);
        return debugManager;
    }*/
    
    Debugger getDebugger() {
        return debugger.get();
    }
    
    DebuggerSession getDebuggerSession() {
        return session.get();
    }
    
    SuspendedEvent getCurrentSuspendedEvent() {
        return currentSuspendedEvent;
    }
    
    static SourcePosition getPosition(SourceSection sourceSection) {
        /*
        SourceSection sourceSection = node.getSourceSection();
        if (sourceSection == null) {
            sourceSection = node.getEncapsulatingSourceSection();
        }
        if (sourceSection == null) {
            System.err.println("Node without sourceSection! node = "+node+", of class: "+node.getClass());
            throw new IllegalStateException("Node without sourceSection! node = "+node+", of class: "+node.getClass());
        }*/
        int line = sourceSection.getStartLine();
        Source source = sourceSection.getSource();
        //System.err.println("source of "+node+" = "+source);
        //System.err.println("  name = "+source.getName());
        //System.err.println("  short name = "+source.getShortName());
        //System.err.println("  path = "+source.getPath());
        //System.err.println("  code at line = "+source.getCode(line));
        String name = source.getName();
        String path = source.getPath();
        if (path == null) {
            path = name;
        }
        String code = source.getCode();
        return new SourcePosition(source, name, path, line, code);
    }

    void dispose() {
        /*
        endExecution();
         */
        DebuggerSession ds = session.get();
        if (ds != null) {
            ds.close();
            session.clear();
        }
    }
    
    /*
    void setExecutionEvent(ExecutionEvent execEvent) {
        //this.execEvent = execEvent;
        if (prepareStepInto) {
            execEvent.prepareStepInto();
            prepareStepInto = false;
        }
    }*/

    void prepareExecStepInto() {
        //System.err.println("prepareExecStepInto()...");
        session.get().suspendNextExecution();
        //prepareStepInto = true;
        // Do not call methods on ExecutionEvent asynchronously.
        /* Rely on another ExecutionEvent comes when needed
        try {
            execEvent.prepareStepInto();
        } catch (RuntimeException rex) {
            // Unable to use the event any more. A new should come when needed.
            // Report until there is some known contract:
            System.err.println("Ignoring prepareStepInto():");
            rex.printStackTrace();
        }
        */
        //System.err.println("prepareExecStepInto() DONE.");
    }

    void prepareExecContinue() {
        //System.err.println("prepareExecContinue()...");
        // TODO: HOW?
        //prepareStepInto = false;
        // Do not call methods on ExecutionEvent asynchronously.
        /* Rely on another ExecutionEvent comes when needed
        try {
            execEvent.prepareContinue();
        } catch (RuntimeException rex) {
            // Unable to use the event any more. A new should come when needed.
            // Report until there is some known contract:
            System.err.println("Ignoring prepareExecContinue():");
            rex.printStackTrace();
        }
        */
        //System.err.println("prepareExecContinue() DONE.");
    }

    @Override
    public void onSuspend(SuspendedEvent event) {
        System.err.println("JPDATruffleDebugManager.onSuspend("+event+")");
        Breakpoint[] breakpointsHit = new Breakpoint[event.getBreakpoints().size()];
        breakpointsHit = event.getBreakpoints().toArray(breakpointsHit);
        Throwable[] breakpointConditionExceptions = new Throwable[breakpointsHit.length];
        for (int i = 0; i < breakpointsHit.length; i++) {
            breakpointConditionExceptions[i] = event.getBreakpointConditionException(breakpointsHit[i]);
        }
        currentSuspendedEvent = event;
        try {
            SourcePosition position = getPosition(event.getSourceSection());
            int stepCmd = JPDATruffleAccessor.executionHalted(
                    this, position,
                    event.isHaltedBefore(),
                    event.getReturnValue(),
                    new FrameInfo(event.getTopStackFrame(), event.getStackFrames()),
                    breakpointsHit,
                    breakpointConditionExceptions,
                    0);
            switch (stepCmd) {
                case 0: event.prepareContinue();
                        break;
                case 1: event.prepareStepInto(1);
                        break;
                case 2: event.prepareStepOver(1);
                        break;
                case 3: event.prepareStepOut();
                        break;
                default:
                        throw new IllegalStateException("Unknown step command: "+stepCmd);
            }
        } finally {
            currentSuspendedEvent = null;
        }
    }
    
}
