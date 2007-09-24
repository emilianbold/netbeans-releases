/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
