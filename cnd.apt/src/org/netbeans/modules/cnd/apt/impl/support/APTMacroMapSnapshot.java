/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.TinyMaps;
import org.openide.util.CharSequences;

/**
 *
 * @author gorrus
 * @author Vladimir Voskresensky
 */
public final class APTMacroMapSnapshot {
    private static final Map<CharSequence, APTMacro> NO_MACROS = Collections.unmodifiableMap(new HashMap<CharSequence, APTMacro>(0));
    /**
     * optimize by memory
     * one of:
     * 1)APTMacro when only one macro is defined
     * 2)Map<CharSequence, APTMacro> - map of macros
     * 3)CharSequence for alone UNDEFINED_MACRO with specified name
     */
    private Object macros;

    /*package*/ final APTMacroMapSnapshot parent;
    
    public APTMacroMapSnapshot(APTMacroMapSnapshot parent) {
        macros = createMacroMap(0);
        assert (parent == null || parent.parent == null || !parent.parent.isEmtpy()) : "how grand father could be empty " + parent;
        // optimization to prevent chaining of empty snapshots
        while (parent != null && parent.isEmtpy()) {
            parent = parent.parent;
        }
        this.parent = parent;
    }

    private Map<CharSequence, APTMacro> createMacroMap(int prefferedSize) {
        if (prefferedSize == 0) {
            return NO_MACROS;
        }
        return TinyMaps.createMap(prefferedSize);
    }

    private void prepareMacroMapToAddMacro(CharSequence name, APTMacro macro) {
        if (macros == NO_MACROS) {
            return;
        }
        if (macros instanceof Map<?,?>) {
            // expand map if needed based on expected next key
            macros = TinyMaps.expandForNextKey((Map<CharSequence, APTMacro>)macros, name);
        } else {
            CharSequence key;
            APTMacro value;
            if (macros instanceof APTMacro) {
                value = (APTMacro) macros;
                key = value.getName().getTextID();
            } else {
                assert macros instanceof CharSequence;
                value = UNDEFINED_MACRO;
                key = (CharSequence) macros;
            }
            if (key.equals(name)) {
                // clean to let putMacro do the job
                macros = NO_MACROS;
            } else {
                // create LW map and remember previous value in map
                macros = createMacroMap(2);
                ((Map<CharSequence, APTMacro>)macros).put(key, value);
            }
        }
    }

    /*package*/ final void putMacro(CharSequence name, APTMacro macro) {
        prepareMacroMapToAddMacro(name, macro);
        if (macros == NO_MACROS) {
            if (macro == UNDEFINED_MACRO) {
                macros = name;
            } else {
                assert macro.getName().getTextID().equals(name);
                macros = macro;
            }
        } else {
            ((Map<CharSequence, APTMacro>)macros).put(name, macro);
        }
    }

    public final APTMacro getMacro(APTToken token) {
        return getMacro(token.getTextID());
    }
    
    /*package*/ final APTMacro getMacro(CharSequence key) {
        assert CharSequences.isCompact(key) : "string can't be here " + key;
        APTMacroMapSnapshot currentSnap = this;
        while (currentSnap != null) {
            APTMacro macro = currentSnap.getMacroImpl(key);
            if (macro != null) {
                return macro;
            }
            currentSnap = currentSnap.parent;
        }
        return null;
    }
    
    private APTMacro getMacroImpl(CharSequence key) {
        if (macros == NO_MACROS) {
            return null;
        } else if (macros.equals(key)) {
            return UNDEFINED_MACRO;
        } else if (macros instanceof APTMacro) {
            assert macros != UNDEFINED_MACRO;
            if (((APTMacro)macros).getName().equals(key)) {
                return (APTMacro)macros;
            }
            return null;
        } else {
            assert macros instanceof Map<?,?>;
            return ((Map<CharSequence, APTMacro>)macros).get(key);
        }
    }
    
    @Override
    public String toString() {
        Map<CharSequence, APTMacro> tmpMap = addAllMacros(this, null);
        return APTUtils.macros2String(tmpMap);
    }
    
