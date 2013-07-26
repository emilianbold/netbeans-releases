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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseMacroMap.StateImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap.FileStateImpl;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.CharSequences;

/**
 * utilities for working with APT states (macro-state, include-state, preproc-state)
 * @author Vladimir Voskresensky
 */
public class APTHandlersSupportImpl {

    /** Prevents creation of an instance of APTHandlersSupportImpl */
    private APTHandlersSupportImpl() {
    }

    public static APTPreprocHandler createPreprocHandler(APTMacroMap macroMap, APTIncludeHandler inclHandler, boolean compileContext, CharSequence lang, CharSequence flavor) {
        return new APTPreprocHandlerImpl(macroMap, inclHandler, compileContext, lang, flavor);
    }

    public static APTPreprocHandler createEmptyPreprocHandler(StartEntry file, MakeConfiguration projectConfiguration) {
        return new APTPreprocHandlerImpl(new APTFileMacroMap(), new APTIncludeHandlerImpl(file, projectConfiguration), false, CharSequences.empty(), CharSequences.empty());
    }

    public static void invalidatePreprocHandler(APTPreprocHandler preprocHandler) {
        ((APTPreprocHandlerImpl)preprocHandler).setValid(false);
    }

    public static APTIncludeHandler createIncludeHandler(StartEntry startFile, List<IncludeDirEntry> sysIncludePaths, List<IncludeDirEntry> userIncludePaths, APTFileSearch fileSearch, MakeConfiguration projectConfiguration) {
        // user paths could contain "-include file" elements
        List<IncludeDirEntry> fileEntries = new ArrayList<IncludeDirEntry>(0);
        SupportAPIAccessor accessor = SupportAPIAccessor.get();
        for (IncludeDirEntry includeDirEntry : userIncludePaths) {
            if (!accessor.isExistingDirectory(includeDirEntry)) {
                // check if this is file
                if (CndFileUtils.isExistingFile(includeDirEntry.getFileSystem(), includeDirEntry.getAsSharedCharSequence().toString())) {
                    fileEntries.add(includeDirEntry);
                }
            }
        }
        return new APTIncludeHandlerImpl(startFile, sysIncludePaths, userIncludePaths, fileEntries, fileSearch, projectConfiguration);
    }
    
    public static long getCompilationUnitCRC(APTPreprocHandler preprocHandler){
        if (preprocHandler instanceof APTPreprocHandlerImpl) {
            return ((APTPreprocHandlerImpl)preprocHandler).getCompilationUnitCRC();
        }
        return 0;
    }

    public static APTMacroMap createMacroMap(APTMacroMap sysMap, List<String> userMacros) {
        APTMacroMap fileMap = new APTFileMacroMap(sysMap, userMacros);
        return fileMap;
    }

