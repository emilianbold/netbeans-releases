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
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.util.NbBundle;

/*
 * An AbstractVariable is an array, pointer, struct, or union.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements LocalVariable, Customizer {
    
    private GdbDebugger debugger;
    private CallStackFrame csf;
    protected String name;
    protected String type;
    protected String value;
    protected Field[] fields;
    
    private Set listeners = new HashSet();
    
    public AbstractVariable(String n, String t, String v) {
        assert n.indexOf('{') == -1; // this means a mis-parsed gdb response...
        name = n;
        type = t;
        value = v;
    }
    
    /**
     * Create the AV from a GV. If the GV has children then create similar children for the AV.
     * Since the GV's children may have been created before all type information was available,
     * we reset the GV's realtype so its guaranteed to be correct (if it was set before type info
     * was returned from gdb, it might be the same as type).
     */
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getType(), var.getValue());
        
        getDebugger().waitForTypeCompletionCompletion();
        var.resetRealType();
        for (GdbVariable child : var.getChildren()) {
            child.resetRealType();
            addField(new AbstractField(this, child.getName(), child.getType(), child.getValue()));
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
    
    private CallStackFrame getCurrentCallStackFrame() {
        if (csf == null) {
            GdbDebugger debugger = getDebugger();
            csf = getDebugger().getCurrentCallStackFrame();
        }
        return csf;
    }
    
    public boolean expandChildren() {
        int fcount = 0;
        for (Field child : fields) {
            fcount += expandChildrenFromValue((AbstractField) child);
        }
        return fcount != 0;
    }
    
    private int expandChildrenFromValue(AbstractField var) {
        int count = 0; // we really only care if its 0 or >0
        
        var.fields = null; // Always recompute children. This fixes various timing problems
        if (var.value != null && var.value.length() > 0 && var.value.charAt(0) == '{') {
            if (GdbUtils.isArray(var.type)) {
                count += parseArray(var, var.name, var.type, var.value.substring(1, var.value.length() - 1));
            } else {
                Object o = getCurrentCallStackFrame().getType(var.type);
                if (o instanceof Map && ((Map) o).size() > 0) {
                    Map map = (Map) o;
                    String v = var.value.substring(1, var.value.length() - 1);
                    int opos = 0;
                    int pos = GdbUtils.findNextComma(v, opos);
                    while (pos != -1) {
                        var.addField(completeFieldDefinition(var, map, v.substring(opos, pos - 1)));
                        opos = pos;
                        pos = GdbUtils.findNextComma(v, opos);
                    }
                    var.addField(completeFieldDefinition(var, map, v.substring(opos)));
                    count++;
                } else if (o instanceof String) {
                    if (GdbUtils.isArray(o.toString())) {
                        count += parseArray(var, var.name, o.toString(), var.value.substring(1, var.value.length() - 1));
                    }
                }
            }
        }
        return count;
    }
    
    private AbstractField completeFieldDefinition(AbstractVariable parent, Map<String, String> map, String info) {
        String name, type, value;
        int pos = info.indexOf('=');
        if (pos != -1) {
            if (info.charAt(0) == '<') {
                name = NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"); // NOI18N
                type = info.substring(1, pos - 2).trim();
            } else {
                name = info.substring(0, pos - 1).trim();
                type = map.get(name);
            }
            value = info.substring(pos + 1).trim();
            if (!name.startsWith("_vptr")) { // NOI18N
                return new AbstractField(parent, name, type, value);
            }
        } else if (info.trim().equals("<No data fields>")) { // NOI18N
            return new AbstractField(parent, "", "", info.trim());
        }
        return null;
    }
    
    private int parseArray(AbstractVariable var, String basename, String type, String value) {
        int pos = type.lastIndexOf('[');
        int vpos = 0;
        int size = 0;
        int count = 0;
        
        try {
            size = Integer.valueOf(type.substring(pos + 1, type.length() - 1));
        } catch (Exception ex) {
        }
        for (int i = 0; i < size && vpos != -1; i++) {
            int nvpos;
            
            if (value.charAt(vpos) == ' ') {
                vpos++;
            }
            if (value.charAt(vpos) == '{') {
                nvpos = GdbUtils.findMatchingCurly(value, vpos) + 1;
            } else {
                nvpos = GdbUtils.findNextComma(value, vpos);
            }
            var.addField(new AbstractField(var, basename + "[" + i + "]", type.substring(0, pos), // NOI18N
                    nvpos == -1 ? value.substring(vpos).trim() : value.substring(vpos, nvpos - 1).trim()));
            vpos = nvpos;
            count++;
        }
        return count;
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
        if (field == null) {
            return;
        }
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

