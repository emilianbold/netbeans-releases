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
package org.netbeans.modules.php.smarty.editor.lexer;

import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test TPL top-level lexer analyzis
 *
 * @author Martin Fousek
 */
public class TplTopLexerBatchTest extends TestCase {

    public TplTopLexerBatchTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTopSmartyAndHtmlTags() {
        String text = "{include file='head.tpl'}{if $logged neq 0}<span color=\"{#fontColor#}\">{$name|upper}!{/if}</span>";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, TplTopTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "include file='head.tpl'");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "if $logged neq 0");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "<span color=\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "#fontColor#");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "\">");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "$name|upper");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/if");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "</span>");
    }

    public void testSmartyLiteralTags() {
        String text = "{literal}{{/literal}{";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, TplTopTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }

    public void testSmartyCommentsTags() {
        String text = "{*{c*}{if}{*c*}{/if}";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, TplTopTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "c");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "c");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/if");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testSmartyPhpTags() {
        String text = "{php}function { this. {/php}{";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, TplTopTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "php");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "fu");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "nc");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "ti");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "on");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "{ this.");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/php");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }
}
