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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.ParseException;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.ReturnVariableImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.util.ThreadInfoPanel;

import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 *
 * @author   Jan Jancura
 */
public abstract class BreakpointImpl implements Executor, PropertyChangeListener {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N

    private JPDADebuggerImpl    debugger;
    private JPDABreakpoint      breakpoint;
    private BreakpointsReader   reader;
    private final Session       session;
    private Expression          compiledCondition;
    private List<EventRequest>  requests = new ArrayList<EventRequest>();
    private int                 hitCountFilter = 0;

    protected BreakpointImpl (JPDABreakpoint p, BreakpointsReader reader, JPDADebuggerImpl debugger, Session session) {
        this.debugger = debugger;
        this.reader = reader;
        breakpoint = p;
        this.session = session;
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    final void set () {
        breakpoint.addPropertyChangeListener (this);
        update ();
    }
    
    /**
     * Called when Fix&Continue is invoked. Reqritten in LineBreakpointImpl.
     */
    void fixed () {
        if (reader != null) {
            reader.storeCachedClassName(breakpoint, null);
        }
        update ();
    }
    
    /**
     * Called from set () and propertyChanged.
     */
    final void update () {
        if ( (getVirtualMachine () == null) ||
             (getDebugger ().getState () == JPDADebugger.STATE_DISCONNECTED)
        ) return;
        removeAllEventRequests ();
        if (breakpoint.isEnabled () && isEnabled()) {
            setRequests ();
        }
    }
    
    protected boolean isEnabled() {
        return true;
    }
    
    protected final void setValidity(Breakpoint.VALIDITY validity, String reason) {
        if (breakpoint instanceof ChangeListener) {
            ((ChangeListener) breakpoint).stateChanged(new ValidityChangeEvent(validity, reason));
        }
    }

    public void propertyChange (PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_DISPOSED.equals(propertyName)) {
            remove();
        } else if (!Breakpoint.PROP_VALIDITY.equals(propertyName) &&
                   !Breakpoint.PROP_GROUP_NAME.equals(propertyName)) {
            if (reader != null) {
                reader.storeCachedClassName(breakpoint, null);
            }
            update ();
        }
    }

    protected abstract void setRequests ();
    
    protected void remove () {
        removeAllEventRequests ();
        breakpoint.removePropertyChangeListener(this);
        setValidity(Breakpoint.VALIDITY.UNKNOWN, null);
    }

    protected JPDABreakpoint getBreakpoint () {
        return breakpoint;
    }

    protected JPDADebuggerImpl getDebugger () {
        return debugger;
    }

    protected VirtualMachine getVirtualMachine () {
        return getDebugger ().getVirtualMachine ();
    }
    
    protected EventRequestManager getEventRequestManager () {
        VirtualMachine vm = getVirtualMachine();
        if (vm == null) {
            // Already disconnected
            throw new VMDisconnectedException();
        }
        return vm.eventRequestManager ();
    }

    protected void addEventRequest (EventRequest r) {
        addEventRequest(r, false);
    }
    
    synchronized protected void addEventRequest (EventRequest r, boolean ignoreHitCount) {
        logger.fine("BreakpointImpl addEventRequest: " + r);
        requests.add (r);
        getDebugger ().getOperator ().register (r, this);
       
        // PATCH #48174
        // if this is breakpoint with SUSPEND_NONE we stop EVENT_THREAD to print output line
        if (getBreakpoint().getSuspend() == JPDABreakpoint.SUSPEND_ALL)
            r.setSuspendPolicy (JPDABreakpoint.SUSPEND_ALL);
        else
            r.setSuspendPolicy (JPDABreakpoint.SUSPEND_EVENT_THREAD);
        int hitCountFilter = getBreakpoint().getHitCountFilter();
        if (!ignoreHitCount && hitCountFilter > 0) {
            r.addCountFilter(hitCountFilter);
            switch (getBreakpoint().getHitCountFilteringStyle()) {
                case MULTIPLE:
                    this.hitCountFilter = hitCountFilter;
                    break;
                case EQUAL:
                    this.hitCountFilter = 0;
                    break;
                case GREATER:
                    this.hitCountFilter = -1;
                    break;
                default:
                    throw new IllegalStateException(getBreakpoint().getHitCountFilteringStyle().name());
            }
        } else {
            this.hitCountFilter = 0;
        }
        r.enable ();
    }

