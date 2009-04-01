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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.MakefileTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * @author Alexey Vladykin
 */
public class MakefileLexerTestCase extends NbTestCase {

    public MakefileLexerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    public void testSimple() {
        String text = "# Environment\n" +
                      "MKDIR=mkdir\n" +
                      "BUILDDIR=build/${CONF}\n" +
                      "OS := $(shell uname | grep -i Darwin)\n\n" +
                      "build:\n" +
                      "\t$(COMPILE.cc) source.cpp -o source.o\n\n" +
                      ".PHONY: build\n" +
                      "include foo.mk\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, new MakefileLanguageHierarchy().language());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.LINE_COMMENT, "# Environment\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "MKDIR");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.MACRO_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "mkdir");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "BUILDDIR");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.MACRO_OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "build/");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.MACRO, "${CONF}");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "OS");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.MACRO_OPERATOR, ":=");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(shell uname | grep -i Darwin)");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "build");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.SEPARATOR, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.TAB, "\t");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.MACRO, "$(COMPILE.cc)");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "source.cpp");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.RULE_OPERATOR, "-");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "o");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "source.o");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.SPECIAL_TARGET, ".PHONY");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.SEPARATOR, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "build");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.KEYWORD, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.IDENTIFIER, "foo.mk");
        LexerTestUtilities.assertNextTokenEquals(ts, MakefileTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
}
