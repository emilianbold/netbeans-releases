/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.python.editor.lexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author tor
 */
public class PythonCommentLexerTest extends NbTestCase{

    public PythonCommentLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        Logger.getLogger(PythonLexer.class.getName()).setLevel(Level.FINEST);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    protected Level logLevel() {
        // enabling logging
        return Level.INFO; // uncomment this to have logging from PyhonLexer
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    public void testTypeVars() {
        String text = "Whatever @type  foo int bye";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.VARNAME, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPE, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, " bye");
        assertFalse(ts.moveNext());
    }

    public void testTypeVarsWithColon() {
        String text = "Whatever @type  foo: int bye";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.VARNAME, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, ": ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPE, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, " bye");
        assertFalse(ts.moveNext());
    }

    public void testTodo() {
        String text = "Whatever TODO this TODOS SANTOS TODO";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TODO, "TODO");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, " this TODOS SANTOS ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TODO, "TODO");
        assertFalse(ts.moveNext());
    }

    public void testTodo2() {
        String text = "STODO"; // Not a TODO
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "STODO");
        assertFalse(ts.moveNext());
    }

    public void testEof1() {
        String text = "Whatever @type  foo int ";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.VARNAME, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPE, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, " ");
        assertFalse(ts.moveNext());
    }

    public void testEof2() {
        String text = "Whatever @type  foo int";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.VARNAME, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPE, "int");
        assertFalse(ts.moveNext());
    }

    public void testEof3() {
        String text = "Whatever @type  foo ";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.VARNAME, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, " ");
        assertFalse(ts.moveNext());
    }

    public void testEof4() {
        String text = "Whatever @type  foo";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.VARNAME, "foo");
        assertFalse(ts.moveNext());
    }

    public void testEof5() {
        String text = "Whatever @type  ";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.SEPARATOR, "  ");
        assertFalse(ts.moveNext());
    }

    public void testEof6() {
        String text = "Whatever @type";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TYPEKEY, "@type");
        assertFalse(ts.moveNext());
    }

    public void testEof7() {
        String text = "Whatever ";
        TokenHierarchy hi = TokenHierarchy.create(text, PythonCommentTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, PythonCommentTokenId.TEXT, "Whatever ");
        assertFalse(ts.moveNext());
    }
}
