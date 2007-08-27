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

package org.netbeans.modules.cnd.apt.support;

import java.util.List;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;

/**
 * utilities for working with APT states (macro-state, include-state, preproc-state)
 * @author Vladimir Voskresensky
 */
public class APTHandlersSupport {
 
    private APTHandlersSupport() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // factory methods for handlers
    public static APTPreprocHandler createPreprocHandler(APTMacroMap macroMap, APTIncludeHandler inclHandler, boolean stateCorrect) {
        return APTHandlersSupportImpl.createPreprocHandler(macroMap, inclHandler, stateCorrect);
    }
    
    public static APTPreprocHandler createEmptyPreprocHandler(StartEntry file) {
        return APTHandlersSupportImpl.createEmptyPreprocHandler(file);
    }

    public static APTIncludeHandler createIncludeHandler(StartEntry startFile, List<String> sysIncludePaths, List<String> userIncludePaths) {
        return APTHandlersSupportImpl.createIncludeHandler(startFile, sysIncludePaths, userIncludePaths);
    }
    
    public static APTMacroMap createMacroMap(APTMacroMap sysMap, List<String> userMacros) {
        return APTHandlersSupportImpl.createMacroMap(sysMap, userMacros);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // help methods for preprocessor states
    public static APTPreprocHandler.State copyPreprocState(APTPreprocHandler.State orig) {
        return APTHandlersSupportImpl.copyPreprocState(orig);
    }
    
    public static APTPreprocHandler.State createCleanPreprocState(APTPreprocHandler.State orig) {
        return APTHandlersSupportImpl.createCleanPreprocState(orig);
    }
    
    public static List<APTIncludeHandler.IncludeInfo> extractIncludeStack(APTPreprocHandler.State state) {
        return APTHandlersSupportImpl.extractIncludeStack(state);
    }
    
    public static APTPreprocHandler.State createInvalidPreprocState(APTPreprocHandler.State orig) {
        return APTHandlersSupportImpl.createInvalidPreprocState(orig);
    }


}
