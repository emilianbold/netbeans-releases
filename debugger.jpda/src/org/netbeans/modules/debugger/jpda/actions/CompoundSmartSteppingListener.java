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

