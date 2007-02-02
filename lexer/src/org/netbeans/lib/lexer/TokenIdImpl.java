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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.TokenId;

/**
 * Token id implementation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenIdImpl implements TokenId {

    private final String name;

    private final int ordinal;

    private final String primaryCategory;

    public TokenIdImpl(String name, int ordinal, String primaryCategory) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        if (ordinal < 0) {
            throw new IllegalArgumentException("ordinal=" + ordinal
                + " of token=" + name + " cannot be < 0");
        }
        
        this.name = name;
        this.ordinal = ordinal;
        this.primaryCategory = primaryCategory;
    }
    
    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    public String toString() {
        return LexerUtilsConstants.idToString(this);
    }

    public String toStringDetail() {
        return name() + "[" + ordinal() + // NOI18N
                (primaryCategory != null ? ", \"" + primaryCategory + "\"" : "") + // NOI18N
                "]";
    }
    
}
