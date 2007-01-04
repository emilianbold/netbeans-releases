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

package org.netbeans.modules.cnd.debugger.gdb;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;

/*
 * LocalVariableImpl.java
 * Implements LocalVariable for primitive variables.
 *
 * @author Nik Molchanov
 */
public class LocalVariableImpl implements LocalVariable, Field {
    private String name;
    private String shortname; 
    private String previousValueText;
    private String currentValueText;
    private String type;
    private String className;
    private int dimCount;
    private GdbDebuggerImpl debugger;
    private Field[]         fields;
    private Field[]         staticFields;
    private Field[]         inheritedFields;
    
    /**
     * Creates a new instance of LocalVariableImpl
     */
    public LocalVariableImpl(String name, String type, String value) {
        setName(name);
        this.currentValueText = value;
        this.previousValueText = value;
        this.type = type;
        dimCount = 1;
        debugger = null;
    }
    
    public String getName() {
        return shortname; // Name to show in Locals View
    }
    
    public void setName(String name) {
        //objectChanged(CHANGED_NAME);
        this.name = name;
        shortname = name;
        int i = shortname.lastIndexOf('.');
        if (i >= 0) {
            i++;
            if (i < shortname.length()) {
                shortname = shortname.substring(i);
            }
            // Copy all stars
            for (i = 0; i < name.length(); i++) {
                if (name.charAt(i) == '*') {
                    shortname = '*' + shortname;
                } else break;
            }
        }
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
        if (debugger == null) return;
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
                            debugger.setVariableValue(name+"["+k+"]", "'"+c+"'"); // NOI18N
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
        if (type != null && !type.equals("")) { // NOI18N
            return type;
        }
        type = debugger.getVariableType(name);
        return type;
    }
    
    public void setType(String type) {
        //objectChanged(CHANGED_TYPE);
        this.type = type;
    }
    
    private boolean typeIsSimple() {
        String type = getType();
        if (type == null) return false;
        if ((type.equals("char")) // NOI18N
            || (type.equals("short")) // NOI18N
            || (type.equals("int")) // NOI18N
            || (type.equals("long")) // NOI18N
            || (type.equals("long long")) // NOI18N
            || (type.equals("double")) // NOI18N
            || (type.equals("long double")) // NOI18N
            || (type.equals("unsigned char")) // NOI18N
            || (type.equals("unsigned short")) // NOI18N
            || (type.equals("unsigned int")) // NOI18N
            || (type.equals("unsigned long")) // NOI18N
            || (type.equals("unsigned long long")) // NOI18N
        ) {
            return true;
        }
        return false;
    }
    
    public String getDeclaredType() {
        return type;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String name) {
        //objectChanged(CHANGED_CLASSNAME);
        this.className = name;
    }
    
    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic() {
        return false;
    }
    
    public int getDimCount() {
        return dimCount;
    }
    
    public void setDimCount(int dimCount) {
        //objectChanged(CHANGED_DIM_COUNT);
        this.dimCount = dimCount;
    }
    
    public void setDebugger(GdbDebuggerImpl debugger) {
        //objectChanged(CHANGED_TYPE);
        this.debugger = debugger;
    }
    
    //public List getAnnotations() {
    //        return Collections.EMPTY_LIST;
    //}
    //
    //void setData(String name, List annotations, int dimCount, String initialValueText) {
    //    this.name = name;
    //    this.isFinal = isFinal;
    //    changeChild(null, initialValue);
    //    this.initialValue = initialValue;
    //    this.initialValueText = initialValueText;
    //    this.dimCount = dimCount;
    //}
    
