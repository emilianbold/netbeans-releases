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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Extracted from StepIntoActionProvider and StepIntoNextMethodActionProvider
 *
 * @author Martin Entlicher
 */
public class StepIntoNextMethod implements Executor, PropertyChangeListener {

    private static final Logger smartLogger = Logger.getLogger("org.netbeans.modules.debugger.jpda.smartstepping"); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N

    private StepRequest stepRequest;
    private String position;
    private int depth;
    private JPDADebuggerImpl debugger;
    private ContextProvider contextProvider;
    private boolean smartSteppingStepOut;

    public StepIntoNextMethod(ContextProvider contextProvider) {
        this.debugger = (JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class);
        this.contextProvider = contextProvider;
        getSmartSteppingFilterImpl ().addPropertyChangeListener (this);
        SourcePath ec = contextProvider.lookupFirst(null, SourcePath.class);
        ec.addPropertyChangeListener (this);
        Map properties = contextProvider.lookupFirst(null, Map.class);
        if (properties != null)
            smartSteppingStepOut = properties.containsKey (StepIntoActionProvider.SS_STEP_OUT);
    }

    private final JPDADebuggerImpl getDebuggerImpl() {
        return debugger;
    }

    public void runAction() {
        synchronized (getDebuggerImpl ().LOCK) {
            smartLogger.finer("STEP INTO NEXT METHOD.");
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t == null || !t.isSuspended()) {
                // Can not step when it's not suspended.
                smartLogger.finer("Can not step into next method! Thread "+t+" not suspended!");
                return ;
            }
            JPDAThread resumeThread = setStepRequest (StepRequest.STEP_INTO);
            position = t.getClassName () + '.' +
                       t.getMethodName () + ':' +
                       t.getLineNumber (null);
            depth = t.getStackDepth();
            logger.fine("JDI Request (action step into next method): " + stepRequest);
            if (stepRequest == null) return ;
            ((JPDAThreadImpl) t).setInStep(true, stepRequest);
            try {
                if (resumeThread == null) {
                    getDebuggerImpl ().resume ();
                } else {
                    //resumeThread.resume();
                    //stepWatch = new SingleThreadedStepWatch(getDebuggerImpl(), stepRequest);
                    getDebuggerImpl().resumeCurrentThread();
                }
            } catch (VMDisconnectedException e) {
                ErrorManager.getDefault().notify(ErrorManager.USER,
                    ErrorManager.getDefault().annotate(e,
                        NbBundle.getMessage(StepIntoNextMethodActionProvider.class,
                            "VMDisconnected")));
            }
        }
    }

    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName () == SmartSteppingFilter.PROP_EXCLUSION_PATTERNS) {
            if (ev.getOldValue () != null) {
                // remove some patterns
                smartLogger.finer("Exclusion patterns removed. Removing step requests.");
                JPDAThreadImpl currentThread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
                if (currentThread != null) {
                    ThreadReference tr = currentThread.getThreadReference ();
                    removeStepRequests (tr);
                }
            } else {
                if (smartLogger.isLoggable(Level.FINER)) {
                    if (stepRequest == null)
                        smartLogger.finer("Exclusion patterns has been added");
                    else
                        smartLogger.finer("Add exclusion patterns: "+ev.getNewValue());
                }
                addPatternsToRequest ((String[])
                    ((Set<String>) ev.getNewValue ()).toArray (
                        new String [((Set) ev.getNewValue ()).size()]
                    )
                );
            }
        } else
        if (ev.getPropertyName () == SourcePathProvider.PROP_SOURCE_ROOTS) {
            smartLogger.finer("Source roots changed");
            JPDAThreadImpl jtr = (JPDAThreadImpl) getDebuggerImpl ().
                getCurrentThread ();
            if (jtr != null) {
                ThreadReference tr = jtr.getThreadReference ();
                removeStepRequests (tr);
            }
        }
    }


    // Executor ................................................................

    /**
     * Executes all step actions and smart stepping.
     *
     * Should be called from Operator only.
     */
    public boolean exec (Event event) {
        StepRequest sr = (StepRequest) event.request();
        JPDAThreadImpl st = (JPDAThreadImpl) getDebuggerImpl().getThread(sr.thread());
        st.setInStep(false, null);
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
        JPDAThread resumeThread = null;
        synchronized (getDebuggerImpl ().LOCK) {
            if (stepRequest != null) {
                stepRequest.disable ();
            }
            LocatableEvent le = (LocatableEvent) event;

            ThreadReference tr = le.thread ();

            try {
                if (tr.frame(0).location().method().isSynthetic()) {
                    //S ystem.out.println("In synthetic method -> STEP INTO again");
                    resumeThread = setStepRequest (StepRequest.STEP_INTO);
                    return true;
                }
            } catch (IncompatibleThreadStateException e) {
                //ErrorManager.getDefault().notify(e);
                // This may happen while debugging a free form project
            }

            JPDAThread t = getDebuggerImpl ().getThread (tr);
            boolean stop = getCompoundSmartSteppingListener ().stopHere
                               (contextProvider, t, getSmartSteppingFilterImpl ());
            if (stop) {
                String stopPosition = t.getClassName () + '.' +
                                      t.getMethodName () + ':' +
                                      t.getLineNumber (null);
                int stopDepth = t.getStackDepth();
                if (position.equals(stopPosition) && depth == stopDepth) {
                    // We are where we started!
                    stop = false;
                    resumeThread = setStepRequest (StepRequest.STEP_INTO);
                    return true;//resumeThread == null;
                }
            }
            if (stop) {
                removeStepRequests (le.thread ());
            } else {
                smartLogger.finer(" => do next step.");
                if (smartSteppingStepOut) {
                    resumeThread = setStepRequest (StepRequest.STEP_OUT);
                } else if (stepRequest != null) {
                    try {
                        stepRequest.enable ();
                    } catch (IllegalThreadStateException itsex) {
                        // the thread named in the request has died.
                        getDebuggerImpl ().getOperator ().unregister(stepRequest);
                        stepRequest = null;
                        return true;
                    }
                } else {
                    resumeThread = setStepRequest (StepRequest.STEP_INTO);
                }
            }

            if (stop) {
                if (smartLogger.isLoggable(Level.FINER))
                    smartLogger.finer("FINISH IN CLASS " +
                        t.getClassName () + " ********"
                    );
                StepActionProvider.setLastOperation(tr, debugger, null);
            }
            return !stop;
        }
    }

    public void removed(EventRequest eventRequest) {
        StepRequest sr = (StepRequest) eventRequest;
        JPDAThreadImpl st = (JPDAThreadImpl) getDebuggerImpl().getThread(sr.thread());
        st.setInStep(false, null);
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
    }


    private StepActionProvider stepActionProvider;

    private StepActionProvider getStepActionProvider () {
        if (stepActionProvider == null) {
            List l = contextProvider.lookup (null, ActionsProvider.class);
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (l.get (i) instanceof StepActionProvider)
                    stepActionProvider = (StepActionProvider) l.get (i);
        }
        return stepActionProvider;
    }

    // other methods ...........................................................

    private void removeStepRequests (ThreadReference tr) {
        JPDADebuggerActionProvider.removeStepRequests (getDebuggerImpl(), tr);
        stepRequest = null;
        smartLogger.finer("removing all patterns, all step requests.");
    }

    private JPDAThreadImpl setStepRequest (int step) {
        JPDAThreadImpl thread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
        ThreadReference tr = thread.getThreadReference ();
        removeStepRequests (tr);
        VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
        if (vm == null) return null;
        stepRequest = vm.eventRequestManager ().createStepRequest (
            tr,
            StepRequest.STEP_LINE,
            step
        );

        getDebuggerImpl ().getOperator ().register (stepRequest, this);
        int suspendPolicy = getDebuggerImpl().getSuspend();
        stepRequest.setSuspendPolicy (suspendPolicy);

        if (smartLogger.isLoggable(Level.FINER)) {
            smartLogger.finer("Set step request("+step+") and patterns: ");
        }
        addPatternsToRequest (
            getSmartSteppingFilterImpl ().getExclusionPatterns ()
        );
        try {
            stepRequest.enable ();
        } catch (IllegalThreadStateException itsex) {
            // the thread named in the request has died.
            getDebuggerImpl ().getOperator ().unregister(stepRequest);
            stepRequest = null;
            return null;
        }
        if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
            return thread;
        } else {
            return null;
        }
    }

    private SmartSteppingFilter smartSteppingFilter;

    private SmartSteppingFilter getSmartSteppingFilterImpl () {
        if (smartSteppingFilter == null)
            smartSteppingFilter = contextProvider.lookupFirst(null, SmartSteppingFilter.class);
        return smartSteppingFilter;
    }

    private CompoundSmartSteppingListener compoundSmartSteppingListener;

    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = contextProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }

    private void addPatternsToRequest (String[] patterns) {
        if (stepRequest == null) return;
        int i, k = patterns.length;
        for (i = 0; i < k; i++) {
            stepRequest.addClassExclusionFilter (patterns [i]);
            smartLogger.finer("   add pattern: "+patterns[i]);
        }
    }

}
