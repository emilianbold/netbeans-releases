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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.TokenStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.utils.cache.TinyCharSequence;

/**
 *
 * @author gorrus
 */
public final class APTMacroMapSnapshot {
    private static final boolean USE_SIMPLIFIED_MAP = true;
    private final Map<CharSequence, APTMacro> macros;

    /*package*/ final APTMacroMapSnapshot parent;
    
    public APTMacroMapSnapshot(APTMacroMapSnapshot parent) {
        macros = createMacroMap(1);
        assert (parent == null || parent.parent == null || !parent.parent.isEmtpy()) : "how grand father could be empty " + parent;
        // optimization to prevent chaining of empty snapshots
        while (parent != null && parent.isEmtpy()) {
            parent = parent.parent;
        }
        this.parent = parent;
    }

    private Map<CharSequence, APTMacro> createMacroMap(int prefferedSize) {
        if (USE_SIMPLIFIED_MAP && prefferedSize < 2) {
            return new SimplifiedMap();
        } else {
            if (prefferedSize < 2) {
                return new HashMap<CharSequence, APTMacro>(2);
            } else {
                return new HashMap<CharSequence, APTMacro>(prefferedSize);
            }
        }
    }

    /*package*/ final void putMacro(CharSequence name, APTMacro macro) {
        macros.put(name, macro);
    }

    public final APTMacro getMacro(APTToken token) {
        return getMacro(token.getTextID());
    }
    
    /*package*/ final APTMacro getMacro(CharSequence key) {
        assert key instanceof TinyCharSequence : "string can't be here " + key;
        APTMacroMapSnapshot currentSnap = this;
        while (currentSnap != null) {
            APTMacro macro = currentSnap.macros.get(key);
            if (macro != null) {
                return macro;
            }
            currentSnap = currentSnap.parent;
        }
        return null;
    }
    
    @Override
    public String toString() {
        Map<CharSequence, APTMacro> tmpMap = new HashMap<CharSequence, APTMacro>();
        addAllMacros(this, tmpMap);
        return APTUtils.macros2String(tmpMap);
    }
    
    public static void addAllMacros(APTMacroMapSnapshot snap, Map<CharSequence, APTMacro> out) {
        if (snap != null) {
            if (snap.parent != null) {
                addAllMacros(snap.parent, out);
            }
            for (Map.Entry<CharSequence, APTMacro> cur : snap.macros.entrySet()) {
                if (cur.getValue() != UNDEFINED_MACRO) {
                    out.put(cur.getKey(), cur.getValue());
                } else {
                    out.remove(cur.getKey());
                }
            }
        }
    }    

    public static int getMacroSize(APTMacroMapSnapshot snap) {
        int size = 0;
        while (snap != null) {
            size += snap.macros.size();
            snap = snap.parent;
        }
        return size;
    }

    public boolean isEmtpy() {
        return macros.isEmpty();
    }

    ////////////////////////////////////////////////////////////////////////////
    // persistence support
    
    public void write(DataOutput output) throws IOException {
        APTSerializeUtils.writeSnapshot(this.parent, output);
        APTSerializeUtils.writeStringToMacroMap(this.macros, output);
    }
    
    public APTMacroMapSnapshot(DataInput input) throws IOException {
        this.parent = APTSerializeUtils.readSnapshot(input);
        int collSize = input.readInt();
        macros = createMacroMap(collSize);
        APTSerializeUtils.readStringToMacroMap(collSize, this.macros, input);
    }  
        
    //This is a single instance of a class to indicate that macro is undefined,
    //not a child of APTMacro to track errors more easily
    public static final APTMacro UNDEFINED_MACRO = new UndefinedMacro();
    private static final class UndefinedMacro implements APTMacro {
        @Override
        public String toString() {
            return "Macro undefined"; // NOI18N
        }

        public CharSequence getFile() {
            return CharSequenceKey.empty();
        }
        
        public Kind getKind() {
            return Kind.USER_SPECIFIED;
        }

        public boolean isFunctionLike() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public APTToken getName() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public Collection<APTToken> getParams() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public TokenStream getBody() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        public APTDefine getDefineNode() {
            throw new UnsupportedOperationException("Not supported in fake impl."); // NOI18N
        }

    }

