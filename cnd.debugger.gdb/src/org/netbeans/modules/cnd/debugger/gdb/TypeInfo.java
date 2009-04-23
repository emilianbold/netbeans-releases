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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbWatchVariable;
import org.netbeans.modules.cnd.debugger.gdb.utils.FieldTokenizer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.util.NbBundle;

/**
 * Define a data type.
 * @author gordonp
 */
public class TypeInfo {
    public static final String ANONYMOUS_PREFIX = "<anonymous";  // NOI18N
    public static final String NAME = "<name>";  // NOI18N
    public static final String NO_DATA_FIELDS = "<No data fields>";  // NOI18N
    public static final String SUPER_PREFIX = "<super";  // NOI18N
    public static final String ANON_COUNT = "<anon-count>";  // NOI18N
    
    private final GdbDebugger debugger;
    private String resolvedType;
    private String rawInfo;
    private Map<String, Object> map;
    private Map<String, TypeInfo> ticache;
    private static final Map<String, Map<String, Object>> mcache = new HashMap<String, Map<String, Object>>();
    protected static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N
//    private static final boolean disable_caches = Boolean.getBoolean("gdb.disable.ti.caches");
//    private static final boolean disable_ti_cache = disable_caches || Boolean.getBoolean("gdb.disable.ti.cache");
//    private static final boolean disable_map_cache = disable_caches || Boolean.getBoolean("gdb.disable.map.cache");
    private static final boolean disable_ti_cache = true; // FIXME - The cache doesn't work (see IZ 112664 test cases)
    private static final boolean disable_map_cache = true; // FIXME - The cache doesn't work (see IZ 112664 test cases)
    
