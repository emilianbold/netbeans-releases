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

import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;


/**
* Operator listens on debugger events and redirects them to the proper Executor.
*
* @author Jan Jancura
*/
public class Operator {

  /**
  * Creates operator.
  */
  public Operator (final EventQueue queue, final Runnable finalizer) {
    new Thread (new Runnable () {
      public void run () {
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
System.out.println ("EVENT: " + e);          
System.out.println ("Operator end");          
                return;
              }    
              if (e.request () == null) {
System.out.println ("EVENT: " + e);          
                continue;
              }  
              Executor exec = (Executor) e.request ().getProperty ("executor");
System.out.println ("EVENT: " + e + " : " + exec);          
              if (exec != null) exec.exec (e);
            }
System.out.println ("END (" + set.suspendPolicy () + ") ===========================================================================");
          }
        } catch (InterruptedException e) {
        }
        if (finalizer != null) finalizer.run ();
System.out.println ("Operator end");          
      }
    }, "Debugger operator thread").start ();
  }
  
  public static void register (EventRequest req, Executor e) {
    req.putProperty ("executor", e);
  }
}

/*
 * Log
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
