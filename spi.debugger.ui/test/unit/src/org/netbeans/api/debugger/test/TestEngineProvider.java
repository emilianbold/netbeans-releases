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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerEngineProvider;


/**
 * Represents a test debugger plug-in - one Debugger Implementation.
 *
 * @author Maros Sandor
 */
public class TestEngineProvider extends DebuggerEngineProvider {

    private DebuggerEngine.Destructor   destructor;
    private Session                     session;  
    
    public TestEngineProvider (Session s) {
        session = s;
    }
    
    public String [] getLanguages () {
        return new String[] { "Basic" };
    }

    public String getEngineTypeID () {
        return TestDebugger.ENGINE_ID;
    }
    
    public Object[] getServices () {
        return null;
    }
    
    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
        this.destructor = desctuctor;
    }
    
    public DebuggerEngine.Destructor getDestructor () {
        return destructor;
    }
    
    public Session getSession () {
        return session;
    }
}

