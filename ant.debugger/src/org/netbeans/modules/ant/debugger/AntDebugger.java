/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.debugger;

import java.io.File;
import java.io.IOException;
import java.lang.StringBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.ant.debugger.breakpoints.AntBreakpoint;
import org.netbeans.modules.ant.debugger.breakpoints.BreakpointModel;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.util.Lookup;

/*
 * AntTest.java
 *
 * Created on 19. leden 2004, 20:03
 */

/**
 *
 * @author  Honza
 */
public class AntDebugger extends ActionsProviderSupport {

    
    private AntProjectCookie            antCookie;
    private AntDebuggerEngineProvider   engineProvider;
    private ContextProvider             contextProvider;
    private Object                      LOCK = new Object ();
    private IOManager                   ioManager;
    private boolean                     doNotStop = false;
    private Object                      currentLine;
    
    
    public AntDebugger (
        ContextProvider contextProvider
    ) {
        
        this.contextProvider = contextProvider;
        
        // init antCookie
        antCookie = (AntProjectCookie) contextProvider.lookupFirst 
            (null, AntProjectCookie.class);
        
        // init engineProvider
        engineProvider = (AntDebuggerEngineProvider) contextProvider.lookupFirst 
            (null, DebuggerEngineProvider.class);
                
        // init actions
        setEnabled (ActionsManager.ACTION_KILL, true);
        setEnabled (ActionsManager.ACTION_STEP_INTO, true);
        setEnabled (ActionsManager.ACTION_STEP_OVER, true);
        setEnabled (ActionsManager.ACTION_STEP_OUT, true);
        setEnabled (ActionsManager.ACTION_CONTINUE, true);
        setEnabled (ActionsManager.ACTION_START, true);
                
        ioManager = new IOManager (antCookie.getFile ().getName ());
    }
    
    
    // ActionsProvider .........................................................
    
    private static Set actions = new HashSet ();
    static {
        actions.add (ActionsManager.ACTION_KILL);
        actions.add (ActionsManager.ACTION_CONTINUE);
        actions.add (ActionsManager.ACTION_START);
        actions.add (ActionsManager.ACTION_STEP_INTO);
        actions.add (ActionsManager.ACTION_STEP_OVER);
        actions.add (ActionsManager.ACTION_STEP_OUT);
    }
    
    public Set getActions () {
        return actions;
    }
        
    public void doAction (Object action) {
        if (action == ActionsManager.ACTION_KILL) {
            finish ();
        } else
        if (action == ActionsManager.ACTION_CONTINUE) {
            doContinue ();
        } else
        if (action == ActionsManager.ACTION_START) {
        } else
        if ( action == ActionsManager.ACTION_STEP_INTO ||
             action == ActionsManager.ACTION_STEP_OUT ||
             action == ActionsManager.ACTION_STEP_OVER
        ) {
            doStep (action);
        }
    }
    
    
    // other methods ...........................................................
    
    private AntEvent lastEvent;
    
    /**
     * Called from DebuggerAntLogger.
     */
    void taskStarted (AntEvent event) {
        if (doNotStop) return;
        lastEvent = event;
        Object taskLine = Utils.getLine (event);
        
        // update callStack
        updateInternalStack (event, taskLine);

        if (lastAction == ActionsManager.ACTION_CONTINUE) {
            // running mode
            if (!onBreakpoint ())
                return;
            // stop on breakpoint
            updateUI ();
        } else {
            // stepping
            if (doNotStopHere ()) {
                //S ystem.out.println("doNotStopHere");
                return;
            }
            doStep (null);
        }
        
        // update variable values
        Set properties = event.getPropertyNames ();
        variables = (String[]) properties.toArray 
            (new String [properties.size ()]);
        getVariablesModel ().fireChanges ();
        getBreakpointModel ().fireChanges ();
        
        // wait for next stepping orders
        synchronized (LOCK) {
            try {
                LOCK.wait ();
            } catch (InterruptedException ex) {
                ex.printStackTrace ();
            }
        }
    }
    
    /**
     * Called from DebuggerAntLogger.
     */
    void buildFinished (AntEvent event) {
        engineProvider.getDestructor ().killEngine ();
        ioManager.closeStream ();
        Utils.unmarkCurrent ();
    }
    
    private boolean doNotStopHere () {
        if (doNotStopInTarget == null) return false;
        int i, k = callStackInternal.length;
        for (i = 0; i < k; i++)
            if (callStackInternal [i].equals (doNotStopInTarget)) return true;
        return false;
    }
    
    File getFile () {
        return antCookie.getFile ();
    }

    private void updateUI () {
        TargetLister.Target nextTarget = getNextTarget ();
        String nextTargetName = nextTarget == null ?
            null : nextTarget.getName ();
        currentLine = callStack [0] instanceof Task ?
            ((Task) callStack [0]).getLine () :
            Utils.getLine (
                (TargetLister.Target) callStack [0], 
                nextTargetName
            );
        updateOutputWindow (currentLine);
        Utils.markCurrent (currentLine);
        getCallStackModel ().fireChanges ();
    }
    
