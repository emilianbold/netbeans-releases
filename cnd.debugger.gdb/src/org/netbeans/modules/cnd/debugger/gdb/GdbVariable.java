/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.util.NbBundle;

/**
 * Defines a variable as gdb sees it. Each variable has a name, type, and value.
 *
 * @author gordonp
 */
public class GdbVariable {
    
    private String name;
    private String type;
    private Object realtype;
    private String value;
    private String derefValue;
    private List<GdbVariable> children;
    private GdbDebugger debugger;
    
    /** Creates a new instance of GdbVariable */
    public GdbVariable(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
        realtype = null;
        derefValue = null;
        children = new ArrayList();
        debugger = null;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Object getRealType() {
        if (realtype == null) {
            if (GdbUtils.isSimple(type) || GdbUtils.isSimplePointer(type)) {
                realtype = type;
            } else {
                CallStackFrame csf = getDebugger().getCurrentCallStackFrame();
                if (csf != null && type != null) { // csf can be null if we're processing a response after killing the session
                    if (type.endsWith("{...}")) { // NOI18N - Anonymous struct/union/type
                        realtype = csf.getType('$' + name);
                    } else if (type.endsWith("]")) { // NOI18N
                        realtype = type;
                    } else {
                        realtype = csf.getType(getDebugger().trimKey(type));
                    }
                }
            }
        }
        return realtype;
    }
    
    /**
     * We may not have enough type information to correctly set realtype. So we have a
     * way of resetting it so it can get correctly updated.
     */
    public void resetRealType() {
        realtype = null;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setDerefedValue(String derefValue) {
        this.derefValue = derefValue;
    }
    
    public List<GdbVariable> getChildren() {
        if (children.size() == 0 && !Thread.currentThread().getName().equals("GdbReaderRP")) {
            if (getRealType() != null) {
                if (!GdbUtils.isFunctionPointer(getRealType()) || GdbUtils.isArray(getRealType())) {
                    if (GdbUtils.isArray(getRealType())) {
                        parseArray(this, value.substring(1, value.length() - 1));
                    } else if (GdbUtils.isStructOrUnion(getRealType())) {
                        if (value.charAt(0) == '{') {
                            parseStructUnionClass(value.substring(1, value.length() - 1));
                        } else if (value.charAt(0) == '(' && derefValue != null) {
                            // derefValue can be null if we're dereferencing an unititialized variable
                            parseStructUnionClass(derefValue.substring(1, derefValue.length() - 1));
                        }
                    } else if (GdbUtils.isClass(getRealType())) {
                        parseStructUnionClass(value.substring(1, value.length() - 1));
                    }
                    for (GdbVariable child : children) {
                        child.children = child.getChildren();
                    }
                }
            }
        }
        return children;
    }
    
    public int getNumberChildren() {
        return children.size();
    }
    
    private static void parseArray(GdbVariable var, String info) {
        int start = 0;
        int pos;
        int size = 0;
        String c_type = "";// NOI18N
        int pos2;
        int size2 = -1;
        GdbVariable child;
        String rt = (String) var.getRealType();
        
        try {
            pos = rt.indexOf('[');
            size = Integer.valueOf(rt.substring(pos + 1, rt.indexOf(']')));
            c_type = rt.substring(0, pos).trim();
            pos2 = rt.indexOf('[', pos + 1);
            if (pos2 != -1) {
                size2 = Integer.valueOf(rt.substring(pos2 + 1, rt.indexOf(']', pos2)));
            }
        } catch (Exception ex) {
        }
        for (int i = 0; i < size; i++) {
            child = new GdbVariable(var.name + "[" + i + "]", null, null); // NOI18N
            var.children.add(child);
            if (info.charAt(start) == '{') {
                pos = GdbUtils.findMatchingCurly(info, start);
                child.value = info.substring(start, pos);
                if (size2 != -1) {
                    child.type = c_type + "[" + size2 + "]"; // NOI18N
                    parseArray(child, child.value.substring(1, child.value.length() - 1));
                } else {
                    child.type = c_type;
                }
                start = GdbUtils.firstNonWhite(info, pos + 1);
            } else {
                pos = GdbUtils.findNextComma(info, start);
                child.type = c_type;
                if (pos != -1) {
                    child.value = info.substring(start, pos - 1);
                    start = GdbUtils.firstNonWhite(info, pos + 1);
                } else {
                    child.value = info.substring(start);
                }
            }
        }
    }
    
    private void parseStructUnionClass(String info) {
        Map<String, Object> map = (Map) getRealType();
        int start = 0;
        int pos;
        String name;
        String type = null;
        String value; 
        
        children.clear();
        while (start != -1 && (pos = info.indexOf('=', start)) != -1) {
            name = info.substring(start, pos - 1);
            if (name.startsWith("static ")) { // NOI18N
                name = name.substring(7).trim();
            } else if (name.startsWith("const ")) { // NOI18N
                name = name.substring(6).trim();
            } else if (name.startsWith("mutable ")) { // NOI18N
                name = name.substring(8).trim();
            }

            Object o = map.get(name);
            if (o instanceof String) {
                type = o.toString();
                getDebugger().addTypeCompletion(type);
            } else if (name.charAt(0) == '<' && name.charAt(name.length() - 1) == '>') {
                String superclass = name.substring(1, name.length() - 1);
                getDebugger().addTypeCompletion(superclass);
                name = NbBundle.getMessage(GdbVariable.class, "LBL_BaseClass"); // NOI18N
                type = superclass; // NOI18N
            } else if (name.startsWith("_vptr")) { // NOI18N
                name = null; // skip this - its not a real field
                type = "";
            }
            
            start = GdbUtils.firstNonWhite(info, pos + 1);
            if (info.charAt(start) == '{') {
                pos = GdbUtils.findMatchingCurly(info, start);
                if (pos == -1) {
                    value = info.substring(start); // mis-formed string...
                } else {
                    value = info.substring(start, pos);
                }
            } else {
                if (type.startsWith("char[")) {
                    pos = findCorrectComma(info, start);
                } else {
                    pos = GdbUtils.findNextComma(info, start);
                }
                if (pos == -1) {
                    value = info.substring(start);
                } else {
                    value = info.substring(start, pos - 1);
                }
            }
            if (name != null) {
                children.add(new GdbVariable(name, type, value));
            }
            start = GdbUtils.firstNonWhite(info, start + value.length() + 1);
        }
    }
    
    /**
     * Special case - char arrays can have an extra comma if the value string
     * contains both a double quoted string and some number of repeated chars
     * (usually zeros).
     *
     * For instance (in struct stat on Solaris):
     *   ... st_fstype = \"ufs\", '\\0' <repeats 12 times>, ...
     */
    private int findCorrectComma(String info, int start) {
        int pos = info.indexOf('=', start);
        if (pos != -1) {
            // at least 1 more field...
            pos = info.substring(0, pos).lastIndexOf(',');
            if (pos != -1) {
                return pos + 1;
            }
        }
        return -1;
    }
    
    /** We need the debugger to get the current stack to get struct type completion... */
    private GdbDebugger getDebugger() {
        if (debugger == null) {
            DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if (currentEngine == null) {
                return null;
            }
            debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
        }
        return debugger;
    }
}
