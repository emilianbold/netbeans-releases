/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Pisl
 */
public class JadeLexerTest extends CslTestBase {
    
    public JadeLexerTest(String testName) {
        super(testName);
    }
    
    public void setUp() {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
//    public void testComment01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/comment01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//
//    public void testComment02() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/comment02.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testComment03() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/comment03.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testComment04() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/comment04.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testCode01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/code01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testCode02() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/code02.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testPlainText01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/plainText01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    public void testConditional01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/conditional01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testConditional02() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/conditional02.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testDoctype01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/doctype01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testExtends01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/extends01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testExtends02() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/extends02.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testFilters01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/filters01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testInclude01() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/include01.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testInclude02() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/include02.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    
//    public void testInclude03() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/include02.jade",
//                JadeTokenId.jadeLanguage());
//    }

//    
//    public void testAttribute03() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/attribute03.jade",
//                JadeTokenId.jadeLanguage());
//    }
//    


//    
//    public void testAttribute07() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/lexer/attribute07.jade",
//                JadeTokenId.jadeLanguage());
//    }
    
    public void testTag01() throws Exception {
        checkLexer("testfiles/lexer/tag01.jade");
    }
    
    public void testTag02() throws Exception {
        checkLexer("testfiles/lexer/tag02.jade");
        
    }
    
    public void testTag03() throws Exception {
        checkLexer("testfiles/lexer/tag03.jade");
    }
    
    public void testAttribute01() throws Exception {
        checkLexer("testfiles/lexer/attribute01.jade");
    }
     
    public void testAttribute02() throws Exception {
        checkLexer("testfiles/lexer/attribute02.jade");
    }
    
    public void testAttribute03() throws Exception {
        checkLexer("testfiles/lexer/attribute03.jade");
    }
    
    public void testAttribute04() throws Exception {
        checkLexer("testfiles/lexer/attribute04.jade");
    }
    
    public void testAttribute05() throws Exception {
        checkLexer("testfiles/lexer/attribute05.jade");
    }
    
    public void testAttribute06() throws Exception {
        checkLexer("testfiles/lexer/attribute06.jade");
    }
    
    public void testAttribute07() throws Exception {
        checkLexer("testfiles/lexer/attribute07.jade");
    }
    
    public void testAttribute08() throws Exception {
        checkLexer("testfiles/lexer/attribute08.jade");
    }
    
    public void testAttribute09() throws Exception {
        checkLexer("testfiles/lexer/attribute09.jade");
    }
    
    public void testAttribute10() throws Exception {
        checkLexer("testfiles/lexer/attribute10.jade");
    }
    
    public void testAttribute11() throws Exception {
        checkLexer("testfiles/lexer/attribute11.jade");
    }
    
    public void testAttribute12() throws Exception {
        checkLexer("testfiles/lexer/attribute12.jade");
    }
    
    public void testAttribute13() throws Exception {
        checkLexer("testfiles/lexer/attribute13.jade");
    }
    
    public void testAttribute14() throws Exception {
        checkLexer("testfiles/lexer/attribute14.jade");
    }
    
    public void testCase01() throws Exception {
        checkLexer("testfiles/lexer/case01.jade");
    }
    
    public void testCase02() throws Exception {
        checkLexer("testfiles/lexer/case02.jade");
    }
    
    public void testCase03() throws Exception {
        checkLexer("testfiles/lexer/case03.jade");
    }
    
    public void testCode01() throws Exception {
        checkLexer("testfiles/lexer/code01.jade");
    }
    
    public void testExpression01() throws Exception {
        checkLexer("testfiles/lexer/expression01.jade");
    }
    
    private void checkLexer(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        Snapshot snapshot = testSource.createSnapshot();
        CharSequence text = snapshot.getText();
        StringBuilder sb = new StringBuilder();
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<? extends JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        assertNotNull("Can not obtain token sequence for file: " + filePath, ts);
        while (ts.moveNext()) {
            sb.append("[").append(ts.token().id()).append("(").append(ts.token().text().length()).append("):");
            if (ts.token().id() != JadeTokenId.EOL){
                sb.append(ts.token().text().toString()).append("]");
            } else {
                sb.append("\\n]").append("\n");
            }
            
        }

        assertDescriptionMatches(filePath, sb.toString(), false, ".lexer");
    }
}
