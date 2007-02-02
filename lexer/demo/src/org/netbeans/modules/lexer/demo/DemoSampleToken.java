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

/**
 * Simple token implementation for demo purposes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DemoSampleToken extends StringToken {

    private int lookahead;

    private int lookback;

    private Object state;

    DemoSampleToken(TokenId id, String text) {
        super(id, text);
    }

    public int getLookahead() {
        return lookahead;
    }

    void setLookahead(int lookahead) {
        this.lookahead = lookahead;
    }

    public int getLookback() {
        return lookback;
    }
    
    void setLookback(int lookback) {
        this.lookback = lookback;
    }
    
    public Object getState() {
        return state;
    }
    
    void setState(Object state) {
        this.state = state;
    }
    
}

