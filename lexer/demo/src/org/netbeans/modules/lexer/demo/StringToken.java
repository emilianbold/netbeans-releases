/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.lexer.inc.RawOffsetToken;
import org.netbeans.modules.lexer.util.IntegerCache;
import org.netbeans.modules.lexer.util.CharSeq;

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

