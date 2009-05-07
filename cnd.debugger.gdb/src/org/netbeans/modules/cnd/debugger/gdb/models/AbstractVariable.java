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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbErrorException;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.TypeInfo;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/*
 * An AbstractVariable is an array, pointer, struct, or union.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements LocalVariable, Customizer, PropertyChangeListener {

    private GdbDebugger debugger;
    protected String name;
    protected String type;
    protected String value;
//    protected String derefValue;
    protected Field[] fields;
    protected TypeInfo tinfo;
    private static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N

    private Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();

    /** Create the AV from a GV. If the GV has children then create similar children for the AV */
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getValue());
    }

    public AbstractVariable(String name) {
        this(name, null);
    }

    public AbstractVariable(String name, String value) {
        assert name.indexOf('{') == -1; // this means a mis-parsed gdb response...
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N
        assert !SwingUtilities.isEventDispatchThread();
        this.name = name;
        type = getDebugger().requestWhatis(name);
        fields = new Field[0];
        tinfo = TypeInfo.getTypeInfo(getDebugger(), this);
        getDebugger().addPropertyChangeListener(GdbDebugger.PROP_VALUE_CHANGED, this);

        if (Utilities.getOperatingSystem() != Utilities.OS_MAC) {
            this.value = value;
        } else {
            // Convert the Mac-specific value to standard gdb/mi format
            this.value = GdbUtils.mackHack(value);
        }

//        if (GdbUtils.isSinglePointer(type)) {
//            derefValue = getDebugger().requestValue('*' + name);
//        } else {
//            derefValue = null;
//        }
    }

    protected AbstractVariable() { // used by AbstractField instantiation...
        if (getDebugger() != null) {
            getDebugger().addPropertyChangeListener(GdbDebugger.PROP_VALUE_CHANGED, this);
        }
    }

    protected TypeInfo getTypeInfo() {
        if (tinfo == null) {
            tinfo = TypeInfo.getTypeInfo(getDebugger(), this);
        }
        return tinfo;
    }

    protected void emptyFields() {
        int i, k = fields.length;
        for (i=0; i < k; i++) {
            Field field = fields[i];
            // we only care about AbstractVariables here, other implementations should care themselves
            if (field instanceof AbstractVariable) {
                getDebugger().removePropertyChangeListener(GdbDebugger.PROP_VALUE_CHANGED, (AbstractVariable)field);
            }
        }
        fields = new Field[0];
    }

    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType() {
        return type;
    }

    /**
     * Similar to getType() except this method will wait for the type to be supplied in
     * an alternate thread.
     *
     * @return declared type of this local
     */
    public String waitForType() {
        if (!SwingUtilities.isEventDispatchThread()) {
            int count = 20;

            // this can get called while var is waiting for its type to be returned
            while (type == null && count-- > 0 && debugger.isStopped()) {
                if (log.isLoggable(Level.FINE) && count == 19) {
                    log.fine("AV.waitForType: Waiting on type for " + name);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
        }
        return type;
    }

    /**
     * Returns string representation of type of this variable.
     *
     * @return string representation of type of this variable.
     */
    public String getValue() {
        if (value != null && value.length() > 0) {
            if (value.charAt(0) == '>' && value.endsWith(".\"<")) { // NOI18N
                return '>' + value.substring(2, value.length() - 3).replace("\\\"", "\"") + '<'; // NOI18N
            } else {
                return value.replace("\\\"", "\""); // NOI18N
            }
        } else {
            return "";
        }
    }

    protected String getResolvedType() {
        return getTypeInfo().getResolvedType(this);
    }

    /**
     * Sets string representation of value of this variable. In this case we ignore the
     * request because we only allow setting values on leaves.
     *
     * @param value string representation of value of this variable.
     */
    public void setValue(String value) {
        // no need to update to the same value
        if (value.equals(this.value)) {
            return;
        }
        String msg = null;
        String rt = getResolvedType();
        int pos;

        if (getDebugger() != null) {
            value = value.trim();
            if (value.length() > 0 && value.charAt(0) == '(' && (pos = GdbUtils.findMatchingParen(value, 0)) != -1) {
                // Strip a cast
                value = value.substring(pos + 1).trim();
            }
            if (rt.equals("char") || rt.equals("unsigned char")) { // NOI18N
                value = setValueChar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char"); // NOI18N
                }
            } else if (rt.equals("char *") || rt.equals("unsigned char *") || rt.equals("const char *")) { // NOI18N
                //see IZ: 151642 - string values may differ
                if (value.replace("\"", "\\\"").equals(this.value)) { // NOI18N
                    return;
                }
                value = setValueCharStar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                }
            } else if ((rt.equals("int") || rt.equals("long"))) { // NOI18N
                value = setValueNumber(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Number"); // NOI18N
                }
            } else if (rt.equals("bool") || (!debugger.isCplusPlus() && rt.equals("_Bool"))) { // NOI18N
                if (!value.equals("true") && !value.equals("false") && !isNumberInRange(value, 0, 1)) { // NOI18N
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_CplusPlus_Bool"); // NOI18N
                }
            } else if (rt.startsWith("enum ")) { // NOI18N
                value = setValueEnum(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Enum"); // NOI18N
                }
            } else if (value.charAt(0) == '"' || (value.startsWith("0x") && value.endsWith("\""))) { // NOI18N
                //see IZ: 151642 - string values may differ
                if (value.replace("\"", "\\\"").equals(this.value)) { // NOI18N
                    return;
                }
                value = setValueCharStar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                }
            } else if (GdbUtils.isPointer(rt)) {
                // no current validation
            }
            if (value != null) {
                // disabled for now, otherwise string values \"xxx\" do not work correctly
//                if (value.endsWith("\\\"")) { // NOI18N
//                    pos = value.indexOf('"');
//                    if (pos != -1) {
//                        value = value.substring(pos, value.length() - 1) + '"';
//                    }
//                }
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
                    fullname = ((GdbWatchVariable) this).getWatch().getExpression();
                } else {
                    fullname = getFullName();
                }
                if (!debugger.isCplusPlus() && rt.equals("_Bool") && !isNumber(value)) { // NOI18N
                    value = value.equals("true") ? "1" : "0"; // NOI18N - gdb doesn't handle
                }
                this.value = getDebugger().updateVariable(fullname, value);
                if (this instanceof AbstractField) {
                    // This code transfers changes between Local Variables and Watches
                    AbstractVariable parent = ((AbstractField) this).parent;
                    while (parent instanceof AbstractField) {
                        parent.value = getDebugger().requestValue(parent.getFullName());
                        getDebugger().variableChanged(parent);
                        parent = ((AbstractField) parent).parent;
                    }
                    parent.value = getDebugger().requestValue(parent.getName());
                    getDebugger().variableChanged(parent);

                    // Special case: If a Watch is changed before the Local Variables view
                    // is displayed, the AbstractVariable will be created from the GdbVariable.
                    // For that case, we need to update the GdbVariable!
                    getDebugger().updateGdbVariable(parent.getName(), parent.value);
                    getDebugger().fireLocalsRefresh(parent);
                }
                getDebugger().variableChanged(this);
            }
        }
        if (msg != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
            nd.setTitle(NbBundle.getMessage(AbstractVariable.class, "TITLE_SetValue_Warning")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    public synchronized void setModifiedValue(String value) {
        this.value = value;
        if (fields.length > 0) {
            emptyFields();
//            derefValue = null;
            if (value.length() > 0) {
                expandChildren();
            }
        }
    }

    /**
     * Validate the string passed to setValue. Verify its a correct char format and remove a leading
     * address if needed.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private static String setValueChar(String value) {
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
    private static String setValueCharStar(String value) {
        int pos;

        if (value.startsWith("0x") && (pos = value.indexOf(" \\\"")) != -1 && value.endsWith("\\\"")) { // NOI18N
            value = '"' + value.substring(pos + 3, value.length() - 2) + '"'; // NOI18N
        } else if (value.startsWith("0x") && (pos = value.indexOf(" \"")) != -1 && value.endsWith("\"")) { // NOI18N
            value = value.substring(pos + 1).replace("\"", "\\\""); // NOI18N
        } else if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') { // NOI18N
            value = value.replace("\"", "\\\""); // NOI18N
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
        int pos1, pos2;

        String info = getResolvedType();
        pos1 = info.indexOf('{');
        pos2 = info.indexOf('}');
        if (pos1 > 0 && pos2 > 0) {
            String enum_values = info.substring(pos1 + 1, pos2);
            for (String frag : enum_values.split(", ")) { // NOI18N
                if (value.equals(frag)) {
                    return value;
                }
            }
        } else {
            info = getTypeInfo().getDetailedType(this);
            pos1 = info.indexOf('{');
            pos2 = info.indexOf('}');
            if (pos1 > 0 && pos2 > 0) {
                String enum_values = info.substring(pos1 + 1, pos2);
                for (String frag : enum_values.split(", ")) { // NOI18N
                    if (value.equals(frag)) {
                        return value;
                    }
                }
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
    private static String setValueNumber(String value) {
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
    * @return 0 if the variable shouldn't have a turner and fields.length if it should
    */
    public int getFieldsCount() {
        if (getDebugger() == null || !getDebugger().isStopped()) {
            return 0;
        } else if (fields.length > 0) {
            return fields.length;
        } else if (mightHaveFields()) {
            return estimateFieldCount();
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
        String rt = getResolvedType();
        if (rt != null && rt.length() > 0) {
            if (GdbUtils.isArray(rt) && !isCharString(rt)) {
                return true;
            } else if (isValidPointerAddress()) {
                if (GdbUtils.isFunctionPointer(rt) || rt.equals("void *") || // NOI18N
                        (isCharString(rt) && !GdbUtils.isMultiPointer(rt))) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        // check if value like {...}
        return value != null && value.length() > 0 &&
            (value.charAt(0) == '{' || value.charAt(value.length() - 1) == '}');
    }

    /**
     * I'd like to estimate field count based on the value string. However, this might
     * actually be better. If I set the children count high then it gets reset once the
     * children get created. If I set it too low, only that number of fields are shown
     * (even though the var has more fields).
     */
    private int estimateFieldCount() {
        return 100;
    }

    private boolean isValidPointerAddress() {
        String frag = "";
        int pos1;
        long i;

        if (value == null && this instanceof GdbWatchVariable) {
            getValue(); // A watch might not have a value yet. This will initialize "value"
        }
        if (value != null) { // value can be null for watches during initialization...
            if (value.length() > 0 && value.charAt(0) == '(') {
                pos1 = value.indexOf("*) 0x"); // NOI18N
                if (pos1 == -1) {
                    pos1 = value.indexOf("* const) 0x"); // NOI18N
                    if (pos1 != -1) {
                        frag = value.substring(pos1 + 11);
                    }
                } else {
                    frag = value.substring(pos1 + 5);
                }
                if (pos1 != -1) {
                    try {
                        i = Long.parseLong(frag, 16);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    return i > 0;
                }
            } else if (value.startsWith("0x")) { // NOI18N
                try {
                    i = Long.parseLong(value.substring(2), 16);
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

    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractVariable &&
                    getFullName(true).equals(((AbstractVariable) o).getFullName(true));
    }

    @Override
    public int hashCode() {
        return getFullName(true).hashCode();
    }

    public String getName() {
        return name;
    }

    protected final GdbDebugger getDebugger() {
        if (debugger == null) {
            debugger = GdbDebugger.getGdbDebugger();
        }
        return debugger;
    }

    public synchronized boolean expandChildren() {
        if (fields.length == 0) {
            createChildren();
        }
        return fields.length > 0;
    }

    private void createChildren() {
        String resolvedType = getResolvedType();
        String t = null;
        String v = null;

        if (GdbUtils.isPointer(resolvedType) && !isCharString(resolvedType) && !GdbUtils.isMultiPointer(resolvedType)) {
            if (value.endsWith(" 0") || value.endsWith(" 0x0")) { // NOI18N
                t = null;
                v = null;
            } else {
                t = GdbUtils.getBaseType(resolvedType);
                try {
                    v = getDebugger().requestValueEx('*' + getFullName(false));
                } catch (GdbErrorException e) {
                    addField(new ErrorField(e.getMessage()));
                    return;
                }
            }
        } else {
            t = resolvedType;
            v = value;
        }
        if (v != null) { // v can be null if we're no longer in a stopped state
            if (GdbUtils.isArray(t)) {
                createChildrenForArray(t, v);
            } else if (GdbUtils.isMultiPointer(t)) {
                createChildrenForMultiPointer(t);
            } else {
                Map<String, Object> map = getTypeInfo().getMap();
                if (map != null) { // a null map means we never got type information
                    // see issues 162747 and 163290
                    if (v.indexOf('{') == -1) {
                        // an empty map means its a pointer to a non-struct/class/union
                        addField(new AbstractField(this, '*' + getName(), t, v));
                    } else if (v.equals("<incomplete type>")) { // NOI18N
                        addField(new AbstractField(this, "", t, v)); // NOI18N
                    } else if (v.length() > 0) {
                        int pos = v.indexOf('{');
                        assert pos != -1;
                        String val = v.substring(pos + 1, v.length() - 1);
                        int start = 0;
                        int end = GdbUtils.findNextComma(val, 0);
                        int anon = 1;
                        while (end != -1) {
                            String vfrag = val.substring(start, end).trim();
                            anon = completeFieldDefinition(this, map, vfrag, anon);
                            start = end + 1;
                            end = GdbUtils.findNextComma(val, end + 1);
                        }
                        completeFieldDefinition(this, map, val.substring(start).trim(), anon);
                    } else {
                        log.fine("AV.createChildren: 0 length value for " + getFullName(false));
                    }
                }
            }
        }
    }

    private void createChildrenForMultiPointer(String t) {
        int i = 0;
        String fullname = getFullName(false);
        String t2 = t.substring(0, t.length() - 1);
        int max_fields = t2.startsWith("char *") ? 20 : 10; // NOI18N
        int maxIndexLog = log10(max_fields-1);

        while (max_fields-- > 0) {
            String v = getDebugger().requestValue(fullname + '[' + i + ']');
            if (v == null || v.length() < 1 || v.endsWith("0x0")) { // NOI18N
                return;
            }
            addField(new AbstractField(this, getName() + getIndexStr(maxIndexLog, i++), t2, v));
        }
    }

    /**
     * Check the type. Does it resolve to a char *? If so then we don't want to
     * expand it further. But if its not a char * then we (probably) do.
     *
     * @param info The string to verify
     * @return true if t is some kind of a character pointer
     */
    private static boolean isCharString(String t) {
        if (t != null && t.endsWith("*") && !t.endsWith("**")) { // NOI18N
            t = GdbUtils.getBaseType(t);
            if (t.equals("char") || t.equals("unsigned char")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     * Complete and create the field information. Its OK to return null because addField
     * ignores it.
     * @return new counter of the anonymous elements
     */
    private int completeFieldDefinition(AbstractVariable parent, Map<String, Object> map, String info, int anon_count) {
        if (info.charAt(0) == '{') { // we've got an anonymous class/struct/union...
            @SuppressWarnings("unchecked")
            Map<String, Object> typeMap = (Map<String, Object>) map.get(TypeInfo.ANONYMOUS_PREFIX + anon_count + ">"); // NOI18N
            if (typeMap != null) {
                String fType = (String) typeMap.get(TypeInfo.NAME);
                addField(new AbstractField(parent, "", fType, info)); // NOI18N
                return ++anon_count;
            } else {
                log.warning("GdbDebugger.completeFieldDefinition: Missing type information for " + info);
            }
        } else {
            String fName, fType, fValue;
            int pos = info.indexOf('=');
            if (pos != -1) {
                if (info.charAt(0) == '<') {
                    fName = NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"); // NOI18N
                    fType = info.substring(1, pos - 2).trim();
                    fValue = info.substring(pos + 1).trim();
                    if (fType.length() == 0) {
                        // I think this is handling a gdb bug. Its hard to say because the exact response
                        // from gdb isn't well documented. In any case, this is triggered when the value
                        // of a superclass is an empty string (<> = {...}). Since we've parsed the super
                        // class already, I'm assuming single inheritance and taking the super from the map.
                        fType = (String) map.get(TypeInfo.SUPER_PREFIX + "1>"); // NOI18N
                    }
                    if (fName.startsWith("_vptr")) { // NOI18N
                        return anon_count;
                    }
                } else {
                    fName = info.substring(0, pos - 1).trim();
                    fValue = info.substring(pos + 1).trim();
                    if (fName.startsWith("_vptr")) { // NOI18N
                        return anon_count;
                    }
                    Object o = map.get(fName);
                    if (o instanceof String) {
                        fType = o.toString();
                    } else if (o instanceof Map) {
                        fType = (String) ((Map) o).get(TypeInfo.NAME);
			if (fType == null) {
			    log.warning("GdbDebugger.completeFieldDefinition: Missing " + TypeInfo.NAME + " from map");
			    return anon_count; // FIXME (See IZ 157133)
			}
                    } else if (isNumber(fValue)) {
                        fType = "int"; // NOI18N - best guess (std::string drops an "int")
                    } else {
                        log.warning("Cannot determine field type for " + fName); // NOI18N
                        return anon_count;
                    }
                }
                addField(new AbstractField(parent, fName, fType, fValue));
            } else if (info.trim().equals(TypeInfo.NO_DATA_FIELDS)) { // NOI18N
                addField(new AbstractField(parent, "", "", info.trim())); // NOI18N
            }
        }
        return anon_count;
    }

    private static void parseCharArray(AbstractVariable var, String basename, String type, int size, String value) {
        String frag;
        int idx = 0;
        int pos;
        boolean truncated = false;

        int maxIndexLog = log10(size-1);

        while (idx < value.length()) {
            if (value.substring(idx).startsWith("\\\"")) { // NOI18N
                pos = value.indexOf("\\\",", idx); // NOI18N
                if (pos >= 0) {
                    frag = value.substring(idx + 2, pos);
                    idx += frag.length() + 4;
                } else {
                    // Reached the end of the string...
                    if (value.endsWith("\\\"...")) { // NOI18N
                        frag = value.substring(idx + 2, value.length() - 5);
                        truncated = true;
                    } else {
                        frag = value.substring(idx + 2, value.length() - 2);
                    }
                    idx = value.length(); // stop iterating...
                }
                parseCharArrayFragment(var, basename, type, maxIndexLog, frag);
                if (var.fields.length < size && idx >= value.length()) {
                    var.addField(new AbstractField(var, basename + getIndexStr(maxIndexLog, size-1),
                            type.substring(0, type.indexOf('[')).trim(), "\'\\000\'")); // NOI18N
                }
                if (truncated) {
                    String high;
                    try {
                        high = type.substring(type.indexOf('[') + 1, type.indexOf(']')); // NOI18N
                        Integer.parseInt(high);
                    } catch (Exception ex) {
                        high = "..."; // NOI18N
                    }

                    var.addField(new AbstractField(var, basename + getIndexStr(maxIndexLog, var.fields.length, "-" + high), // NOI18N
                            "", "...")); // NOI18N
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
                parseRepeatArrayFragment(var, basename, type, maxIndexLog, frag);
                idx += frag.length();
            }
        }
    }

    private static void parseRepeatArrayFragment(AbstractVariable var, String basename, String type, int maxIndexLog, String value) {
        String t = type.substring(0, type.indexOf('[')).trim();
        int count;
        int idx = var.fields.length;
        int pos = value.indexOf(' ');
        String val = value.substring(0, pos).replace("\\\\", "\\"); // NOI18N
        int pos1 = value.indexOf("<repeats "); // NOI18N
        int pos2 = value.indexOf(" times>"); // NOI18N

        try {
            count = Integer.parseInt(value.substring(pos1 + 9, pos2));
        } catch (Exception ex) {
            return;
        }

        while (--count >=0) {
            var.addField(new AbstractField(var, basename + getIndexStr(maxIndexLog, idx++), t, val));
        }
    }

    private static void parseCharArrayFragment(AbstractVariable var, String basename, String type, int maxIndexLog, String value) {
        String t = type.substring(0, type.indexOf('[')).trim();
        int idx = 0;
        value = value.replace("\\\\", "\\"); // NOI18N - gdb doubles all backslashes...
        int count = value.length();
        int fcount = var.fields.length;

        while (idx < count) {
            int vstart = idx;
            char ch = value.charAt(idx++);
            String val;

            if (ch == '\\' && idx < count) {
                ch = value.charAt(idx++);
                if (ch >= '0' && ch <= '7') { // Octal constant
                    StringBuilder sb = new StringBuilder();
                    sb.append('\\');
                    sb.append(ch);
                    int i;
                    for (i = 0; i < 2 && idx < count; i++) {
                        ch = value.charAt(idx);
                        if (ch < '0' || ch > '7') {
                            break;
                        } else {
                            sb.append(ch);
                            idx++;
                        }
                    }
                    val = sb.toString();
                } else if (ch == 'x' || ch == 'X') { // Hex constant
                    StringBuilder sb = new StringBuilder();
                    sb.append('\\');
                    sb.append(ch);
                    int i;
                    for (i = 0; i < 2 && idx < count; i++) {
                        ch = value.charAt(idx);
                        if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')) {
                            break;
                        } else {
                            sb.append(ch);
                            idx++;
                        }
                    }
                    val = sb.toString();
                } else if (value.substring(idx - 1, idx).matches("['\"?abfnrt]") || ch == '\\') { // NOI18N
                    val = '\\' + value.substring(idx - 1, idx);
                } else {
                    log.warning("AV.parseCharArrayFragment: Ignoring invalid character array fragment"); // NOI18N
                    continue;
                }
            } else {
                val = value.substring(vstart, idx);
            }
            var.addField(new AbstractField(var, basename + getIndexStr(maxIndexLog, fcount++),
                t, '\'' + val + '\''));
        }
    }

    private void createChildrenForArray(String type, String value) {
        String t;
        int lbpos;
        int cbrace = type.lastIndexOf('}');
        if (cbrace == -1) {
            lbpos = type.indexOf('[');
        } else {
            lbpos = type.indexOf('[', cbrace);
            cbrace = type.indexOf('{');
        }
        int rbpos = GdbUtils.findMatchingBrace(type, lbpos);
        assert rbpos != -1;
        int vstart = 0;
        int vend;
        int size;
        int nextbrace = type.indexOf('[', rbpos);
        String extra;

        if (nextbrace == -1) {
            extra = "";
        } else {
            extra = type.substring(nextbrace);
        }
        if (cbrace == -1) {
            t = type.substring(0, lbpos).trim() + extra;
        } else {
            t = type.substring(0, cbrace).trim() + extra;
        }

        try {
            size = Integer.valueOf(type.substring(lbpos + 1, rbpos));
        } catch (Exception ex) {
            size = 0;
        }
        if (t.equals("char") || t.equals("unsigned char")) { // NOI18N
            parseCharArray(this, getName(), type, size, value);
        } else {
            value = value.substring(1, value.length() - 1);
            int maxIndexLog = log10(size-1);
            for (int i = 0; i < size && vstart != -1; i++) {
                if (value.charAt(vstart) == '{') {
                    vend = GdbUtils.findNextComma(value, GdbUtils.findMatchingCurly(value, vstart));
                } else {
                    vend = GdbUtils.findNextComma(value, vstart);
                }
                addField(new AbstractField(this, getName() + getIndexStr(maxIndexLog, i), t,
                        vend == -1 ? value.substring(vstart) : value.substring(vstart, vend)));
                vstart = GdbUtils.firstNonWhite(value, vend + 1);
            }
        }
    }

    private static String getIndexStr(int maxIndexLog, int index) {
        return getIndexStr(maxIndexLog, index, ""); // NOI18N
    }

    private static String getIndexStr(int maxIndexLog, int index, String postfix) {
        int num0 = maxIndexLog - log10(index);
        String data = index + postfix;
        if (num0 > 0) {
            data = zeros(num0) + data;
        }
        return "[" + data + "]"; // NOI18N
    }

    static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) {
            l++;
        }
        return l;
    }

    // We have the same in BreakpointsNodeModel
    private static final String ZEROS = "            "; // NOI18N

    static String zeros(int n) {
        // Perf & mem optimization
        switch (n) {
            case 1 : return " "; // NOI18N
            case 2 : return "  "; // NOI18N
        }
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) {
                z += " ";  // NOI18N
            }
            return z;
        }
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
    private void addField(Field field) {
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

    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(GdbDebugger.PROP_VALUE_CHANGED)) {
            assert ev.getNewValue() instanceof AbstractVariable;
            AbstractVariable av = (AbstractVariable) ev.getNewValue();
            if (av.getFullName().equals(getFullName())) {
                if (av instanceof AbstractField) {
                    final AbstractVariable ancestor = ((AbstractField) this).getAncestor();
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            ancestor.updateVariable();
                        }
                    });
                } else {
                    setModifiedValue(av.getValue());
                }
            }
        }
    }

    private void updateVariable() {
        value = getDebugger().requestValue("\"" + getName() + "\""); // NOI18N
//        String rt = getTypeInfo().getResolvedType(this);
//        if (GdbUtils.isPointer(rt)) {
//            derefValue = getDebugger().requestValue('*' + getName());
//        }
        setModifiedValue(value);
    }

    @Override
    public String toString() {
        return getFullName(false);
    }

    private static boolean isNumber(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean isNumberInRange(String value, long low, long high) {
        try {
            long val = Long.parseLong(value);
            return val >= low && val <= high;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public final String getFullName() {
        return getFullName(false);
    }

    protected String getFullName(boolean showBase) {
        return name;
    }

    private static class AbstractField extends AbstractVariable implements Field {
        private AbstractVariable parent;

        public AbstractField(AbstractVariable parent, String name, String type, String value) {
            assert name != null : "AbstractField with null name" ;// NOI18N
            if (name.startsWith("static ")) { // NOI18N
                this.name = name.substring(7);
            } else {
                this.name = name;
            }
            if (type == null) {
                this.type = "";
            } else {
                int lcurly = type.indexOf('{');
                if (lcurly == -1) {
                    this.type = type;
                } else {
                    int rcurly = type.indexOf('}', lcurly);
                    this.type = type.substring(0, lcurly).trim() + type.substring(rcurly + 1);
                }
            }
            this.parent = parent;
            fields = new Field[0];
//            derefValue = null;
            tinfo = null;

            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                this.value = GdbUtils.mackHack(value);
            } else {
                this.value = value;
            }
        }

        protected AbstractVariable getAncestor() {
            if (parent instanceof AbstractField) {
                return ((AbstractField) parent).getAncestor();
            } else {
                return parent;
            }
        }

        public boolean isStatic() {
            return false;
        }

        @Override
        protected String getFullName(boolean showBaseClass) {
            String pname; // parent part of name
            String fullname;
            int pos;

            if (parent instanceof AbstractField) {
                pname = ((AbstractField) parent).getFullName(showBaseClass);
            } else {
                pname = parent.getName();
                if (pname.charAt(0) == '*') { // NOI18N
                    pname = '(' + pname + ')';
                }
            }

            if (name.equals(NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"))) { // NOI18N
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
            } else if (GdbUtils.isSimplePointer(parent.getType()) && name.charAt(0) == '*') { // NOI18N
                fullname = '*' + pname;
            } else if (GdbUtils.isPointer(parent.getType())) {
                fullname = pname + "->" + name; // NOI18N
            } else if (name.length() > 0) {
                fullname = pname + '.' + name;
            } else {
                fullname = pname;
            }
            return fullname;
        }
    }

    public static class ErrorField implements Field {
        private final String msg;

        public ErrorField(String msg) {
            // Cut error signs > and <
            this.msg = msg.substring(1, msg.length()-1);
        }

        public String getName() {
            return NbBundle.getMessage(AbstractVariable.class, "LBL_Error"); // NOI18N
        }

        public boolean isStatic() {
            return false;
        }

        public void setValue(String value) throws InvalidExpressionException {
            throw new UnsupportedOperationException("Not supported"); // NOI18N
        }

        public String getType() {
            return "";
        }

        public String getValue() {
            return msg;
        }
    }
}