    public static TypeInfo getTypeInfo(GdbDebugger debugger, AbstractVariable var) {
        String resolvedType;
        String rawInfo;
        Map<String, TypeInfo> ticache = debugger.getTypeInfoCache();
        
        TypeInfo tinfo = ticache.get(var.getType());
        if (tinfo != null) {
            return tinfo;
        }
        
        if (var.getName().equals(NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"))) { // NOI18N
            rawInfo = debugger.requestSymbolType(var.getType());
        } else {
            rawInfo = debugger.requestSymbolTypeFromName(var.getFullName());
        }
        log.fine("TI.getTypeInfo[rawInfo]: " + var.getType() + " ==> [" + rawInfo + "]");
        
        if (rawInfo != null && rawInfo.length() > 0) {
            rawInfo = rawInfo.replace("\\n", "").trim(); // NOI18N
            int pos1 = rawInfo.indexOf('{');
            if (pos1 == -1) {
                resolvedType = rawInfo;
            } else {
                resolvedType = rawInfo.substring(0, pos1).trim();
                int pos2 = resolvedType.indexOf(" : "); // NOI18N
                if (pos2 != -1) {
                    resolvedType = resolvedType.substring(0, pos2);
                }
                pos2 = GdbUtils.findMatchingCurly(rawInfo, pos1);
                if (pos2 != -1) {
                    resolvedType = resolvedType + rawInfo.substring(pos2 + 1);
                }
            }
            tinfo = ticache.get(resolvedType);
            if (tinfo != null) {
//                log.fine("TI.getTypeInfo[rt]: " + var.getType() + " ==> [" + resolvedType + "]"); // NOI18N
                return tinfo;
            }
        } else {
            resolvedType = null;
        }
        
        return new TypeInfo(debugger, var.getType(), resolvedType, rawInfo);
    }
    
    public TypeInfo(GdbDebugger debugger, String vartype, String resolvedType, String rawInfo) {
        this.debugger = debugger;
        this.resolvedType = resolvedType;
        this.rawInfo = rawInfo;
        map = null;
        
        if (resolvedType != null && resolvedType.length() > 0 && !disable_ti_cache) {
            ticache = debugger.getTypeInfoCache();
            log.fine("TI.<Init>: " + vartype + " ==> [" + resolvedType + ", " + rawInfo + "]");

            if (vartype != null && vartype.length() > 0) {
                if (!vartype.equals(resolvedType)) {
                    ticache.put(resolvedType, this);
                }
                ticache.put(vartype, this);
            }
        }
    }
    
    public String getResolvedType(AbstractVariable var) {
        if (var instanceof GdbWatchVariable) {
            // Reset type info because a watch can change its type from stop to stop
            resolvedType = null;
            rawInfo = null;
            map = null;
        }
        if (resolvedType == null) {
            if (rawInfo == null) {
                rawInfo = debugger.requestSymbolTypeFromName(var.getFullName());
            }
            if (rawInfo != null) {
                rawInfo = rawInfo.replace("\\n", "").trim(); // NOI18N
                int pos1 = rawInfo.indexOf('{');
                if (pos1 == -1) {
                    resolvedType = rawInfo;
                } else {
                    resolvedType = rawInfo.substring(0, pos1).trim();
                    int pos2 = resolvedType.indexOf(" : "); // NOI18N
                    if (pos2 != -1) {
                        resolvedType = resolvedType.substring(0, pos2);
                    }
                    pos2 = GdbUtils.findMatchingCurly(rawInfo, pos1);
                    if (pos2 != -1) {
                        resolvedType = resolvedType + rawInfo.substring(pos2 + 1);
                    }
                }
            }
        }
        return resolvedType;
    }
    
    public String getDetailedType(AbstractVariable var) {
        if (rawInfo == null) {
            rawInfo = debugger.requestSymbolTypeFromName(var.getFullName());
        }
        return rawInfo;
    }
    
    public Map<String, Object> getMap() {
        if (map == null) {
            map = getCachedMap();
            if (map == null) {
                map = createMap();
            }
        }
        return map;
    }
    
    private Map<String, Object> getCachedMap() {
        if (resolvedType != null) {
            Map<String, Object> m = mcache.get(resolvedType);
            if (m != null) {
                log.fine("TI.getCachedMap: Got Map for " + resolvedType); // NOI18N
            }
            return m;
        } else {
            return null;
        }
    }
    
    private Map<String, Object> createMap() {
        if (resolvedType != null) {
            Map<String, Object> m = createFieldMap();
            if (!disable_map_cache) {
                mcache.put(resolvedType, m);
            }
            return m;
        } else {
            return null;
        }
    }
    
    private Map<String, Object> createFieldMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        int pos0;
        int pos1 = rawInfo.indexOf('{');
        int pos2 = GdbUtils.findMatchingCurly(rawInfo, pos1);
        String fields = null;
        String n;
        
        if (pos1 != -1) {
            if ((pos0 = getSuperclassColon(rawInfo.substring(0, pos1))) != -1) {
                m = addSuperclassEntries(m, rawInfo.substring(pos0 + 1, pos1));
            }
            if (pos0 == -1) {
                n = rawInfo.substring(0, pos1).trim();
            } else {
                n = rawInfo.substring(0, pos0).trim();
            }
            m.put(NAME, n.startsWith("class ") ? n.substring(5).trim() : n); // NOI18N
        }
        String ri = rawInfo;
        if (pos1 == -1 && pos2 == -1) {
            if (GdbUtils.isPointer(rawInfo)) {
                ri = ri.replace('*', ' ').trim();
            }
        } else if (pos1 != -1 && pos2 != -1 && pos2 > (pos1 + 1)) {
            fields = ri.substring(pos1 + 1, pos2);
        }
        if (fields != null) {
            m = parseFields(m, shortenType(resolvedType), fields);
            if (m.isEmpty()) {
                m.put("<" + ri.substring(0, pos1) + ">", NO_DATA_FIELDS); // NOI18N
            }
        }
        return m;
    }
    
    /**
     * Find the first ":" which isn'f part of a "::".
     * @param info The string to check
     * @return The index (if found) or -1
     */
    private int getSuperclassColon(String info) {
        char lastc = 0;
        char nextc;
        char ch;
        
        for (int i = 0; i < info.length(); i++) {
            ch = info.charAt(i);
            nextc = (i + 1) < info.length() ? info.charAt(i + 1) : 0;
            if (ch == ':' && nextc != ':' && lastc != ':') {
                return i;
            } else if (ch == '<') {
                i = GdbUtils.findMatchingLtGt(info, i);
            }
            lastc = ch;
        }
        return -1;
    }
    
