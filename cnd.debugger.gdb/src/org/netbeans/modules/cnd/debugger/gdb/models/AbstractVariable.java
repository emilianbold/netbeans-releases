/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    private GdbDebugger debugger;
    private CallStackFrame csf;
    protected String name;
    protected String type;
    protected String value;
    protected String ovalue;
    protected String derefValue;
    protected Field[] fields;
    private Object derefLock = new Object();
    protected Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    private Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();
    
    /** Create the AV from a GV. If the GV has children then create similar children for the AV */
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getType(), var.getValue(), var.getDerefedValue());
        
        getDebugger(); // get and save debugger
    }
    
    public AbstractVariable(String name) {
        this(name, null, null, null);
    }
    
    public AbstractVariable(String name, String type, String value, String derefValue) {
        assert name.indexOf('{') == -1; // this means a mis-parsed gdb response...
        this.name = name;
        this.type = type;
        this.value = value;
        this.ovalue = null;
        this.derefValue = derefValue;
        fields = new Field[0];
        
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            // Convert the Mac-specific value to standard gdb/mi format
            this.value = GdbUtils.mackHack(value);
        }
        
        if (getDebugger() != null) {
            getDebugger().waitForTypeCompletionCompletion();
            if (!(this instanceof GdbWatchVariable)) { // not fully constructed yet...
                expandChildren();
            }
        }
    }
    
    protected AbstractVariable() {}
    
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
    public void setValue(String value) {
        String msg = null;
        int pos;
        
        if (getDebugger() != null) {
            value = value.trim();
            if (value.length() > 0 && value.charAt(0) == '(' && (pos = GdbUtils.findMatchingParen(value, 0)) != -1) {
                // Strip a cast
                value = value.substring(pos + 1).trim();
            }
            if (type.equals("char") || type.equals("unsigned char")) { // NOI18N
                value = setValueChar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char"); // NOI18N
                }
            } else if (type.equals("char *") || type.equals("unsigned char *")) { // NOI18N
                value = setValueCharStar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                }
            } else if ((type.equals("int") || type.equals("long"))) { // NOI18N
                value = setValueNumber(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Number"); // NOI18N
                }
            } else if (getDebugger().isCplusPlus() && type.equals("bool")) { // NOI18N
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
            } else if (GdbUtils.isPointer(type)) {
                // no current validation
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
                        fullname = ((AbstractField) this).getFullName(false);
                    } else {
                        fullname = name;
                    }
                }
                ovalue = this.value;
                getDebugger().updateVariable(this, fullname, value);
            }
        }
        if (msg != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
            nd.setTitle("TITLE_SetValue_Warning"); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    public void restoreOldValue() {
        value = ovalue;
    }
    
    public synchronized void setModifiedValue(String value) {
        this.value = value;
        if (fields.length > 0) {
            fields = new Field[0];
            derefValue = null;
            expandChildren();
        }
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
    public synchronized int getFieldsCount() {
        if (fields.length == 0) {
            expandChildren();
        }
        if (fields.length > 0) {
            return fields.length;
        } else if (mightHaveFields()) {
            return 5;
        } else {
            return 0;
        }
    }
    
    /**
     * The else-if in getFieldsCount() was getting too complex, so I've factored it out and
     * made it into multiple if/else-if statements. I think its easier to track this way.
     * 
     * @return true if the field should have a turner and false if it shouldn't
     */
    private boolean mightHaveFields() {
        if (type == null || value == null || type.endsWith(")")) { // NOI18N
            return false;
        } else if (type.indexOf('[') != -1) { // its an array
            return true;
        } else if (value.length() > 0 && value.charAt(0) == '{') {
            return true;
        } else if (isValidPointerAddress(value)) {
            if (type.indexOf("**") != -1) { // NOI18N - double pointers always need a turner
                return true;
            } else if (!isCharString(type)) { // Possibly a struct/class pointer
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isValidPointerAddress(String info) {
        String frag = "";
        int pos1;
        int i;
        if (info.length() > 0) {
            if (info.charAt(0) == '(') {
                pos1 = info.indexOf("*) 0x"); // NOI18N
                if (pos1 == -1) {
                    pos1 = info.indexOf("* const) 0x"); // NOI18N
                    if (pos1 != -1) {
                        frag = info.substring(pos1 + 11);
                    }
                } else {
                    frag = info.substring(pos1 + 5);
                }
                if (pos1 != -1) {
                    try {
                        i = Integer.parseInt(frag, 16);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    return i > 0;
                }
            } else if (info.startsWith("0x")) { // NOI18N
                try {
                    i = Integer.parseInt(info.substring(2), 16);
                } catch (NumberFormatException ex) {
                    return false;
                }
                return i > 0;
            }
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
            if (fields.length == 0) {
                expandChildren();
            }
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
        return o instanceof AbstractVariable &&
                    getFullName(true).equals(((AbstractVariable) o).getFullName(true));
    }
    
    @Override
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
            if (getDebugger() != null) {
                csf = getDebugger().getCurrentCallStackFrame();
            }
            if (csf == null) {
                throw new IllegalStateException();
            }
        }
        return csf;
    }
    
    public synchronized boolean expandChildren() {
        int fcount = 0;
        if (fields.length == 0) {
            fcount = expandChildrenFromValue(this);
        }
        return fcount != 0;
    }
    
    private int expandChildrenFromValue(AbstractVariable var) {
        String v;
        String t;
        String rt;
        int count = 0; // we really only care if its 0 or >0
        int start, end;
        
        log.fine("AV.expandChildrenFromValue[" + Thread.currentThread().getName() +
                "]: " + var.getName()); // NOI18N
        if (var.fields.length == 0) {
            if (var.derefValue != null) {
                v = var.derefValue;
                if (var.type.endsWith("*")) { // NOI18N
                    t = var.type.substring(0, var.type.length() - 1).trim();
                } else if (var.type.endsWith("* const")) { // NOI18N
                    t = var.type.substring(0, var.type.length() - 7).trim();
                } else {
                    log.fine("Unexpected type of dereferenced variable");
                    t = var.type; // Is this an error? Shouldn't happen...
                }
            } else if (var.value != null && GdbUtils.isPointer(var.type) && !isCharString(var.type) &&
                        uncast(var.value).startsWith("0x") && // NOI18N
                        !uncast(var.value).equals("0x0")) { // NOI18N
                var.derefValue = getDerefValue(var, '*' + var.getFullName(false));
                v = var.derefValue;
                if (var.type.endsWith("*")) { // NOI18N
                    t = var.type.substring(0, var.type.length() - 1).trim();
                } else if (var.type.endsWith("* const")) { // NOI18N
                    t = var.type.substring(0, var.type.length() - 7).trim();
                } else {
                    log.fine("Unexpected type of pointer variable");
                    t = var.type; // Is this an error? Shouldn't happen...
                }
            } else {
                v = var.value;
                t = var.type;
            }

            if (v != null && v.length() > 0) {
                rt = resolveType(t);
                if (v.charAt(0) == '{') {
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
                } else if (GdbUtils.isArray(rt) && 
                        ((rt.startsWith("char") && rt.substring(4).trim().startsWith("[")) || // NOI18N
                         (rt.startsWith("unsigned char") && rt.substring(13).trim().startsWith("[")))) { // NOI18N
                    // gdb puts them in string
                    count += parseCharArray(var, var.name, t, v);
                } else if (var.derefValue != null) {
                    if (var.type.endsWith(" **") && // NOI18N
                            isCharString(var.type.substring(0, var.type.length() - 1))) {
                        int idx = 0;
                        int max = 100;
                        v = var.derefValue;
                        AbstractField field = new AbstractField(var, var.name + '[' + idx++ + ']', t, v);
                        while (v != null && v.startsWith("0x") && !v.equals("0x0") && // NOI18N
                                v.indexOf('<') == -1) {
                            var.addField(field);
                            count++;
                            String n = var.name + '[' + idx++ + ']';
                            field = new AbstractField(var, n, t, null);
                            v = getDerefValue(field, n);
                            field.value = v;
                            if (idx > max) {
                                break;
                            }
                        }
                    } else if (!isCharString(var.type)) {
                        var.addField(new AbstractField(var, '*' + var.name, t, v));
                        count ++;
                    }
                }
            }
        }
//            if (count > 0 && this instanceof GdbWatchVariable) {
//                WatchesTreeModel.getWatchesTreeModel().fireTreeChanged();
//            }
        return count;
    }
    
    private String resolveType(String t) {
        if (getCurrentCallStackFrame() != null) {
            String base = GdbUtils.getBaseType(t);
            String extra;
            
            if (base != null) {
                if (!base.equals(t)) {
                    extra = t.substring(base.length());
                } else {
                    extra = "";
                }
                Object o = getCurrentCallStackFrame().getType(base);
                if (o instanceof String) {
                    return o.toString() + extra;
                }
            }
        }
        return t;
    }
    
    /**
     * Check the type. Does it resolve to a char *? If so then we don't want to
     * expand it further. But if its not a char * then we (probably) do.
     * 
     * @param info The string to verify
     * @return true if t is some kind of a character pointer
     */
    private boolean isCharString(String t) {
        if (t != null && t.endsWith("*") && !t.endsWith("**")) { // NOI18N
            t = GdbUtils.getBaseType(t);
            if (t.equals("char") || t.equals("unsigned char")) { // NOI18N
                return true;
            } else if (getCurrentCallStackFrame() != null) {
                Object o = getCurrentCallStackFrame().getType(t);
                if (o instanceof String) {
                    String t2 = o.toString();
                    if (t2.equals("char") || t2.equals("unsigned char")) { // NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Remove the cast of a value string */
    private String uncast(String info) {
        if (info.length() > 0 && info.charAt(0) == '(') {
            int pos = GdbUtils.findMatchingParen(info, 0);
            if (pos != -1) {
                return info.substring(pos + 1).trim();
            }
        }
        return info;
    }
    
    private String getDerefValue(AbstractVariable var, String name) {
        synchronized (derefLock) {
            try {
                getDebugger().requestDerefValue(var, name);
                derefLock.wait(200);
            } catch (InterruptedException ex) {
                return "";
            }
        }
        return var.derefValue;
    }
    
    public void setDerefValue(String value) {
        synchronized (derefLock) {
            derefValue = value;
            derefLock.notifyAll();
        }
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
    
    private int parseCharArray(AbstractVariable var, String basename, String type, String value) {
        String frag;
        int count = 0;
        int idx = 0;
        int pos;
        
        while (idx < value.length()) {
            if (value.substring(idx).startsWith("\\\"")) { // NOI18N
                pos = value.indexOf("\\\",", idx);
                if (pos > 0) {
                    frag = value.substring(idx + 2, pos);
                    count += parseCharArrayFragment(var, basename, type, frag);
                    idx += frag.length() + 4;
                }
            } else if (value.charAt(idx) == ' ' || value.charAt(idx) == ',') {
                idx++;
            } else {
                pos = GdbUtils.findNextComma(value, idx);
                if (pos > 0) {
                    frag = value.substring(idx, pos);
                } else {
                    frag = value.substring(idx);
                }
                count += parseRepeatArrayFragment(var, basename, type, frag);
                idx += frag.length();
            }
        }
        return count;
    }
    
    private int parseRepeatArrayFragment(AbstractVariable var, String basename, String type, String value) {
        String t = type.substring(0, type.indexOf('[')).trim();
        int count;
        int idx = var.fields.length;
        int pos = value.indexOf(' ');
        String val = value.substring(0, pos);
        int pos1 = value.indexOf("<repeats ");
        int pos2 = value.indexOf(" times>");
        
        try {
            count = Integer.parseInt(value.substring(pos1 + 9, pos2));
        } catch (Exception ex) {
            return 0;
        }
        
        while (--count >=0) {
            var.addField(new AbstractField(var, basename + "[" + idx++ + "]", // NOI18N
                t, '\'' + val + '\''));
        }
        return 0;   
    }
    
    private int parseCharArrayFragment(AbstractVariable var, String basename, String type, String value) {
        String t = type.substring(0, type.indexOf('[')).trim();
        int vidx = 0;
        int count = value.length();
        int idx = var.fields.length;
        
        while (vidx < count) {
            String val;
            if (vidx < (count - 2) && value.substring(vidx, vidx + 2).equals("\\\\")) { // NOI18N
                char ch = value.charAt(vidx + 2);
                if (Character.isDigit(ch)) {
                    val = '\\' + value.substring(vidx + 2, vidx + 5);
                    vidx += 5;
                } else {
                    val = '\\' + value.substring(vidx + 2, vidx + 3);
                    vidx += 3;
                }
            } else if (charAt(value, vidx) == '\\') { // we're done...
                val = "\\000"; // NOI18N
            } else {
                val = value.substring(vidx, vidx + 1);
                vidx++;
            }
            var.addField(new AbstractField(var, basename + "[" + idx++ + "]", // NOI18N
                t, '\'' + val + '\''));
        }
        return count;
    }
    private char charAt(String info, int idx) {
        try {
            return info.charAt(idx);
        } catch (StringIndexOutOfBoundsException e) {
            return 0;
        }
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
        
    public String getFullName(boolean showBase) {
        if (this instanceof AbstractField) {
            return ((AbstractField) this).getFullName(showBase);
        } else {
            return getName();
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
                this.value = GdbUtils.mackHack(value);
            }
        }
        
        public boolean isStatic() {
            return false;
        }
        
        @Override
        public String getFullName(boolean showBaseClass) {
            String pname; // parent part of name
            String fullname;
            int pos;
            
            if (parent instanceof AbstractField) {
                pname = ((AbstractField) parent).getFullName(showBaseClass);
            } else {
                pname = parent.getName();
            }
            
            if (name.equals("<Base class>")) { // NOI18N
                if (showBaseClass) {
                    fullname = pname + ".<" + type + ">"; // NOI18N
                } else {
                    fullname = pname;
                }
            } else if (name.indexOf('[') != -1) {
                if ((pos = pname.lastIndexOf('.')) != -1) {
                    fullname = pname.substring(0, pos) + '.' + name;
                } else {
                    fullname = name;
                }
            } else if (GdbUtils.isSimplePointer(parent.getType()) && name.startsWith("*")) { // NOI18N
                fullname = '*' + pname;
            } else if (GdbUtils.isPointer(parent.getType())) {
                fullname = pname + "->" + name; // NOI18N
            } else {
                fullname = pname + '.' + name;
            }
            return fullname;
        }
    }
}

