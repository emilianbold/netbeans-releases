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

/**
* Operator listens on debugger events and redirects them to the proper Executor.
*
* @author Jan Jancura
*/
public class Operator {

    private boolean           resume = false;
    private boolean           stopRequest = false;
    private boolean           disconnected = false;
    private Thread            thread;

    /**
    * Creates operator for given event queue.
    */
    public Operator (
        final VirtualMachine virtualMachine,
        final Runnable starter,
        final Runnable finalizer
    ) {
        thread = new Thread (new Runnable () {
         public void run () {
             EventQueue queue = virtualMachine.eventQueue ();
             try {
                 for (;;) {
                     EventSet set = queue.remove ();
                     resume = false;
                     stopRequest = false;
                     EventIterator i = set.eventIterator ();
                     while (i.hasNext ()) {
                         Event e = i.nextEvent ();
                         if ((e instanceof VMDeathEvent) ||
                                 (e instanceof VMDisconnectEvent)
                            ) {
                             disconnected = true;
                             if (finalizer != null) finalizer.run ();
                             //S ystem.out.println ("EVENT: " + e); // NOI18N
                             //S ystem.out.println ("Operator end"); // NOI18N
                             return;
                         }
                         if ((e instanceof VMStartEvent) && (starter != null)) {
                             starter.run ();
                             //S ystem.out.println ("Operator.start VM"); // NOI18N
                             continue;
                         }
                         Executor exec = null;
                         if (e.request () == null) {
                             //S ystem.out.println ("EVENT: " + e + " REQUEST: null"); // NOI18N
                         } else
                             exec = (Executor) e.request ().getProperty ("executor");

                         // printEvent (e, exec);

                         // safe invocation of user action
                         if (exec != null)
                             try {
                                 exec.exec (e);
                             } catch (Exception ex) {
                                 ex.printStackTrace ();
                             }
                     }
                     //            S ystem.out.println ("END (" + set.suspendPolicy () + ") ==========================================================================="); // NOI18N
                     if (resume && !stopRequest)
                         virtualMachine.resume ();
                 }
             } catch (VMDisconnectedException e) {   
             } catch (InterruptedException e) {
             } catch (Exception e) {
                 e.printStackTrace ();
             }
             if (finalizer != null) finalizer.run ();
             //S ystem.out.println ("Operator end"); // NOI18N
         }
     }, "Debugger operator thread"); // NOI18N
    }

    /**
    * Starts checking of JPDA messages.
    */
    public void start () {
        thread.start ();
    }

    public void register (EventRequest req, Executor e) {
        req.putProperty ("executor", e); // NOI18N
    }

    public void unregister (EventRequest req) {
        req.putProperty ("executor", null); // NOI18N
    }

    /**
    * Requests resume after curent event set dispatch.
    */
    public void resume () {
        resume = true;
    }

    /**
    * Requests stop after curent event set dispatch.
    */
    public void stopRequest () {
        stopRequest = true;
    }
    
    public boolean isDisconnected () {
        return disconnected;
    }

    private void printEvent (Event e, Executor exec) {
        try {
            if (e instanceof ClassPrepareEvent) {
                System.out.println ("EVENT: ClassPrepareEvent " + ((ClassPrepareEvent) e).referenceType ()); // NOI18N
            } else
                if (e instanceof ClassUnloadEvent) {
                    System.out.println ("EVENT: ClassUnloadEvent " + ((ClassUnloadEvent) e).className ()); // NOI18N
                } else
                    if (e instanceof ThreadStartEvent) {
                        try {
                            System.out.println ("EVENT: ThreadStartEvent " + ((ThreadStartEvent) e).thread ()); // NOI18N
                        } catch (Exception ex) {
                            System.out.println ("EVENT: ThreadStartEvent1 " + e); // NOI18N
                        }
                    } else
                        if (e instanceof ThreadDeathEvent) {
                            try {
                                System.out.println ("EVENT: ThreadDeathEvent " + ((ThreadDeathEvent) e).thread ()); // NOI18N
                            } catch (Exception ex) {
                                System.out.println ("EVENT: ThreadDeathEvent1 " + e); // NOI18N
                            }
                        } else
                            if (e instanceof MethodEntryEvent) {
                                /*        try {
                                          System.out.println ("EVENT: MethodEntryEvent " + e);
                                        } catch (Exception ex) {
                                          System.out.println ("EVENT: MethodEntryEvent " + e);
                                        }*/
                            } else
                                if (e instanceof BreakpointEvent) {
                                    //        System.out.println ("EVENT: BreakpointEvent " + ((BreakpointEvent) e).thread () + " : " + ((BreakpointEvent) e).location ()); // NOI18N
                                } else
                                    if (e instanceof StepEvent) {
                                        System.out.println ("EVENT: BreakpointEvent " + ((StepEvent) e).thread () + " : " + ((StepEvent) e).location ()); // NOI18N
                                    } else
                                        System.out.println ("EVENT: " + e + " : " + exec); // NOI18N
        } catch (Exception ex) {
        }
    }
}
