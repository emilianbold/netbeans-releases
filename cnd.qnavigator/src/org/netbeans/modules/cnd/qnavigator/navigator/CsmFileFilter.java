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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Alexander Simon
 */
public class CsmFileFilter {
    
    public CsmFileFilter(){
        Preferences ps = NbPreferences.forModule(CsmFileFilter.class);
        showForwardFunctionDeclarations = ps.getBoolean("ShowForwardFunctionDeclarations", showForwardFunctionDeclarations); // NOI18N
    }

    public boolean isApplicable(CsmOffsetable object){
       if (!isShowForwardFunctionDeclarations() &&CsmKindUtilities.isFunctionDeclaration((CsmObject) object)) {
            CsmFunctionDefinition def = ((CsmFunction) object).getDefinition();
            if (def != null && !def.equals(object) && !CsmKindUtilities.isMethod(def)) {
                return !object.getContainingFile().equals(def.getContainingFile());
            }
        }
        return true;
    }
    public boolean isApplicableInclude(){
        return isShowInclude();
    }
    public boolean isApplicableMacro(){
        return isShowMacro();
    }
    
    public boolean isShowInclude() {
        return showInclude;
    }

    public void setShowInclude(boolean showInclude) {
        this.showInclude = showInclude;
    }

    public boolean isShowMacro() {
        return showMacro;
    }

    public void setShowMacro(boolean showMacro) {
        this.showMacro = showMacro;
    }

    public boolean isShowForwardFunctionDeclarations() {
        return showForwardFunctionDeclarations;
    }

    public void setShowForwardFunctionDeclarations(boolean showForwardFunctionDeclarations) {
        this.showForwardFunctionDeclarations = showForwardFunctionDeclarations;
        Preferences ps = NbPreferences.forModule(CsmFileFilter.class);
        ps.putBoolean("ShowForwardFunctionDeclarations", showForwardFunctionDeclarations); // NOI18N
    }

    private boolean showInclude = true;
    private boolean showMacro = true;
    private boolean showForwardFunctionDeclarations = false;
}
