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

package org.netbeans.modules.ruby;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;
import org.netbeans.modules.ruby.lexer.RubyTokenId;

/**
 * Test Ruby lexer incremental operation. Based on Java one by Mila Metelka.
 * 
 * Currently does not pass. There appears to be a problem with the command-state flag.
 */
public class RubyLexerRandomTest extends TestCase {

    public RubyLexerRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        System.setProperty("netbeans.debug.lexer.test", "true");
    }

    public void testRandom() throws Exception {
        test(0);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(RubyTokenId.language());
        
        //randomModify.setDebugOperation(true);
        //randomModify.setDebugDocumentText(true);
        //randomModify.setDebugHierarchy(true);

        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("/**/", 0.2),
            FixedTextDescriptor.create("*/", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.space(0.3),
            RandomCharDescriptor.lf(0.3),
            RandomCharDescriptor.chars(new char[] { '+', '-', '*', '/'}, 0.3),
        };

        RandomCharDescriptor[] anyChar = new RandomCharDescriptor[] {
            RandomCharDescriptor.anyChar(1.0),
        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);
        RandomTextProvider anyCharTextProvider = new RandomTextProvider(anyChar, fixedTexts);
        
        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(1000, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0),
            }
        );

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(1000, anyCharTextProvider,
                        0.4, 0.2, 0.2,
                        0.1, 0.1,
                        0.0, 0.0),
                new RandomModifyDescriptor(1000, anyCharTextProvider,
                        0.2, 0.2, 0.1,
                        0.4, 0.3,
                        0.0, 0.0),
            }
        );
    }
}
