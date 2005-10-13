/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.event.ChangeEvent;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 */
class LoggingTestCaseHid extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.LoggingTestCaseHid$Lkp");
        assertNotNull ("ErrManager has to be in lookup", Lookup.getDefault().lookup(ErrManager.class));
    }
    
    
    protected LoggingTestCaseHid (String name) {
        super (name);
    }

    /** If execution fails we wrap the exception with 
     * new log message.
     */
    protected void runTest () throws Throwable {
        ErrManager.messages.setLength(0);
        ErrManager.messages.append ("Starting test ");
        ErrManager.messages.append (getName ());
        ErrManager.messages.append ('\n');
        
        try {
            super.runTest ();
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        }
    }
    

    //
    // Our fake lookup
    //
    public static final class Lkp extends ProxyLookup {
        private InstanceContent ic;
        
        public Lkp () {
            super(new Lookup[0]);
            this.ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            setLookups(new Lookup[] { al, Lookups.metaInfServices(getClass().getClassLoader()) });

            ic.add (new ErrManager ());
        }
        
    }
    //
    // Logging support
    //
    public static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance (String name) {
            if (
                true
//                name.startsWith ("org.openide.loaders.FolderList")
//              || name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            if (prefix != null) {
                messages.append (prefix);
                messages.append (s);
                messages.append (" by thread ");
                messages.append (Thread.currentThread().getName());
                messages.append ('\n');
                
                if (messages.length() > 40000) {
                    messages.delete(0, 20000);
                }
            }
        }
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }
        
    } // end of ErrManager
}
