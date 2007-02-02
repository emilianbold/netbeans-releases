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

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.inc.RawOffsetToken;
import org.netbeans.spi.lexer.util.IntegerCache;

/**
 * Simple token implementation for demo purposes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class StringToken implements Token {

    private final TokenId id;

    private final String text;

    StringToken(TokenId id, String text) {
        if (id == null) {
            throw new NullPointerException();
        }
        
        if (text == null) {
            throw new NullPointerException();
        }
        
        this.id = id;
        this.text = text;
    }
    
    public TokenId getId() {
        return id;
    }

    public CharSequence getText() {
        return (CharSequence)(Object)text; // jdk1.3 compilability
    }
    
    public String toString() {
        return text;
    }

}

