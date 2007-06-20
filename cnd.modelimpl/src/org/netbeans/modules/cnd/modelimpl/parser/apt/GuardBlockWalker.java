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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import antlr.Token;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTIfndef;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;

/**
 *
 * @author Alexander Simon
 */
public class GuardBlockWalker extends APTWalker {
    
    private APTIfndef guardCheck;
    private Boolean hasGuard = null;
            
            /** Creates a new instance of GuardBlockWalker */
    public GuardBlockWalker(APTFile apt, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler == null ? null: preprocHandler.getMacroMap());
    }

    public Token getGuard(){
        if (hasGuard == Boolean.TRUE && guardCheck != null){
            return  guardCheck.getMacroName();
        }
        return null;
    }
    
    protected void onDefine(APT apt) {
        hasGuard = Boolean.FALSE;
    }
    
    protected void onUndef(APT apt) {
        hasGuard = Boolean.FALSE;
    }
    
    protected boolean onIf(APT apt) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    protected boolean onIfdef(APT apt) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    protected boolean onIfndef(APT apt) {
        guardCheck = (APTIfndef)apt;
        hasGuard = (hasGuard == null) ? Boolean.TRUE : Boolean.FALSE;
        return false;
    }
    
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    protected void onEndif(APT apt, boolean wasInBranch) {
        hasGuard = (hasGuard == Boolean.TRUE) ? Boolean.TRUE : Boolean.FALSE;
    }
    
    protected void onInclude(APT apt) {
        hasGuard = Boolean.FALSE;
    }
    
    protected void onIncludeNext(APT apt) {
        hasGuard = Boolean.FALSE;
    }

    protected void onOtherNode(APT apt) {
        hasGuard = Boolean.FALSE;
    }

    public void clearGuard() {
        hasGuard = Boolean.FALSE;
    }
    
}
