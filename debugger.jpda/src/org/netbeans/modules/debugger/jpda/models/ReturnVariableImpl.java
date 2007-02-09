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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

import org.netbeans.api.debugger.jpda.ReturnVariable;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 *
 * @author Martin Entlicher
 */
public class ReturnVariableImpl extends AbstractVariable implements ReturnVariable {
    
    private String methodName;
    
    /** Creates a new instance of ClassVariableImpl */
    public ReturnVariableImpl(
        JPDADebuggerImpl debugger,
        Value returnValue,
        String parentID,
        String methodName
    ) {
        super (
            debugger,
            returnValue,
            parentID + ".return " + methodName + "=" + getStringValue(returnValue) // To have good equals()
        );
        this.methodName = methodName;
    }
    
    private static String getStringValue(Value v) {
        if (v == null) return "null";
        if (v instanceof VoidValue) return "void";
        if (v instanceof PrimitiveValue) return v.toString ();
        else return "#" + ((ObjectReference) v).uniqueID ();
    }
    
    public String methodName() {
        return methodName;
    }

    public ReturnVariableImpl clone() {
        return new ReturnVariableImpl(
                getDebugger(),
                getJDIValue(),
                getID().substring(0, getID().length() - ".return".length()),
                methodName);
    }
    
    public String toString () {
        return "ReturnVariable " + getValue();
    }

}
