/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.debugger.jpda.util;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;

/**
* Operator listens on debugger events and redirects them to the proper Executor.
*
* @author Jan Jancura
*/
public class Operator {

  private boolean           resume = false;
  
  /**
  * Creates operator for given event queue.
  */
  public Operator (
    final VirtualMachine virtualMachine, 
    final Runnable starter,
    final Runnable finalizer
  ) {
    new Thread (new Runnable () {
      public void run () {
        EventQueue queue = virtualMachine.eventQueue ();
        try {
          for (;;) {
            EventSet set = queue.remove ();
            EventIterator i = set.eventIterator ();
            while (i.hasNext ()) {
              Event e = i.nextEvent ();
              if ((e instanceof VMDeathEvent) || 
                  (e instanceof VMDisconnectEvent)
              ) {
                if (finalizer != null) finalizer.run ();
                //S ystem.out.println ("EVENT: " + e);          
                //S ystem.out.println ("Operator end");          
                return;
              }    
              if ((e instanceof VMStartEvent) && (starter != null)) {
                starter.run ();
                //S ystem.out.println ("Operator.start VM");          
                continue;
              }
              Executor exec = null;
              if (e.request () == null) {
                //S ystem.out.println ("EVENT: " + e + " REQUEST: null");          
              } else 
                exec = (Executor) e.request ().getProperty ("executor");
              
              //printEvent (e, exec);

              // safe invocation of user action
              if (exec != null) 
                try {
                  exec.exec (e);
                } catch (Exception ex) {
                  ex.printStackTrace ();
                } catch (Error ex) {
                  ex.printStackTrace ();
                }
            }
            //S ystem.out.println ("END (" + set.suspendPolicy () + ") ===========================================================================");
            if (resume) {
              resume = false;
              virtualMachine.resume ();
            }
          }
        } catch (InterruptedException e) {
        }
        if (finalizer != null) finalizer.run ();
        //S ystem.out.println ("Operator end");          
      }
    }, "Debugger operator thread").start ();
  }
  
  public void register (EventRequest req, Executor e) {
    req.putProperty ("executor", e);
  }
  
  /**
  * Calls resume after curent event set dispatch.
  */
  public void resume () {
    resume = true;
  }
  
  private void printEvent (Event e, Executor exec) {
    if (e instanceof ClassPrepareEvent) {
      System.out.println ("EVENT: ClassPrepareEvent " + ((ClassPrepareEvent) e).referenceType ());
    } else
    if (e instanceof ClassUnloadEvent) {
      System.out.println ("EVENT: ClassUnloadEvent " + ((ClassUnloadEvent) e).className ());
    } else
    if (e instanceof ThreadStartEvent) {
      System.out.println ("EVENT: ThreadStartEvent " + ((ThreadStartEvent) e).thread ());
    } else
    if (e instanceof ThreadDeathEvent) {
      System.out.println ("EVENT: ThreadDeathEvent " + ((ThreadDeathEvent) e).thread ());
    } else
    if (e instanceof BreakpointEvent) {
      System.out.println ("EVENT: BreakpointEvent " + ((BreakpointEvent) e).thread () + " : " + ((BreakpointEvent) e).location ());
    } else
    if (e instanceof StepEvent) {
      System.out.println ("EVENT: BreakpointEvent " + ((StepEvent) e).thread () + " : " + ((StepEvent) e).location ());
    } else
      System.out.println ("EVENT: " + e + " : " + exec);          
  }
}

/*
 * Log
 *  2    Gandalf   1.1         9/2/99   Jan Jancura     
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
