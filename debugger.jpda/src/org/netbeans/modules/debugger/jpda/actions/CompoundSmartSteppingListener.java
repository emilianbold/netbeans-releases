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
package org.netbeans.modules.debugger.jpda.actions;

import java.util.Iterator;
import java.util.List;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SmartSteppingListener;


/**
 * Loads all different SmartSteppingListeners and delegates to them.
 *
 * @author  Jan Jancura
 */
public class CompoundSmartSteppingListener extends SmartSteppingListener {
    
    
    private List smartSteppings;
    private LookupProvider lookupProvider;
    
    
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;

    
    CompoundSmartSteppingListener (LookupProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }
    
    public void initFilter (SmartSteppingFilter filter) {
        // init list of smart stepping listeners
        smartSteppings = lookupProvider.lookup (SmartSteppingListener.class);
        Iterator i = smartSteppings.iterator ();
        while (i.hasNext ()) {
            SmartSteppingListener ss = (SmartSteppingListener) i.next ();
            ss.initFilter (filter);
        }
    }
    
    /**
     * Asks all SmartSteppingListener listeners if executiong should stop on the 
     * current place represented by JPDAThread.
     */
    public boolean stopHere (DebuggerEngine engine, JPDAThread t, SmartSteppingFilter filter) {
        if (ssverbose)
            System.out.println("\nSS  CompoundSmartSteppingListener.stopHere? : " + 
                t.getClassName () + '.' +
                t.getMethodName () + ':' +
                t.getLineNumber (null)
            );
        
        Iterator i = smartSteppings.iterator ();
        boolean stop = true;
        while (i.hasNext ()) {
            SmartSteppingListener ss = (SmartSteppingListener) i.next ();
            boolean sh = ss.stopHere (engine, t, filter);
            stop = stop && sh;
            if (ssverbose)
                System.out.println("SS    " + ss.getClass () + 
                    " = " + sh
                );
        }
        return stop;
    }
}

