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
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.utils.ValueTokenizer;

/*
 * An AbstractVariable is an array, pointer, struct, or union.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements LocalVariable, Customizer {
    
    private GdbDebugger debugger;
    protected String name;
    protected String type;
    protected String value;
    private Field[] fields;
    
    private Set listeners = new HashSet();
    
    public AbstractVariable(String n, String t, String v) {
        name = n;
        type = t;
        value = v;
    }
    
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getType(), var.getValue());
        
        for (GdbVariable child : var.getChildren()) {
            AbstractField field = new AbstractField(this, child.getName(), child.getType(), child.getValue());
            addField(field);
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
     * Sets string representation of value of this variable. In this case we ignore the
     * request because we only allow setting values on leaves.
     *
     * @param value string representation of value of this variable.
     */
    public void setValue(String expression) throws InvalidExpressionException {
        System.err.println("AbstractVariable.setValue: (Ignored)");
    }

    public void setObject(Object bean) {
        System.err.println("AbstractVariable.setObject: (Ignored)");
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
    
    public boolean expandChildren() {
        int fcount = fields.length;
        for (Field child : fields) {
            expandChildrenFromValue((AbstractField) child);
        }
//        expandChildrenFromValue(this);
        return fcount != fields.length;
    }
    
    private void expandChildrenFromValue(AbstractVariable var) {
        Object o = getDebugger().getCurrentCallStackFrame().getType(var.type);
        int i = 0;
        int flen = var.fields == null ? 0 : var.fields.length;
        
        if (o instanceof Map) {
            AbstractField field;
            Map<String, Object> map = (Map) o;
            ValueTokenizer tok = new ValueTokenizer(var.value);
            while (tok.hasMoreTokens()) {
                String[] token = tok.nextToken();
                if (i >= flen) {
                    String type;
                    if (token[0].startsWith("<") && token[0].endsWith(">")) { // NOI18N
                        type = "super"; // NOI18N
                    } else {
                        type = token[1];
                    }
                    field = new AbstractField(this, token[0], type, token[1]);
                    var.addField(field);
                } else {
                    field = (AbstractField) var.fields[i];
                }
//                expandChildrenFromValue(field);
                i++;
            }
//        } else if (o instanceof String) {
//            System.err.println("AV.expandChildrenFromValue: Type is string [" + o + "]");
//        } else if (o == null) {
//            System.err.println("AV.expandChildrenFromValue: Type is Null");
//        } else {
//            System.err.println("AV.expandChildrenFromValue: Type is unexpected class [" + o.getClass().getName() + "]");
        }
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
        
        AbstractVariable parent;
        
        public AbstractField(AbstractVariable parent, String name, String type, String value) {
            super(name, type, value);
            this.parent = parent;
        }
        
        public boolean isStatic() {
            return false;
        }
        
        private String getFullName() {
            if (parent instanceof AbstractField) {
                return ((AbstractField) parent).getFullName() + '.' + name;
            } else {
                return parent.getName() + '.' + name;
            }
        }
        
        public void setValue(String value) throws InvalidExpressionException {
            getDebugger().getGdbProxy().data_evaluate_expression(getFullName() + '=' + value);
        }
    }
}