    private void initFields() {
        this.fields = new Field [0];
        this.staticFields = new Field [0];
        this.inheritedFields = new Field [0];
        if (typeIsSimple()) {
            // It is known that this variable does not have children
            return;
        }
        if (name.indexOf('[') >= 0) {
            // Algorithm below does not work for array
            return; // Algorithm for array is not implemented yet.
        }
        // Check if this variable has children. Try to get * value
        String expression = "*" + name; // NOI18N
        String v = debugger.getExpressionValue(expression);
        // Multi value format: \"{name1 = addr1 \\\"value1\\\", name2 = addr2 \\\"value2\\\", ...}\"\n
        if (v != null) {
            if (!v.startsWith("\"{")) { // NOI18N
                // Single value format: string v contains value or error message
                int index = v.indexOf("\" is not a known variable in current context <"); // NOI18N
                if (index < 0) {
                    // Known variable
                    String sName = expression;
                    String sType = debugger.getVariableType(sName);
                    if (sType == null) {
                        // Generate a type
                        sType = getType();
                        if ((sType != null) && (sType.endsWith("*"))) { // NOI18N
                            sType = sType.substring(0, sType.length() - 1);
                        }
                    }
                    String sValue = v;
                    if (sValue.endsWith("\n")) { // NOI18N
                        sValue = sValue.substring(0, sValue.length() - 1);
                    }
                    LocalVariableImpl lvi = new LocalVariableImpl(sName, sType, sValue);
                    lvi.setDebugger(debugger);
                    addField((Field) lvi);
                } else {
                    // Update type
                    getType();
                }
                return;
            }
            // Multi value format: \"{name1 = addr1 \\\"value1\\\", ...}\"\n
            v = v.substring(2);
            if (v.endsWith("}\"\n")) { // NOI18N
                v = v.substring(0, v.length() - 3);
            }
            for (int cp = 0; cp < v.length(); ) {
                int index = v.indexOf(" = ", cp); // NOI18N
                if (index < 0) break;
                String sName = v.substring(cp, index);
                // Add parent's name
                sName = name + '.' + sName;
                String sType = debugger.getVariableType(sName);
                String sValue = "UNKNOWN VALUE"; // FIXUP // NOI18N
                cp = index + 3;
                index = v.indexOf(", ", cp); // NOI18N
                if (index < 0) index = v.length();
                else {
                    // Check if it is a simple value
                    int k = v.indexOf(" = {", cp - 3); // NOI18N
                    if (k >= 0 && k < index) {
                        // "{_p = 0x0, _bf = {_base = 0x0, _size = 0}, _lbfsize = 0, ...}"
                        k = v.indexOf("}, ", cp); // NOI18N
                        if (k < 0) index = v.length();
                        else index = k + 1;
                    }
                }
                sValue = v.substring(cp, index);
                LocalVariableImpl lvi = new LocalVariableImpl(sName, sType, sValue);
                lvi.setDebugger(debugger);
                addField((Field) lvi);
                cp = index + 2;
            }
        }
    }
    
    /**
     * Adds a field.
     *
     * @parameter field A field to add.
     */
    public void addField(Field field) {
        if (fields == null) {
            initFields();
        }
        int n = fields.length;
        Field[] fv = new Field [n + 1];
        System.arraycopy(fields, 0, fv, 0, n);
        fields = fv;
        fields[n] = field;
    }
    
    /**
     * Returns string representation of type of this variable.
     *
     * @return string representation of type of this variable.
     */
    public int getFieldsCount() {
        if (fields == null) initFields();
        return fields.length;
    }
    
    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    public Field getField(String name) {
        if (fields == null) return null;
        int i, k = fields.length;
        for (i=0; i < k; i++) {
            Field f = fields[i];
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null; // Not found
    }
    
    /**
     * Returns all fields declared in this type that are in interval
     * &lt;<code>from</code>, <code>to</code>).
     */
    public Field[] getFields(int from, int to) {
        //either the fields are cached or we have to init them
        if (fields == null) initFields();
        if (to != 0) {
            to = Math.min(fields.length, to);
            from = Math.min(fields.length, from);
            Field[] fv = new Field [to - from];
            System.arraycopy(fields, from, fv, 0, to - from);
            return fv;
        }
        return fields;
    }
    
}
