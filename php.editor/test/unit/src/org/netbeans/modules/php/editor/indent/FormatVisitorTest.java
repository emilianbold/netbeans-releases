/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import java.util.List;
import java_cup.runtime.Symbol;
import java.io.File;
import java.io.StringReader;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPLexerUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.ASTPHP5Parser;
import org.netbeans.modules.php.editor.parser.ASTPHP5Scanner;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class FormatVisitorTest extends PHPTestBase {

    public FormatVisitorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }
    
    public void testArrays1()  throws Exception {
        executeTest("testfiles/formatting/arrays1.php");
    }

    public void testComment01()  throws Exception {
        executeTest("testfiles/formatting/comment01.php");
    }

    public void testContinuedExpression()  throws Exception {
        executeTest("testfiles/formatting/continued_expression.php");
    }

    public void testClass03()  throws Exception {
        executeTest("testfiles/formatting/blankLines/Class03.php");
    }

    void executeTest(String fileName) throws Exception {
        FileObject fo = getTestFile(fileName);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);

        String content = PHPLexerUtils.getFileContent(new File(getDataDir(), fileName));
        TokenSequence<?> ts = PHPLexerUtils.seqForText(content, PHPTokenId.language());
        FormatVisitor formatVisitor = new FormatVisitor(doc);
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(content));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        Program program = (Program)root.value;
        formatVisitor.scan(program);
        List<FormatToken> formatTokens = formatVisitor.getFormatTokens();

        int index = 0;
        ts.move(0);
        while (ts.moveNext()) {
            Token tokenTS = ts.token();
            FormatToken tokenF = formatTokens.get(index);
            if (tokenF.getOldText() == null) {
                while(index < formatTokens.size() && tokenF.getOldText() == null) {
                    tokenF = formatTokens.get(index);
                    index++;
                }
            } else {
                index++;
            }

            assertEquals(tokenTS.text().toString(), tokenF.getOldText());
            assertEquals(ts.offset(), tokenF.getOffset());
        }
    }

}
