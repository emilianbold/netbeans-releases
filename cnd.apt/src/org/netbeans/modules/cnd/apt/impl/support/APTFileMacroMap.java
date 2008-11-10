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

import antlr.Token;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;

/**
 * macro map is created for each translation unit and
 * it has specified system predefined map where it delegates
 * requests about macros if not found in own macro map
 * @author Vladimir Voskresensky
 */
public class APTFileMacroMap extends APTBaseMacroMap implements APTMacroMap {
    private APTMacroMap sysMacroMap;
    private Map<String,APTMacro> macroCache = new HashMap<String,APTMacro>();

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
        fill(userMacros);
    }

    public void setSysMacros(APTMacroMap sysMacroMap) {
        this.sysMacroMap = sysMacroMap;
    }

    @Override
    public APTMacro getMacro(Token token) {
        // check own map
        String macroText = token.getText();
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
            macroCache.put(macroText, res);
        }
        // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }

    @Override
    public void define(Token name, Collection<Token> params, List<Token> value) {
        if (false && sysMacroMap != null && sysMacroMap.isDefined(name)) { // disable for IZ#124635
            // TODO: report error about redefining system macros
        } else {
            super.define(name, params, value);
            macroCache.remove(name.getText());
        }
    }

    @Override
    public void undef(Token name) {
        if (false && sysMacroMap != null && sysMacroMap.isDefined(name)) { // disable for IZ#124635
            // TODO: report error about undefined system macros
        }
        super.undef(name);
        macroCache.remove(name.getText());
    }

    protected APTMacro createMacro(Token name, Collection<Token> params, List<Token> value) {
        return new APTMacroImpl(name, params, value, false);
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

    private Stack<String> expandingMacros = new Stack<String>();

    public boolean pushExpanding(Token token) {
        assert (token != null);
        if (!isExpanding(token)) {
            expandingMacros.push(token.getText());
            return true;
        }
        return false;
    }

    public void popExpanding() {
        Object curMacro = null;
        try {
            curMacro = expandingMacros.pop();
        } catch (ArrayIndexOutOfBoundsException ex) {
            assert (false) : "why pop from empty stack?"; // NOI18N
        }
//        return curMacro;
    }

    public boolean isExpanding(Token token) {
        try {
            return expandingMacros.contains(token.getText());
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
}
