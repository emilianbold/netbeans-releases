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

package org.netbeans.modules.cnd.debugger.gdb;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

/*
 * LocalVariableImpl.java
 * Implements LocalVariable for primitive variables.
 *
 * @author Nik Molchanov
 */
public class LocalVariableImpl implements LocalVariable, Field {
    private String name;
    private String previousValueText;
    private String currentValueText;
    private String type;
    private GdbDebugger debugger;
    
    /**
     * Creates a new instance of LocalVariableImpl
     */
    public LocalVariableImpl(String name, String type, String value) {
        this.name = name;
        this.currentValueText = value;
        this.previousValueText = value;
        this.type = type;
        debugger = null;
    }
    
    public LocalVariableImpl(GdbVariable var) {
        name = var.getName();
        type = var.getType();
        currentValueText = var.getValue();
        previousValueText = var.getValue();
        debugger = null;
    }
    
    public String getName() {
        return name; // Name to show in Locals View
    }
    
    public String getValue() {
        return currentValueText;
    }
    
    /**
     * Sets value of this local represented as text.
     *
     * @param value a new value of this local represented as text
     * @throws InvalidExpressionException if the expression is not correct
     */
    public void setValue(String expression) throws InvalidExpressionException {
        if (debugger == null) {
	    // Don't set it unless its needed...
	    DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
	    debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
	}
        
        debugger.getGdbProxy().data_evaluate_expression(name + "=" + expression);
    }
    
    public String getType() {
        return type;
    }
    
    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic() {
        return false;
    }
}
