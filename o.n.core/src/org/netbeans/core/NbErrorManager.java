/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.*;
import java.util.*;

import org.xml.sax.SAXParseException;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/** This is the implementation of the famous exception manager.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public final class NbErrorManager extends ErrorManager {

    public NbErrorManager() {
        this(null, defaultSeverity(), null);
    }
    
    /**
     * Construct for testing.
     * @see "#18141"
     */
    NbErrorManager(PrintStream pw) {
        this(null, defaultSeverity(), pw);
    }
    
    private static int defaultSeverity() {
        String dsev = System.getProperty("ErrorManager.minimum");
        // use e.g. 17 to avoid logging WARNING messages.
        if (dsev != null) {
            try {
                return Integer.parseInt(dsev);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        // I.e. 2: avoid logging INFORMATIONAL messages.
        return ErrorManager.INFORMATIONAL + 1;
    }
    
    private NbErrorManager(String pfx, int sev, PrintStream pw) {
        prefix = pfx;
        minLogSeverity = sev;
        logWriter = pw;
        synchronized (uniquifiedIds) {
            Integer i = (Integer)uniquifiedIds.get(pfx);
            if (i == null) {
                uniquifier = 1;
            } else {
                uniquifier = i.intValue() + 1;
            }
            uniquifiedIds.put(pfx, new Integer(uniquifier));
        }
        //System.err.println("NbErrorManager[" + pfx + " #" + uniquifier + "]: minLogSeverity=" + sev);
    }

    /** maps Throwables to java.util.List (Ann) */
    private static final Map map = new WeakHashMap (11);
    
    /** message to print date to */
    private static final java.text.MessageFormat EXC_HEADER = new java.text.MessageFormat (
        "{0}*********** Exception occurred ************ at {1,time,short} on {1,date,medium}", // NOI18N
        java.util.Locale.ENGLISH
    );

    /** The writer to the log file*/
    private PrintStream logWriter;
    
   /** assciates each thread with the lastly notified throwable
    * (Thread, Reference (Throwable))
    */
    private static final Map lastException = new WeakHashMap (27);

    /** Minimum value of severity to write message to the log file*/
    private final int minLogSeverity;

    /** Prefix preprended to customized loggers, if any. */
    private final String prefix;
    
    // Make sure two distinct EM impls log differently even with the same name.
    private final int uniquifier; // 0 for root EM (prefix == null), else >= 1
    private static final Map uniquifiedIds = new HashMap(20); // Map<String,Integer>
    
    static {
        System.setProperty("sun.awt.exception.handler", "org.netbeans.core.NbErrorManager$AWTHandler"); // NOI18N
    }
    
    /** Initializes the log stream.
     */
    private PrintStream getLogWriter () {
        synchronized (this) {
            if (logWriter != null) return logWriter;
            // to prevent further initializations during TopLogging.getLogOutputStream method
            logWriter = System.err;
        }
        
        PrintStream pw = org.netbeans.core.startup.TopLogging.getLogOutputStream();
        
        synchronized (this) {
            logWriter = pw;
            return logWriter;
        }
    }

    public synchronized Throwable annotate (
        Throwable t,
        int severity, String message, String localizedMessage,
        Throwable stackTrace, java.util.Date date
    ) {
        Object o = map.get (t);

        List ll;
        if (o == null) {
            ll = new ArrayList ();
            map.put (t, ll);
        } else {
            ll = (List)o;
        }

        ll.add(0,
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
        Object o = map.get (t);
        List l;
        if (o == null) {
            l = new ArrayList(arr.length + 5);
            map.put (t, l);
        } else {
            l = (List)o;
        }
        l.addAll(0, Arrays.asList(arr));
        
        lastException.put (Thread.currentThread(), new WeakReference (t));

        return t;
    }

    /** Honor configured min-severity levels, more or less.
     * Actually bump up the effective severity of an exception by one.
     * Thus by default INFORMATIONAL stack traces are displayed, but not
     * messages. By playing with min log severity levels, you can get
     * both, or neither.
     * @see "#24056"
     */
    public boolean isNotifiable(int severity) {
        return isLoggable(severity + 1);
    }

    /** Notifies all the exceptions associated with
    * this thread.
    */
    public synchronized void notify (int severity, Throwable t) {
        // synchronized to ensure that only one exception is
        // written to the thread
        
        Exc ex = createExc(t, severity);
        
        if (!isNotifiable(ex.getSeverity())) {
            return;
        }
        
        //issue 36878 - printing the stack trace on a user error is
        //disconcerting because it makes it look like something went wrong
        //with the software.
        
        //Note the algorithm below is different than that of Exc.getSeverity() -
        //there are cases (e.g. a property editor over a filesystem) where
        //an exception may be annotated as severe, but in the context
        //it is not - thus we check if *any* annotation is USER, rather than
        //that the highest level severity in the annotation is USER
        boolean wantStackTrace = severity != USER;
        if (wantStackTrace) {
            Annotation[] ann = findAnnotations(t);
            if (ann != null) {
                for (int i=0; i < ann.length; i++) {
                    if (ann[i] instanceof Ann) {
                        if (((Ann) ann[i]).getSeverity() == USER) {
                            wantStackTrace = false;
                            break;
                        }
                    }
                }
            }
        }
        if (wantStackTrace) {
            PrintStream log = getLogWriter();
            if (prefix != null)
                log.print ("[" + prefix + "] "); // NOI18N        
            String level = ex.getSeverity() == INFORMATIONAL ? "INFORMATIONAL " : "";// NOI18N
            
            
            log.println (EXC_HEADER.format (new Object[] { level, ex.getDate() }));
            ex.printStackTrace(log);
        }

        if (ex.getSeverity () > INFORMATIONAL) {
            NotifyException.notify (ex);
        }
    }
    
    /**
     * Just create the exception information for a throwable being notified.
     * Useful for the unit test.
     */
    Exc createExc(Throwable t, int severity) {
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
        lastException.remove (Thread.currentThread ());

        return new Exc (t, severity, ann, findAnnotations0(t, true, new HashSet()));
    }

    public void log(int severity, String s) {
        if (isLoggable (severity)) {
            //System.err.println(toString() + " logging '" + s + "' at " + severity);
            PrintStream log = getLogWriter ();
            
            if (prefix != null) {
                boolean showUniquifier;
                // Print a unique EM sequence # if there is more than one
                // with this name. Shortcut: if the # > 1, clearly there are.
                if (uniquifier > 1) {
                    showUniquifier = true;
                } else if (uniquifier == 1) {
                    synchronized (uniquifiedIds) {
                        int count = ((Integer)uniquifiedIds.get(prefix)).intValue();
                        showUniquifier = count > 1;
                    }
                } else {
                    throw new IllegalStateException("prefix != null yet uniquifier == 0");
                }
                if (showUniquifier) {
                    log.print ("[" + prefix + " #" + uniquifier + "] "); // NOI18N
                } else {
                    log.print ("[" + prefix + "] "); // NOI18N
                }
            }
            log.println(s);
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
        String pfx = (prefix == null) ? name : prefix + '.' + name;
        int sev = minLogSeverity;
        String prop = pfx;
        while (prop != null) {
            String value = System.getProperty (prop);
            //System.err.println ("Trying; prop=" + prop + " value=" + value);
            if (value != null) {
                try {
                    sev = Integer.parseInt (value);                    
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
        return new NbErrorManager(pfx, sev, logWriter);
    }    
    
    /** Method (or field) names in various exception classes which give
     * a nested exception. Fields should be public, methods public no-arg.
     * Field names should be prefixed with a dot.
     */
    private static Map NESTS = null; // Map<String,String>
    private static Throwable extractNestedThrowable(Throwable t) {
        synchronized (NbErrorManager.class) {
            if (NESTS == null) {
                NESTS = new HashMap(50);
                NESTS.put("java.lang.ClassNotFoundException", "getException"); // NOI18N
                NESTS.put("java.lang.ExceptionInInitializerError", "getException"); // NOI18N
                NESTS.put("java.lang.reflect.InvocationTargetException", "getTargetException"); // NOI18N
                NESTS.put("java.lang.reflect.UndeclaredThrowableException", "getUndeclaredThrowable"); // NOI18N
                NESTS.put("java.security.PrivilegedActionException", "getException"); // NOI18N
                NESTS.put("javax.naming.NamingException", "getRootCause"); // NOI18N
                NESTS.put("javax.xml.parsers.FactoryConfigurationError", "getException"); // NOI18N
                NESTS.put("javax.xml.transform.TransformerException", "getException"); // NOI18N
                NESTS.put("javax.xml.transform.TransformerFactoryConfigurationError", "getException"); // NOI18N
                NESTS.put("org.openide.compiler.CompilerGroupException", ".exception"); // NOI18N
                NESTS.put("org.openide.src.SourceException$IO", "getReason"); // NOI18N
                NESTS.put("org.openide.src.SourceException$Veto", "getReason"); // NOI18N
                NESTS.put("org.openide.src.SourceVetoException", "getNestedException"); // NOI18N
                NESTS.put("org.openide.util.MutexException", "getException"); // NOI18N
                NESTS.put("org.openide.util.io.OperationException", "getException"); // NOI18N
                NESTS.put("org.openide.util.io.SafeException", "getException"); // NOI18N
                NESTS.put("org.xml.sax.SAXException", "getException"); // NOI18N
            }
        }
        for (Class c = t.getClass(); c != Object.class; c = c.getSuperclass()) {
            String getter = (String)NESTS.get(c.getName());
            if (getter != null) {
                try {
                    if (getter.charAt(0) == '.') { // NOI18N
                        Field f = c.getField(getter.substring(1));
                        return (Throwable)f.get(t);
                    } else {
                        Method m = c.getMethod(getter, null);
                        return (Throwable)m.invoke(t, null);
                    }
                } catch (Exception e) {
                    // Should not happen.
                    System.err.println("From throwable class: " + c.getName()); // NOI18N
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /** Finds annotations associated with given exception.
    * @param t the exception
    * @return array of annotations or null
    */
    public synchronized Annotation[] findAnnotations (Throwable t) {
        return findAnnotations0(t, false, new HashSet());
    }

    /** If recursively is true it is not adviced to print all annotations
     * because a lot of warnings will be printed. But while searching for
     * localized message we should scan all the annotations (even recursively).
     */
    private synchronized Annotation[] findAnnotations0(Throwable t, boolean recursively, Set alreadyVisited) {
        List l = (List)map.get (t);
        // MissingResourceException should be printed nicely... --jglick
        if (t instanceof MissingResourceException) {
            if (l == null) {
                l = new ArrayList(1);
            } else {
                // make a copy, do not modify it
                l = new ArrayList(l);
            }
            MissingResourceException mre = (MissingResourceException) t;
            String cn = mre.getClassName ();
            if (cn != null) {
                l.add (new Ann (EXCEPTION, NbBundle.getMessage (NbErrorManager.class, "EXC_MissingResourceException_class_name", cn), null, null, null));
            }
            String k = mre.getKey ();
            if (k != null) {
                l.add (new Ann (EXCEPTION, NbBundle.getMessage (NbErrorManager.class, "EXC_MissingResourceException_key", k), null, null, null));
            }
            if (l.size () == 0) l = null; // not clear if null means something other than new Annotation[0]
        } else {
            // #15611: find all kinds of nested exceptions and deal with them too.
            // If and when JDK 1.4 causes are widely implemented, this will no
            // longer be necessary... would be able to use a single call.
            Throwable t2 = extractNestedThrowable(t);
            if (t2 != null) {
                if (l == null) {
                    l = new ArrayList(1);
                } else {
                    l = new ArrayList(l);
                }
                l.add(new Ann(UNKNOWN, null, null, t2, null));
            }
        }
        if (t instanceof SAXParseException) {
            // For some reason these fail to come with useful data, like location.
            SAXParseException spe = (SAXParseException)t;
            String pubid = spe.getPublicId();
            String sysid = spe.getSystemId();
            if (pubid != null || sysid != null) {
                int col = spe.getColumnNumber();
                int line = spe.getLineNumber();
                String msg;
                if (col != -1 || line != -1) {
                    msg = NbBundle.getMessage(NbErrorManager.class, "EXC_sax_parse_col_line", new Object[] {String.valueOf(pubid), String.valueOf(sysid), new Integer(col), new Integer(line)});
                } else {
                    msg = NbBundle.getMessage(NbErrorManager.class, "EXC_sax_parse", String.valueOf(pubid), String.valueOf(sysid));
                }
                if (l == null) {
                    l = new ArrayList(1);
                } else {
                    l = new ArrayList(l);
                }
                l.add(new Ann(UNKNOWN, msg, null, null, null));
            }
        }
        
        if (recursively) {
            if (l != null) {
                ArrayList al = new ArrayList();
                for (Iterator i = l.iterator(); i.hasNext(); ) {
                    Annotation ano = (Annotation)i.next();
                    Throwable t1 = ano.getStackTrace();
                    if ((t1 != null) && (! alreadyVisited.contains(t1))) {
                        alreadyVisited.add(t1);
                        Annotation[] tmpAnnoArray = findAnnotations0(t1, true, alreadyVisited);
                        if ((tmpAnnoArray != null) && (tmpAnnoArray.length > 0)) {
                            al.addAll(Arrays.asList(tmpAnnoArray));
                        }
                    }
                }
                l.addAll(al);
            }
        }
        
        Annotation[] arr;
        if (l == null) {
            arr = null;
        } else {
            arr = new Annotation[l.size ()];
            l.toArray (arr);
        }

        return arr;
    }
    
    public String toString() {
        return super.toString() + "<" + prefix + "," + minLogSeverity + ">"; // NOI18N
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
        
        public String toString() {
            return "NbEM.Ann[severity=" + severity + ",message=" + message + ",localizedMessage=" + localizedMessage + ",stackTrace=" + stackTrace + ",date=" + date + "]"; // NOI18N
        }
        
    } // end of Ann

    /**
     * Another final class that is used to communicate with
     * NotifyException and provides enough information to the dialog.
     */
    final class Exc
    {
        /** the original throwable */
        private Throwable t;
        private Date d;
        private Annotation[] arr;
        private Annotation[] arrAll; // all - recursively
        private int severity;

        /** @param severity if -1 then we will compute the
         * severity from annotations
         */
        Exc (Throwable t, int severity, Annotation[] arr, Annotation[] arrAll) {
            this.t = t;
            this.severity = severity;
            this.arr = arr == null ? new Annotation[0] : arr;
            this.arrAll = arrAll == null ? new Annotation[0] : arrAll;
        }

        /** @return message */
        String getMessage () {
            String m = t.getMessage();
            if (m != null) {
                return m;
            }
            return (String)find (1);
        }

        /** @return localized message */
        String getLocalizedMessage () {
            String m = t.getLocalizedMessage();
            if (m != null && !m.equals(t.getMessage())) {
                return m;
            }
            if (arrAll == null) {
                // arrAll not filled --> use the old non recursive variant
                return (String)find(2);
            }
            for (int i = 0; i < arrAll.length; i++) {
                String s = arrAll[i].getLocalizedMessage ();
                if (s != null) {
                    return s;
                }
            }
            return m;
        }
	
        boolean isLocalized() {
            String m = t.getLocalizedMessage();
            if (m != null && !m.equals(t.getMessage())) {
                return true;
            }
            if (arrAll == null) {
                // arrAll not filled --> use the old non recursive variant
                return (String)find(2) != null;
            }
            for (int i = 0; i < arrAll.length; i++) {
                String s = arrAll[i].getLocalizedMessage ();
                if (s != null) {
                    return true;
                }
            }
            return false;
	}

        /** @return class name of the exception */
        String getClassName () {
            return (String)find (3);
        }

        /** @return the severity of the exception */
        int getSeverity () {
            if (severity != UNKNOWN) {
                return severity;
            }

            Annotation[] anns = (arrAll != null) ? arrAll : arr;
            for (int i = 0; i < anns.length; i++) {
                int s = anns[i].getSeverity ();
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
        Date getDate () {
            if (d == null) {
                d = (Date)find (4);
            }
            return d;
        }

        /** Prints stack trace of all annotations and if
         * there is no annotation trace then of the exception
         */
        void printStackTrace (PrintStream ps) {
            printStackTrace(new PrintWriter(ps, true));
        }
        
        /** Prints stack trace of all annotations and if
         * there is no annotation trace then of the exception
         */
        void printStackTrace (PrintWriter pw) {
            // #19487: don't go into an endless loop here
            printStackTrace(pw, new HashSet(10));
        }
        
        private void printStackTrace(PrintWriter pw, Set/*<Throwable>*/ nestingCheck) {
            if (t != null && !nestingCheck.add(t)) {
                // Unlocalized log message - this is for developers of NB, not users
                log(ErrorManager.WARNING, "WARNING - ErrorManager detected cyclic exception nesting:"); // NOI18N
                Iterator it = nestingCheck.iterator();
                while (it.hasNext()) {
                    Throwable t = (Throwable)it.next();
                    log(ErrorManager.WARNING, "\t" + t); // NOI18N
                    Annotation[] anns = findAnnotations(t);
                    if (anns != null) {
                        for (int i = 0; i < anns.length; i++) {
                            Throwable t2 = anns[i].getStackTrace();
                            if (t2 != null) {
                                log(ErrorManager.WARNING, "\t=> " + t2); // NOI18N
                            }
                        }
                    }
                }
                log(ErrorManager.WARNING, "Be sure not to annotate an exception with itself, directly or indirectly."); // NOI18N
                return;
            }
            /*Heaeder
            pw.print (getDate ());
            pw.print (": "); // NOI18N
            pw.print (getClassName ());
            pw.print (": "); // NOI18N
            String theMessage = getMessage();
            if (theMessage != null) {
                pw.print(theMessage);
            } else {
                pw.print("<no message>"); // NOI18N
            }
            pw.println ();
            */
            /*Annotations */
          for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) continue;
                
                Throwable thr = arr[i].getStackTrace();                
                String annotation = arr[i].getLocalizedMessage();
                
                if (annotation == null) annotation = arr[i].getMessage();
                /*
                if (annotation == null && thr != null) annotation = thr.getLocalizedMessage();
                if (annotation == null && thr != null) annotation = thr.getMessage();
                 */
                
                if (annotation != null) {
                    if (thr == null) {
                        pw.println ("Annotation: "+annotation);// NOI18N
                    }
                    //else pw.println ("Nested annotation: "+annotation);// NOI18N
                }                
            }            
            
            // ok, print trace of the original exception too
            // Attempt to show an annotation indicating where the exception
            // was caught. Not 100% reliable but often helpful.
            if (t instanceof VirtualMachineError) {
                // Decomposition may not work here, e.g. for StackOverflowError.
                // Play it safe.
                t.printStackTrace(pw);
            } else {
            // All other kinds of throwables we check for a stack trace.
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
            }
            /*Nested annotations */            
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) continue;
                
                Throwable thr = arr[i].getStackTrace();
                if (thr != null) {
                    Annotation[] ans = findAnnotations (thr);
                    Exc ex = new Exc (thr, 0, ans, null);
                    pw.println("==>"); // NOI18N
                    ex.printStackTrace(pw, nestingCheck);
                }
            }
        }

        /** Get a throwable's stack trace, decomposed into individual lines. */
        private  String[] decompose (Throwable t) {
            // XXX using JDK 1.4's StackTraceElement[] Throwable.getStackTrace()
            // might be much better...
            StringWriter sw = new StringWriter ();
            t.printStackTrace (new PrintWriter (sw));
            StringTokenizer tok = new StringTokenizer (sw.toString (), "\n\r"); // NOI18N
            int c = tok.countTokens ();
            String[] lines = new String[c];
            for (int i = 0; i < c; i++)
                lines[i] = tok.nextToken ();
            return lines;
        }

        /**
         * Method that iterates over annotations to find out
         * the first annotation that brings the requested value.
         *
         * @param kind what to look for (1, 2, 3, 4, ...);
         * @return the found object
         */
        private Object find (int kind) {
	    return find(kind, true);
	}

        /**
         * Method that iterates over annotations to find out
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

    /** Instances are created in awt.EventDispatchThread */
    public static final class AWTHandler
    {
        /** The name MUST be handle and MUST be public */
        public static void handle(Throwable t) {
            // Either org.netbeans or org.netbeans.core.execution pkgs:
            if (t.getClass().getName().endsWith(".ExitSecurityException")) { // NOI18N
                return;
            }
            ErrorManager.getDefault().notify((ERROR << 1), t);
        }
    }
}
