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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariableImpl;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;

/*
 * An AbstractVariable is an array, pointer, struct, or union.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements LocalVariable, Customizer {
    
    private GdbDebugger debugger;
    private String          name;
    private String          type;
    private String          value;
    private Field[]         fields;
    
    private Set listeners = new HashSet();
    
    public AbstractVariable(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getType(), var.getValue());
        
        for (GdbVariable child : var.getChildren()) {
            addField(new AbstractField(child));
        }
    }
    
    /**
     * Returns string representation of type of this variable.
     *
     * @return string representation of type of this variable.
     */
    public String getValue() {
       return value;
    }

    /**
     * Sets string representation of value of this variable.
     *
     * @param value string representation of value of this variable.
     */
    // FIXME - Havn't reimplemented or verified this yet
    public void setValue(String expression) throws InvalidExpressionException {
        // evaluate expression to Value
        //NM Value value = getDebugger().evaluateIn (expression);
        String value = expression;
        // set new value to remote veriable
        //NM setValue (value);
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
//                    getDebugger().setVariableValue(name, strAddr);
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
//                            getDebugger().setVariableValue(name+"["+k+"]", "'"+c+"'"); // NOI18N
                        }
                    }
                }
            } else {
                // Something wrong
            }
            return;
        }
////        getDebugger().setVariableValue(name, value);
//        // set new value to this model
//        // setInnerValue (value);
//        // refresh tree
///*NM TEMPORARY COMMENTED OUT
//        PropertyChangeEvent evt = new PropertyChangeEvent(this, "value", null, value);
//        Object[] ls;
//        synchronized (listeners) {
//            ls = listeners.toArray();
//        }
//        for (int i = 0; i < ls.length; i++) {
//            ((PropertyChangeListener) ls[i]).propertyChange(evt);
//        }
//        //pchs.firePropertyChange("value", null, value);
//        //getModel ().fireTableValueChangedChanged (this, null);
//NM*/
    }

    public void setObject(Object bean) {
        try {
            if (bean instanceof String) {
                setValue((String) bean);
            //} else if (bean instanceof Value) {
            //    setValue((Value) bean); -- do not call directly
            } else {
                throw new IllegalArgumentException(""+bean); // NOI18N
            }
        } catch (InvalidExpressionException ieex) {
            IllegalArgumentException iaex = new IllegalArgumentException(ieex.getLocalizedMessage());
            iaex.initCause(ieex);
            throw iaex;
        }
    }

   /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public int getFieldsCount() {
        if (fields == null) {
            initFields();
        }
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
        if (fields == null) {
            return null;
        }
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
            if (fields == null) {
                initFields();
            }
            if (to != 0) {
                to = Math.min(fields.length, to);
                from = Math.min(fields.length, from);
                Field[] fv = new Field[to - from];
                System.arraycopy(fields, from, fv, 0, to - from);
                return fv;
            }
            return fields;
    }
    
    /**
     * In the JPDA implementation a value isn't always a String. We're (currently)
     * storing the value as a String so no conversion is done. However, keeping
     * this method makes it possible for us to change at a later date...
     *
     * @return The value of this instance
     */
    public String getToStringValue () throws InvalidExpressionException {
        return getValue();
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType() {
        if (type != null) {
            return type;
        } else {
            return ""; // NOI18N
        }
    }
    
    public boolean equals(Object o) {
        return  (o instanceof AbstractVariable) && (name.equals(((AbstractVariable) o).name));
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
    
    protected final GdbDebugger getDebugger() {
        if (debugger == null) {
            DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if (currentEngine == null) {
                return null;
            }
            debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);

        }
        return debugger;
    }
    
//    public boolean expandChildren() {
//        if (fields.length == 1 && fields[0].getName().length() == 0) {
//            // Need to make the real children into fields...
//            initFields(); // removes the bogus placeholder field
//            if (isStruct()) {
//                System.err.println("AbstractVariable.expandChildren[" + name + "]: This is a struct (unimplemented)");
//            } else if (isPointer()) {
//                System.err.println("AbstractVariable.expandChildren[" + name + "]: This is a pointer (unimplemented)");
//            } else {
//                System.err.println("AbstractVariable.expandChildren[" + name + "]: Unknown");
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }
    
    private boolean isStruct() {
        return type.indexOf('*') == -1 && value.charAt(0) == '{';
    }
    
    private boolean isPointer() {
        return type.indexOf('*') > 0;
    }

    private void initFields() {
        fields = new Field[0];
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
        Field[] fv = new Field[n + 1];
        System.arraycopy(fields, 0, fv, 0, n);
        fields = fv;
        fields[n] = field;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    public String toString() {
        return "AbstractVariable "; // NOI18N
    }
    
    public class AbstractField extends AbstractVariable implements Field {
        
        public AbstractField(GdbVariable var) {
            super(var);
        }
        
        public AbstractField(String name, String type, String value) {
            super(name, type, value);
        }
        
        public boolean isStatic() {
            return false;
        }
        
        public void setValue(String value) throws InvalidExpressionException {
            // ignore value - only allow simple vars to change value
        }
    }
}

