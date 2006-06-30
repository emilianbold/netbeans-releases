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

package org.netbeans.modules.debugger.jpda.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;


public class SmartSteppingImpl extends SmartSteppingCallback implements 
PropertyChangeListener {
    
    
    private Set exclusionPatterns = new HashSet (); 
    private SmartSteppingFilter smartSteppingFilter;
    
    
    /**
     * Defines default set of smart stepping filters. Method is called when 
     * a new JPDA debugger session is created.
     *
     * @param f a filter to be initialized
     */
    public void initFilter (SmartSteppingFilter f) {
        smartSteppingFilter = f;
    }
    
    /**
     * This method is called during stepping through debugged application.
     * The execution is stopped when all registerred SmartSteppingListeners
     * returns true.
     *
     * @param thread contains all available information about current position
     *        in debugged application
     * @param f a filter
     * @return true if execution should be stopped on the current position
     */
    public boolean stopHere (
        ContextProvider lookupProvider, 
        JPDAThread thread, 
        SmartSteppingFilter f
    ) {
        String className = thread.getClassName ();
        if (className == null) return false;

        SourcePath ectx = getEngineContext (lookupProvider);
        boolean b = ectx.sourceAvailable (thread, null, false);
        if (b) return true;
        
        // find pattern
        String name, n1 = className.replace ('.', '/');
        do {
            name = n1;
            int i = name.lastIndexOf ('/');
            if (i < 0) break;
            n1 = name.substring (0, i);
        } while (!ectx.sourceAvailable (n1, false));
        HashSet s = new HashSet ();
        s.add (name.replace ('/', '.') + ".*");
        addExclusionPatterns (s);
        return false;
    }
    
    private void addExclusionPatterns (
        Set ep
    ) {
        smartSteppingFilter.addExclusionPatterns (ep);
        exclusionPatterns.addAll (ep);
    }
    
    private void removeExclusionPatterns () {
        smartSteppingFilter.removeExclusionPatterns (exclusionPatterns);
        exclusionPatterns = new HashSet ();
    }
    
    private SourcePath engineContext;
    
    private SourcePath getEngineContext (ContextProvider lookupProvider) {
        if (engineContext == null) {
            engineContext = (SourcePath) lookupProvider.lookupFirst 
                (null, SourcePath.class);
            engineContext.addPropertyChangeListener (this);
        }
        return engineContext;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName () == SourcePathProvider.PROP_SOURCE_ROOTS) {
            removeExclusionPatterns ();
        }
    }
}
