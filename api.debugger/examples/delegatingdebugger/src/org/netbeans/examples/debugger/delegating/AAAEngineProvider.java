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

package org.netbeans.examples.debugger.delegating;

import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

/**
 * Represents one debugger plug-in - one Debugger Implementation.
 * Each Debugger Implementation can add support for debugging of some
 * language or environment to the IDE.
 *
 * @author Jan Jancura
 */
public class AAAEngineProvider extends DebuggerEngineProvider {

    
    public AAAEngineProvider (Session s) {
        super (s);
    }
    
    public String[] getLanguages () {
        return new String[] {"AAA"};
    }

    public String getEngineTypeID () {
        return "netbeans-AAADebuggerEngine";
    }
    
    public Object[] getServices () {
        return null;
    }
}

