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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTMacroUtils;

/**
 * utilities for working with APT states (macro-state, include-state, preproc-state)
 * @author Vladimir Voskresensky
 */
public class APTHandlersSupportImpl {
    
    /** Creates a new instance of APTHandlersSupportImpl */
    private APTHandlersSupportImpl() {
    }

    public static APTPreprocHandler createPreprocHandler(APTMacroMap macroMap, APTIncludeHandler inclHandler, boolean stateCorrect) {
        return new APTPreprocHandlerImpl(macroMap, inclHandler, stateCorrect);
    }

    public static APTPreprocHandler createEmptyPreprocHandler(StartEntry file) {
        return new APTPreprocHandlerImpl(new APTFileMacroMap(), new APTIncludeHandlerImpl(file), false);
    }

    public static APTIncludeHandler createIncludeHandler(StartEntry startFile, List<String> sysIncludePaths, List<String> userIncludePaths) {
        return new APTIncludeHandlerImpl(startFile, sysIncludePaths, userIncludePaths);
    }

    public static APTMacroMap createMacroMap(APTMacroMap sysMap, List<String> userMacros) {
        APTMacroMap fileMap = new APTFileMacroMap(sysMap);
        APTMacroUtils.fillMacroMap(sysMap, userMacros);
        return fileMap;
    }

    public static APTPreprocHandler.State createCleanPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copyCleaned();
    }
    
    public static APTPreprocHandler.State createInvalidPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copyInvalid();
    }
    
    public static List<APTIncludeHandler.IncludeInfo> extractIncludeStack(APTPreprocHandler.State state) {
        assert state != null;
        List<APTIncludeHandler.IncludeInfo> inclStack = ((APTPreprocHandlerImpl.StateImpl)state).getIncludeStack();
        return inclStack == null ? Collections.<APTIncludeHandler.IncludeInfo>emptyList() : 
            new ArrayList<APTIncludeHandler.IncludeInfo>(inclStack);
    }

    public static APTPreprocHandler.State copyPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copy();
    }
    
    public static List<String> extractSystemIncludePaths(APTPreprocHandler.State state) {
        assert state != null;
        List<String> sysPaths = ((APTPreprocHandlerImpl.StateImpl)state).getSysIncludePaths();
        return sysPaths;
    }
    
    public static List<String> extractUserIncludePaths(APTPreprocHandler.State state) {
        assert state != null;
        List<String> usrPaths = ((APTPreprocHandlerImpl.StateImpl)state).getUserIncludePaths();
        return usrPaths;
    } 
    
    /*package*/ static APTIncludeHandler.State copyIncludeState(APTIncludeHandler.State inclState, boolean cleanState) {
        return inclState == null ? null : ((APTIncludeHandlerImpl.StateImpl)inclState).copy(cleanState);
    }
    
    /*package*/ static List<APTIncludeHandler.IncludeInfo> getIncludeStack(APTIncludeHandler.State inclState) {
        return inclState == null ? null : ((APTIncludeHandlerImpl.StateImpl)inclState).getIncludeStack();
    }    

    /*package*/ static APTMacroMap.State createCleanMacroState(APTMacroMap.State macroState) {
        APTMacroMap.State out = null;
        if (macroState != null) {
            out = ((APTBaseMacroMap.StateImpl)macroState).copyCleaned();
        }
        return out;
    }

    /*package*/ static boolean isCleanedIncludeState(APTIncludeHandler.State inclState) {
        assert inclState != null;
        return ((APTIncludeHandlerImpl.StateImpl)inclState).isCleaned();
    }     

    /*package*/ static List<String> extractSystemIncludePaths(APTIncludeHandler.State inclState) {
        assert inclState != null;
        return ((APTIncludeHandlerImpl.StateImpl)inclState).getSysIncludePaths();
    }  
    
    /*package*/ static List<String> extractUserIncludePaths(APTIncludeHandler.State inclState) {
        assert inclState != null;
        return ((APTIncludeHandlerImpl.StateImpl)inclState).getUserIncludePaths();
    }        
}
