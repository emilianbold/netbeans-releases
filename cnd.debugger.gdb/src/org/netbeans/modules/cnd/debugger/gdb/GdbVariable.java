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

package org.netbeans.modules.cnd.debugger.gdb;

import java.util.ArrayList;
import java.util.List;
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
    private Object realtype;
    private String value;
    private String derefValue;
    private List<GdbVariable> children;
    private GdbDebugger debugger;
    
    /** Creates a new instance of GdbVariable */
    public GdbVariable(String name, String type, String value) {
        if (name.startsWith("(anonymous namespace)::")) {  // NOI18N
            this.name = name.substring(23);
        } else {
            this.name = name;
        }
        this.type = type;
        this.value = value;
        realtype = null;
        derefValue = null;
        children = new ArrayList<GdbVariable>();
        debugger = null;
        
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            throw new IllegalStateException(); // creating a var after the session has been killed...
        }
        debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
    }
    
    public String getName() {
        return name;
    }
    
//    public String getType() {
//        return type;
//    }
//    
//    public void setType(String type) {
//        this.type = type;
//    }
//    
//    public Object getRealType() {
//        if (realtype == null) {
//            if (GdbUtils.isSimple(type) || GdbUtils.isSimplePointer(type)) {
//                realtype = type;
//            } else {
//                CallStackFrame csf = getDebugger().getCurrentCallStackFrame();
//                if (csf != null && type != null) { // csf can be null if we're processing a response after killing the session
//                    if (type.endsWith("{...}")) { // NOI18N - Anonymous struct/union/type
//                        realtype = csf.getType('$' + name);
//                    } else if (type.endsWith("]")) { // NOI18N
//                        realtype = type;
//                    } else {
//                        realtype = csf.getType(getDebugger().trimKey(type));
//                    }
//                }
//            }
//        }
//        return realtype;
//    }
//    
//    /**
//     * We may not have enough type information to correctly set realtype. So we have a
//     * way of resetting it so it can get correctly updated.
//     */
//    public void resetRealType() {
//        realtype = null;
//    }
    
    public String getValue() {
        return value;
    }
    
