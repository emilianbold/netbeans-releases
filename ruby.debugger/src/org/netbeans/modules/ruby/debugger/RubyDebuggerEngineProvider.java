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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

/**
 * @author Martin Krauskopf
 */
public final class RubyDebuggerEngineProvider extends DebuggerEngineProvider {
    
    static final String RUBY_LANGUAGE = "Ruby"; // NOI18N
    
    private DebuggerEngine.Destructor destructor;
    
    public String[] getLanguages() {
        return new String[] { RUBY_LANGUAGE };
    }
    
    public String getEngineTypeID() {
        return "RubyDebuggerEngine"; // NOI18N
    }
    
    public Object[] getServices() {
        return new Object[] {};
    }
    
    public void setDestructor(DebuggerEngine.Destructor destructor) {
        this.destructor = destructor;
    }
    
    public DebuggerEngine.Destructor getDestructor() {
        return destructor;
    }
    
}
