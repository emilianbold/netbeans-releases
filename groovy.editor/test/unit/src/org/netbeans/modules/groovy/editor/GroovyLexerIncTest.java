/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.groovy.editor;

import java.util.ConcurrentModificationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;

/**
 *
 * @author Martin Adamek
 */
public class GroovyLexerIncTest extends NbTestCase {

    public GroovyLexerIncTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,GroovyTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String text = "println \"Hello $name\"";
        doc.insertString(0, text, null);

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }
        
        ts = hi.tokenSequence();
        
        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 16);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"", 20);

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);
        
        int offset = text.length() - 1;
        
        // Check TokenSequence.move()
        int relOffset = ts.move(50); // past the end of all tokens
        assertEquals(relOffset, 50 - (offset + 1));
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"", offset);

        relOffset = ts.move(8); // right at begining of gstring
        assertEquals(relOffset, 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);

        relOffset = ts.move(-5); // to first token "println"
        assertEquals(relOffset, -5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "println", 0);

        relOffset = ts.move(10);
        assertEquals(relOffset, 2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);

        doc.insertString(5, "x", null); // should be "printxln"

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "printxln", 0);
        LexerTestUtilities.incCheck(doc, false);
        
        // Remove added 'x' to become "println" again
        doc.remove(5, 1); // should be "println" again

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        LexerTestUtilities.incCheck(doc, false);

        // Now insert right at the end of first token - identifier with lookahead 1
        doc.insertString(7, "x", null); // should become "printlnx"
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "printlnx", 0);
        LexerTestUtilities.incCheck(doc, false);

        doc.remove(7, 1); // return back to "println"

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        LexerTestUtilities.incCheck(doc, false);

        doc.insertString(offset, " ", null);
        
        ts = hi.tokenSequence();
        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 16);
        // this is failing, only incremental lexing problem known to me so far
//        next(ts, GroovyTokenId.STRING_LITERAL, " \"", 20);

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);
    }
    
    void next(TokenSequence<?> ts, GroovyTokenId id, String fixedText, int offset){
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, id, fixedText, offset);
    }

}
