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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APTFile;
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
    private APTMacroMap sysMacroMap;
    private Map<CharSequence,APTMacro> macroCache = new HashMap<CharSequence,APTMacro>();

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
        fill(userMacros, false);
    }

    public void setSysMacros(APTMacroMap sysMacroMap) {
        this.sysMacroMap = sysMacroMap;
    }

    @Override
    public APTMacro getMacro(APTToken token) {
        // check own map
        CharSequence macroText = token.getTextID();
        APTMacro res = macroCache.get(macroText);
        if (res == null) {
            res = super.getMacro(token);
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
    public void define(APTFile file, APTToken name, Collection<APTToken> params, List<APTToken> value, Kind macroType) {
        if (false && sysMacroMap != null && sysMacroMap.isDefined(name)) { // disable for IZ#124635
            // TODO: report error about redefining system macros
        } else {
            super.define(file, name, params, value, Kind.DEFINED);
            macroCache.remove(name.getTextID());
        }
    }

    @Override
    public void undef(APTFile file, APTToken name) {
        if (false && sysMacroMap != null && sysMacroMap.isDefined(name)) { // disable for IZ#124635
            // TODO: report warning about undefined system macros
        }
        super.undef(file, name);
        macroCache.remove(name.getTextID());
    }

    protected APTMacro createMacro(CharSequence file, APTToken name, Collection<APTToken> params, List<APTToken> value, Kind macroType) {
        APTMacro macro = new APTMacroImpl(file, name, params, value, macroType);
        APTMacro prev = null;
        if (APTTraceFlags.APT_SHARE_MACROS) {
            ConcurrentMap<APTMacro, APTMacro> sharedMap = getSharedMap();
            prev = sharedMap.get(macro);
            if (prev == null) {
                prev = sharedMap.putIfAbsent(macro, macro);
                if (TRACE_HITS && prev != null) {
                    cacheCollisionsHits++;
                }
            }
            if (TRACE_HITS && prev != null) {
                cacheHits++;
                traceHits(sharedMap.size());
            }
        }
        return prev != null ? prev : macro;
    }

    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        return new APTMacroMapSnapshot(parent);
    }

    @Override
    public State getState() {
        //Create new snapshot instance in the tree
        changeActiveSnapshotIfNeeded();
        return new FileStateImpl(active.parent, sysMacroMap);
    }

    @Override
    public void setState(State state) {
        active = makeSnapshot(((StateImpl)state).snap);
        if (state instanceof FileStateImpl) {
            sysMacroMap = ((FileStateImpl)state).sysMacroMap;
        }
        macroCache.clear();
    }

    public static class FileStateImpl extends StateImpl {
        public final APTMacroMap sysMacroMap;

        public FileStateImpl(APTMacroMapSnapshot snap, APTMacroMap sysMacroMap) {
            super(snap);
            this.sysMacroMap = sysMacroMap;
        }

        private FileStateImpl(FileStateImpl state, boolean cleanedState) {
            super(state, cleanedState);
            this.sysMacroMap = state.sysMacroMap;
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
            APTSerializeUtils.writeSystemMacroMap(this.sysMacroMap, output);
        }

        public FileStateImpl(final DataInput input) throws IOException {
            super(input);

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

    private Stack<CharSequence> expandingMacros = new Stack<CharSequence>();

    public boolean pushExpanding(APTToken token) {
        assert (token != null);
        if (!isExpanding(token)) {
            expandingMacros.push(token.getTextID());
            return true;
        }
        return false;
    }

    public void popExpanding() {
        try {
            expandingMacros.pop();
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

    private static ConcurrentMap<APTMacro, APTMacro> getSharedMap() {
        ConcurrentMap<APTMacro, APTMacro> map = mapRef.get();
        if (map == null) {
            try {
                maRefLock.lock();
                map = mapRef.get();
                if (map == null) {
                    cacheHits = 0;
                    cacheCollisionsHits = 0;
                    map = new ConcurrentHashMap<APTMacro, APTMacro>();
                    mapRef = new SoftReference<ConcurrentMap<APTMacro, APTMacro>>(map);
                }
            } finally {
                maRefLock.unlock();
            }
        }
        return map;
    }

    private static void traceHits(int size) {
        if (cacheHits % 5000 == 0) {
            System.err.printf("%s hits with %s collisions, map size %s\n", cacheHits, cacheCollisionsHits, size);
        }
    }
    private static final Lock maRefLock = new ReentrantLock();
    private static Reference<ConcurrentMap<APTMacro, APTMacro>> mapRef = new SoftReference<ConcurrentMap<APTMacro, APTMacro>>(new ConcurrentHashMap<APTMacro, APTMacro>());
    private static volatile long cacheHits = 0; // we can unsync a little, but it's fine
    private static volatile long cacheCollisionsHits = 0; // we can unsync a little, but it's fine
    private static final boolean TRACE_HITS = false;
}
