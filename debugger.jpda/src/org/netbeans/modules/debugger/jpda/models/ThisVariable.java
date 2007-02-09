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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ThisVariable extends AbstractVariable implements This {

    ThisVariable (
        JPDADebuggerImpl debugger,
        Value value,
        String parentID
    ) {
        super (
            debugger,
            value,
            parentID + ".this^"
        );
    }


    // This impl................................................................

    public ThisVariable clone() {
        return new ThisVariable(getDebugger(), getJDIValue(),
                getID().substring(0, getID().length() - ".this^".length()));
    }

    // other methods ...........................................................
    
    public String toString () {
        return "ThisVariable this";
    }
}
