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

package org.netbeans.modules.cnd.apt.support;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;

/**
 * utilities for working with APT states (macro-state, include-state, preproc-state)
 * @author Vladimir Voskresensky
 */
public class APTHandlersSupport {

    private APTHandlersSupport() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // factory methods for handlers
    public static APTPreprocHandler createPreprocHandler(APTMacroMap macroMap, APTIncludeHandler inclHandler, boolean compileContext, CharSequence lang, CharSequence flavor) {
        return APTHandlersSupportImpl.createPreprocHandler(macroMap, inclHandler, compileContext, lang, flavor);
    }
    
    public static APTPreprocHandler createEmptyPreprocHandler(StartEntry file, MakeConfiguration projectConfiguration) {
        return APTHandlersSupportImpl.createEmptyPreprocHandler(file, projectConfiguration);
    }

    public static void invalidatePreprocHandler(APTPreprocHandler preprocHandler) {
        APTHandlersSupportImpl.invalidatePreprocHandler(preprocHandler);
    }
 
    public static APTIncludeHandler createIncludeHandler(StartEntry startFile, List<IncludeDirEntry> sysIncludePaths, List<IncludeDirEntry> userIncludePaths, APTFileSearch fileSearch, MakeConfiguration projectConfiguration) {
        return APTHandlersSupportImpl.createIncludeHandler(startFile, sysIncludePaths, userIncludePaths, fileSearch, projectConfiguration);
    }

    public static long getCompilationUnitCRC(APTPreprocHandler preprocHandler){
        return APTHandlersSupportImpl.getCompilationUnitCRC(preprocHandler);
    }

    public static APTMacroMap createMacroMap(APTMacroMap sysMap, List<String> userMacros) {
        return APTHandlersSupportImpl.createMacroMap(sysMap, userMacros);
    }

    public static Map<CharSequence, APTMacro> extractMacroMap(APTPreprocHandler.State state){
        return APTHandlersSupportImpl.extractMacroMap(state);
    }

    public static APTBaseMacroMap.State extractMacroMapState(APTPreprocHandler.State state){
        return APTHandlersSupportImpl.extractMacroMapState(state);
    }

    public static APTIncludeHandler.State extractIncludeState(APTPreprocHandler.State state) {
        return APTHandlersSupportImpl.extractIncludeState(state);
    }
    
    public static APTPreprocHandler.StateKey getStateKey(APTPreprocHandler.State state){
        return APTHandlersSupportImpl.getStateKey(state);
    }

    public static boolean isEmptyActiveMacroMap(APTPreprocHandler.State state) {
        return APTHandlersSupportImpl.isEmptyActiveMacroMap(state);
    }

    public static int getMacroSize(APTPreprocHandler.State state) {
        return APTHandlersSupportImpl.getMacroSize(state);
    }

    public static int getIncludeStackDepth(APTPreprocHandler.State state) {
        return APTHandlersSupportImpl.getIncludeStackDepth(state);
    }
    ////////////////////////////////////////////////////////////////////////////
    // help methods for preprocessor states
//    public static APTPreprocHandler.State copyPreprocState(APTPreprocHandler.State orig) {
//        return APTHandlersSupportImpl.copyPreprocState(orig);
//    }
    
    public static APTPreprocHandler.State createCleanPreprocState(APTPreprocHandler.State orig) {
        return APTHandlersSupportImpl.createCleanPreprocState(orig);
    }
    
    public static LinkedList<APTIncludeHandler.IncludeInfo> extractIncludeStack(APTPreprocHandler.State state) {
        return APTHandlersSupportImpl.extractIncludeStack(state);
    }
    
    public static StartEntry extractStartEntry(APTPreprocHandler.State state) {
	return APTHandlersSupportImpl.extractStartEntry(state);
    }
    
    public static APTPreprocHandler.State createInvalidPreprocState(APTPreprocHandler.State orig) {
        return APTHandlersSupportImpl.createInvalidPreprocState(orig);
    }

    public static boolean equalsIgnoreInvalid(APTPreprocHandler.State state1, APTPreprocHandler.State state2) {
        return APTHandlersSupportImpl.equalsIgnoreInvalid(state1, state2);
    }
}
