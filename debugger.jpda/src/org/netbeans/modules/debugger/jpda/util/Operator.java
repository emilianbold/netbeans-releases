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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.ThreadReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.ErrorManager;

/**
 * Listens for events coming from a remove VM and notifies registered objects.
 * <P>
 * Any object implementing interface {@link Executor} can bind itself
 * with an {@link EventRequest}. Each time an {@link Event} corresponding
 * to the request comes from the virtual machine the <TT>Operator</TT>
 * notifies the registered object by calling its <TT>exec()</TT> method.
 * <P>
 * The only exceptions to the above rule are <TT>VMStartEvent</TT>,
 * <TT>VMDeathEvent</TT> and <TT>VMDisconnectEvent</TT> that cannot be
 * bound to any request. To listen for these events, specify <EM>starter</EM>
 * and <EM>finalizer</EM> in the constructor.
 * <P>
 * The operator is not active until it is started - use method <TT>start()</TT>.
 * The operator stops itself when either <TT>VMDeathEvent</TT> or <TT>VMDisconnectEvent</TT>
 * is received; it can be started again.
 * <P>
 * Use method {@link #register} to bind a requst with an object.
 * The object can be unregistered - use method {@link #unregister}.
 * <P>
 * There should be only one <TT>Operator</TT> per remote VM.
*
* @author Jan Jancura
*/
public class Operator {

    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N

    public static final String SILENT_EVENT_PROPERTY = "silent"; // NOI18N

    private Thread            thread;
    private boolean           breakpointsDisabled;
    private List<EventSet>    staledEvents = new ArrayList<EventSet>();
    private List<EventRequest> staledRequests = new ArrayList<EventRequest>();
    private boolean           stop;
    private boolean           canInterrupt;
    private JPDADebuggerImpl  debugger;

