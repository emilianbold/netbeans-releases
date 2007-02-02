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

package org.netbeans.modules.lexer.demo.javacc;

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
        return 0; //3;
    }
    
}