    private final static class SimplifiedMap implements Map<CharSequence, APTMacro>{
        private Map<CharSequence, APTMacro> proxyMap;
        private CharSequence key;
        private APTMacro macro;
        private SimplifiedMap(){
        }

        public int size() {
            if (proxyMap == null) {
                if (key == null) {
                    return 0;
                }
                return 1;
            }
            return proxyMap.size();
        }

        public boolean isEmpty() {
            if (proxyMap == null) {
                if (key == null) {
                    return true;
                }
                return false;
            }
            return proxyMap.isEmpty();
        }

        public boolean containsKey(Object aKey) {
            if (proxyMap == null) {
                if (key == null) {
                    return false;
                }
                return key.equals(aKey);
            }
            return proxyMap.isEmpty();
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public APTMacro get(Object aKey) {
            if (proxyMap == null) {
                if (key == null) {
                    return null;
                }
                if (key.equals(aKey)){
                    return macro;
                }
            }
            return proxyMap.get(aKey);
        }

        public APTMacro put(CharSequence aKey, APTMacro aMacro) {
            if (proxyMap == null) {
                if (key == null) {
                    key = aKey;
                    macro = aMacro;
                    return null;
                }
                if (key.equals(aKey)){
                    APTMacro res = macro;
                    macro = aMacro;
                    return res;
                }
                proxyMap = new HashMap<CharSequence, APTMacro>();
                proxyMap.put(key, macro);
                proxyMap.put(aKey, aMacro);
                key = null;
                macro = null;
                return null;
            }
            return proxyMap.put(aKey, aMacro);
        }

        public APTMacro remove(Object aKey) {
            if (proxyMap == null) {
                if (key == null) {
                    return null;
                }
                if (key.equals(aKey)){
                    APTMacro res = macro;
                    key = null;
                    macro = null;
                    return res;
                }
                return null;
            }
            return proxyMap.remove(aKey);
        }

        public void putAll(Map<? extends CharSequence, ? extends APTMacro> t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            if (proxyMap == null) {
                if (key == null) {
                    return;
                }
                key = null;
                macro = null;
                return;
            }
            proxyMap.clear();
        }

        public Set<CharSequence> keySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection<APTMacro> values() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<Entry<CharSequence, APTMacro>> entrySet() {
            if (proxyMap == null) {
                if (key == null) {
                    return Collections.<Entry<CharSequence, APTMacro>>emptySet();
                }
                return new Set<Entry<CharSequence, APTMacro>>(){
                    public int size() {
                        return 1;
                    }
                    public boolean isEmpty() {
                        return false;
                    }
                    public boolean contains(Object o) {
                        return key.equals(o);
                    }
                    public Iterator<Entry<CharSequence, APTMacro>> iterator() {
                        return new Iterator<Entry<CharSequence, APTMacro>>(){
                            private boolean last = false;
                            public boolean hasNext() {
                                return !last;
                            }
                            public Entry<CharSequence, APTMacro> next() {
                                if (!last) {
                                    last = true;
                                    return new Entry<CharSequence, APTMacro>(){
                                        public CharSequence getKey() {
                                            return key;
                                        }
                                        public APTMacro getValue() {
                                            return macro;
                                        }
                                        public APTMacro setValue(APTMacro value) {
                                            APTMacro res = macro;
                                            macro = value;
                                            return res;
                                        }
                                    };
                                }
                                return null;
                            }
                            public void remove() {
                                throw new UnsupportedOperationException("Not supported yet.");
                            }
                        };
                    }
                    public Object[] toArray() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public <T> T[] toArray(T[] a) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public boolean add(Entry<CharSequence, APTMacro> o) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public boolean remove(Object o) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public boolean containsAll(Collection<?> c) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public boolean addAll(Collection<? extends Entry<CharSequence, APTMacro>> c) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public boolean retainAll(Collection<?> c) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public boolean removeAll(Collection<?> c) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public void clear() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
            return proxyMap.entrySet();
        }
    }
}
