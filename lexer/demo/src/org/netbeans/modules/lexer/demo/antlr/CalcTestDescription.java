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

package org.netbeans.modules.lexer.demo.antlr;

import org.netbeans.api.lexer.Language;
import org.netbeans.spi.lexer.util.LexerTestDescription;

/**
 * Example of using the lexer framework.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CalcTestDescription extends LexerTestDescription {

    public Language getLanguage() {
        return CalcLanguage.get();
    }
    
    public TestRound[] getTestRounds() {
        return new TestRound[] {
            new TestRound(10000, 0.6, 5, 0.4, 4),
            new TestRound(10000, 0.4, 1, 0.6, 3)
        };
    }
    
    public TestChar[] getTestChars() {
        return new TestChar[] {
            new TestChar('*', 0.3),
            new TestChar('+', 0.3),
            new TestChar(' ', 0.3),
            new TestChar('\n', 0.05),
            new TestChar('a', 0.05) // errorneous char to test error tokens
        };
    }

    public TestCharInterval[] getTestCharIntervals() {
        return new TestCharInterval[] {
            new TestCharInterval('0', '9', 0.2)
        };
    }
    
    public TestString[] getTestStrings() {
        return new TestString[] {
            new TestString("/*", 0.1),
            new TestString("*/", 0.1)
        };
    }

    public int getDebugLevel() {
        return 0; // 3;
    }
    
}

