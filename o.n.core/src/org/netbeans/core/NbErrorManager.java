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

package com.netbeans.developer.impl;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.TopManager;

/** This is the implementation of the famous exception manager.
*
* @author  Jaroslav Tulach
*/
final class NbErrorManager extends ErrorManager {
  /** maps Throwables to java.util.List (Ann) */
  private Map map = new WeakHashMap (11);

  /** assciates each thread with the lastly notified throwable 
  * (Thread, Reference (Throwable))
  */
  private Map lastException = new WeakHashMap (27);

  /** Creates new NbExceptionManager. */
  public NbErrorManager() {
    System.setProperty("sun.awt.exception.handler", "com.netbeans.developer.impl.NbErrorManager$AWTHandler");
  }
  
  /** Adds these values. All the 
  * previous annotations are kept and this new is added at 
  * the top of the annotation stack (index 0 of the annotation
  * array).
  *
  * @param severity integer describing severity (one of const values
  *   from this class)
  * @param date date or null
  * @param message message to attach to the exception or null
  * @param localizedMessage localized message for the user or null
  * @param stackTrace exception representing the stack trace or null
  */
  public synchronized Throwable annotate (
    Throwable t,
    int severity, String message, String localizedMessage, 
    Throwable stackTrace, java.util.Date date
  ) {
    Object o = map.get (t);

    LinkedList ll;
    if (o == null) {
      ll = new LinkedList ();
      map.put (t, ll);
    } else if (o instanceof LinkedList) {
      ll = (LinkedList)o;
    } else {
      // o should still implement List interface
      ll = new LinkedList ((List)o);
    }

    ll.addFirst(
      new Ann (severity, message, localizedMessage, stackTrace, date)
    );

    // remember last exception of this thread
    lastException.put (Thread.currentThread(), new WeakReference (t));

     return t;
  }


  /** Associates annotations with this thread.
  *
  * @param arr array of annotations (or null)
  */
  public synchronized Throwable attachAnnotations (Throwable t, Annotation[] arr) {
    map.put (t, Arrays.asList(arr));
    lastException.put (Thread.currentThread(), new WeakReference (t));

    return t;
  }

  /** Notifies all the exceptions associated with
  * this thread.
  * @param clear should the current exception be cleared or not?
  */
  public synchronized void notify (int severity, Throwable t) {
    // synchronized to ensure that only one exception is 
    // written to the thread
    
    Annotation[] ann = findAnnotations (t);

    if (ann == null) {
      // check whether there is another annotation associated with this thread
      // this could be useful when an exception occures during clean-up and
      // the previous annotation could be lost
      Reference r = (Reference)lastException.get (Thread.currentThread ());
      if (r != null) {
        Throwable lastT = (Throwable)r.get ();
        if (lastT != null) {
          ann = findAnnotations (lastT);
        }
      }
    }

    Exc ex = new Exc (t, severity, ann);
    
    
    PrintWriter ps = new PrintWriter (
      TopLogging.getLogOutputStream ()
    );
    
    ps.println ("*********** Exception occured ************"); // NOI18N
    
        
    /** If netbeans.debug.exceptions is set, print the exception to console */
    if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
      PrintWriter pw = new PrintWriter (System.err);
      ex.printStackTrace (pw);
      pw.flush();
    }

    // log into the log file
    ex.printStackTrace(ps);
    
    ps.flush();
    
