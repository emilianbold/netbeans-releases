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
    private GdbDebuggerImpl debugger;
    
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
	    debugger = (GdbDebuggerImpl) currentEngine.lookupFirst(null, GdbDebugger.class);
	}
        
        // evaluate expression to Value
        //String value = debugger.evaluateIn (expression);
        String value = expression;
        // Adjust value according to the type
        if(type.equals("char *")) { // NOI18N
            // There are 2 values: pointer and string
            // First check if pointer is changed
            // Second check if string value is changed
            // Only one of them can be changed at once.
            String strAddr = null;
            String strValue = null;
            int i = expression.indexOf(' ');
            if (i >= 0) {
                strAddr = expression.substring(0, i);
                strValue = expression.substring(i+1);
            } else {
                if (expression.startsWith("0x")) { // NOI18N
                    strAddr = expression;
                } else {
                    strValue = expression;
                }
            }
            String oldValue = getValue();
            if (strAddr != null) {
                if (!oldValue.startsWith(strAddr)) {
                    // Pointer is changed
                    strAddr = expression;
                    debugger.setVariableValue(name, strAddr);
                }
            }
            // Compare string value
            i = oldValue.indexOf(' ');
            if (i >= 0) {
                String oldstrValue = oldValue.substring(i+1);
                if (!oldstrValue.equals(strValue)) {
                    // String value is changed
                    // Now let's update it in memory byte-by-byte
                    for (int n = 0; n < strValue.length(); n++) {
                        char c = strValue.charAt(n);
                        if (c != oldstrValue.charAt(n)) {
                            if (n < 2) break; // First 2 characters must match (\")
                            int k = n - 2;
                            debugger.setVariableValue(name + "[" + k + "]", "'" + c + "'"); // NOI18N
                        }
                    }
                }
            } else {
                // Something wrong
            }
            return;
        }
        debugger.setVariableValue(name, value);
        // set new value to this model
        // setInnerValue (value);
        // refresh tree
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
