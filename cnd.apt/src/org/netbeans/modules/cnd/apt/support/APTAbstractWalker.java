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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.support;

import antlr.Token;
import org.netbeans.modules.cnd.apt.structure.APT;

/**
 * abstract Tree walker for APT
 * @author Vladimir Voskresensky
 */
public abstract class APTAbstractWalker extends APTWalker {
    
    /** Creates a new instance of APTAbstractWalker */
    public APTAbstractWalker(APT apt, APTMacroMap macros) {
        super(apt, macros);
    }

    protected void onInclude(APT apt) {
    }

    protected void onIncludeNext(APT apt) {
    }

    protected void onDefine(APT apt) {
    }

    protected void onUndef(APT apt) {
    }

    protected boolean onIf(APT apt) {
        return true;
    }

    protected boolean onIfdef(APT apt) {
        return true;
    }

    protected boolean onIfndef(APT apt) {
        return true;
    }

    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        return true;
    }

    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        return true;
    }

    protected void onEndif(APT apt, boolean wasInBranch) {
    }

//    protected Token onToken(Token token) {
//        return token;
//    }
//    
}
