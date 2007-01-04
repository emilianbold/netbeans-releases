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

package org.netbeans.modules.cnd.apt.structure;

import antlr.Token;
import java.util.Collection;
import java.util.List;

/**
 * #define directive
 * @author Vladimir Voskresensky
 */
public interface APTDefine extends APT {
    /** 
     * returns token of macro name
     */
    public Token getName();
    
    /** 
     * returns array of macro params
     * @see isFunctionLike
     * if function-like macro => return is non null
     * otherwise it is null
     */
    public Collection getParams();
    
    /** 
     * returns true if macro was defined as function
     * #define MAX(x,y) ...
     * or 
     * #define A() ...
     */
    public boolean isFunctionLike();

    /** 
     * returns List of tokens of macro body
     */      
    public List getBody();
}
