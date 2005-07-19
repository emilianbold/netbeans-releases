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
package org.netbeans.modules.debugger.jpda.actions;

import java.util.Iterator;
import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;


/**
 * Loads all different SmartSteppingListeners and delegates to them.
 *
 * @author  Jan Jancura
 */
public class CompoundSmartSteppingListener extends SmartSteppingCallback {
    
    
    private List smartSteppings;
    private ContextProvider lookupProvider;
    
    
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;

    
    public CompoundSmartSteppingListener (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        SmartSteppingFilter smartSteppingFilter = (SmartSteppingFilter) lookupProvider.
            lookupFirst (null, SmartSteppingFilter.class);
        initFilter (smartSteppingFilter);
    }
    
    public void initFilter (SmartSteppingFilter filter) {
        // init list of smart stepping listeners
        smartSteppings = lookupProvider.lookup 
            (null, SmartSteppingCallback.class);
        Iterator i = smartSteppings.iterator ();
        while (i.hasNext ()) {
            SmartSteppingCallback ss = (SmartSteppingCallback) i.next ();
            ss.initFilter (filter);
        }
    }
    
    /**
     * Asks all SmartSteppingListener listeners if executiong should stop on the 
     * current place represented by JPDAThread.
     */
    public boolean stopHere (
        ContextProvider lookupProvider, 
        JPDAThread t, 
        SmartSteppingFilter smartSteppingFilter
    ) {
        if (ssverbose)
            System.out.println("\nSS  CompoundSmartSteppingListener.stopHere? : " + 
                t.getClassName () + '.' +
                t.getMethodName () + ':' +
                t.getLineNumber (null)
            );
        
        Iterator i = smartSteppings.iterator ();
        boolean stop = true;
        while (i.hasNext ()) {
            SmartSteppingCallback ss = (SmartSteppingCallback) i.next ();
            boolean sh = ss.stopHere (lookupProvider, t, smartSteppingFilter);
            stop = stop && sh;
            if (ssverbose)
                System.out.println("SS    " + ss.getClass () + 
                    " = " + sh
                );
        }
        return stop;
    }
}