    private TargetLister.Target getNextTarget () {
        if (callStackInternal.length > (callStack.length + 1) &&
            callStack [0] == callStackInternal [
                callStackInternal.length - callStack.length
            ]
        ) 
            return (TargetLister.Target) callStackInternal [
                callStackInternal.length - callStack.length - 1
            ];
        return null;
    }
    
    private void updateOutputWindow (Object currentLine) {
        if (callStack [0] instanceof Task) {
            Task task = (Task) callStack [0];
            ioManager.println (
                task.getFile ().getName () + ":" + 
                    (Utils.getLineNumber (currentLine) + 1) + 
                    ": Task " + getStackAsString (), 
                currentLine
             );
        } else {
            TargetLister.Target target = (TargetLister.Target) callStack [0];
            ioManager.println (
                target.getScript ().getFile ().getName () + ":" + 
                    (Utils.getLineNumber (currentLine) + 1) + 
                    ": Target " + getStackAsString (), 
                currentLine
             );
        }
    }
    
    private String getStackAsString () {
        StringBuffer sb = new StringBuffer ();
        int i = callStack.length - 1;
        sb.append (getFrameName (callStack [i--]));
        while (i >= 0)
            sb.append ('.').append (getFrameName (callStack [i--]));
        return new String (sb);
    }
    
    private static String getFrameName (Object frame) {
        return frame instanceof Task ?
            ((Task) frame).getTaskStructure ().getName () :
            ((TargetLister.Target) frame).getName ();
    }
    
    private Map watches = new HashMap ();
    
    private boolean onBreakpoint () {
        // 1) stop on watch value change
        Watch[] ws = DebuggerManager.getDebuggerManager ().
            getWatches ();
        int j, jj = ws.length;
        for (j = 0; j < jj; j++) {
            Object value = getVariableValue (ws [j].getExpression ());
            if (value == null) value = new Integer (0);
            if ( watches.containsKey (ws [j].getExpression ()) &&
                 !watches.get (ws [j].getExpression ()).equals (value)
            ) {
                callStack = new Object [jj - j];
                System.arraycopy 
                    (callStackInternal, j, callStack, 0, jj - j);
                watches.put (
                    ws [j].getExpression (), 
                    value
                );
                return true;
            } else
                watches.put (
                    ws [j].getExpression (), 
                    value
                );
        }
        
        // 2) check line breakpoints
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        jj = callStackInternal.length;
        for (j = 0; j < jj; j++) {
            Object line = callStackInternal [j] instanceof Task ?
                ((Task) callStackInternal [j]).getLine () :
                Utils.getLine (
                    (TargetLister.Target) callStackInternal [j], 
                    null
                );
            int i, k = breakpoints.length;
            for (i = 0; i < k; i++)
                if ( breakpoints [i] instanceof AntBreakpoint &&
                     Utils.contains (
                         line,
                         ((AntBreakpoint) breakpoints [i]).getLine ()

                     )
                ) {
                    callStack = new Object [jj - j];
                    System.arraycopy 
                        (callStackInternal, j, callStack, 0, jj - j);
                    return true;
                }
        }
        return false;
    }

    public Object getCurrentLine () {
        return currentLine;
    }
    
    
    // stepping hell ...........................................................
    
    private Object      lastAction;
    private Set         finishedTasks = new HashSet ();
    
    private void doContinue () {
        Utils.unmarkCurrent ();
        lastAction = ActionsManager.ACTION_CONTINUE;
        doEngineStep ();
    }

    /**
     * should define callStack based on callStackInternal & action.
     */
    private void doStep (Object action) {
        //S ystem.out.println("doStep " + action);
        if (action != null)
            lastAction = action;
        if (callStack.length == 0) {
            callStack = new Object [1];
            callStack [0] = callStackInternal [callStackInternal.length - 1];
            updateUI ();
        } else
        if (callStack [0].equals (callStackInternal [0])
        )
           doEngineStep ();
        else
        if ( callStack.length == callStackInternal.length &&
             ( callStack.length == 1 ||
               callStack [1].equals (callStackInternal [1])
             )
        ) {
            if (lastAction == ActionsManager.ACTION_STEP_OUT) {
                if (callStack.length == 1)
                    doContinue ();
                else
                    stepUp ();
            } else {
                callStack = callStackInternal;
                updateUI ();
            }
        } else
        if ( callStack.length < callStackInternal.length &&
             callStack [0].equals (callStackInternal [
                 callStackInternal.length - callStack.length
             ])
        ) {
            //TargetLister.Target nextTarget = getNextTarget ();
            if (lastAction == ActionsManager.ACTION_STEP_INTO)
                stepDown ();
            else
            if (action == ActionsManager.ACTION_STEP_OVER)
                doEngineStep (getNextTarget ());
            else
            if (lastAction == ActionsManager.ACTION_STEP_OVER)
                updateUI ();
            else
                doEngineStep ((TargetLister.Target) callStack [0]);
        } else
            stepUp ();
        //S ystem.out.println("doStep - end");
    }
    
    private void doEngineStep (TargetLister.Target t) {
        doNotStopInTarget = t;
        doEngineStep ();
    }
    
