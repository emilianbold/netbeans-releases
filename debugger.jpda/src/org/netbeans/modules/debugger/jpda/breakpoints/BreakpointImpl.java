/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
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

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.ParseException;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.ReturnVariableImpl;
import org.netbeans.modules.debugger.jpda.util.ConditionedExecutor;
import org.netbeans.modules.debugger.jpda.util.Executor;

import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 *
 * @author   Jan Jancura
 */
abstract class BreakpointImpl implements ConditionedExecutor, PropertyChangeListener {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N

    private JPDADebuggerImpl    debugger;
    private JPDABreakpoint      breakpoint;
    private BreakpointsReader   reader;
    private Expression          compiledCondition;
    private List<EventRequest>  requests = new ArrayList<EventRequest>();
    private int                 hitCountFilter = 0;

    protected BreakpointImpl (JPDABreakpoint p, BreakpointsReader reader, JPDADebuggerImpl debugger, Session session) {
        this.debugger = debugger;
        this.reader = reader;
        breakpoint = p;
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
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    // Update lazily in RP. We'll access java source parsing and JDI.
                    update();
                }
            });
        }
    }

    protected abstract void setRequests ();
    
    protected void remove () {
        if (SwingUtilities.isEventDispatchThread()) {
            // One can not want to access the requests in AWT EQ
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    removeAllEventRequests ();
                }
            });
        } else {
            removeAllEventRequests ();
        }
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

    private Variable processedReturnVariable;
    private Throwable conditionException;

    public boolean processCondition(
            Event event,
            String condition,
            ThreadReference threadReference,
            Value value) {

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

        Variable variable = null;
        if (getBreakpoint() instanceof MethodBreakpoint &&
                (((MethodBreakpoint) getBreakpoint()).getBreakpointType()
                 & MethodBreakpoint.TYPE_METHOD_EXIT) != 0) {
            if (value != null) {
                JPDAThreadImpl jt = (JPDAThreadImpl) getDebugger().getThread(threadReference);
                ReturnVariableImpl retVariable = new ReturnVariableImpl(getDebugger(), value, "", jt.getMethodName());
                jt.setReturnVariable(retVariable);
                variable = retVariable;
            }
        }
        boolean success;
        if (condition != null && condition.length() > 0) {
            //PATCH 48174
            try {
                getDebugger().setAltCSF(threadReference.frame(0));
            } catch (com.sun.jdi.IncompatibleThreadStateException e) {
                String msg = "Thread '" + threadReference.name() +
                        "': status = " + threadReference.status() +
                        ", is suspended = " + threadReference.isSuspended() +
                        ", suspend count = " + threadReference.suspendCount() +
                        ", is at breakpoint = " + threadReference.isAtBreakpoint();
                Logger.getLogger(BreakpointImpl.class.getName()).log(Level.INFO, msg, e);
            } catch (java.lang.IndexOutOfBoundsException e) {
                // No frame in case of Thread and "Main" class breakpoints, PATCH 56540
            }
            success = evaluateCondition (
                    condition,
                    threadReference
                );
            getDebugger().setAltCSF(null);
        } else {
            success = true;
        }
        if (success) { // perform() will be called, store the data
            processedReturnVariable = variable;
        }
        return success;
    }

    protected boolean perform (
        Event event,
        ThreadReference threadReference,
        ReferenceType referenceType,
        Value value
    ) {
        //S ystem.out.println("BreakpointImpl.perform");
        boolean resume;
        
        Variable variable = processedReturnVariable;
        processedReturnVariable = null;
        if (variable == null) {
            variable = debugger.getVariable(value);
        }
        JPDABreakpointEvent e;
        if (conditionException == null) {
            e = new JPDABreakpointEvent (
                getBreakpoint (),
                debugger,
                JPDABreakpointEvent.CONDITION_TRUE,
                debugger.getThread (threadReference), 
                referenceType, 
                variable
            );
        } else {
            e = new JPDABreakpointEvent (
                getBreakpoint (),
                debugger,
                conditionException,
                debugger.getThread (threadReference),
                referenceType,
                variable
            );
            conditionException = null;
        }
        getDebugger ().fireBreakpointEvent (
            getBreakpoint (),
            e
        );
        resume = getBreakpoint().getSuspend() == JPDABreakpoint.SUSPEND_NONE || e.getResume ();
        logger.fine("BreakpointImpl: perform breakpoint: " + this + " resume: " + resume);
        if (!resume) {
            resume = checkWhetherResumeToFinishStep(threadReference);
        }
        if (!resume) {
            ((JPDAThreadImpl) getDebugger().getThread(threadReference)).setCurrentBreakpoint(breakpoint);
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
                    int stepThreadStatus;
                    try {
                        stepThreadStatus = step.thread().status();
                    } catch (ObjectCollectedException ocex) {
                        stepThreadStatus = ThreadReference.THREAD_STATUS_ZOMBIE;
                    }
                    if (stepThreadStatus == ThreadReference.THREAD_STATUS_ZOMBIE) {
                        thread.virtualMachine().eventRequestManager().deleteEventRequest(step);
                        debugger.getOperator().unregister(step);
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
                    JPDAThreadImpl tr = (JPDAThreadImpl) debugger.getThread(thread);
                    tr.setStepSuspendedBy(breakpoint);

                    /*final String message;
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
                                if (pe.getNewValue().equals(debugger.STATE_RUNNING) ||
                                    pe.getNewValue().equals(debugger.STATE_DISCONNECTED)) {
                                    debugger.removePropertyChangeListener(this);
                                    tiPanelRef[0].dismiss();
                                }
                            }
                        }
                    });*/
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
                            debugger.getOperator().unregister(step);
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
        ThreadReference thread
    ) {
        try {
            try {
                boolean success;
                synchronized (debugger.LOCK) {
                    StackFrame sf = thread.frame (0);
                    success = evaluateConditionIn (condition, sf, 0);
                }
                // condition true => stop here (do not resume)
                // condition false => resume
                logger.fine("BreakpointImpl: perform breakpoint (condition = " + success + "): " + this + " resume: " + (!success));
                return success;
            } catch (ParseException ex) {
                conditionException = ex;
                logger.fine("BreakpointImpl: perform breakpoint (bad condition): '" + condition + "', got " + ex.getMessage());
                return true; // Act as if the condition was satisfied when it's invalid
            } catch (InvalidExpressionException ex) {
                conditionException = ex;
                logger.fine("BreakpointImpl: perform breakpoint (bad condition): '" + condition + "', got " + ex.getMessage());
                return true; // Act as if the condition was satisfied when it's invalid
            }
        } catch (IncompatibleThreadStateException ex) {
            // should not occurre
            Exceptions.printStackTrace(ex);
            return true; // Act as if the condition was satisfied when an error occurs
        }
    }

    /*private boolean evaluateCondition (
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
                    result = evaluateConditionIn (condition, sf, 0);
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
    }*/

    /**
     * Evaluates given condition. Returns value of condition evaluation. 
     * Returns true othervise (bad expression).
     */
    private boolean evaluateConditionIn (
        String condExpr, 
        StackFrame frame,
        int frameDepth
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
            frame,
            frameDepth
        );
        try {
            return ((com.sun.jdi.BooleanValue) value).booleanValue ();
        } catch (ClassCastException e) {
            throw new InvalidExpressionException ("Expecting boolean value instead of "+value.type());
        } catch (NullPointerException npe) {
            throw new InvalidExpressionException (npe);
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
