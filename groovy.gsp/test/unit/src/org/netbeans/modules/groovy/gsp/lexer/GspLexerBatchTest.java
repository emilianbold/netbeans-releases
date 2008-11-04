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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.gsp.lexer;

import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;

/**
 * Test GSP lexer
 *
 * @author Martin Adamek
 */
public class GspLexerBatchTest extends TestCase {

    public GspLexerBatchTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void test1() {
        String text = 
                "<html>" +
                "<g:if>" +
                "</g:if>" +
                "</html>";
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, GspTokenId.language());
        TokenSequence<?> sequence = hierarchy.tokenSequence();
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<html>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GTAG, "<g:if>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GTAG, "</g:if>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "</html>", -1);
    }

    public void test2() {
        String text = 
                "<html>" +
                "<g:if test=\"${t}\">" +
                "<div class=\"e\">" +
                "<g:renderErrors bean=\"${f.u}\" />" +
                "</div>" +
                "</g:if>" +
                "<div class=\"s\">${e.s}</div>" +
                "</html>";
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, GspTokenId.language());
        TokenSequence<?> sequence = hierarchy.tokenSequence();
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<html>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GTAG, "<g:if test=\"${t}\">", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<div class=\"e\">", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GTAG, "<g:renderErrors bean=\"${f.u}\" />", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "</div>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GTAG, "</g:if>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<div class=\"s\">", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.DELIMITER, "${", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GROOVY, "e.s", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.DELIMITER, "}", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "</div></html>", -1);
    }
    
    public void testExclamation() {
        String text =
                "<p>a!</p>";
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, GspTokenId.language());
        TokenSequence<?> sequence = hierarchy.tokenSequence();
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<p>a!</p>", -1);
    }

    public void testPercent() {
        String text = "<a class=\"home\" href=\"${createLinkTo(dir:'')}\">Home</a>";
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, GspTokenId.language());
        TokenSequence<?> sequence = hierarchy.tokenSequence();
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<a class=\"home\" href=\"", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.DELIMITER, "${", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GROOVY, "createLinkTo(dir:'')", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.DELIMITER, "}", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "\">Home</a>", -1);
    }

    public void testExpressionInValue() {
        String text = 
                "<%@ page import=\"org.grails.bookmarks.*\" %>" +
                "<style type=\"text/css\">.searchbar {width:97%;}</style>";
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, GspTokenId.language());
        TokenSequence<?> sequence = hierarchy.tokenSequence();
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.DELIMITER, "<%@", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.GROOVY, " page import=\"org.grails.bookmarks.*\" ", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.DELIMITER, "%>", -1);
        assertTrue(sequence.moveNext());
        LexerTestUtilities.assertTokenEquals(sequence,GspTokenId.HTML, "<style type=\"text/css\">.searchbar {width:97%;}</style>", -1);
    }

}
