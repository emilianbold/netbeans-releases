/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.xtest.plugin.ide.services;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.openide.*;
import java.util.TreeMap;

/**
 * When there is thrown exception from ide the test should fail. This class is made for notify 
 * TestCaseResult listener about thrown exception in ide. The funcionality is easily enabled by property of 
 * xtest (xtest.ide.error.manager).
 *
 * @author  pzajac
 */
public class XTestErrorManager extends ErrorManager {
    /** not yet proccessed exceptions
     */
    private static ArrayList/*Throwable*/ exceptions = new ArrayList();
    
    /** all instances of error manager
     */
    private TreeMap instances = new TreeMap();
    
    /** Creates a new instance of XTestErrorManager */
    public XTestErrorManager() {
    }

    public java.lang.Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
        return t;
    }

    public org.openide.ErrorManager.Annotation[] findAnnotations(Throwable t) {
        return null;
    }

    public org.openide.ErrorManager getInstance(java.lang.String name) {
        ErrorManager erm = (ErrorManager)instances.get(name);
        if (erm == null) {
            // create new instance
            erm = new XTestErrorManager();
            instances.put(name,erm);
        }
        return erm; 
    }

    public void log(int severity, String s) {
        // ignored
        // these messages are logged to IDE log
    }

    /** It uses the same logic as in org.openide.ErrorManager.DelegatingErrorManager.
     * Test whether a messages with given severity will be logged in advance.
     * Can be used to avoid the construction of complicated and expensive
     * logging messages.
     * @param severity the severity to check, e.g. {@link #EXCEPTION}
     * @return <code>false</code> if the next call to {@link #log(int,String)} with this severity will
     *    discard the message
    */
    public boolean isLoggable(int severity) {
        return severity > INFORMATIONAL;
    }

    public void notify(int severity, Throwable t) {
        // log only ERROR, EXCEPTION or UNKNOWN severity
        if(severity == ERROR || severity == EXCEPTION || severity == UNKNOWN) {
            // add the exception to exceptions queue of NbTestCase
            exceptions.add(t);
        }
    }

    public Throwable attachAnnotations(Throwable t, org.openide.ErrorManager.Annotation[] arr) {
        return t;
    }

    /** @returns thrown exceptions  
     */
    public static Collection/*Throwable*/ getExceptions() {
        return exceptions;
    }
    
    /** clears container of thrown exceptions
     * this method is called after the test has finished.
     */
    public static void clearExceptions () {
        exceptions.clear();
    }
}
