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

import org.netbeans.api.debugger.jpda.ReturnVariable;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 *
 * @author Martin Entlicher
 */
public class ReturnVariableImpl extends AbstractVariable implements ReturnVariable {
    
    private JPDADebuggerImpl debugger;
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
            parentID + ".return"
        );
        this.debugger = debugger;
        this.methodName = methodName;
    }
    
    public String methodName() {
        return methodName;
    }
    
    public String toString () {
        return "ReturnVariable " + getValue();
    }

}
