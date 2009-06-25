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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.Collections;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport.StateKey;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;

/**
 * macro map is created for each translation unit and
 * it has specified system predefined map where it delegates
 * requests about macros if not found in own macro map
 * @author Vladimir Voskresensky
 */
public class APTFileMacroMap extends APTBaseMacroMap {
    private static final Map<CharSequence,APTMacro> NO_CACHE = Collections.unmodifiableMap(new HashMap<CharSequence,APTMacro>(0));
    private static final int INITIAL_CACHE_SIZE = 512;
    private APTMacroMap sysMacroMap;
    private Map<CharSequence,APTMacro> macroCache = NO_CACHE;
    private int crc1 = 0;
    private int crc2 = 0;

    public APTFileMacroMap() {
    }

    /**
     * Creates a new instance of APTFileMacroMap
     */
    public APTFileMacroMap(APTMacroMap sysMacroMap, List<String> userMacros) {
        if (sysMacroMap == null) {
            sysMacroMap = APTBaseMacroMap.EMPTY;
        }
        this.sysMacroMap = sysMacroMap;
        this.macroCache = new HashMap<CharSequence,APTMacro>(INITIAL_CACHE_SIZE);
        fill(userMacros, false);
    }

    @Override
    public APTMacro getMacro(APTToken token) {
        // check own map
        CharSequence macroText = token.getTextID();
        initCache();
        APTMacro res = macroCache.get(macroText);
        if (res == null) {
            // no need to check in super, because everything is in cache already
            if (false) {
                res = super.getMacro(token);
            }
            // then check system map
            if (res == null && sysMacroMap != null) {
                res = sysMacroMap.getMacro(token);
            }
            if (res == null) {
                res = APTMacroMapSnapshot.UNDEFINED_MACRO;
            }
            if (res.getKind() != APTMacro.Kind.POSITION_PREDEFINED) {
                // do not remember position based macro values
                macroCache.put(macroText, res);
            }
        }
        // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }

    @Override
    protected void putMacro(CharSequence name, APTMacro macro) {
        initCache();
        super.putMacro(name, macro);
        APTMacro old = macroCache.put(name, macro);
        int i1 = name.hashCode();
        int i2;
        if (old != null) {
            i2 = old.hashCode();
            crc1 -= i1 + i2;
            crc2 -= i1 ^ i2;
        }
        if (macro != APTMacroMapSnapshot.UNDEFINED_MACRO) {
            i2 = macro.hashCode();
            crc1 += i1 + i2;
            crc2 += i1 ^ i2;
        }
    }

    protected APTMacro createMacro(CharSequence file, APTDefine define, Kind macroType) {
        APTMacro macro = new APTMacroImpl(file, define, macroType);
        if (APTTraceFlags.APT_SHARE_MACROS) {
            macro = cache.getMacro(macro);
        }
        return macro;
    }

    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        return new APTMacroMapSnapshot(parent);
    }

    @Override
    public State getState() {
        //Create new snapshot instance in the tree
        changeActiveSnapshotIfNeeded();
        return new FileStateImpl(active.parent, sysMacroMap, crc1, crc2);
    }

    @Override
    public void setState(State state) {
        active = makeSnapshot(((StateImpl)state).snap);
        crc1 = 0;
        crc2 = 0;
        if (state instanceof FileStateImpl) {
            FileStateImpl fileState = (FileStateImpl) state;
            sysMacroMap = fileState.sysMacroMap;
            crc1 = fileState.crc1;
            crc2 = fileState.crc2;
        }
        macroCache = NO_CACHE;
    }

    private void initCache() {
        if (macroCache == NO_CACHE) {
            macroCache =  new HashMap<CharSequence,APTMacro>(INITIAL_CACHE_SIZE);
            // fill cache to speedup getMacro
            APTMacroMapSnapshot.addAllMacros(active, macroCache);
            if (crc1 == 0 && crc2 == 0) {
                for(Map.Entry<CharSequence, APTMacro> entry : macroCache.entrySet()){
                    int i1 = entry.getKey().hashCode();
                    int i2 = entry.getValue().hashCode();
                    crc1 += i1 + i2;
                    crc2 += i1 ^ i2;
                }
            }
        }
    }

    public StateKey getStateKey() {
        return new StateKey(crc1, crc2);
    }

    public static class FileStateImpl extends StateImpl {
        private final APTMacroMap sysMacroMap;
        private final int crc1;
        private final int crc2;

        private FileStateImpl(APTMacroMapSnapshot snap, APTMacroMap sysMacroMap, int crc1, int crc2) {
            super(snap);
            this.sysMacroMap = sysMacroMap;
            this.crc1 = crc1;
            this.crc2 = crc2;
        }

        private FileStateImpl(FileStateImpl state, boolean cleanedState) {
            super(state, cleanedState);
            this.sysMacroMap = state.sysMacroMap;
            this.crc1 = state.crc1;
            this.crc2 = state.crc2;
        }

        StateKey getStateKey() {
            return new StateKey(crc1, crc2);
        }

        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("FileState\n"); // NOI18N
            retValue.append("Snapshot\n"); // NOI18N
            retValue.append(super.toString());
            retValue.append("\nSystem MacroMap\n"); // NOI18N
            retValue.append(sysMacroMap);
            return retValue.toString();
        }

        ////////////////////////////////////////////////////////////////////////
        // persistence support

        @Override
        public void write(DataOutput output) throws IOException {
            super.write(output);
            output.writeInt(crc1);
            output.writeInt(crc2);
            APTSerializeUtils.writeSystemMacroMap(this.sysMacroMap, output);
        }

        public FileStateImpl(final DataInput input) throws IOException {
            super(input);
            this.crc1 = input.readInt();
            this.crc2 = input.readInt();
            APTMacroMap systemMap = APTSerializeUtils.readSystemMacroMap(input);
            if (systemMap == null) {
                this.sysMacroMap = APTBaseMacroMap.EMPTY;
            } else {
                this.sysMacroMap = systemMap;
            }
        }

        @Override
        public StateImpl copyCleaned() {
            return new FileStateImpl(this, true);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // manage macro expanding stack

    private LinkedList<CharSequence> expandingMacros = new LinkedList<CharSequence>();

    public boolean pushExpanding(APTToken token) {
        assert (token != null);
        if (!isExpanding(token)) {
            expandingMacros.addLast(token.getTextID());
            return true;
        }
        return false;
    }

    public void popExpanding() {
        try {
            expandingMacros.removeLast();
        } catch (ArrayIndexOutOfBoundsException ex) {
            assert (false) : "why pop from empty stack?"; // NOI18N
        }
    }

    public boolean isExpanding(APTToken token) {
        try {
            return expandingMacros.contains(token.getTextID());
        } catch (ArrayIndexOutOfBoundsException ex) {
            assert (false) : "why ask empty stack?"; // NOI18N
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    // implementation details
    /*public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        boolean retValue = false;
        if (obj instanceof APTFileMacroMap) {
            retValue = super.equals(obj);
            if (retValue) {
                // use '==' as we share system maps
                retValue = (this.sysMacroMap == ((APTFileMacroMap)obj).sysMacroMap);
            }
        }
        return retValue;
    }*/

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("Own Map:\n"); // NOI18N
        retValue.append(super.toString());
        retValue.append("System Map:\n"); // NOI18N
        retValue.append(sysMacroMap);
        return retValue.toString();
    }

    private static final APTMacroCache cache = APTMacroCache.getManager();
}
