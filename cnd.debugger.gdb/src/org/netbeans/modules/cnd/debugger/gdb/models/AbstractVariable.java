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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/*
 * An AbstractVariable is an array, pointer, struct, or union.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements LocalVariable, Customizer {
    
    private GdbDebugger _debugger;
    private CallStackFrame csf;
    protected String name;
    protected String type;
    protected String value;
    protected String mvalue;
    protected String ovalue;
    protected String derefValue;
    protected Field[] fields;
    protected Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    private Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();
    
    /** Create the AV from a GV. If the GV has children then create similar children for the AV */
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getType(), var.getValue(), var.getDerefedValue());
        
        GdbDebugger debugger = getDebugger();
        if (debugger != null) {
            if (fields.length == 0 && var.getNumberChildren() > 0) {
                for (GdbVariable child : var.getChildren()) {
                    addField(new AbstractField(this, child.getName(), child.getType(), child.getValue()));
                }
            }
        }
    }
    
    public AbstractVariable(String name) {
        this(name, null, null, null);
    }
    
    public AbstractVariable(String name, String type, String value, String derefValue) {
        assert name.indexOf('{') == -1; // this means a mis-parsed gdb response...
        this.name = name;
        this.type = type;
        this.value = value;
        this.mvalue = null;
        this.ovalue = null;
        this.derefValue = derefValue;
        fields = new Field[0];
        
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            // Convert the Mac-specific value to standard gdb/mi format
            value = GdbUtils.mackHack(value);
        }
        
        GdbDebugger debugger = getDebugger();
        if (debugger != null) {
            debugger.waitForTypeCompletionCompletion();
            expandChildrenFromValue(this);
        }
    }
    
    protected AbstractVariable() {}
    
    /**
     * Returns string representation of type of this variable.
     *
     * @return string representation of type of this variable.
     */
    public String getValue() {
        if (mvalue != null) {
            return mvalue;
        } else {
            return value;
        }
    }

    /**
     * Sets string representation of value of this variable. In this case we ignore the
     * request because we only allow setting values on leaves.
     *
     * @param value string representation of value of this variable.
     */
    public void setValue(String value) {
        String msg = null;
        int pos;
        
        if (getFieldsCount() == 0) {
            GdbDebugger debugger = getDebugger();
            if (debugger != null) {
                value = value.trim();
                if (value.length() > 0 && value.charAt(0) == '(' && (pos = GdbUtils.findMatchingParen(value, 0)) != -1) {
                    // Strip a cast
                    value = value.substring(pos + 1).trim();
                }
                if (type.equals("char")) { // NOI18N
                    value = setValueChar(value);
                    if (value == null) { // Invalid input
                        msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char"); // NOI18N
                    }
                } else if (type.equals("char *")) { // NOI18N
                    value = setValueCharStar(value);
                    if (value == null) { // Invalid input
                        msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                    }
                } else if ((type.equals("int") || type.equals("long"))) { // NOI18N
                    value = setValueNumber(value);
                    if (value == null) { // Invalid input
                        msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Number"); // NOI18N
                    }
                } else if (debugger.isCplusPlus() && type.equals("bool")) { // NOI18N
                    if (!value.equals("true") && !value.equals("false") && !isNumber(value)) { // NOI18N
                        msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_CplusPlus_Bool"); // NOI18N
                    }
                } else if (type.startsWith("enum")) { // NOI18N
                    value = setValueEnum(value);
                    if (value == null) { // Invalid input
                        msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Enum"); // NOI18N
                    }
                } else if (value.charAt(0) == '"' || (value.startsWith("0x") && value.endsWith("\""))) { // NOI18N
                    value = setValueCharStar(value);
                    if (value == null) { // Invalid input
                        msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                    }
                }
                if (value != null) {
                    if (value.endsWith("\\\"")) { // NOI18N
                        pos = value.indexOf('"');
                        if (pos != -1) {
                            value = value.substring(pos, value.length() - 1) + '"';
                        }
                    }
                    if (value.charAt(0) == '(') {
                        pos = GdbUtils.findMatchingParen(value, 0);
                        if (pos != -1) {
                            value = value.substring(pos + 1).trim();
                        }
                    }
                }
                if (msg == null) {
                    String fullname;
                    if (this instanceof GdbWatchVariable) {
                        fullname = ((GdbWatchVariable) this).getExpression();
                    } else {
                        if (this instanceof AbstractField) {
                            fullname = ((AbstractField) this).getFullName();
                        } else {
                            fullname = name;
                        }
                    }
                    ovalue = this.value;
                    debugger.updateVariable(this, fullname, value);
                }
            }
        }
        if (msg != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
            nd.setTitle("TITLE_SetValue_Warning"); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    public void restoreOldValue() {
        mvalue = ovalue;
    }
    
    public void setModifiedValue(String mvalue) {
        this.mvalue = mvalue;
    }
    
    /**
     * Validate the string passed to setValue. Verify its a correct char format and remove a leading
     * address if needed.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueChar(String value) {
        int pos;
        
        if (value.startsWith("0x") && (pos = value.indexOf(" '")) != -1 && value.endsWith("'")) { // NOI18N
            value = value.substring(pos + 1);
        } else if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
            // OK
        } else {
            value = null;
        }
        return value;
    }
    
    /**
     * Validate the string passed to setValue. Verify its a correct char format and remove a leading
     * address if needed.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueCharStar(String value) {
        int pos;
        
        if (value.startsWith("0x") && (pos = value.indexOf(" \\\"")) != -1 && value.endsWith("\\\"")) { // NOI18N
            value = '"' + value.substring(pos + 3, value.length() - 2) + '"'; // NOI18N
        } else if (value.startsWith("0x") && (pos = value.indexOf(" \"")) != -1 && value.endsWith("\"")) { // NOI18N
            value = value.substring(pos + 1);
        } else if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') { // NOI18N
            // OK
        } else {
            value = null;
        }
        return value;
    }
    
    /**
     * Validate the string passed to setValue.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueEnum(String value) {
        CallStackFrame sf = getCurrentCallStackFrame();
        Object o;
        if (sf != null && (o = sf.getType(type)) != null) {
            List list = (List) o;
            int idx = list.indexOf(value);
            if (idx != -1) {
                return value;
            }
        }
        return null;
    }
    
    /**
     * Validate the string passed to setValue. Verify its a correct numerical format .
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueNumber(String value) {
        if (isNumber(value)) {
            // OK
        } else {
            value = null;
        }
        return value;
    }

    public void setObject(Object bean) {
    }

   /**
    * See if this variable <i>will</i> have fields and should show a turner.
    * We're not actually creating or counting fields here.
    *
    * @return 0 if the variable shouldn't have a turner and 5 if it should
    */
    public int getFieldsCount() {
        if (fields.length > 0) {
            return fields.length;
        } else if (type != null && value != null &&
                ((type.indexOf('[') != -1 || (type.indexOf("**") != -1 && isValidPointerAddress(value))) || // NOI18N
                (value.length() > 0 && value.charAt(0) == '{'))) {
            return 5;
        } else {
            return 0;
        }
    }
    
    private boolean isValidPointerAddress(String info) {
        int pos1 = info.indexOf("*) 0x"); // NOI18N
        int i;
        if (info.charAt(0) == '(' && pos1 != -1) {
            try {
                i = Integer.parseInt(info.substring(pos1 + 5), 16);
            } catch (NumberFormatException ex) {
                return false;
            }
            return i > 0;
        }
        return false;
    }
    
    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     * @return field defined in this object
     */
    public Field getField(String name) {
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
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof AbstractVariable) && (name.equals(((AbstractVariable) o).name));
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
    
    protected final GdbDebugger getDebugger() {
        if (_debugger == null) {
            DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if (currentEngine == null) {
                return null;
            }
            _debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
        }
        return _debugger;
    }
    
    private CallStackFrame getCurrentCallStackFrame() {
        if (csf == null) {
            GdbDebugger debugger = getDebugger();
            if (debugger != null) {
                csf = debugger.getCurrentCallStackFrame();
            }
            if (csf == null) {
                throw new IllegalStateException();
            }
        }
        return csf;
    }
    
    public boolean expandChildren() {
        int fcount = 0;
        if (fields.length != getFieldsCount()) {
            fcount = expandChildrenFromValue(this);
        }
        return fcount != 0;
    }
    
    protected int expandChildrenFromValue(AbstractVariable var) {
        String v;
        String t;
        int count = 0; // we really only care if its 0 or >0
        int start, end;
        
        if (var.fields.length == 0) {
            if (var.derefValue != null) {
                v = var.derefValue;
                t = var.type.substring(0, var.type.length() - 1).trim();
            } else {
                v = var.value;
                t = var.type;
            }

            if (v != null && v.length() > 0 && v.charAt(0) == '{') {
                if (GdbUtils.isArray(t)) {
                    count += parseArray(var, var.name, t, v.substring(1, v.length() - 1));
                } else {
                    Object o;
                    try {
                    o = getCurrentCallStackFrame().getType(t);
                    if (o == null) {
                        o = getCurrentCallStackFrame().getType('$' + var.name);
                    }
                    } catch (IllegalStateException ex) {
                        // This happens when the debug session is killed while processing input...
                        return 0;
                    }
                    if (o instanceof Map && ((Map) o).size() > 0) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) o;
                        String val = v.substring(1, v.length() - 1);
                        start = 0;
                        end = GdbUtils.findNextComma(val, 0);
                        while (end != -1) {
                            String vfrag = val.substring(start, end).trim();
                            var.addField(completeFieldDefinition(var, map, vfrag));
                            start = end + 1;
                            end = GdbUtils.findNextComma(val, end + 1);
                        }
                        var.addField(completeFieldDefinition(var, map, val.substring(start).trim()));
                        count++;
                    } else if (o instanceof String) {
                        if (GdbUtils.isArray(o.toString())) {
                            count += parseArray(var, var.name, o.toString(), var.value.substring(1, var.value.length() - 1));
                        }
                    } else if (o == null && t.endsWith("{...}")) { // NOI18N
                        // An anonymous class/struct/union, but we can still show children (from value) without type info
                        if (v.charAt(0) == '{' && v.endsWith("}")) { // NOI18N
                            v = v.substring(1, v.length() - 1);
                            start = 0;
                            end = GdbUtils.findNextComma(v, 0);
                            while (end != -1) {
                                String vfrag = v.substring(start, end).trim();
                                var.addField(completeFieldDefinition(var, null, vfrag));
                                start = end + 1;
                                end = GdbUtils.findNextComma(v, end + 1);
                            }
                            var.addField(completeFieldDefinition(var, null, v.substring(start).trim()));
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }
    
    /**
     * Complete and create the field information. Its OK to return null because addField
     * ignores it.
     */
    private AbstractField completeFieldDefinition(AbstractVariable parent, Map<String, Object> map, String info) {
        String n, t, v;
        if (info.charAt(0) == '{') { // we've got an anonymous class/struct/union...
            int count = Integer.parseInt((String) map.get("<anon-count>")); // NOI18N
            info = info.substring(1, info.length() - 1);
            for (int i = 1; i <= count; i++) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) map.get("<anonymous" + i + ">"); // NOI18N
                int start = 0;
                int end = GdbUtils.findNextComma(info, 0);
                while (end != -1) {
                    String vfrag = info.substring(start, end).trim();
                    parent.addField(completeFieldDefinition(parent, m, vfrag));
                    start = end + 1;
                    end = GdbUtils.findNextComma(info, end + 1);
                }
                parent.addField(completeFieldDefinition(parent, m, info.substring(start).trim()));
            }
        } else {
            int pos = info.indexOf('=');
            if (pos != -1) {
                if (info.charAt(0) == '<') {
                    n = NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"); // NOI18N
                    t = info.substring(1, pos - 2).trim();
                    v = info.substring(pos + 1).trim();
                    if (n.startsWith("_vptr")) { // NOI18N
                        return null;
                    }
                } else {
                    n = info.substring(0, pos - 1).trim();
                    v = info.substring(pos + 1).trim();
                    if (n.startsWith("_vptr")) { // NOI18N
                        return null;
                    }
                    if (map == null) {
                        map = lookupMap(n, parent);
                    }
                    if (map != null) {
                        Object o = map.get(n);
                        if (o instanceof String) {
                            t = o.toString();
                        } else if (o instanceof Map) {
                            t = (String) ((Map) o).get("<typename>"); // NOI18N
                        } else if (isNumber(v)) {
                            t = "int"; // NOI18N - best guess (std::string drops an "int")
                        } else {
                            log.warning("Cannot determine field type for " + n); // NOI18N
                            return null;
                        }
                    } else {
                        if (!parent.getType().endsWith("{...}")) { // NOI18N
                            log.warning("Connot determine field type for " + n + " (no type Map found)"); // NOI18N
                        }
                        t = "";
                    }
                }
                return new AbstractField(parent, n, t, v);
            } else if (info.trim().equals("<No data fields>")) { // NOI18N
                return new AbstractField(parent, "", "", info.trim()); // NOI18N
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> lookupMap(String name, AbstractVariable parent) {
        String t;
        String oname = name;
        
        while (parent != null) {
            t = parent.getType();
            if (t != null) {
                Object o = getCurrentCallStackFrame().getType(t);
                if (o instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) o;
                    o = map.get(name);
                    if (o instanceof Map && ((Map) o).get(oname) != null) {
                        return (Map<String, Object>) o;
                    }
                }
            }
            if (parent instanceof AbstractField) {
                name = parent.getName();
                parent = ((AbstractField) parent).parent;
            } else {
                parent = null;
            }
        }
        return null;
    }
    
    private int parseArray(AbstractVariable var, String basename, String type, String value) {
        int lbpos = type.indexOf('[');
        assert lbpos != -1;
        int rbpos = GdbUtils.findMatchingBrace(type, lbpos);
        assert rbpos != -1;
        int vstart = 0;
        int vend;
        int size;
        int count = 0;
        int nextbrace = type.indexOf('[', rbpos);
        String extra;
        if (nextbrace == -1) {
            extra = "";
        } else {
            extra = type.substring(nextbrace);
        }
        
        try {
            size = Integer.valueOf(type.substring(lbpos + 1, rbpos));
        } catch (Exception ex) {
            size = 0;
        }
        for (int i = 0; i < size && vstart != -1; i++) {
            if (value.charAt(vstart) == '{') {
                vend = GdbUtils.findNextComma(value, GdbUtils.findMatchingCurly(value, vstart));
            } else {
                vend = GdbUtils.findNextComma(value, vstart);
            }
            var.addField(new AbstractField(var, basename + "[" + i + "]", type.substring(0, lbpos).trim() + extra, // NOI18N
                    vend == -1 ? value.substring(vstart) : value.substring(vstart, vend)));
            vstart = GdbUtils.firstNonWhite(value, vend + 1);
            count++;
        }
        return count;
    }
    
    /**
     * Adds a field.
     *
     * Note: completeFieldDefinition returns null for _vptr data. Its easier to let it return null and
     * ignore it here than to check the return value in expandChildrenFromValue and not call addField
     * if its null.
     *
     * @parameter field A field to add.
     */
    public void addField(Field field) {
        if (field != null) {
            int n = fields.length;
            Field[] fv = new Field[n + 1];
            System.arraycopy(fields, 0, fv, 0, n);
            fields = fv;
            fields[n] = field;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    @Override
    public String toString() {
        return "AbstractVariable "; // NOI18N
    }
        
    private boolean isNumber(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    
    public class AbstractField extends AbstractVariable implements Field {
        
        private AbstractVariable parent;
        
        public AbstractField(AbstractVariable parent, String name, String type, String value) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.parent = parent;
            fields = new Field[0];
            derefValue = null;
        
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                value = GdbUtils.mackHack(value);
            }
            expandChildrenFromValue(this);
        }
        
        public boolean isStatic() {
            return false;
        }
        
        private String getFullName() {
            String n;
            String pname; // parent part of name
            int pos;
            
            if (parent instanceof AbstractField) {
                pname = ((AbstractField) parent).getFullName();
            } else {
                pname = parent.getName();
            }
            
            if (name.equals("<Base class>")) { // NOI18N
                return pname;
            } else if (name.indexOf('[') != -1) {
                if ((pos = pname.lastIndexOf('.')) != -1) {
                    return pname.substring(0, pos) + '.' + name;
                } else {
                    return name;
                }
            } else {
                return pname + '.' + name;
            }
        }
    }
}