    /**
     * Creates an operator for a given virtual machine. The operator will listen
     * to the VM's event queue.
     *
     * @param  virtualMachine  remote VM this operator will listen to
     * @param  starter  thread to be started upon start of the remote VM
     *                  (may be <TT>null</TT>)
     * @param  finalizer  thread to be started upon death of the remote VM
     *                    or upon disconnection from the VM
     *                    (may be <TT>null</TT>)
    */
    public Operator (
        VirtualMachine virtualMachine,
        final JPDADebuggerImpl debugger,
        Executor starter,
        Runnable finalizer,
        final Object resumeLock
    ) {
        EventQueue eventQueue = virtualMachine.eventQueue ();
        if (eventQueue == null)
            throw new NullPointerException ();
        this.debugger = debugger;
        final Object[] params = new Object[] {eventQueue, starter, finalizer};
        thread = new Thread (new Runnable () {
        public void run () {
            EventQueue eventQueue = (EventQueue) params [0];
            Executor starter = (Executor) params [1];
            Runnable finalizer = (Runnable) params [2];
            params [0] = null;
            params [1] = null;
            params [2] = null;
            boolean processStaledEvents = false;

       loop: for (;;) {
                 try {
                     EventSet eventSet = null;
                     if (processStaledEvents) {
                         synchronized (Operator.this) {
                             if (staledEvents.size() == 0) {
                                 processStaledEvents = false;
                             } else {
                                eventSet = staledEvents.remove(0);
                                while (staledRequests.size() > 0) {
                                    EventRequest request = staledRequests.remove(0);
                                    request.virtualMachine().eventRequestManager().deleteEventRequest(request);
                                }
                                //eventSet.virtualMachine.suspend();
                             }
                         }
                     }
                     if (eventSet == null) {
                        try {
                            synchronized (Operator.this) {
                                if (stop) break;
                                canInterrupt = true;
                            }
                            eventSet = eventQueue.remove ();
                            if (logger.isLoggable(Level.FINE)) {
                                try {
                                    logger.fine("HAVE EVENT(s) in the Queue: "+eventSet);
                                } catch (ObjectCollectedException ocex) {
                                    ErrorManager.getDefault().notify(ocex);
                                }
                            }
                        } catch (InterruptedException iexc) {
                            synchronized (Operator.this) {
                                if (stop) {
                                    break;
                                }
                            }
                            processStaledEvents = true;
                            continue;
                        }
                        synchronized (Operator.this) {
                            canInterrupt = false;
                        }
                     }
                     boolean silent = eventSet.size() > 0;
                     for (Event e: eventSet) {
                         EventRequest r = e.request();
                         if (r == null || !Boolean.TRUE.equals(r.getProperty (SILENT_EVENT_PROPERTY))) {
                             silent = false;
                             break;
                         }
                     }
                     if (!silent) {
                         synchronized (Operator.this) {
                             if (breakpointsDisabled) {
                                 if (eventSet.suspendPolicy() == EventRequest.SUSPEND_ALL) {
                                    staledEvents.add(eventSet);
                                    eventSet.resume();
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("RESUMING "+eventSet);
                                    }
                                 }
                                 continue;
                             }
                         }
                     }
                     Map<Event, Executor> eventsToProcess = new HashMap<Event, Executor>();
                     for (Event e: eventSet) {
                         EventRequest r = e.request();
                         Executor exec = (r != null) ? (Executor) r.getProperty ("executor") : null;
                         if (exec instanceof ConditionedExecutor) {
                             boolean success = ((ConditionedExecutor) exec).processCondition(e);
                             if (success) {
                                eventsToProcess.put(e, exec);
                             }
                         } else {
                             eventsToProcess.put(e, exec);
                         }
                     }
                     if (eventsToProcess.size() == 0) {
                         eventSet.resume();
                         continue;
                     }
                     boolean resume = true, startEventOnly = true;
                     int suspendPolicy = eventSet.suspendPolicy();
                     boolean suspendedAll = suspendPolicy == EventRequest.SUSPEND_ALL;
                     JPDAThreadImpl suspendedThread = null;
                     if (!silent && suspendedAll) debugger.notifySuspendAll();
                     if (suspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
                         ThreadReference tref = null;
                         for (Event e: eventSet) {
                            tref = getEventThread(e);
                            if (tref != null) {
                                break;
                            }
                         }
                         if (tref != null && !silent) {
                            suspendedThread = ((JPDAThreadImpl) debugger.getThread(tref));
                            suspendedThread.notifySuspended();
                         }
                     }
                     if (logger.isLoggable(Level.FINE)) {
                         switch (suspendPolicy) {
                             case EventRequest.SUSPEND_ALL:
                                 logger.fine("JDI new events (suspend all)=============================================");
                                 break;
                             case EventRequest.SUSPEND_EVENT_THREAD:
                                 logger.fine("JDI new events (suspend one)=============================================");
                                 break;
                             case EventRequest.SUSPEND_NONE:
                                 logger.fine("JDI new events (suspend none)=============================================");
                                 break;
                             default:
                                 logger.fine("JDI new events (?????)=============================================");
                                 break;
                         }
                         logger.fine("  event is silent = "+silent);
                     }
                     for (Event e: eventSet) {
                         if (!eventsToProcess.containsKey(e)) {
                             // Ignore events whose executor conditions did not evaluate successfully.
                             continue;
                         }
                         if ((e instanceof VMDeathEvent) ||
                                 (e instanceof VMDisconnectEvent)
                            ) {

                             if (logger.isLoggable(Level.FINE)) {
                                 printEvent (e, null);
                             }
                             synchronized (Operator.this) {
                                 stop = true;
                             }
                             break loop;
                         }

                         if ((e instanceof VMStartEvent) && (starter != null)) {
                             resume = resume & starter.exec (e);
                             //S ystem.out.println ("Operator.start VM"); // NOI18N
                             if (logger.isLoggable(Level.FINE)) {
                                 printEvent (e, null);
                             }
                             continue;
                         }
                         Executor exec = null;
                         if (e.request () == null) {
                             if (logger.isLoggable(Level.FINE)) {
                                 logger.fine("EVENT: " + e + " REQUEST: null"); // NOI18N
                             }
                         } else
                             exec = eventsToProcess.get(e);

                         if (logger.isLoggable(Level.FINE)) {
                             printEvent (e, exec);
                         }

                         // safe invocation of user action
                         if (exec != null)
                             try {
                                 startEventOnly = false;
                                 resume = resume & exec.exec (e);
                             } catch (VMDisconnectedException exc) {
//                                 disconnected = true;
                                 synchronized (Operator.this) {
                                     stop = true;
                                 }
                                 //S ystem.out.println ("EVENT: " + e); // NOI18N
                                 //S ystem.out.println ("Operator end"); // NOI18N
                                 break loop;
                             } catch (Exception ex) {
                                 ErrorManager.getDefault().notify(ex);
                             }
                     } // while
                     //            S ystem.out.println ("END (" + set.suspendPolicy () + ") ==========================================================================="); // NOI18N
                     if (logger.isLoggable(Level.FINE)) {
                         logger.fine("JDI events dispatched (resume " + (resume && (!startEventOnly)) + ")");
                         logger.fine("  resume = "+resume+", startEventOnly = "+startEventOnly);
                     }
                     if (!startEventOnly) {
                         if (resume) {
                             if (!silent && suspendedAll) {
                                 //TODO: Not really all might be suspended!
                                 debugger.notifyToBeResumedAll();
                             }
                             if (!silent && suspendedThread != null) {
                                 suspendedThread.notifyToBeResumed();
                             }
                             synchronized (resumeLock) {
                                eventSet.resume ();
                             }
                         } else if (!silent && (suspendedAll || suspendedThread != null)) {
                            Session session = debugger.getSession();
                            if (session != null) {
                                DebuggerManager.getDebuggerManager().setCurrentSession(session);
                            }
                            ThreadReference tref = null;
                            for (Event e: eventSet) {
                                tref = getEventThread(e);
                                if (tref != null) {
                                    break;
                                }
                             }
                            if (tref != null) debugger.setStoppedState (tref);
                         }
                     }
                     if (!silent && !resume) { // Check for multiply-suspended threads
                         synchronized (resumeLock) {
                             List<ThreadReference> threads = eventSet.virtualMachine().allThreads();
                             for (ThreadReference t : threads) {
                                 try {
                                     JPDAThreadImpl jt = (JPDAThreadImpl) debugger.getExistingThread(t);
                                     while (t.suspendCount() > 1) {
                                         if (jt != null) {
                                             jt.notifyToBeResumed();
                                         }
                                         t.resume();
                                     } // while
                                 } catch (ObjectCollectedException e) {
                                 }
                             } // for
                         }
                     }
                 } catch (VMDisconnectedException e) {
                     break;
                 //} catch (InterruptedException e) {
                 } catch (Exception e) {
                     ErrorManager.getDefault().notify(e);
                 }
             }// for
             if (finalizer != null) finalizer.run ();
             //S ystem.out.println ("Operator end"); // NOI18N
             finalizer = null;
             eventQueue = null;
             starter = null;
         }
     }, "Debugger operator thread"); // NOI18N
    }

    private static final ThreadReference getEventThread(Event e) {
        ThreadReference tref = null;
        if (e instanceof LocatableEvent) {
            tref = ((LocatableEvent) e).thread();
        } else
        if (e instanceof ClassPrepareEvent) {
            tref = ((ClassPrepareEvent) e).thread();
        } else
        if (e instanceof ThreadStartEvent) {
            tref = ((ThreadStartEvent) e).thread();
        } else
        if (e instanceof ThreadDeathEvent) {
            tref = ((ThreadDeathEvent) e).thread();
        }
        return tref;
    }

    /**
    * Starts checking of JPDA messages.
    */
    public void start () {
        thread.start ();
    }

    /**
     * Binds the specified object with the event request.
     * If the request is already bound with another object,
     * the old binding is removed.
     *
     * @param  req  request
     * @param  e  object to be bound with the request
     *            (if <TT>null</TT>, the binding is removed - the same as <TT>unregister()</TT>)
     * @see  #unregister
     */
    public synchronized void register (EventRequest req, Executor e) {
        req.putProperty ("executor", e); // NOI18N
        if (staledEvents.size() > 0 && req instanceof StepRequest) {
            boolean addAsStaled = false;
            for (Iterator<EventSet> it = staledEvents.iterator(); it.hasNext(); ) {
                EventSet evSet = it.next();
                for (Iterator<Event> itSet = evSet.iterator(); itSet.hasNext(); ) {
                    Event ev = itSet.next();
                    EventRequest evReq = ev.request();
                    if (!(evReq instanceof StepRequest)) {
                        addAsStaled = true;
                        break;
                    } else {
                        ThreadReference evThread = ((StepRequest) evReq).thread();
                        ThreadReference reqThread = ((StepRequest) req).thread();
                        if (reqThread.equals(evThread)) {
                            addAsStaled = true;
                            break;
                        }
                    }
                }
                if (addAsStaled) break;
            }
            // Will be added if there is not a staled step event or if all staled
            // step events are on different threads.
            if (addAsStaled) {
                staledRequests.add(req);
            }
        };
    }

    /**
     * Removes binding between the specified event request and a registered object.
     *
     * @param  req  request
     * @see  #register
     */
    public synchronized void unregister (EventRequest req) {
        Executor e = (Executor) req.getProperty("executor");
        req.putProperty ("executor", null); // NOI18N
        if (e != null) {
            e.removed(req);
        }
        staledRequests.remove(req);
        if (req instanceof StepRequest) {
            ThreadReference tr = ((StepRequest) req).thread();
            ((JPDAThreadImpl) debugger.getThread(tr)).setInStep(false, null);
        }
    }

    /**
     * Stop the operator thread.
     */
    public void stop() {
        synchronized (this) {
            staledRequests.clear();
            staledEvents.clear();
            if (stop) return ; // Do not interrupt the thread when we're stopped
            stop = true;
            if (canInterrupt) {
                thread.interrupt();
            }
        }
    }

    /**
     * Notifies that breakpoints were disabled and therefore no breakpoint events should occur
     * until {@link #breakpointsEnabled} is called.
     */
    public synchronized void breakpointsDisabled() {
        breakpointsDisabled = true;
    }

    /**
     * Notifies that breakpoints were enabled again and therefore breakpoint events can occur.
     */
    public synchronized void breakpointsEnabled() {
        breakpointsDisabled = false;
    }

    public boolean flushStaledEvents() {
        boolean areStaledEvents;
        synchronized (this) {
            areStaledEvents = staledEvents.size() > 0;
            if (areStaledEvents) {
                thread.interrupt();
            }
        }
        return areStaledEvents;
    }

    private void printEvent (Event e, Executor exec) {
        try {
            if (e instanceof ClassPrepareEvent) {
                logger.fine("JDI EVENT: ClassPrepareEvent " + ((ClassPrepareEvent) e).referenceType ()); // NOI18N
            } else
            if (e instanceof ClassUnloadEvent) {
                logger.fine("JDI EVENT: ClassUnloadEvent " + ((ClassUnloadEvent) e).className ()); // NOI18N
            } else
            if (e instanceof ThreadStartEvent) {
                try {
                    logger.fine("JDI EVENT: ThreadStartEvent " + ((ThreadStartEvent) e).thread ()); // NOI18N
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: ThreadStartEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof ThreadDeathEvent) {
                try {
                    logger.fine("JDI EVENT: ThreadDeathEvent " + ((ThreadDeathEvent) e).thread ()); // NOI18N
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: ThreadDeathEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof MethodEntryEvent) {
                try {
                    logger.fine("JDI EVENT: MethodEntryEvent " + e);
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: MethodEntryEvent " + e);
                }
            } else
            if (e instanceof BreakpointEvent) {
                logger.fine("JDI EVENT: BreakpointEvent " + ((BreakpointEvent) e).thread () + " : " + ((BreakpointEvent) e).location ()); // NOI18N
            } else
            if (e instanceof StepEvent) {
                logger.fine("JDI EVENT: StepEvent " + ((StepEvent) e).thread () + " : " + ((StepEvent) e).location ()); // NOI18N
            } else
                logger.fine("JDI EVENT: " + e + " : " + exec); // NOI18N
        } catch (Exception ex) {
        }
    }
}
