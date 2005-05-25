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

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
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

    private Thread            thread;

    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.jdievents") != null;

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
        Executor starter,
        Runnable finalizer
    ) {
        EventQueue eventQueue = virtualMachine.eventQueue ();
        if (eventQueue == null) 
            throw new NullPointerException ();
        final Object[] params = new Object[] {eventQueue, starter, finalizer};
        thread = new Thread (new Runnable () {
        public void run () {
            EventQueue eventQueue = (EventQueue) params [0];
            Executor starter = (Executor) params [1];
            Runnable finalizer = (Runnable) params [2];
            params [0] = null;
            params [1] = null;
            params [2] = null;
            
             try {
                 for (;;) {
                     EventSet eventSet = eventQueue.remove ();
                     boolean resume = true, startEventOnly = true;
                     EventIterator i = eventSet.eventIterator ();
                     if (verbose)
                         switch (eventSet.suspendPolicy ()) {
                             case EventRequest.SUSPEND_ALL:
                                 System.out.println("\nJDI new events (suspend all)=============================================");
                                 break;
                             case EventRequest.SUSPEND_EVENT_THREAD:
                                 System.out.println("\nJDI new events (suspend one)=============================================");
                                 break;
                             case EventRequest.SUSPEND_NONE:
                                 System.out.println("\nJDI new events (suspend none)=============================================");
                                 break;
                             default:
                                 System.out.println("\nJDI new events (?????)=============================================");
                                 break;
                         }
                     while (i.hasNext ()) {
                         Event e = i.nextEvent ();
                         if ((e instanceof VMDeathEvent) ||
                                 (e instanceof VMDisconnectEvent)
                            ) {

                             if (verbose)
                                 printEvent (e, null);
//                             disconnected = true;
                             if (finalizer != null) finalizer.run ();
                             //S ystem.out.println ("EVENT: " + e); // NOI18N
                             //S ystem.out.println ("Operator end2"); // NOI18N
                             finalizer = null;
                             eventQueue = null;
                             starter = null;
                             return;
                         }
                         
                         if ((e instanceof VMStartEvent) && (starter != null)) {
                             resume = resume & starter.exec (e);
                             //S ystem.out.println ("Operator.start VM"); // NOI18N
                             if (verbose)
                                 printEvent (e, null);
                             continue;
                         }
                         Executor exec = null;
                         if (e.request () == null) {
                             if (verbose)
                                 System.out.println ("EVENT: " + e + " REQUEST: null"); // NOI18N
                         } else
                             exec = (Executor) e.request ().getProperty ("executor");

                         if (verbose)
                             printEvent (e, exec);

                         // safe invocation of user action
                         if (exec != null)
                             try {
                                 startEventOnly = false;
                                 resume = resume & exec.exec (e);
                             } catch (VMDisconnectedException exc) {   
//                                 disconnected = true;
                                 if (finalizer != null) finalizer.run ();
                                 //S ystem.out.println ("EVENT: " + e); // NOI18N
                                 //S ystem.out.println ("Operator end"); // NOI18N
                                 return;
                             } catch (Exception ex) {
                                 ErrorManager.getDefault().notify(ex);
                             }
                     } // while
                     //            S ystem.out.println ("END (" + set.suspendPolicy () + ") ==========================================================================="); // NOI18N
                     if (verbose) {
                         System.out.println("JDI events dispatched (resume " + (resume && (!startEventOnly)) + ")");
                         System.out.println("  resume = "+resume+", startEventOnly = "+startEventOnly);
                     }
                     if (resume && (!startEventOnly)) {
                         eventSet.resume ();
                     }
                 }// for
             } catch (VMDisconnectedException e) {   
             } catch (InterruptedException e) {
             } catch (Exception e) {
                 ErrorManager.getDefault().notify(e);
             }
             if (finalizer != null) finalizer.run ();
             //S ystem.out.println ("Operator end"); // NOI18N
             finalizer = null;
             eventQueue = null;
             starter = null;
         }
     }, "Debugger operator thread"); // NOI18N
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
    public void register (EventRequest req, Executor e) {
        req.putProperty ("executor", e); // NOI18N
    }

    /**
     * Removes binding between the specified event request and a registered object.
     *
     * @param  req  request
     * @see  #register
     */
    public void unregister (EventRequest req) {
        req.putProperty ("executor", null); // NOI18N
    }

    private void printEvent (Event e, Executor exec) {
        try {
            if (e instanceof ClassPrepareEvent) {
                System.out.println ("\nJDI EVENT: ClassPrepareEvent " + ((ClassPrepareEvent) e).referenceType ()); // NOI18N
            } else
            if (e instanceof ClassUnloadEvent) {
                System.out.println ("\nJDI EVENT: ClassUnloadEvent " + ((ClassUnloadEvent) e).className ()); // NOI18N
            } else
            if (e instanceof ThreadStartEvent) {
                try {
                    System.out.println ("\nJDI EVENT: ThreadStartEvent " + ((ThreadStartEvent) e).thread ()); // NOI18N
                } catch (Exception ex) {
                    System.out.println ("\nJDI EVENT: ThreadStartEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof ThreadDeathEvent) {
                try {
                    System.out.println ("\nJDI EVENT: ThreadDeathEvent " + ((ThreadDeathEvent) e).thread ()); // NOI18N
                } catch (Exception ex) {
                    System.out.println ("\nJDI EVENT: ThreadDeathEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof MethodEntryEvent) {
                try {
                    System.out.println ("\nJDI EVENT: MethodEntryEvent " + e);
                } catch (Exception ex) {
                    System.out.println ("\nJDI EVENT: MethodEntryEvent " + e);
                }
            } else
            if (e instanceof BreakpointEvent) {
                System.out.println ("\nJDI EVENT: BreakpointEvent " + ((BreakpointEvent) e).thread () + " : " + ((BreakpointEvent) e).location ()); // NOI18N
            } else
            if (e instanceof StepEvent) {
                System.out.println ("\nJDI EVENT: StepEvent " + ((StepEvent) e).thread () + " : " + ((StepEvent) e).location ()); // NOI18N
            } else
                System.out.println ("\nJDI EVENT: " + e + " : " + exec); // NOI18N
        } catch (Exception ex) {
        }
    }
}
