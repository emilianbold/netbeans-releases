/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SmartSteppingListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.ThreadsTreeModel;
import org.netbeans.modules.debugger.jpda.util.Executor;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standart debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
public class StepActionProvider extends JPDADebuggerActionProvider 
implements Executor {
    
    
    private SmartSteppingListener   ssListener;
    private SSManager               ssManager;
    private StepRequest             stepRequest;
    private String                  position;
    private LookupProvider          lookupProvider;

    
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;


    private static int getJDIAction (Object action) {
        if (action == DebuggerManager.ACTION_STEP_INTO) 
            return StepRequest.STEP_INTO;
        if (action == DebuggerManager.ACTION_STEP_OUT) 
            return StepRequest.STEP_OUT;
        if (action == DebuggerManager.ACTION_STEP_OVER) 
            return StepRequest.STEP_OVER;
        throw new IllegalArgumentException ();
    }
    
    
    public StepActionProvider (LookupProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        
        // init smart stepping filter
        ssManager = new SSManager ();
        ssListener = new CompoundSmartSteppingListener (lookupProvider);
        ssListener.initFilter (ssManager);
    }


    // ActionProviderSupport ...................................................
    
    public Set getActions () {
        return new HashSet (Arrays.asList (new Object[] {
            DebuggerManager.ACTION_STEP_INTO,
            DebuggerManager.ACTION_STEP_OUT,
            DebuggerManager.ACTION_STEP_OVER
        }));
    }
    
    public void doAction (Object action) {
        synchronized (getDebuggerImpl ().LOCK) {
            //S ystem.err.println("StepAction.doAction");
            if (stepRequest != null) {
                removeRequest (stepRequest);
                stepRequest = null;
            }
            try {
                ThreadReference tr = ((JPDAThreadImpl) getDebuggerImpl ().
                    getCurrentThread ()).getThreadReference ();
                StackFrame sf = tr.frame (0);
                Location l = sf.location ();
                position = l.declaringType ().name () + ":" + 
                           l.lineNumber (null);
                
                // 1) create step request
                stepRequest = getDebuggerImpl ().getVirtualMachine ().
                    eventRequestManager ().createStepRequest (
                        tr,
                        StepRequest.STEP_LINE,
                        getJDIAction (action)
                    );
                stepRequest.addCountFilter (1);
                getDebuggerImpl ().getOperator ().register (stepRequest, this);
                stepRequest.setSuspendPolicy (getDebuggerImpl ().getSuspend ());
                stepRequest.enable ();
                
                // 2) resume JVM
                getDebuggerImpl ().resume ();
            } catch (IncompatibleThreadStateException e) {
                e.printStackTrace ();
            } catch (VMDisconnectedException e) {
            }
            //S ystem.err.println("StepAction.doAction end");
        }
    }
    
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                debuggerState == getDebuggerImpl ().STATE_STOPPED
            );
    }

    public SmartSteppingFilter getSmartSteppingFilter () {
        return ssManager;
    }
    
    
    // Executor ................................................................
    
    /**
     * Executes all step actions and smart stepping. 
     *
     * Should be called from Operator only.
     */
    public boolean exec (Event ev) {
        // TODO: fetch current engine from the Event
        synchronized (getDebuggerImpl ().LOCK) {
            // 1) remove old request
            if (stepRequest != null) {
                removeRequest (stepRequest);
                stepRequest = null;
            }
            
            // 2) init info about current state
            LocatableEvent event = (LocatableEvent) ev;
            String className = event.location ().declaringType ().name ();
            ThreadReference tr = event.thread ();
            JPDAThread t = getThread (tr);
            
            // 3) stop execution here?
            boolean fsh = ssManager.stopHere (className);
            if (ssverbose)
                System.out.println("SS  SmartSteppingFilter.stopHere (" + 
                    className + ") ? " + fsh
                );
            if ( fsh &&
                 ssListener.stopHere (lookupProvider, t, ssManager)
            ) {
                // YES!
                getDebuggerImpl ().setStoppedState (tr);
                return false; // do not resume
            }

            // do not stop here -> start smart stepping!
            if (ssverbose)
                System.out.println("\nSS:  SMART STEPPING START! ********** ");
            boolean stepInto = ((StepRequest) ev.request ()).depth () == 
                            StepRequest.STEP_INTO;
            ssManager.start (tr, stepInto, position);
            return true; // resume

        }
    }
    
    
    // helper private methods ..................................................
   
    /**
    * Removes last step request.
    */
    private void removeRequest (EventRequest request) {
        try {
            getDebuggerImpl ().getVirtualMachine ().
                eventRequestManager ().deleteEventRequest (request);
            getDebuggerImpl ().getOperator ().unregister (request);
        } catch (VMDisconnectedException e) {
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (InvalidRequestStateException e) {
            e.printStackTrace();
        }
    }

    private ThreadsTreeModel threadsTreeModel;

    ThreadsTreeModel getThreadsTreeModel () {
        if (threadsTreeModel == null)
            threadsTreeModel = (ThreadsTreeModel) lookupProvider.lookupFirst 
                ("ThreadsView", TreeModel.class);
        return threadsTreeModel;
    }
    
    private JPDAThread getThread (ThreadReference tr) {
        try {
            return (JPDAThread) getThreadsTreeModel ().translate (tr);
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    
    // innerclasses ............................................................
    
    private class SSManager extends SmartSteppingFilterImpl implements Executor {
        
        private StepRequest stepRequest;
        private ThreadReference tr;
        private String position;
        
        void start (ThreadReference tr, boolean stepInto, String position) {
            this.tr = tr;
            this.position = position;
            refreshRequest ();
        }
        
        private void stop () {
            if (stepRequest != null)
                removeRequest (stepRequest);
            stepRequest = null;
        }
        
        public boolean exec (Event event) {
            stepRequest.disable ();
            LocatableEvent le = (LocatableEvent) event;
            String np = le.location ().declaringType ().name () + ":" + 
                        le.location ().lineNumber (null);
            
            ThreadReference tr = le.thread ();
            JPDAThread t = getThread (tr);
            boolean stop = (!np.equals (position)) && 
                           ssListener.stopHere (lookupProvider, t, this);
            if (stop) {
                stop ();
                getDebuggerImpl ().setStoppedState (tr);
            } else {
                stepRequest.enable ();
            }
            
            if (ssverbose)
                if (stop) {
                    System.out.println("SS  FINISH IN CLASS " +  
                        t.getClassName () + " ********\n"
                    );
                }
            return !stop;
        }
        
        public void addExclusionPatterns (Set patterns) {
            if (ssverbose)
                System.out.println("SS      add exclusion patterns: " + patterns);
            Set reallyNew = new HashSet (patterns);
            reallyNew.removeAll (getPatterns ());
            
            super.addExclusionPatterns (patterns);
            if (stepRequest != null)
                addPatternsToRequest (reallyNew);
        }

        public void removeExclusionPatterns (Set patterns) {
            if (ssverbose)
                System.out.println("SS      remove exclusion patterns: " + patterns);
            super.removeExclusionPatterns (patterns);
            refreshRequest ();
        }
        
        private void refreshRequest () {
            stepRequest = getDebuggerImpl ().getVirtualMachine ().
                eventRequestManager ().createStepRequest (
                    tr,
                    StepRequest.STEP_LINE,
                    StepRequest.STEP_INTO
                );
            getDebuggerImpl ().getOperator ().register (stepRequest, this);
            stepRequest.setSuspendPolicy (getDebuggerImpl ().getSuspend ());
            
            addPatternsToRequest (getPatterns ());
            stepRequest.enable ();
        }
            
        private void addPatternsToRequest (Set patterns) {
            Iterator i = patterns.iterator ();
            while (i.hasNext ()) {
                String p = (String) i.next ();
                stepRequest.addClassExclusionFilter (p);
                //methodExitRequest.addClassExclusionFilter (p);
            }
        }
    }
}
