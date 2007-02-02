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

package org.netbeans.lib.lexer.test.simple;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerRandomTest extends TestCase {

    public SimpleLexerRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testRandom() throws Exception {
        test(0);
    }
    
    public void testWithSeed_1140557399761L() throws Exception {
        test(1140557399761L);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(SimpleTokenId.language());
        
        //randomModify.setDebugOperation(true);
        //randomModify.setDebugDocumentText(true);
        //randomModify.setDebugHierarchy(true);

        // Check for incorrect lookahead counting problem
        // after one of the larger updates of the LexerInputOperation
        randomModify.insertText(0, "+--+");
        randomModify.removeText(2, 1);
        
        randomModify.clearDocument();

        // Check that token list updater respects that the lookaheads
        // of subsequent tokens (already present in the token list)
        // correspond to the lookahead of the relexed token.
        // In the following example a "+-+" token must be created.
        randomModify.insertText(0, "---+");
        randomModify.insertText(1, "+");
        randomModify.removeText(3, 1);
        
        randomModify.clearDocument();

        // Check that the token list updater checks respects the rule
        // that the lookahead of the incrementally created tokens
        // is the same like in a regular batch lexing.
        randomModify.insertText(0, "+--+--");
        randomModify.removeText(2, 1);
        
        randomModify.clearDocument();
   
        // Check for the previous case but this time the relexing would normally
        // be skipped but this would lead to lookahead 1 for the "-" token
        // after the removed "+" (while the batch lexing would produce lookahead 0)
        randomModify.insertText(0, "-+--");
        randomModify.removeText(1, 1);
        
        randomModify.clearDocument();
        
        // Similar case to the previous one but with more tokens
        randomModify.insertText(0, "-+-++--");
        randomModify.removeText(1, 4);
        
        randomModify.clearDocument();

        // Check for the case when token validation cannot be performed
        // because although the lenghth of the removal is less than
        // the "+-+" token's length the removal spans token boundaries
        randomModify.insertText(0, "-+-+ --");
        randomModify.removeText(3, 2);
        
        randomModify.clearDocument();


        // Begin really randomized testing
        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("-+--+-+", 0.2),
            FixedTextDescriptor.create("+-", 0.2),
            FixedTextDescriptor.create("-+", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.space(0.3),
            RandomCharDescriptor.lf(0.3),
            RandomCharDescriptor.chars(new char[] { '+', '-', '*', '/'}, 0.3),
        };

        RandomCharDescriptor[] plusMinusChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.chars(new char[] { '+', '-' }, 0.3),
//            RandomCharDescriptor.chars(new char[] { '*', '/' }, 0.1),
            RandomCharDescriptor.space(0.1),
        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);
        RandomTextProvider plusMinusTextProvider = new RandomTextProvider(plusMinusChars, fixedTexts);
        
        RandomTextProvider textProvider = plusMinusTextProvider;

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, plusMinusTextProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0), // snapshots create/destroy
            }
        );

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.4, 0.2, 0.2,
                        0.1, 0.1,
                        0.0, 0.0), // snapshots create/destroy
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.4, 0.3,
                        0.0, 0.0), // snapshots create/destroy
            }
        );
    }
    
}