    public static Map<CharSequence, APTMacro> addAllMacros(APTMacroMapSnapshot snap, Map<CharSequence, APTMacro> out) {
        if (snap != null) {
            int i = 0;
            LinkedList<APTMacroMapSnapshot> stack = new LinkedList<APTMacroMapSnapshot>();
            while(snap != null) {
                i += snap.size();
                stack.add(snap);
                snap = snap.parent;
            }
            if (out == null) {
                out = new HashMap<CharSequence, APTMacro>(i);
            }
            while(!stack.isEmpty()) {
                snap = stack.removeLast();
                if (snap.macros instanceof Map<?,?>) {
                    for (Map.Entry<CharSequence, APTMacro> cur : ((Map<CharSequence, APTMacro>)snap.macros).entrySet()) {
                        if (cur.getValue() != UNDEFINED_MACRO) {
                            out.put(cur.getKey(), cur.getValue());
                        } else {
                            out.remove(cur.getKey());
                        }
                    }
                } else if (snap.macros instanceof APTMacro) {
                    assert snap.macros != UNDEFINED_MACRO;
                    APTMacro m = (APTMacro) snap.macros;
                    out.put(m.getName().getTextID(), m);
                } else {
                    // this is undefined name
                    assert snap.macros instanceof CharSequence;
                    out.remove((CharSequence)snap.macros);
                }
            }
        }
        if (out == null) {
            out = new HashMap<CharSequence, APTMacro>();
        }
        return out;
    }

    public static int getMacroSize(APTMacroMapSnapshot snap) {
        int size = 0;
        while (snap != null) {
            size += snap.size();
            snap = snap.parent;
        }
        return size;
    }

    public boolean isEmtpy() {
        return size() == 0;
    }

    private int size() {
        if (macros == NO_MACROS) {
            return 0;
        } else if (macros instanceof Map<?, ?>) {
            return ((Map<?,?>)macros).size();
        } else {
            return 1;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // persistence support
    
    public void write(RepositoryDataOutput output) throws IOException {
        APTSerializeUtils.writeSnapshot(this.parent, output);
        if (this.macros instanceof Map<?, ?>) {
            output.writeInt(size());
            APTSerializeUtils.writeStringToMacroMap((Map<CharSequence, APTMacro>)this.macros, output);
        } else if (this.macros instanceof CharSequence) {
            output.writeInt(-1);
            output.writeCharSequenceUTF((CharSequence)this.macros);
        } else {
            assert this.macros instanceof APTMacro : "unexpected object " + this.macros;
            output.writeInt(-2);
            APTSerializeUtils.writeMacro((APTMacro)this.macros, output);
        }
    }
    
    public APTMacroMapSnapshot(RepositoryDataInput input) throws IOException {
        this.parent = APTSerializeUtils.readSnapshot(input);
        int collSize = input.readInt();
        if (collSize == -2) {
            this.macros = APTSerializeUtils.readMacro(input);
        } else if (collSize == -1) {
            this.macros = CharSequences.create(input.readCharSequenceUTF());
        } else {
            macros = createMacroMap(collSize);
            APTSerializeUtils.readStringToMacroMap(collSize, (Map<CharSequence, APTMacro>)this.macros, input);
        }
    }  
        
    //This is a single instance of a class to indicate that macro is undefined,
    //not a child of APTMacro to track errors more easily
    public static final APTMacro UNDEFINED_MACRO = new UndefinedMacro();

    private static final class UndefinedMacro implements APTMacro {
        @Override
        public String toString() {
            return "Macro undefined"; // NOI18N
        }

        @Override
        public CharSequence getFile() {
            return CharSequences.empty();
        }
        
        @Override
        public Kind getKind() {
            return Kind.USER_SPECIFIED;
        }

        @Override
        public boolean isFunctionLike() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public APTToken getName() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public Collection<APTToken> getParams() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public TokenStream getBody() {
            throw new UnsupportedOperationException("Not supported in fake impl"); // NOI18N
        }

        @Override
        public APTDefine getDefineNode() {
            throw new UnsupportedOperationException("Not supported in fake impl."); // NOI18N
        }

    }
}
