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

package org.netbeans.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.io.*;
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

    /** The writer to the log file*/
    private PrintWriter logWriter = null;
    
   /** assciates each thread with the lastly notified throwable
    * (Thread, Reference (Throwable))
    */
    private Map lastException = new WeakHashMap (27);

    /** Minimum value of severity to write message to the log file*/
    private int minLogSeverity = ErrorManager.INFORMATIONAL;

    /** Prefix preprended to customized loggers, if any. */
    private String prefix = null;
    
    /** Creates new NbExceptionManager. */
    public NbErrorManager() {
        System.setProperty("sun.awt.exception.handler", "org.netbeans.core.NbErrorManager$AWTHandler");
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


        PrintWriter ps = getLogWriter ();

        if (prefix != null) ps.print ("[" + prefix + "] "); // NOI18N
        ps.println ("*********** Exception occurred ************"); // NOI18N
        ps.flush ();


        /** If netbeans.debug.exceptions is set, print the exception to console */
        if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
            PrintWriter pw = new PrintWriter (System.err);
            ex.printStackTrace (pw);
            pw.flush();
            // This will show up in the log too, of course.
        } else {
            ex.printStackTrace(ps);
        }

        ps.flush();

        if (ex.getSeverity () > INFORMATIONAL) {
            NotifyException.notify (ex);
        }
    }

    public void log(int severity, String s) {
        if(severity >= minLogSeverity) {
            if (prefix != null)
                getLogWriter ().print ("[" + prefix + "] "); // NOI18N
            getLogWriter().println(s);
            getLogWriter().flush(); 
        }
    }
    
    /** Allows to test whether messages with given severity will be logged
     * or not prior to constraction of complicated and time expensive
     * logging messages.
     *
     * @param severity the severity to check
     * @return false if the next call to log method with the same severity will
     *    discard the message
     */
    public boolean isLoggable (int severity) {
        return severity >= minLogSeverity;
    }
    
    
    /** Returns an instance with given name. The name
     * can be dot separated list of names creating
     * a hierarchy.
     */
    public final ErrorManager getInstance(String name) {
        // TODO: change this to create a hierarchy
        // [jglick] is the following enough or is more needed?
        NbErrorManager newEM = new NbErrorManager();
        newEM.prefix = (prefix == null) ? name : prefix + '.' + name;
        newEM.minLogSeverity = minLogSeverity;
        String prop = newEM.prefix;
        while (prop != null) {
            String value = System.getProperty (prop);
            //System.err.println ("Trying; prop=" + prop + " value=" + value);
            if (value != null) {
                try {
                    newEM.minLogSeverity = Integer.parseInt (value);                    
                } catch (NumberFormatException nfe) {
                    notify (WARNING, nfe);
                }
                break;
            } else {
                int idx = prop.lastIndexOf ('.');
                if (idx == -1)
                    prop = null;
                else
                    prop = prop.substring (0, idx);
            }
        }
        //System.err.println ("getInstance: prefix=" + prefix + " mls=" + minLogSeverity + " name=" + name + " prefix2=" + newEM.prefix + " mls2=" + newEM.minLogSeverity);
        return newEM;
    }    

    /** Finds annotations associated with given exception.
    * @param t the exception
    * @return array of annotations or null
    */
    public synchronized Annotation[] findAnnotations (Throwable t) {
        // [PENDING] probably this should work recursively?
        // Or Exc should look for its own sub-annotations and deal
        // with them sensibly.
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

    /** Lazy getter for the writer */
    private PrintWriter getLogWriter() {
        if (logWriter == null) {
            logWriter = new PrintWriter (TopLogging.getLogOutputStream ());
        }
        return logWriter;
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
    final class Exc extends Object {
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
	
	public boolean isLocalized() {
	    if (find(2, false) == null) {
		String locMsg = getLocalizedMessage();
		return locMsg != null && !locMsg.equals(getMessage());
	    } 
	    else
		return true;	
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
            /*Heaeder */
            pw.print (getDate ());
            pw.print (getClassName ());
            pw.print (": "); // NOI18N
            pw.print (getMessage ());
            pw.println ();
            
            /*Annotations */
          for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) continue;
                
                Throwable thr = arr[i].getStackTrace();                
                String annotation = arr[i].getLocalizedMessage();
                
                if (annotation == null) annotation = arr[i].getMessage();
                if (annotation == null && thr != null) annotation = thr.getLocalizedMessage();
                if (annotation == null && thr != null) annotation = thr.getMessage();
                
                if (annotation != null) {
                    if (thr != null) pw.println ("Nested annotation: "+annotation);// NOI18N
                    else pw.println ("Annotation: "+annotation);// NOI18N
                }                
            }            
            
            // ok, print trace of the original exception too
            //t.printStackTrace(pw);
            // Attempt to show an annotation indicating where the exception
            // was caught. Not 100% reliable but often helpful.
            String[] tLines = decompose (t);
            String[] hereLines = decompose (new Throwable ());
            int idx = -1;
            for (int i = 1; i <= Math.min (tLines.length, hereLines.length); i++) {
                if (! tLines[tLines.length - i].equals (hereLines[hereLines.length - i])) {
                    idx = tLines.length - i;
                    break;
                }
            }
            for (int i = 0; i < tLines.length; i++) {
                if (i == idx) {
                    pw.print ("[catch]"); // NOI18N
                    // Also translate following tab -> space since formatting is bad in
                    // Output Window (#8104) and some mail agents screw it up etc.
                    if (tLines[i].charAt (0) == '\t') {
                        pw.print (' ');
                        tLines[i] = tLines[i].substring (1);
                    }
                }
                pw.println (tLines[i]);
            }
            /*Nested annotations */            
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) continue;
                
                Throwable thr = arr[i].getStackTrace();
                if (thr != null) {
                    Annotation[] ans = findAnnotations (thr);
                    Exc ex = new Exc (thr, 0, ans);
                    pw.println();
                    ex.printStackTrace (pw);    
                }
            }
        }

        /** Get a throwable's stack trace, decomposed into individual lines. */
        private  String[] decompose (Throwable t) {
            StringWriter sw = new StringWriter ();
            t.printStackTrace (new PrintWriter (sw));
            StringTokenizer tok = new StringTokenizer (sw.toString (), "\n\r"); // NOI18N
            int c = tok.countTokens ();
            String[] lines = new String[c];
            for (int i = 0; i < c; i++)
                lines[i] = tok.nextToken ();
            return lines;
        }

        /** Method that iterates over annotations to find out
        * the first annotation that brings the requested value.
        *
        * @param kind what to look for (1, 2, 3, 4, ...);
        * @return the found object
        */
        private Object find (int kind) {
	    return find(kind, true);
	}

        /** Method that iterates over annotations to find out
        * the first annotation that brings the requested value.
        *
        * @param kind what to look for (1, 2, 3, 4, ...);
        * @return the found object
        */
        private Object find (int kind, boolean def) {
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
	    
	    if (!def)
		return null;

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
            if (t instanceof org.netbeans.core.execution.ExitSecurityException) {
                return;
            }
            TopManager.getDefault().getErrorManager().notify((ERROR << 1), t);
        }
    }

}