    private Map<String, Object>  addSuperclassEntries(Map<String, Object> m, String info) {
        char c;
        int pos;
        int start = 0;
        int scount = 1;
        
        for (int i = 0; i < info.length(); i++) {
            if (info.substring(i).startsWith("public ")) { // NOI18N
                i += 7;
                start = i;
            } else if (info.substring(i).startsWith("private ")) { // NOI18N
                i += 8;
                start = i;
            } else if (info.substring(i).startsWith("protected ")) { // NOI18N
                i += 10;
                start = i;
            }
            if (i < info.length()) {
                c = info.charAt(i);
                if (c == '<') {
                    pos = GdbUtils.findMatchingLtGt(info, i);
                    if (pos != -1) {
                        i = pos;
                    }
                } else if (c == ',') {
                    m.put(SUPER_PREFIX + scount++ + ">", info.substring(start, i).trim()); // NOI18N
                    if ((i + 1) < info.length()) {
                        info = info.substring(i + 1);
                        i = 0;
                        start = 0;
                    }
                }
            }
        }
        m.put(SUPER_PREFIX + scount++ + ">", info.substring(start).trim()); // NOI18N
        return m;
    }
        
    private Map<String, Object> parseFields(Map<String, Object> m, String name, String info) {
        if (info != null) {
            if (m.get(NAME) == null) { // NOI18N
                m.put( NAME, name); // NOI18N
            }
            int pos, pos2;
            FieldTokenizer tok = new FieldTokenizer(info);
            while (tok.hasMoreFields()) {
                String[] field = tok.nextField();
                if (field[0] != null) {
                    if (isNonAnonymousCSUDef(field)) {
                        pos = field[0].indexOf('{');
                        pos2 = field[0].lastIndexOf('}');
                        Map<String, Object> m2 = new HashMap<String, Object>();
                        m = parseFields(m2, shortenType(field[0]), field[0].substring(pos + 1, pos2).trim());
                        m.put(field[1], m2);
                    } else if (field[1].startsWith(ANONYMOUS_PREFIX) || field[0].endsWith("}")) { // NOI18N
                        // replace string def with Map
                        pos = field[0].indexOf('{');
                        String frag = field[0].substring(pos + 1, field[0].length() - 1).trim();
                        Map<String, Object> m2 = parseFields(new HashMap<String, Object>(), shortenType(field[0]), frag);
                        m.put(field[1], m2);
                    } else {
                        pos = field[1].indexOf('[');
                        if (pos == -1) {
                            m.put(field[1], field[0]);
                        } else {
                            m.put(field[1].substring(0, pos), field[0] + field[1].substring(pos));
                        }
                    }
                }
            }
        }
        return m;
    }
    
    private String shortenType(String type) {
        if (type.startsWith("class ")) { // NOI18N
            return type.charAt(6) == '{' ? "class {...}" : type.substring(6); // NOI18N
        } else if (type.startsWith("struct ")) { // NOI18N
            return type.charAt(7) == '{' ? "struct {...}" : type.substring(7); // NOI18N
        } else if (type.startsWith("union {")) { // NOI18N
            return type.charAt(6) == '{' ? "union {...}" : type.substring(6); // NOI18N
        } else {
            return type;
        }
    }
    
    /**
     * See if the info string defines an embedded class/struct/union which is <b>not</b> an
     * anonymous c/s/u (those don't get typenames).
     *
     * @param info The string to check for a non-anonymous class/struct/union definition
     * @returns True for a non-anonymous class/struct/union definition
     */
    private boolean isNonAnonymousCSUDef(String[] field) {
        String info = field[0];
        if (!field[1].startsWith(ANONYMOUS_PREFIX) && // NOI18N
                (info.startsWith("class {") || info.startsWith("struct {") || info.startsWith("union {"))) { // NOI18N
            int start = info.indexOf('{');
            int end = GdbUtils.findMatchingCurly(info, start) + 1;
            if (start != -1 && end != 0 && !info.substring(start, end).equals("{...}") && end != info.length()) { // NOI18N
                return true;
            }
        }
        return false;
    }
}