    public static APTPreprocHandler.State createCleanPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copyCleaned();
    }
    
    public static APTPreprocHandler.State createInvalidPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copyInvalid();
    }

    public static boolean equalsIgnoreInvalid(State state1, State state2) {
        if (state1 instanceof APTPreprocHandlerImpl.StateImpl) {
            return ((APTPreprocHandlerImpl.StateImpl) state1).equalsIgnoreInvalidFlag(state2);
        } else if (state2 instanceof APTPreprocHandlerImpl.StateImpl) {
            return ((APTPreprocHandlerImpl.StateImpl) state2).equalsIgnoreInvalidFlag(state1);
        } else {
            return state1.equals(state2);
        }
    }

    public static boolean isFirstLevel(APTIncludeHandler includeHandler) {
        if (includeHandler != null) {
            return ((APTIncludeHandlerImpl) includeHandler).isFirstLevel();
        } else {
            return false;
        }
    }

    public static Collection<IncludeDirEntry> extractIncludeFileEntries(APTIncludeHandler includeHandler) {
        Collection<IncludeDirEntry> out = new ArrayList<IncludeDirEntry>(0);
        if (includeHandler != null) {
            return ((APTIncludeHandlerImpl) includeHandler).getUserIncludeFilePaths();
        }
        return out;
    }

    public static Map<CharSequence, APTMacro> extractMacroMap(APTPreprocHandler.State state){
        assert state != null;
        APTBaseMacroMap.StateImpl macro = (StateImpl) ((APTPreprocHandlerImpl.StateImpl)state).macroState;
        return macro.snap.getAll();
    }

    public static APTBaseMacroMap.State extractMacroMapState(APTPreprocHandler.State state){
        assert state != null;
        return (StateImpl) ((APTPreprocHandlerImpl.StateImpl)state).macroState;
    }

    public static long getCompilationUnitCRC(APTMacroMap map){
        assert map != null;
        if (map instanceof APTFileMacroMap) {
            return ((APTFileMacroMap)map).getCompilationUnitCRC();
        }
        return 0;
    }

    public static APTIncludeHandler.State extractIncludeState(APTPreprocHandler.State state) {
        assert state != null;
        return ((APTPreprocHandlerImpl.StateImpl) state).inclState;
    }
    
    public static StateKeyImpl getStateKey(APTPreprocHandler.State state){
        assert state != null;
        APTFileMacroMap.FileStateImpl macro = (FileStateImpl) ((APTPreprocHandlerImpl.StateImpl)state).macroState;
        StartEntry extractStartEntry = extractStartEntry(((APTPreprocHandlerImpl.StateImpl)state).inclState);
        return macro.getStateKey(extractStartEntry == null ? null : extractStartEntry.getStartFileProject());
    }

    public static boolean isEmptyActiveMacroMap(APTPreprocHandler.State state) {
        assert state != null;
        APTFileMacroMap.FileStateImpl macro = (FileStateImpl) ((APTPreprocHandlerImpl.StateImpl) state).macroState;
        return macro.isEmptyActiveMacroMap();
    }

    public static int getMacroSize(APTPreprocHandler.State state) {
        assert state != null;
        APTBaseMacroMap.StateImpl macro = (StateImpl) ((APTPreprocHandlerImpl.StateImpl)state).macroState;
        return macro.snap.getAll().size();
    }

    public static int getIncludeStackDepth(APTPreprocHandler.State state) {
        assert state != null;
        APTIncludeHandlerImpl.StateImpl incl = (APTIncludeHandlerImpl.StateImpl) ((APTPreprocHandlerImpl.StateImpl) state).inclState;
        return incl == null ? 0 : incl.getIncludeStackDepth();
    }

    public static LinkedList<APTIncludeHandler.IncludeInfo> extractIncludeStack(APTPreprocHandler.State state) {
        assert state != null;
        Collection<APTIncludeHandler.IncludeInfo> inclStack = getIncludeStack(((APTPreprocHandlerImpl.StateImpl)state).inclState);
        // return copy to prevent modification of frozen state objects
        return inclStack == null ? new LinkedList<APTIncludeHandler.IncludeInfo>() : new LinkedList<APTIncludeHandler.IncludeInfo>(inclStack);
    }

    public static StartEntry extractStartEntry(APTPreprocHandler.State state) {
        return (state == null) ? null : extractStartEntry(((APTPreprocHandlerImpl.StateImpl)state).inclState);
    }
    
//    public static APTPreprocHandler.State copyPreprocState(APTPreprocHandler.State orig) {
//        return ((APTPreprocHandlerImpl.StateImpl)orig).copy();
//    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    private static StartEntry extractStartEntry(APTIncludeHandler.State state) {
	return (state == null) ? null : ((APTIncludeHandlerImpl.StateImpl) state).getStartEntry();
    }
    
    private static Collection<APTIncludeHandler.IncludeInfo> getIncludeStack(APTIncludeHandler.State inclState) {
        return inclState == null ? null : ((APTIncludeHandlerImpl.StateImpl)inclState).getIncludeStack();
    }
    
    /*package*/ static APTIncludeHandler.State copyIncludeState(APTIncludeHandler.State inclState, boolean cleanState) {
        return inclState == null ? null : ((APTIncludeHandlerImpl.StateImpl)inclState).copy(cleanState);
    }

    /*package*/ static APTMacroMap.State createCleanMacroState(APTMacroMap.State macroState) {
        APTMacroMap.State out = null;
        if (macroState != null) {
            out = ((APTBaseMacroMap.StateImpl)macroState).copyCleaned();
        }
        return out;
    }

    public static final class StateKeyImpl implements APTPreprocHandler.StateKey {

        private final int crc1, crc2;
        private final Key startProjectKey;
        private final int hashCode;

        public StateKeyImpl(int crc1, int crc2, Key startProjectKey) {
            this.crc1 = crc1;
            this.crc2 = crc2;
            this.startProjectKey = startProjectKey;
            int hash = startProjectKey == null ? -1 : startProjectKey.hashCode();
            this.hashCode = crc1 ^ crc2 ^ hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StateKeyImpl)) {
                return false;
            }
            final StateKeyImpl other = (StateKeyImpl) obj;
            if (this.hashCode != other.hashCode) {
                return false;
            }
            if (this.crc1 != other.crc1) {
                return false;
            }
            if (this.crc2 != other.crc2) {
                return false;
            }
            if (this.startProjectKey != other.startProjectKey && (this.startProjectKey == null || !this.startProjectKey.equals(other.startProjectKey))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "<" + crc1 + "," + crc2 + ">" + "from " + startProjectKey; // NOI18N
        }
    }

}