    if (ex.getSeverity () != INFORMATIONAL) {
      NotifyException.notify (ex);
    }
  }


  /** Finds annotations associated with given exception.
  * @param t the exception
  * @return array of annotations or null
  */
  public synchronized Annotation[] findAnnotations (Throwable t) {
    List l = (List)map.get (t);
    Annotation[] arr;
    if (l == null) {
      arr = null;
    } else {
      arr = new Annotation[l.size ()];
      l.toArray (arr);
    }

    return arr;
  }

  /** Implementation of annotation interface.
  */
  private static final class Ann extends Object
  implements ErrorManager.Annotation {
    private int severity;
    private String message;
    private String localizedMessage;
    private Throwable stackTrace;
    private Date date;
    
    /** Constructor.
    */
    public Ann (
      int severity,
      String message,
      String localizedMessage,
      Throwable stackTrace,
      Date date
    ) {
      this.severity = severity;
      this.message = message;
      this.localizedMessage = localizedMessage;
      this.stackTrace = stackTrace;
      this.date = date;
    }
    
    /** Non-localized message.
     * @return associated message or null
     */
    public String getMessage() {
      return message;
    }
    /** Localized message.
     * @return message to be presented to the user or null
     */
    public String getLocalizedMessage() {
      return localizedMessage;
    }
    /** Stack trace. The stack trace should locate the method
     * and possition in the method where error occured.
     *
     * @return exception representing the location of error or null
     */
    public Throwable getStackTrace() {
      return stackTrace;
    }
    /** Date when the exception occured.
     * @return the date or null
     */
    public java.util.Date getDate() {
      return date;
    }
    /** Severity of the exception.
     * @return number representing the severity
     */
    public int getSeverity() {
      return severity;
    }
  } // end of Ann
  
  /** Another final class that is used to communicate with
  * NotifyException and provides enough information to the dialog.
  */
  static final class Exc extends Object {
    /** the original throwable */
    private Throwable t;

    private Annotation[] arr;
    private int severity;
    
    /** @param severity if -1 then we will compute the 
    * severity from annotations
    */
    public Exc (Throwable t, int severity, Annotation[] arr) {
      this.t = t;
      this.severity = severity;
      this.arr = arr == null ? new Annotation[0] : arr;
    }
    
    /** @return message */
    public String getMessage () {
      return (String)find (1);
    } 
    
    /** @return localized message */
    public String getLocalizedMessage () {
      return (String)find (2);
    }
    
    /** @return class name of the exception */
    public String getClassName () {
      return (String)find (3);
    }
    
    /** @return the severity of the exception */
    public int getSeverity () {
      if (severity != UNKNOWN) {
        return severity;
      }
      
      for (int i = 0; i < arr.length; i++) {
        int s = arr[i].getSeverity ();
        if (s > severity) {
          severity = s;
        }
      }
      
      if (severity == UNKNOWN) {
        // no severity specified, assume this is an error
        severity = t instanceof Error ? ERROR : EXCEPTION;
      }
      
      return severity;
    }
    
    /** @return date assigned to the exception */
    public Date getDate () {
      return (Date)find (4);
    }
    
    /** Prints stack trace of all annotations and if 
    * there is no annotation trace then of the exception
    */
    public void printStackTrace (PrintWriter pw) {
      pw.print (getDate ());
      pw.print (getClassName ());
      pw.print (": "); // NOI18N
      pw.print (getMessage ());
      pw.println ();
      
      int cnt = 0;
      
      for (int i = 0; i < arr.length; i++) {
        if (arr[i].getStackTrace() != null) {
          cnt++;
          arr[i].getStackTrace ().printStackTrace(pw);
        }
      }
      
      if (cnt == 0) {
        // ok, no stack printed log the original exception
        t.printStackTrace(pw);
      }
    }
    
    /** Method that iterates over annotations to find out
    * the first annotation that brings the requested value.
    *
    * @param kind what to look for (1, 2, 3, 4, ...);
    * @return the found object
    */
    private Object find (int kind) {
      for (int i = 0; i < arr.length; i++) {
        Annotation a = arr[i];
        
        Object o = null;
        switch (kind) {
        case 1: // message
          o = a.getMessage (); break;
        case 2: // localized
          o = a.getLocalizedMessage (); break;
        case 3: // class name
          {
            Throwable t = a.getStackTrace ();
            o = t == null ? null : t.getClass().getName();
            break;
          }
        case 4: // date
          o = a.getDate (); break;
        }
        
        if (o != null) {
          return o;
        }
      }
      
      switch (kind) {
      case 1: // message
        return t.getMessage ();
      case 2: // loc.msg.
        return t.getLocalizedMessage(); 
      case 3: // class name
        return t.getClass ().getName ();
      case 4: // date
        return new Date ();
      default:
        throw new IllegalArgumentException (
          "Unknown " + new Integer (kind) // NOI18N
        );
      }
    }
  }

  // part of bugfix #6120
  /** Instances are created in awt.EventDispatchThread */
  public static final class AWTHandler {
    
    /** The name MUST be handle and MUST be public */
    public static void handle(Throwable t) {
      if (t instanceof com.netbeans.developer.impl.execution.ExitSecurityException) {
        return;
      }
      TopManager.getDefault().getErrorManager().notify((ERROR << 1), t);
    }
  }
}

/* 
* Log
*  3    Jaga      1.2         4/10/00  Ales Novak      #6120
*  2    Jaga      1.1         4/5/00   Jaroslav Tulach Works even there is no 
*       annotation associated with the exception.
*  1    Jaga      1.0         4/4/00   Jaroslav Tulach 
* $ 
*/ 
  