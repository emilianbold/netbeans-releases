/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;


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
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standart debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
public class StepIntoActionProvider extends JPDADebuggerActionProvider 
implements Executor, PropertyChangeListener {
    
    public static final String SS_STEP_OUT = "SS_ACTION_STEPOUT";
    
    private static final Logger smartLogger = Logger.getLogger("org.netbeans.modules.debugger.jpda.smartstepping"); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N
        
    private StepRequest stepRequest;
    private String position;
    private ContextProvider contextProvider;
    private boolean smartSteppingStepOut;

    public StepIntoActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.contextProvider = contextProvider;
        getSmartSteppingFilterImpl ().addPropertyChangeListener (this);
        SourcePath ec = (SourcePath) contextProvider.
            lookupFirst (null, SourcePath.class);
        ec.addPropertyChangeListener (this);
        Map properties = (Map) contextProvider.lookupFirst (null, Map.class);
        if (properties != null)
            smartSteppingStepOut = properties.containsKey (SS_STEP_OUT);
        setProviderToDisableOnLazyAction(this);
    }


    // ActionProviderSupport ...................................................
    
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
        }));
    }
    
    public void doAction (Object action) {
        runAction();
    }
    
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(new Runnable() {
            public void run() {
                try {
                    runAction();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction() {
        synchronized (getDebuggerImpl ().LOCK) {
            smartLogger.finer("STEP INTO.");
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t == null || !t.isSuspended()) {
                // Can not step when it's not suspended.
                smartLogger.finer("Can not step into! Thread "+t+" not suspended!");
                return ;
            }
            setStepRequest (StepRequest.STEP_INTO);
            position = t.getClassName () + '.' +
                       t.getMethodName () + ':' +
                       t.getLineNumber (null);
            logger.fine("JDI Request (action step into): " + stepRequest);
            try {
                getDebuggerImpl ().resume ();
            } catch (VMDisconnectedException e) {
                ErrorManager.getDefault().notify(ErrorManager.USER,
                    ErrorManager.getDefault().annotate(e,
                        NbBundle.getMessage(StepIntoActionProvider.class,
                            "VMDisconnected")));
            }
        }
    }
    
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName () == SmartSteppingFilter.PROP_EXCLUSION_PATTERNS) {
            if (ev.getOldValue () != null) {
                // remove some patterns
                smartLogger.finer("Exclusion patterns removed. Removing step requests.");
                ThreadReference tr = ((JPDAThreadImpl) getDebuggerImpl ().
                    getCurrentThread ()).getThreadReference ();
                removeStepRequests (tr);
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
        } else {
            super.propertyChange (ev);
        }
    }
    
    
    // Executor ................................................................
    
    /**
     * Executes all step actions and smart stepping. 
     *
     * Should be called from Operator only.
     */
    public boolean exec (Event event) {
        synchronized (getDebuggerImpl ().LOCK) {
            if (stepRequest != null) {
                stepRequest.disable ();
            }
            LocatableEvent le = (LocatableEvent) event;

            ThreadReference tr = le.thread ();

            try {
                if (tr.frame(0).location().method().isSynthetic()) {
                    //S ystem.out.println("In synthetic method -> STEP INTO again");
                    setStepRequest (StepRequest.STEP_INTO);
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
                if (position.equals(stopPosition)) {
                    // We are where we started!
                    stop = false;
                    setStepRequest (StepRequest.STEP_INTO);
                    return true;
                }
            }
            if (stop) {
                removeStepRequests (le.thread ());
                Session session = (Session) contextProvider.lookupFirst(null, Session.class);
                if (session != null) {
                    DebuggerManager.getDebuggerManager().setCurrentSession(session);
                }
                getDebuggerImpl ().setStoppedState (tr);
            } else {
                smartLogger.finer(" => do next step.");
                if (smartSteppingStepOut) {
                    setStepRequest (StepRequest.STEP_OUT);
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
                    setStepRequest (StepRequest.STEP_INTO);
                }
            }

            if (smartLogger.isLoggable(Level.FINER))
                if (stop) {
                    smartLogger.finer("FINISH IN CLASS " +  
                        t.getClassName () + " ********"
                    );
                }
            return !stop;
        }
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
    
    void removeStepRequests (ThreadReference tr) {
        super.removeStepRequests (tr);
        stepRequest = null;
        smartLogger.finer("removing all patterns, all step requests.");
    }
    
    private void setStepRequest (int step) {
        ThreadReference tr = ((JPDAThreadImpl) getDebuggerImpl ().
            getCurrentThread ()).getThreadReference ();
        removeStepRequests (tr);
        VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
        if (vm == null) return ;
        stepRequest = vm.eventRequestManager ().createStepRequest (
            tr,
            StepRequest.STEP_LINE,
            step
        );

        getDebuggerImpl ().getOperator ().register (stepRequest, this);
        stepRequest.setSuspendPolicy (getDebuggerImpl ().getSuspend ());
        
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
            return ;
        }
    }

    private SmartSteppingFilter smartSteppingFilter;
    
    private SmartSteppingFilter getSmartSteppingFilterImpl () {
        if (smartSteppingFilter == null)
            smartSteppingFilter = (SmartSteppingFilter) contextProvider.
                lookupFirst (null, SmartSteppingFilter.class);
        return smartSteppingFilter;
    }

    private CompoundSmartSteppingListener compoundSmartSteppingListener;
    
    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = (CompoundSmartSteppingListener) 
                contextProvider.lookupFirst (null, CompoundSmartSteppingListener.class);
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