//    public void setValue(String value) {
//        this.value = value;
//    }
//    
//    public String getDerefedValue() {
//        return derefValue;
//    }
//    
//    public void setDerefedValue(String derefValue) {
//        this.derefValue = derefValue;
//    }
//    
//    public List<GdbVariable> getChildren() {
//        synchronized (children) {
//            if (children.size() == 0 && !Thread.currentThread().getName().equals("GdbReaderRP")) { // NOI18N
//                if (getRealType() != null) {
//                    if (!GdbUtils.isFunctionPointer(getRealType()) || GdbUtils.isArray(getRealType())) {
//                        if (GdbUtils.isArray(getRealType())) {
//                            parseArray(this, value.substring(1, value.length() - 1));
//                        } else if (GdbUtils.isStructOrUnion(getRealType())) {
//                            if (value.charAt(0) == '{') {
//                                parseStructUnionClass(value.substring(1, value.length() - 1));
//                            } else if (value.charAt(0) == '(' && derefValue != null) {
//                                // derefValue can be null if we're dereferencing an unititialized variable
//                                parseStructUnionClass(derefValue.substring(1, derefValue.length() - 1));
//                            }
//                        } else if (GdbUtils.isClass(getRealType())) {
//                            parseStructUnionClass(value.substring(1, value.length() - 1));
//                        }
//                        for (GdbVariable child : children) {
//                            child.children = child.getChildren();
//                        }
//                    }
//                }
//            }
//        }
//        return children;
//    }
//    
//    public int getNumberChildren() {
//        return children.size();
//    }
//    
//    private static void parseArray(GdbVariable var, String info) {
//        int start = 0;
//        int pos;
//        int size = 0;
//        String c_type = "";// NOI18N
//        int pos2;
//        int size2 = -1;
//        GdbVariable child;
//        String rt = (String) var.getRealType();
//        
//        try {
//            pos = rt.indexOf('[');
//            size = Integer.valueOf(rt.substring(pos + 1, rt.indexOf(']')));
//            c_type = rt.substring(0, pos).trim();
//            pos2 = rt.indexOf('[', pos + 1);
//            if (pos2 != -1) {
//                size2 = Integer.valueOf(rt.substring(pos2 + 1, rt.indexOf(']', pos2)));
//            }
//        } catch (Exception ex) {
//        }
//        for (int i = 0; i < size; i++) {
//            child = new GdbVariable(var.name + "[" + i + "]", null, null); // NOI18N
//            var.children.add(child);
//            if (info.charAt(start) == '{') {
//                pos = GdbUtils.findMatchingCurly(info, start);
//                child.value = info.substring(start, pos);
//                if (size2 != -1) {
//                    child.type = c_type + "[" + size2 + "]"; // NOI18N
//                    parseArray(child, child.value.substring(1, child.value.length() - 1));
//                } else {
//                    child.type = c_type;
//                }
//                start = GdbUtils.firstNonWhite(info, pos + 1);
//            } else {
//                pos = GdbUtils.findNextComma(info, start);
//                child.type = c_type;
//                if (pos != -1) {
//                    child.value = info.substring(start, pos - 1);
//                    start = GdbUtils.firstNonWhite(info, pos + 1);
//                } else {
//                    child.value = info.substring(start);
//                }
//            }
//        }
//    }
//    
//    private void parseStructUnionClass(String info) {
//        @SuppressWarnings("unchecked")
//        Map<String, Object> map = (Map) getRealType();
//        int start = 0;
//        int pos;
//        String lcl_name;
//        String lcl_type = null;
//        String lcl_value; 
//        
//        children.clear();
//        while (start != -1 && (pos = info.indexOf('=', start)) != -1) {
//            lcl_name = info.substring(start, pos - 1);
//            if (lcl_name.startsWith("static ")) { // NOI18N
//                lcl_name = lcl_name.substring(7).trim();
//            } else if (lcl_name.startsWith("const ")) { // NOI18N
//                lcl_name = lcl_name.substring(6).trim();
//            } else if (lcl_name.startsWith("mutable ")) { // NOI18N
//                lcl_name = lcl_name.substring(8).trim();
//            }
//
//            Object o = map.get(lcl_name);
//            if (o instanceof String) {
//                lcl_type = o.toString();
//                getDebugger().addTypeCompletion(lcl_type);
//            } else if (lcl_name.charAt(0) == '<' && lcl_name.charAt(lcl_name.length() - 1) == '>') {
//                String superclass = lcl_name.substring(1, lcl_name.length() - 1);
//                getDebugger().addTypeCompletion(superclass);
//                lcl_name = NbBundle.getMessage(GdbVariable.class, "LBL_BaseClass"); // NOI18N
//                lcl_type = superclass; // NOI18N
//            } else if (lcl_name.startsWith("_vptr")) { // NOI18N
//                lcl_name = null; // skip this - its not a real field
//                lcl_type = "";
//            }
//            
//            start = GdbUtils.firstNonWhite(info, pos + 1);
//            if (info.charAt(start) == '{') {
//                pos = GdbUtils.findMatchingCurly(info, start);
//                if (pos == -1) {
//                    lcl_value = info.substring(start); // mis-formed string...
//                } else {
//                    lcl_value = info.substring(start, pos);
//                }
//            } else {
//                if (lcl_type.startsWith("char[")) { // NOI18N
//                    pos = findCorrectComma(info, start);
//                } else {
//                    pos = GdbUtils.findNextComma(info, start);
//                }
//                if (pos == -1) {
//                    lcl_value = info.substring(start);
//                } else {
//                    lcl_value = info.substring(start, pos - 1);
//                }
//            }
//            if (lcl_name != null) {
//                children.add(new GdbVariable(lcl_name, lcl_type, lcl_value));
//            }
//            start = GdbUtils.firstNonWhite(info, start + lcl_value.length() + 1);
//        }
//    }
//    
//    /**
//     * Special case - char arrays can have an extra comma if the value string
//     * contains both a double quoted string and some number of repeated chars
//     * (usually zeros).
//     *
//     * For instance (in struct stat on Solaris):
//     *   ... st_fstype = \"ufs\", '\\0' <repeats 12 times>, ...
//     */
//    private int findCorrectComma(String info, int start) {
//        int pos = info.indexOf('=', start);
//        if (pos != -1) {
//            // at least 1 more field...
//            pos = info.substring(0, pos).lastIndexOf(',');
//            if (pos != -1) {
//                return pos + 1;
//            }
//        }
//        return -1;
//    }
    
    /** We need the debugger to get the current stack to get struct type completion... */
    private GdbDebugger getDebugger() {
        return debugger;
    }
}
