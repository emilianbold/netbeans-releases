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

package org.netbeans.modules.debugger.jpda;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.DebuggerEngineProvider;


/**
 *
 * @author Jan Jancura
 */
public class JSR45DebuggerEngineProvider extends DebuggerEngineProvider {
    
    private String language;
    private DebuggerEngine.Destructor desctuctor;

    JSR45DebuggerEngineProvider (String language) {
        this.language = language;
    }
    
    public String[] getLanguages () {
        return new String[] {language};
    }

    public String getEngineTypeID () {
        return "netbeans-JPDASession/" + language;
    }

    public Object[] getServices () {
        return new Object [0];
    }

    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
        this.desctuctor = desctuctor;
    }

    public DebuggerEngine.Destructor getDesctuctor() {
        return desctuctor;
    }
}

