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

package org.netbeans.modules.debugger.jpda;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.JPDADebugger;
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
        return JPDADebugger.SESSION_ID + "/" + language;
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

