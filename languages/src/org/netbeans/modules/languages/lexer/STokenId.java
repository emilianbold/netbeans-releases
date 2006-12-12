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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.lexer;

import org.netbeans.api.lexer.TokenId;


/**
 *
 * @author Jan Jancura
 */
public class STokenId implements TokenId {
    
    private String  name;
    private int     ordinal;
    private String  primaryCategory;
    
    STokenId (
        String  name,
        int     ordinal, 
        String  primaryCategory
    ) {
        this.name = name;
        this.ordinal = ordinal;
        this.primaryCategory = primaryCategory;
    }
    
    public String name () {
        return name;
    }

    public int ordinal () {
        return ordinal;
    }

    public String primaryCategory () {
        return primaryCategory;
    }
}
