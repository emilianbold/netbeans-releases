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

package org.netbeans.modules.lexer.demo.handcoded.link;

import org.netbeans.api.lexer.Language;
import org.netbeans.spi.lexer.util.LexerTestDescription;

/**
 * Example of using the lexer framework.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LinkTestDescription extends LexerTestDescription {

    public Language getLanguage() {
        return LinkLanguage.get();
    }
    
    public TestRound[] getTestRounds() {
        return new TestRound[] {
            new TestRound(10000, 0.6, 5, 0.4, 4),
            new TestRound(10000, 0.4, 1, 0.6, 3)
        };
    }
    
    public TestChar[] getTestChars() {
        return new TestChar[] {
            new TestChar('/', 0.5),
            new TestChar(':', 0.3),
            new TestChar('.', 0.1),
            new TestChar('#', 0.1),
            new TestChar(' ', 0.2),
            new TestChar('\n', 0.05)
        };
    }
    
    public TestString[] getTestStrings() {
        return new TestString[] {
            new TestString("http://", 0.05),
            new TestString("ftp://", 0.05),
            new TestString("ascheme://", 0.05),
            new TestString("://", 0.05)
        };
    }

    public TestCharInterval[] getTestCharIntervals() {
        return new TestCharInterval[] {
            new TestCharInterval('0', '9', 0.1),
            new TestCharInterval('a', 'z', 0.1)
        };
    }
    
    public int getDebugLevel() {
        return 0; // 3;
    }
    

}