    synchronized private void removeAllEventRequests () {
        if (requests.size () == 0) return;
        VirtualMachine vm = getDebugger().getVirtualMachine();
        if (vm == null) return; 
        int i, k = requests.size ();
        try {
            for (i = 0; i < k; i++) { 
                EventRequest r = requests.get (i);
                logger.fine("BreakpointImpl removeEventRequest: " + r);
                vm.eventRequestManager().deleteEventRequest(r);
                getDebugger ().getOperator ().unregister (r);
            }
            
        } catch (VMDisconnectedException e) {
        } catch (com.sun.jdi.InternalException e) {
        }
        requests = new LinkedList<EventRequest>();
    }
    
    synchronized private void removeEventRequest(EventRequest r) {
        VirtualMachine vm = getDebugger().getVirtualMachine();
        if (vm == null) return; 
        try {
            logger.fine("BreakpointImpl removeEventRequest: " + r);
            vm.eventRequestManager().deleteEventRequest(r);
            getDebugger ().getOperator ().unregister (r);
        } catch (VMDisconnectedException e) {
        } catch (com.sun.jdi.InternalException e) {
        }
        requests.remove(r);
    }
    
    /** Called when a new event request needs to be created, e.g. after hit count
     * was met and hit count style is "greater than".
     */
    protected abstract EventRequest createEventRequest(EventRequest oldRequest);

    protected boolean perform (
        Event event,
        String condition,
        ThreadReference thread,
        ReferenceType referenceType,
        Value value
    ) {
        //S ystem.out.println("BreakpointImpl.perform");
        boolean resume;
        
        if (hitCountFilter > 0) {
            event.request().disable();
            //event.request().addCountFilter(hitCountFilter);
            // This submits the event with the filter again
            event.request().enable();
        }
        if (hitCountFilter == -1) {
            event.request().disable();
            removeEventRequest(event.request());
            addEventRequest(createEventRequest(event.request()), true);
        }
        //PATCH 48174
        try {
            getDebugger().setAltCSF(thread.frame(0));
        } catch (com.sun.jdi.IncompatibleThreadStateException e) {
            ErrorManager.getDefault().notify(e);
        } catch (java.lang.IndexOutOfBoundsException e) {
            // No frame in case of Thread and "Main" class breakpoints, PATCH 56540 
        } 
        Variable variable = null;
        if (getBreakpoint() instanceof MethodBreakpoint &&
                (((MethodBreakpoint) getBreakpoint()).getBreakpointType()
                 & MethodBreakpoint.TYPE_METHOD_EXIT) != 0) {
            JPDAThreadImpl jt = (JPDAThreadImpl) getDebugger().getThread(thread);
            if (value != null) {
                ReturnVariableImpl retVariable = new ReturnVariableImpl(getDebugger(), value, "", jt.getMethodName());
                jt.setReturnVariable(retVariable);
                variable = retVariable;
            }
        }
        if (variable == null) {
            variable = debugger.getVariable(value);
        }
        
        
        if ((condition == null) || condition.equals ("")) {
            JPDABreakpointEvent e = new JPDABreakpointEvent (
                getBreakpoint (),
                debugger,
                JPDABreakpointEvent.CONDITION_NONE,
                debugger.getThread (thread), 
                referenceType, 
                variable
            );
            getDebugger ().fireBreakpointEvent (
                getBreakpoint (),
                e
            );
            resume = getBreakpoint().getSuspend() == JPDABreakpoint.SUSPEND_NONE || e.getResume ();
            logger.fine("BreakpointImpl: perform breakpoint (no condition): " + this + " resume: " + resume);
        } else {
            resume = evaluateCondition (
                condition, 
                thread,
                referenceType,
                variable
            );
            //PATCH 48174
            resume = getBreakpoint().getSuspend() == JPDABreakpoint.SUSPEND_NONE || resume;
        }
        getDebugger().setAltCSF(null);
        if (!resume) {
            resume = checkWhetherResumeToFinishStep(thread);
            if (!resume) {
                DebuggerManager.getDebuggerManager().setCurrentSession(session);
                getDebugger ().setStoppedState (thread);
            }
        }
        //S ystem.out.println("BreakpointImpl.perform end");
        return resume; 
    }
    