    private void doEngineStep () {
        //S ystem.out.println("doEngineStep " + doNotStopInTarget);
        synchronized (LOCK) {
            LOCK.notify ();
        }
    }
    
    private void finish () {
        Utils.unmarkCurrent ();
        doNotStop = true;
        synchronized (LOCK) {
            LOCK.notify ();
        }
    }
    
    private void stepUp () {
        //S ystem.out.println("stepUp");
        if (callStack.length == 1) {
            callStack = new Object [1];
            callStack [0] = callStackInternal [callStackInternal.length - 1];
        } else {
            Object[] newStack = new Object [callStack.length - 1];
            System.arraycopy (
                callStack, 
                1,
                newStack, 
                0, 
                newStack.length
            );
            callStack = newStack;
        }
        updateUI ();
    }
    
    private void stepDown () {
        //S ystem.out.println("stepDown");
        callStack = new Object [callStack.length + 1];
        System.arraycopy (
            callStackInternal, 
            callStackInternal.length - callStack.length, 
            callStack, 
            0, 
            callStack.length
        );
        updateUI ();
    }
    
    
    // support for call stack ..................................................
    
    private CallStackModel              callStackModel;

    private CallStackModel getCallStackModel () {
        if (callStackModel == null)
            callStackModel = (CallStackModel) contextProvider.lookupFirst 
                ("CallStackView", TreeModel.class);
        return callStackModel;
    }
    
    private Object[] callStackInternal = new String [0];
    private Object[] callStack = new String [0];
    private TargetLister.Target doNotStopInTarget = null;
    
    Object[] getCallStack () {
        return callStack;
    }
    
    private void updateInternalStack (
        AntEvent    event,
        Object      taskLine
    ) {
        LinkedList path = null;
        if (event.getTargetName () == null) {
            callStackInternal = new Object[] {
                new Task (
                    event.getTaskStructure (), 
                    taskLine, 
                    event.getScriptLocation ()
                )
            };
            return;
        }
        String[] originalTargets = event.getSession ().getOriginatingTargets ();
        String end = event.getTargetName ();
        int i, k = originalTargets.length;
        for (i = 0; i < k; i++) {
            String start = originalTargets [i];
            path = findPath (getNameToTargetMap (), start, end);
            if (path != null) {
                path.addFirst (
                    new Task (
                        event.getTaskStructure (), 
                        taskLine, 
                        event.getScriptLocation ()
                    )
                );
                callStackInternal = path.toArray ();
                return;
            }
        }
        throw new InternalError ("path not found");
    }
    
    private static LinkedList findPath (
        Map nameToTarget,
        String start,
        String end
    ) {
        TargetLister.Target t = (TargetLister.Target) nameToTarget.get (start);
        if (start.equals (end)) {
            LinkedList ll = new LinkedList ();
            if (t == null)
                throw new NullPointerException ();
            ll.addFirst (t);
            return ll;
        }
        String depends = t.getElement ().getAttribute ("depends");
        StringTokenizer st = new StringTokenizer (depends, ",");
        while (st.hasMoreTokens ()) {
            String newStart = st.nextToken ();
            LinkedList ll = findPath (
                nameToTarget,
                newStart,
                end
            );
            if (ll == null) continue;
            ll.addLast (t);
            return ll;
        }
        return null;
    }
    
    private Map nameToTarget = null;
    
    private Map getNameToTargetMap () {
        try {
            if (nameToTarget == null) {
                nameToTarget = new HashMap ();
                Set targets = TargetLister.getTargets (antCookie);
                Iterator it = targets.iterator ();
                while (it.hasNext ()) {
                    TargetLister.Target t = (TargetLister.Target) it.next ();
                    nameToTarget.put (t.getName (), t);
                }
            }
            return nameToTarget;
        } catch (IOException ex) {
            ex.printStackTrace ();
            return null;
        }
    }
    
    
    // support for variables ...................................................
    
    private VariablesModel              variablesModel;

    private VariablesModel getVariablesModel () {
        if (variablesModel == null)
            variablesModel = (VariablesModel) contextProvider.lookupFirst 
                ("LocalsView", TreeModel.class);
        return variablesModel;
    }
    
    private BreakpointModel             breakpointModel;

    private BreakpointModel getBreakpointModel () {
        if (breakpointModel == null) {
            Iterator it = DebuggerManager.getDebuggerManager ().lookup 
                ("BreakpointsView", TableModel.class).iterator ();
            while (it.hasNext ()) {
                TableModel model = (TableModel) it.next ();
                if (model instanceof BreakpointModel) {
                    breakpointModel = (BreakpointModel) model;
                    break;
                }
            }
        }
        return breakpointModel;
    }

    String evaluate (String expression) {
        String value = getVariableValue (expression);
        if (value != null) return value;
        if (lastEvent == null) return null;
        return lastEvent.evaluate (expression);
    }

    private String[] variables = new String [0];
    
    String[] getVariables () {
        return variables;
    }
    
    String getVariableValue (String variableName) {
        if (lastEvent == null) return null;
        return lastEvent.getProperty (variableName);
    }
}
