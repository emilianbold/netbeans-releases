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
                System.out.println ("EVENT: " + e + " REQUEST: null");          
              } else 
                exec = (Executor) e.request ().getProperty ("executor");
              
              printEvent (e, exec);

              // safe invocation of user action
              if (exec != null) 
                try {
                  exec.exec (e);
                } catch (Exception ex) {
                  ex.printStackTrace ();
                }
            }
//            System.out.println ("END (" + set.suspendPolicy () + ") ===========================================================================");
            if (resume) {
              resume = false;
              virtualMachine.resume ();
            }
          }
        } catch (InterruptedException e) {
        } catch (VMDisconnectedException e) {
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
    try {
      if (e instanceof ClassPrepareEvent) {
        System.out.println ("EVENT: ClassPrepareEvent " + ((ClassPrepareEvent) e).referenceType ());
      } else
      if (e instanceof ClassUnloadEvent) {
        System.out.println ("EVENT: ClassUnloadEvent " + ((ClassUnloadEvent) e).className ());
      } else
      if (e instanceof ThreadStartEvent) {
        try {
          System.out.println ("EVENT: ThreadStartEvent " + ((ThreadStartEvent) e).thread ());
        } catch (Exception ex) {
          System.out.println ("EVENT: ThreadStartEvent1 " + e);
        }
      } else
      if (e instanceof ThreadDeathEvent) {
        try {
          System.out.println ("EVENT: ThreadDeathEvent " + ((ThreadDeathEvent) e).thread ());
        } catch (Exception ex) {
          System.out.println ("EVENT: ThreadDeathEvent1 " + e);
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
//        System.out.println ("EVENT: BreakpointEvent " + ((BreakpointEvent) e).thread () + " : " + ((BreakpointEvent) e).location ());
      } else
      if (e instanceof StepEvent) {
        System.out.println ("EVENT: BreakpointEvent " + ((StepEvent) e).thread () + " : " + ((StepEvent) e).location ());
      } else
        System.out.println ("EVENT: " + e + " : " + exec);          
    } catch (Exception ex) {
    }
  }
}

/*
 * Log
 *  8    Gandalf   1.7         1/4/00   Jan Jancura     Use trim () on user 
 *       input.
 *  7    Gandalf   1.6         11/9/99  Jan Jancura     sout commented out.
 *  6    Gandalf   1.5         11/8/99  Jan Jancura     Somma classes renamed
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/28/99  Jan Jancura     
 *  3    Gandalf   1.2         9/28/99  Jan Jancura     
 *  2    Gandalf   1.1         9/2/99   Jan Jancura     
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
