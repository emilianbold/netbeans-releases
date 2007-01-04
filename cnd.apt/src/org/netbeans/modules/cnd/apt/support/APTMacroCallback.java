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

/**
 * callback interface to get information about macros
 * @author Vladimir Voskresensky
 */
public interface APTMacroCallback {
    /**
     * getting macro information
     */
    public boolean isDefined(Token token);
    public APTMacro getMacro(Token token);
    

    /**
     * remember in stack currently expanded macro 
     * used to prevent recurse in macro expandings
     * @return false if macro already in expanding state
     */   
    public boolean pushExpanding(Token token);
    
    /**
     * remove last expanded macro from expanding stack
     */    
    public void popExpanding();
    
    /**
     * check if macro is already somewhere in macro expanding stack
     */    
    public boolean isExpanding(Token token);    
}
