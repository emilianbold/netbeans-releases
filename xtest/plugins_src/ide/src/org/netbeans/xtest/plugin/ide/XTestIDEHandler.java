/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.xtest.plugin.ide;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.ArrayList;
import java.util.Collection;

/**
 * XTestIDEHandler collects unexpected exceptions which can be thrown from IDE.
 * When a test ends, TestListener adds test error if any exception is in the list.
 * This handler catch only exceptions with severity greater than Level.INFO.
 * It is used in MainWithExec class.
 * XTestIDEHandler can be controlled by xtest.ide.handler property.
 */
public class XTestIDEHandler extends Handler {
    /** not yet proccessed exceptions */
    private static ArrayList/*Throwable*/ exceptions = new ArrayList();

    /** Creates a new instance of XTestIDEHandler. */
    public XTestIDEHandler() {
        setLevel(Level.ALL);
    }
    
    /** Add exception to the list if severity is greater that Level.INFO.
     * @param record record
     */
    public void publish(LogRecord record) {
        int severity = record.getLevel().intValue();
        if(severity > Level.INFO.intValue() && record.getThrown() != null) {
            // add the exception to exceptions queue
            exceptions.add(record.getThrown());
        }
    }
    
    /** Not used. */
    public void flush() {
    }

    /** Not used. */
    public void close() {
    }

    /** Returns list of exceptions. 
     * @returns thrown exceptions. */
    public static Collection/*Throwable*/ getExceptions() {
        return exceptions;
    }
    
    /** Clears container of thrown exceptions.
     * This method is called after the test has finished.
     */
    public static void clearExceptions() {
        exceptions.clear();
    }
}
