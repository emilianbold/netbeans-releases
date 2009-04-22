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
package org.netbeans.lib.jsp.lexer;

import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**Jsp Lexer Test
 *
 * @author Marek.Fukala@Sun.COM
 */
public class JspLexerTest extends NbTestCase {

    public JspLexerTest() {
        super("JspLexerTest");
    }

    private CharSequence readFile(String fileName) throws IOException {
        File inputFile = new File(getDataDir(), fileName);
        return Utils.readFileContentToString(inputFile);
    }

    private static String getTokenInfo(Token token, TokenHierarchy tokenHierarchy) {
        return "TOKEN[text=\"" + token.text() + "\"; tokenId=" + token.id().name() + "; offset=" + token.offset(tokenHierarchy) + "]";
    }

    private void dumpTokens(CharSequence charSequence) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.create(charSequence, JspTokenId.language());
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.moveStart();
        while (tokenSequence.moveNext()) {
            getRef().println(getTokenInfo(tokenSequence.token(), tokenHierarchy));
        }
    }

    //test methods -----------
    public void testComplexJSP() throws BadLocationException, IOException {
        dumpTokens(readFile("input/JspLexerTest/testComplexJSP.jsp"));
        compareReferenceFiles();
    }

    public void test146930() {
        TokenHierarchy th = TokenHierarchy.create("<${}", JspTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());

        assertEquals("<", ts.token().text().toString());
        assertEquals(JspTokenId.TEXT, ts.token().id());

        assertTrue(ts.moveNext());

        assertEquals("${}", ts.token().text().toString());
        assertEquals(JspTokenId.EL, ts.token().id());

        assertFalse(ts.moveNext());
    }

    public void testIssue158106() {
        String code = "<jsp:useBean\n scope=\"application\"\n id=\"x\">";

        TokenHierarchy th = TokenHierarchy.create(code, JspTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("<", ts.token().text().toString());
        assertEquals(JspTokenId.SYMBOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("jsp:useBean", ts.token().text().toString());
        assertEquals(JspTokenId.TAG, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("\n", ts.token().text().toString());
        assertEquals(JspTokenId.EOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals(" ", ts.token().text().toString());
        assertEquals(JspTokenId.WHITESPACE, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("scope", ts.token().text().toString());
        assertEquals(JspTokenId.ATTRIBUTE, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("=", ts.token().text().toString());
        assertEquals(JspTokenId.SYMBOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("\"application\"", ts.token().text().toString());
        assertEquals(JspTokenId.ATTR_VALUE, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("\n", ts.token().text().toString());
        assertEquals(JspTokenId.EOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals(" ", ts.token().text().toString());
        assertEquals(JspTokenId.WHITESPACE, ts.token().id());


    }

    //test whether content of <jsp:expression>...</jsp:expression> is java
    //http://www.netbeans.org/issues/show_bug.cgi?id=162546
    public void testExpressionTagContent() {
        TokenHierarchy th = TokenHierarchy.create("<jsp:expression>\"x\"</jsp:expression>", JspTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertToken(ts, "<", JspTokenId.SYMBOL);
        assertToken(ts, "jsp:expression", JspTokenId.TAG);
        assertToken(ts, ">", JspTokenId.SYMBOL);
        assertToken(ts, "\"x\"", JspTokenId.SCRIPTLET);
        assertToken(ts, "</", JspTokenId.SYMBOL);
        assertToken(ts, "jsp:expression", JspTokenId.ENDTAG);
        assertToken(ts, ">", JspTokenId.SYMBOL);

        assertFalse(ts.moveNext());

    }

    private void assertToken(TokenSequence ts, String tokenText, JspTokenId tokenId) {
        assertTrue(ts.moveNext());
        assertEquals(tokenId, ts.token().id());
        assertEquals(tokenText, ts.token().text().toString());
    }

    public void testRegressions() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testRegressions.jsp.txt",
                JspTokenId.language());
    }
}
