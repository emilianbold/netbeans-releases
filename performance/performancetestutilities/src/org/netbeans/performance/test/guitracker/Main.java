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

package org.netbeans.performance.test.guitracker;

/** An entry point to start an application that initializes 
 * event logging and passes logic to original main class 
 * that is specified as first parameter
 *
 * @author Radim Kubacki
 */
public class Main {

    public Main() {
    }

    public static void main(String [] args) {
        String clzName = System.getProperty("guitracker.mainclass");
        if (clzName == null) {
            throw new IllegalStateException("No main class defined. Use -Dguitracker.mainclass=<classname>");
        }
        // init tracker and EQ now
        ActionTracker tr;
        
        LoggingRepaintManager rm;
        
        LoggingEventQueue leq;
        
        // load our EQ and repaint manager
        tr = ActionTracker.getInstance();
        rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        leq = new LoggingEventQueue(tr);
        leq.setEnabled(true);
        tr.setInteractive(true);
        tr.connectToAWT(true);
        tr.startNewEventList("ad hoc");
        tr.startRecording();
        
        try {
            Class<?> clz = Class.forName(clzName);
            clz.getMethod("main", String[].class).invoke(null, (Object)args);
        } catch(Exception ex) {
            throw new IllegalStateException("Cannot pass control to "+clzName, ex);
        }
    }
}
