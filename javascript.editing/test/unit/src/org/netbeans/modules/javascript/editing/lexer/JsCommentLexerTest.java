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
package org.netbeans.modules.javascript.editing.lexer;

import java.util.Map;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Jan Lahoda
 * @author Martin Adamek
 */
public class JsCommentLexerTest extends NbTestCase {

    public JsCommentLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testNextToken1() {
        String text = "@param aaa <code>aaa</code> xyz {@link org.Aaa#aaa()}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JsCommentTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.HTML_TAG, "<code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.HTML_TAG, "</code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.IDENT, "xyz");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.LCURL, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.TAG, "@link");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.IDENT, "org");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.IDENT, "Aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.HASH, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.OTHER_TEXT, "()");
        LexerTestUtilities.assertNextTokenEquals(ts, JsCommentTokenId.RCURL, "}");
    }

//    public void testModification1() throws Exception {
//        PlainDocument doc = new PlainDocument();
//        doc.putProperty(Language.class, JsCommentTokenId.language());
//        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
//        
//        {
//            TokenSequence<?> ts = hi.tokenSequence();
//            ts.moveStart();
//            assertFalse(ts.moveNext());
//        }
//        
//        doc.insertString(0, "@", null);
//    }
    
    public void testFindFunctionTypes() {
        String text =
                "/**\n" +
                "* Returns a function that will return a number ...\n" +
                "* @alias fooBar\n" +
                "* @alias FOO.Lib.fooBar\n" +
                "* @param\n" +
                "* @param {Object} n	Number to start with. Default is 1.\n" +
                "* @param {String, Date} myDate	Specifies the date, if applicable.\n" +
                "* @param {Object/Array} values The bla bla bloo (i.e. {0}) or an object (i.e. {foo: 'bar'})\n" +
                "* @param {Foo.Bar/Baz} values2 Bla bla\n" +
                "* @return {Function} Returns a function that will ...\n" +
                "*/";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JsCommentTokenId.language());
        @SuppressWarnings("unchecked")
        TokenSequence<JsCommentTokenId> ts = (TokenSequence<JsCommentTokenId>) hi.tokenSequence();
        
        Map<String, String> types = JsCommentLexer.findFunctionTypes(ts);
        
        assertEquals(types.size(), 5);
        assertEquals("Object", types.get("n"));
        assertEquals("String|Date", types.get("myDate"));
        assertEquals("Object|Array", types.get("values"));
        assertEquals("Foo.Bar|Baz", types.get("values2"));
        assertEquals("Function", types.get("@return"));
    }
    
}
