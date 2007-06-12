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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;

/**
 * Defines a variable as gdb sees it. Each variable has a name, type, and value.
 *
 * @author gordonp
 */
public class GdbVariable {
    
    private String name;
    private String type;
    private String realtype;
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
    
    public String getRealType() {
        return realtype;
    }
    
    public void setRealType(String realtype) {
        this.realtype = realtype;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setDerefedValue(String derefValue) {
        this.derefValue = derefValue;
        if (type.indexOf('[') != -1) {
            parseArray(this, derefValue.substring(1, derefValue.length() - 1));
        } else if (derefValue.charAt(0) == '{') {
            String type = getRealType();
            if (type.indexOf("struct") != -1 || type.indexOf("class") != -1) { // NOI18N
                parseStructOrUnion(getTypeMap(), derefValue.substring(1, derefValue.length() - 1));
            }
        }
    }
    
    private Map getTypeMap() {
        Object o = getDebugger().getCurrentCallStackFrame().getType(realtype);
        if (o instanceof Map) {
            return (Map) o;
        } else {
            return null;
        }
    }
    
    public List<GdbVariable> getChildren() {
        if (children.size() == 0) {
            if (GdbUtils.isArray(realtype)) {
                parseArray(this, value.substring(1, value.length() - 1));
            } else if (GdbUtils.isStructOrUnion(realtype)) {
                parseStructOrUnion(getTypeMap(), value.substring(1, value.length() - 1));
            } else if (GdbUtils.isClass(realtype)) {
                parseClass(getTypeMap(), value.substring(1, value.length() - 1));
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
        int size2 = 0;
        GdbVariable child;
        
        try {
            pos = var.type.indexOf('[');
            size = Integer.valueOf(var.type.substring(pos + 1, var.type.indexOf(']')));
            c_type = var.type.substring(0, pos).trim();
            pos2 = var.type.indexOf('[', pos + 1);
            if (pos2 != -1) {
                size2 = Integer.valueOf(var.type.substring(pos2 + 1, var.type.indexOf(']', pos2)));
            }
        } catch (Exception ex) {
        }
        for (int i = 0; i < size; i++) {
            child = new GdbVariable(var.name + "[" + i + "]", null, null); // NOI18N
            var.children.add(child);
            if (info.charAt(start) == '{') {
                pos = GdbUtils.findMatchingCurly(info, start);
                child.type = c_type + "[" + size2 + "]"; // NOI18N
                child.value = info.substring(start, pos);
                parseArray(child, child.value.substring(1, child.value.length() - 1));
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
    
    private void parseStructOrUnion(Map map, String info) {
        int start = 0;
        int pos;
        String name;
        String type;
        String value; 
        
        children.clear();
        while (start != -1 && (pos = info.indexOf('=', start)) != -1) {
            name = info.substring(start, pos - 1);
            start = GdbUtils.firstNonWhite(info, pos + 1);
            if (info.charAt(start) == '{') {
                pos = GdbUtils.findMatchingCurly(info, start);
                if (pos == -1) {
                    value = info.substring(start); // mis-formed string...
                } else {
                    value = info.substring(start, pos);
                }
            } else {
                pos = GdbUtils.findNextComma(info, start);
                if (pos == -1) {
                    value = info.substring(start);
                } else {
                    value = info.substring(start, pos - 1);
                }
            }

            Object o;
            if (map != null && (o = map.get(name)) != null && o instanceof String) {
                type = (String) o;
            } else {
                type = "";
            }
            children.add(new GdbVariable(name, type, value));
            start = GdbUtils.firstNonWhite(info, start + value.length() + 1);
        }
    }
    
    private void parseClass(Map map, String info) {
        int start = 0;
        int pos;
        String name;
        String type;
        String value; 
        
        children.clear();
        while (start != -1 && (pos = info.indexOf('=', start)) != -1) {
            name = info.substring(start, pos - 1);
            start = GdbUtils.firstNonWhite(info, pos + 1);
            if (info.charAt(start) == '{') {
                pos = GdbUtils.findMatchingCurly(info, start);
                if (pos == -1) {
                    value = info.substring(start); // mis-formed string...
                } else {
                    value = info.substring(start, pos);
                }
            } else {
                pos = GdbUtils.findNextComma(info, start);
                if (pos == -1) {
                    value = info.substring(start);
                } else {
                    value = info.substring(start, pos - 1);
                }
            }

            Object o;
            if (map != null && (o = map.get(name)) != null && o instanceof String) {
                type = (String) o;
            } else {
                type = "";
            }
            children.add(new GdbVariable(name, type, value));
            start = GdbUtils.firstNonWhite(info, start + value.length() + 1);
        }
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
