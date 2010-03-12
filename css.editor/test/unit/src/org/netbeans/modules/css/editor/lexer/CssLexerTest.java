/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor.lexer;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lexer.api.CssTokenId;

/**
 * @author  marek.fukala@sun.com
 */
public class CssLexerTest extends TestBase {

    public CssLexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInputGeneratedCode.css.txt",
                CssTokenId.language());
    }

    public void testImportsLexing() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testImportsLexing.css.txt",
                CssTokenId.language());
    }

    //http://www.netbeans.org/issues/show_bug.cgi?id=161642
    public void testIssue161642() throws Exception {
        String input = "/* c */;";
        TokenHierarchy th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("/* c */", ts.token().text().toString());
        assertEquals(CssTokenId.COMMENT, ts.token().id());
        assertEquals("comment", ts.token().id().primaryCategory());

        assertTrue(ts.moveNext());
        assertEquals(";", ts.token().text().toString());
        assertEquals(CssTokenId.SEMICOLON, ts.token().id());
    }

}
