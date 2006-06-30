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