    private boolean checkWhetherResumeToFinishStep(ThreadReference thread) {
        List<StepRequest> stepRequests = thread.virtualMachine().eventRequestManager().stepRequests();
        if (stepRequests.size() > 0) {
            int suspendState = breakpoint.getSuspend();
            if (suspendState == JPDABreakpoint.SUSPEND_ALL ||
                suspendState == JPDABreakpoint.SUSPEND_EVENT_THREAD) {

                boolean thisThreadHasStep = false;
                List<StepRequest> activeStepRequests = new ArrayList<StepRequest>(stepRequests);
                for (int i = 0; i < activeStepRequests.size(); i++) {
                    StepRequest step = activeStepRequests.get(i);
                    ThreadReference stepThread = step.thread();
                    if (!step.isEnabled()) {
                        activeStepRequests.remove(i);
                        continue;
                    }
                    if (step.thread().status() == ThreadReference.THREAD_STATUS_ZOMBIE) {
                        thread.virtualMachine().eventRequestManager().deleteEventRequest(step);
                        activeStepRequests.remove(i);
                        continue;
                    }
                    if (thread.equals(stepThread)) {
                        thisThreadHasStep = true;
                    }
                }
                if (thisThreadHasStep) { // remove this if the debugger should warn you in the same thread as well. See #104101.
                    return false;
                }
                if (activeStepRequests.size() > 0 && (thisThreadHasStep || suspendState == JPDABreakpoint.SUSPEND_ALL)) {
                    Boolean resumeDecision = debugger.getStepInterruptByBptResumeDecision();
                    if (resumeDecision != null) {
                        return resumeDecision.booleanValue();
                    }
                    final String message;
                    if (thisThreadHasStep) {
                        message = NbBundle.getMessage(BreakpointImpl.class,
                                "MSG_StepThreadInterruptedByBR",
                                breakpoint.toString());
                    } else {
                        message = NbBundle.getMessage(BreakpointImpl.class,
                                "MSG_StepInterruptedByBR",
                                breakpoint.toString(),
                                thread.name(),
                                activeStepRequests.get(0).thread().name());
                    }
                    final ThreadInfoPanel[] tiPanelRef = new ThreadInfoPanel[] { null };
                    try {
                        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                tiPanelRef[0] = ThreadInfoPanel.create(message,
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn1"),
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn1_TIP"),
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn2"),
                                        NbBundle.getMessage(BreakpointImpl.class, "StepInterruptedByBR_Btn2_TIP"));
                            }
                        });
                    } catch (InterruptedException iex) {
                    } catch (java.lang.reflect.InvocationTargetException itex) {
                        ErrorManager.getDefault().notify(itex);
                    }
                    if (tiPanelRef[0] == null) {
                        return false;
                    }
                    tiPanelRef[0].setButtonListener(new ThreadInfoPanel.ButtonListener() {
                        public void buttonPressed(int n) {
                            if (n == 2) {
                                debugger.setStepInterruptByBptResumeDecision(Boolean.TRUE);
                            }
                            debugger.resume();
                        }
                    });
                    debugger.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent pe) {
                            if (pe.getPropertyName().equals(debugger.PROP_STATE)) {
                                if (pe.getNewValue().equals(debugger.STATE_RUNNING)) {
                                    debugger.removePropertyChangeListener(this);
                                    tiPanelRef[0].dismiss();
                                }
                            }
                        }
                    });
                    return false;
                    
                    /*
                    JCheckBox cb = new JCheckBox(NbBundle.getMessage(BreakpointImpl.class, "RememberDecision"));
                    DialogDescriptor dd = new DialogDescriptor(
                            //message,
                            createDlgPanel(message, cb),
                            new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION).getTitle(),
                            true,
                            NotifyDescriptor.YES_NO_OPTION,
                            null,
                            null);
                    dd.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
                    // Set the stopped state to show the breakpoint location
                    DebuggerManager.getDebuggerManager().setCurrentSession(session);
                    getDebugger ().setStoppedState (thread);
                    Object option = org.openide.DialogDisplayer.getDefault().notify(dd);
                    boolean yes = option == NotifyDescriptor.YES_OPTION;
                    boolean no  = option == NotifyDescriptor.NO_OPTION;
                    if (cb.isSelected() && (yes || no)) {
                        debugger.setStepInterruptByBptResumeDecision(Boolean.valueOf(yes));
                    }
                    if (yes) {
                        // We'll resume...
                        getDebugger ().setRunningState();
                    }
                    if (no) {
                        // The user wants to stop on the breakpoint, remove
                        // the step requests to prevent confusion
                        for (StepRequest step : activeStepRequests) {
                            thread.virtualMachine().eventRequestManager().deleteEventRequest(step);
                        }
                    }
                    return yes;
                     */
                }
            }
        }
        return false;
    }

    /*
    private static JPanel createDlgPanel(String message, JCheckBox cb) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        JTextArea area = new JTextArea(message);
        Color color = UIManager.getColor("Label.background"); // NOI18N
        if (color != null) {
            area.setBackground(color);
        }
        //area.setLineWrap(true);
        //area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setTabSize(4); // looks better for module sys messages than 8
        panel.add(area, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(12, 0, 0, 0);
        panel.add(cb, c);
        return panel;
    }
     */
    
    private boolean evaluateCondition (
        String condition, 
        ThreadReference thread,
        ReferenceType referenceType,
        Variable variable
    ) {
        try {
            try {
                boolean result;
                JPDABreakpointEvent ev;
                synchronized (debugger.LOCK) {
                    StackFrame sf = thread.frame (0);
                    result = evaluateConditionIn (condition, sf);
                    ev = new JPDABreakpointEvent (
                        getBreakpoint (),
                        debugger,
                        result ? 
                            JPDABreakpointEvent.CONDITION_TRUE : 
                            JPDABreakpointEvent.CONDITION_FALSE,
                        debugger.getThread (thread), 
                        referenceType, 
                        variable
                    );
                }
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                            
                // condition true => stop here (do not resume)
                // condition false => resume
                logger.fine("BreakpointImpl: perform breakpoint (condition = " + result + "): " + this + " resume: " + (!result || ev.getResume ()));
                return !result || ev.getResume ();
            } catch (ParseException ex) {
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    debugger,
                    ex,
                    debugger.getThread (thread), 
                    referenceType, 
                    variable
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                logger.fine("BreakpointImpl: perform breakpoint (bad condition): " + this + " resume: " + ev.getResume ());
                return ev.getResume ();
            } catch (InvalidExpressionException ex) {
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    debugger,
                    ex,
                    debugger.getThread (thread), 
                    referenceType, 
                    variable
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                logger.fine("BreakpointImpl: perform breakpoint (invalid condition): " + this + " resume: " + ev.getResume ());
                return ev.getResume ();
            }
        } catch (IncompatibleThreadStateException ex) {
            // should not occurre
            ex.printStackTrace ();
        }
        // some error occured during evaluation of expression => do not resume
        return false; // do not resume
    }

    /**
     * Evaluates given condition. Returns value of condition evaluation. 
     * Returns true othervise (bad expression).
     */
    private boolean evaluateConditionIn (
        String condExpr, 
        StackFrame frame
    ) throws ParseException, InvalidExpressionException {
        // 1) compile expression
        if ( compiledCondition == null || 
             !compiledCondition.getExpression ().equals (condExpr)
        )
            compiledCondition = Expression.parse (
                condExpr, 
                Expression.LANGUAGE_JAVA_1_5
            );
        
        // 2) evaluate expression
        // already synchronized (debugger.LOCK)
        com.sun.jdi.Value value = getDebugger ().evaluateIn (
            compiledCondition, 
            frame
        );
        try {
            return ((com.sun.jdi.BooleanValue) value).booleanValue ();
        } catch (ClassCastException e) {
            throw new InvalidExpressionException (e);
        }
    }
    
    /**
     * Support method for simple patterns.
     */
    static boolean match (String name, String pattern) {
        if (pattern.startsWith ("*"))
            return name.endsWith (pattern.substring (1));
        else
        if (pattern.endsWith ("*"))
            return name.startsWith (
                pattern.substring (0, pattern.length () - 1)
            );
        return name.equals (pattern);
    }
    
    private static final class ValidityChangeEvent extends ChangeEvent {
        
        private String reason;
        
        public ValidityChangeEvent(Breakpoint.VALIDITY validity, String reason) {
            super(validity);
            this.reason = reason;
        }
        
        public String toString() {
            return reason;
        }
    }
}
