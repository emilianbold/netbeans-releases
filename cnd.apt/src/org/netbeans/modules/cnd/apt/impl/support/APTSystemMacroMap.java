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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTSystemMacroMap extends APTBaseMacroMap {    
    private APTMacroMap preMacroMap;
    
    /** Creates a new instance of APTSystemMacroMap */
    public APTSystemMacroMap() {           
        preMacroMap = new APTPredefinedMacroMap();
    }
    
    public APTSystemMacroMap(APTMacroMap preMacroMap) {
         this.preMacroMap = preMacroMap;
    }
    
    protected APTMacro createMacro(Token name, Collection params, List/*<Token>*/ value) {
        return new APTMacroImpl(name, params, value, true);
    }
    
    public boolean pushExpanding(Token token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }

    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException());// NOI18N
//        return null;
    }

    public boolean isExpanding(Token token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }  
    
    public APTMacro getMacro(Token token) {
        APTMacro res = super.getMacro(token);
        
        if(res == null) {
            res = preMacroMap.getMacro(token);
        }
        return res;        
    }
    
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        assert parent == null : "parent must be null";
        return new APTMacroMapSnapshot(parent);
    }
}
