/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.lib.html.lexer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Tor Norbye
 */
public class HtmlLexerTest extends NbTestCase {

    public HtmlLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new HtmlLexerTest("testEmbeddedCss"));
        return suite;
    }

    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInput.rb.txt",
                HTMLTokenId.language());
    }

    public void test146930() {
        TokenHierarchy th = TokenHierarchy.create("<<body>", HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("<", ts.token().text().toString());
        assertEquals(HTMLTokenId.TEXT, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("<", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_OPEN_SYMBOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("body", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_OPEN, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals(">", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_CLOSE_SYMBOL, ts.token().id());

        assertFalse(ts.moveNext());
    }

    public void test149018() throws Exception { //JSP editor not recognizing valid end-of-html comment
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInput.html.txt",
                HTMLTokenId.language());
    }

    public void testEmptyTag() {
        checkTokens("<div/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", "/>|TAG_CLOSE_SYMBOL");
    }

    public void testUnfinishedTag() {
        checkTokens("<div/", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", "/|TEXT");
    }

    public void testIssue149968() {
        checkTokens("<div @@@/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " @@@|WS", "/>|TAG_CLOSE_SYMBOL");
    }

    public void testEmbeddedCss() {
        //not css attribute
        checkTokens("<div align=\"center\"/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "align|ARGUMENT",
                "=|OPERATOR", "\"center\"|VALUE", "/>|TAG_CLOSE_SYMBOL");

        //css attribute
        checkTokens("<div class=\"myclass\"/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT",
                "=|OPERATOR", "\"myclass\"|VALUE_CSS");
    }

    public void testGenericCssClassEmbedding() {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        map.put("c:button", Collections.singletonList("styleClass"));

        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(HTMLTokenId.language(), "cssClassTagAttrMap", map, false); //NOI18N

        String text = "<c:button styleClass=\"myclass\"/>";
        TokenHierarchy th = TokenHierarchy.create(text, true, HTMLTokenId.language(), Collections.<HTMLTokenId>emptySet(), inputAttributes);
        TokenSequence ts = th.tokenSequence();

        checkTokens(ts, "<|TAG_OPEN_SYMBOL", "c:button|TAG_OPEN", " |WS", "styleClass|ARGUMENT",
                "=|OPERATOR", "\"myclass\"|VALUE_CSS");
        
    }

    private void checkTokens(String text, String... descriptions) {
        TokenHierarchy<String> th = TokenHierarchy.create(text, HTMLTokenId.language());
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
        checkTokens(ts, descriptions);
    }

    private void checkTokens(TokenSequence<HTMLTokenId> ts, String... descriptions) {
        ts.moveStart();
        for(String descr : descriptions) {
            //parse description
            int slashIndex = descr.indexOf('|');
            assert slashIndex >= 0;

            String image = descr.substring(0, slashIndex);
            String id = descr.substring(slashIndex + 1);

            assertTrue(ts.moveNext());
            Token t = ts.token();
            assertNotNull(t);

            if(image.length() > 0) {
                assertEquals(image, t.text().toString());
            }

            if(id.length() > 0) {
                assertEquals(id, t.id().name());
            }
        }
    }

}
