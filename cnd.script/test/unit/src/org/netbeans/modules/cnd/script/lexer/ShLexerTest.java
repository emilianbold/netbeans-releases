/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.script.lexer;

import org.netbeans.modules.cnd.script.lexer.ShLanguageHierarchy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import static org.netbeans.modules.cnd.script.lexer.ShTokenId.*;
import static org.netbeans.lib.lexer.test.LexerTestUtilities.assertNextTokenEquals;

/**
 * @author Alexey Vladykin
 */
public class ShLexerTest extends NbTestCase {

    public ShLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testSimple() {
        String text = "#!/bin/sh\n\n" +
                "for f in foo.tar foo.bar; do\n" +
                "\techo if for do $f \\\"asd\\\" \"fasdf\" >/dev/null 2>&1\n" +
                "done\n\n" +
                "tar xf foo.tar\n";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, new ShLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertNextTokenEquals(ts, COMMENT, "#!/bin/sh\n");
        assertNextTokenEquals(ts, WHITESPACE, "\n");
        assertNextTokenEquals(ts, KEYWORD, "for");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "f");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "in");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, ".");
        assertNextTokenEquals(ts, IDENTIFIER, "tar");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, ".");
        assertNextTokenEquals(ts, IDENTIFIER, "bar");
        assertNextTokenEquals(ts, OPERATOR, ";");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, KEYWORD, "do");
        assertNextTokenEquals(ts, WHITESPACE, "\n\t");
        assertNextTokenEquals(ts, COMMAND, "echo");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "if");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "for");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "do");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OPERATOR, "$");
        assertNextTokenEquals(ts, IDENTIFIER, "f");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OPERATOR, "\\\"");
        assertNextTokenEquals(ts, IDENTIFIER, "asd");
        assertNextTokenEquals(ts, OPERATOR, "\\\"");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, STRING, "\"fasdf\"");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OPERATOR, ">");
        assertNextTokenEquals(ts, OPERATOR, "/");
        assertNextTokenEquals(ts, IDENTIFIER, "dev");
        assertNextTokenEquals(ts, OPERATOR, "/");
        assertNextTokenEquals(ts, IDENTIFIER, "null");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, NUMBER, "2");
        assertNextTokenEquals(ts, OPERATOR, ">");
        assertNextTokenEquals(ts, OPERATOR, "&");
        assertNextTokenEquals(ts, NUMBER, "1");
        assertNextTokenEquals(ts, WHITESPACE, "\n");
        assertNextTokenEquals(ts, KEYWORD, "done");
        assertNextTokenEquals(ts, WHITESPACE, "\n\n");
        assertNextTokenEquals(ts, COMMAND, "tar");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "xf");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, ".");
        assertNextTokenEquals(ts, IDENTIFIER, "tar");
        assertNextTokenEquals(ts, WHITESPACE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
}
