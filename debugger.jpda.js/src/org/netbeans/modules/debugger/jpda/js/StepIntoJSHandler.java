/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 * Handler of step into JavaScript code from Java.
 * 
 * @author Martin
 */
@LazyActionsManagerListener.Registration(path="netbeans-JPDASession/Java")
public class StepIntoJSHandler extends LazyActionsManagerListener implements PropertyChangeListener {
    
    private final JPDADebugger debugger;
    private final ClassLoadUnloadBreakpoint scriptBP;
    
    public StepIntoJSHandler(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_THREAD, new CurrentThreadTracker());
        scriptBP = ClassLoadUnloadBreakpoint.create(JSUtils.NASHORN_SCRIPT+"*",
                                                    false,
                                                    ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
        scriptBP.setHidden(true);
        scriptBP.setSuspend(EventRequest.SUSPEND_ALL);
        scriptBP.disable();
        scriptBP.addJPDABreakpointListener(new ScriptBPListener());
        DebuggerManager.getDebuggerManager().addBreakpoint(scriptBP);
    }

    @Override
    protected void destroy() {
        scriptBP.disable();
        DebuggerManager.getDebuggerManager().removeBreakpoint(scriptBP);
    }

    @Override
    public String[] getProperties() {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED, "actionToBeRun" };
    }

    @Override
    public void actionPerformed(Object action) {
        if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
            //scriptBP.disable(); - no, the action may end too soon, some work
            //                      can continue on background
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("actionToBeRun".equals(evt.getPropertyName())) {
            Object action = evt.getNewValue();
            if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
                scriptBP.enable();
            }
        }
    }
    
    private class CurrentThreadTracker implements PropertyChangeListener {
        
        private JPDAThread currentThread;
        private final Object currentThreadLock = new Object();
        
        public CurrentThreadTracker() {
            currentThread = debugger.getCurrentThread();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (JPDADebugger.PROP_CURRENT_THREAD.equals(propertyName)) {
                synchronized (currentThreadLock) {
                    if (currentThread != null) {
                        ((Customizer) currentThread).removePropertyChangeListener(this);
                    }
                    currentThread = debugger.getCurrentThread();
                    if (currentThread != null) {
                        ((Customizer) currentThread).addPropertyChangeListener(this);
                    }
                    scriptBP.disable();
                }
            /*} else if (JPDAThread.PROP_SUSPENDED.equals(propertyName)) {
                if (Boolean.FALSE.equals(evt.getNewValue())) {
                    // The current thread is just going to be resumed
                    JPDAThread t = (JPDAThread) evt.getSource();
                }
            }*
            } else if ("inStep".equals(propertyName)) {
                StepRequest sr = (StepRequest) evt.getNewValue();
                if (sr != null) {
                    if (StepRequest.STEP_INTO == sr.depth()) {
                        scriptBP.enable();
                    }
                } else {
                    //scriptBP.disable();
                }
            */
            }
        }
        
    }
    
    private class ScriptBPListener implements JPDABreakpointListener {

        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            if (!scriptBP.isEnabled()) {
                return ;
            }
            //scriptBP.disable();
            ReferenceType referenceType = event.getReferenceType();
            /*
            final MethodBreakpoint anyMethodBP = MethodBreakpoint.create(referenceType.name(), "");
            anyMethodBP.setHidden(true);
            DebuggerManager.getDebuggerManager().addBreakpoint(anyMethodBP);
            anyMethodBP.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(anyMethodBP);
            }
            });
             */
            try {
                List<Location> lineLocations = referenceType.allLineLocations(JSUtils.JS_STRATUM, null);
                if (!lineLocations.isEmpty()) {
                    Location l = lineLocations.get(0);
                    //final LineBreakpoint lineBP = LineBreakpoint.create(, l.lineNumber(JSUtils.JS_STRATUM));
                    BreakpointRequest br = l.virtualMachine().eventRequestManager().createBreakpointRequest(l);
                    br.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                    br.addCountFilter(1);
                    br.enable();
                }
            } catch (AbsentInformationException aiex) {}
            event.resume();
        }
        
    }
    
}
