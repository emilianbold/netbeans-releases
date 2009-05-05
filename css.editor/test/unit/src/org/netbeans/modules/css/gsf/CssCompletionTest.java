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
package org.netbeans.modules.css.gsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;

/**
 *
 * @author marekfukala
 */
public class CssCompletionTest extends TestBase {

    private static String[] AT_RULES = new String[]{"@charset", "@import", "@media", "@page", "@font-face"};

    public static enum Match {
        EXACT, CONTAINS, EMPTY, NOT_EMPTY, DOES_NOT_CONTAIN;
    }

    public CssCompletionTest() {
        super(CssCompletionTest.class.getName());
    }

    public void checkCC(String documentText, final String[] expectedItemsNames) throws ParseException {
        checkCC(documentText, expectedItemsNames, Match.EXACT);
    }

    public void checkCC(String documentText, final String[] expectedItemsNames, final Match type) throws ParseException {
        StringBuffer content = new StringBuffer(documentText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0;

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        Document doc = getDocument(content.toString());
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult();
                assertNotNull(result);
                assertTrue(result instanceof CssParserResult);

                CssParserResult cssresult = (CssParserResult)result;

                CodeCompletionHandler cc = getPreferredLanguage().getCompletionHandler();
                String prefix = cc.getPrefix(cssresult, pipeOffset, false);
                CodeCompletionResult ccresult = cc.complete(createContext(pipeOffset, cssresult, prefix));
                
                assertCompletionItemNames(expectedItemsNames, ccresult, type);
            }
        });

    }

    public void testAtRules() throws ParseException {
        checkCC("|", AT_RULES, Match.CONTAINS);
        checkCC("@|", AT_RULES);
        checkCC("@pa|", new String[]{"@page"}, Match.CONTAINS);
    }

    public void testPropertyNames() throws ParseException {
        //empty rule
        checkCC("h1 { | }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { az| }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { azimuth| }", arr("azimuth"), Match.CONTAINS);

        //beginning of the rule
        checkCC("h1 { | \n color: red; }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { az| \n color: red; }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { azimuth| \n color: red; }", arr("azimuth"), Match.CONTAINS);

        //middle in the rule
        checkCC("h1 { color: red;\n | \n padding: 2px;}", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n az| \n padding: 2px;}", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n azimuth| \n padding: 2px;}", arr("azimuth"), Match.CONTAINS);

        //end of the rule
        checkCC("h1 { color: red;\n | }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n az| }", arr("azimuth"), Match.CONTAINS);
        checkCC("h1 { color: red;\n azimuth| }", arr("azimuth"), Match.CONTAINS);
    }

    //there are only some basic checks since the values completion itself
    //is tested by org.netbeans.modules.css.editor.PropertyModelTest
    public void testPropertyValues() throws ParseException {
        checkCC("h1 { color: | }", arr("red"), Match.CONTAINS);
        checkCC("h1 { color: r| }", arr("red"), Match.CONTAINS);

        //fails - questionable whether this is a bug or not,
        //at least it is not consistent with the property names completion
        //checkCC("h1 { color: red| }", arr("red"), Match.CONTAINS);
        
        checkCC("h1 { color: red | }", arr(), Match.EMPTY);
        checkCC("h1 { border: dotted | }", arr("blue"), Match.CONTAINS);
    }

    public void testCorners() throws ParseException {
        checkCC("h1 { bla| }", arr(), Match.EMPTY);
//        checkCC("h1 { color: ble| }", arr(), Match.EMPTY); //fails - issue #161129
    }

    public void testIssue160870() throws ParseException {
        checkCC("h1 { display : | }", arr("block"), Match.CONTAINS);
    }

    public void testHtmlSelectorsCompletion() throws ParseException {
        checkCC("|", arr("html"), Match.CONTAINS);
        checkCC("ht| ", arr("html"), Match.EXACT);
        checkCC("html | ", arr("body"), Match.CONTAINS);
        checkCC("html bo| ", arr("body"), Match.EXACT);
        checkCC("html, bo| ", arr("body"), Match.EXACT);
        checkCC("html > bo| ", arr("body"), Match.EXACT);
        checkCC("html tit| { }", arr("title"), Match.CONTAINS);
    }

    public void testSystemColors() throws ParseException {
        checkCC("div { color: | }", arr("menu", "window"), Match.CONTAINS);
    }

    public void testHtmlSelectorsInMedia() throws ParseException {
        checkCC("@media page {  |   } ", arr("html"), Match.CONTAINS);
        checkCC("@media page {  |   } ", arr("@media"), Match.DOES_NOT_CONTAIN); //media not supported here

//        checkCC("@media page {  htm|   } ", arr("html"), Match.EXACT);
//        checkCC("@media page {  html, |   } ", arr("body"), Match.CONTAINS);
//        checkCC("@media page {  html, bo|   } ", arr("body"), Match.CONTAINS);
//        checkCC("@media page {  html > bo|   } ", arr("body"), Match.CONTAINS);
    }

    //--- utility methods ---

    private String[] arr(String... args) {
        return args;
    }

    private void assertCompletionItemNames(String[] expected, CodeCompletionResult ccresult, Match type) {
        Collection<String> real = new ArrayList<String>();
        for(CompletionProposal ccp : ccresult.getItems()) {
            real.add(ccp.getName());
        }
        Collection<String> exp = new ArrayList<String>(Arrays.asList(expected));

        if(type == Match.EXACT) {
            assertEquals(exp, real);
        } else if(type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(Collections.EMPTY_LIST, exp);
        } else if(type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if(type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals("The unexpected element(s) '" + arrayToString(expected)+ "' are present in the completion items list", originalRealSize, real.size());
        }

    }

    private String arrayToString(String[] elements) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < elements.length; i++) {
            buf.append(elements[i]);
            if(i < elements.length - 1) {
                buf.append(',');
                buf.append(' ');
            }
        }
        return buf.toString();
    }


    private static TestCodeCompletionContext createContext(int offset, ParserResult result, String prefix) {
        return new TestCodeCompletionContext(offset, result, prefix, QueryType.COMPLETION, false);
    }

    private static class TestCodeCompletionContext extends CodeCompletionContext {

        private int caretOffset;
        private ParserResult result;
        private String prefix;
        private QueryType type;
        private boolean isCaseSensitive;

        public TestCodeCompletionContext(int caretOffset, ParserResult result, String prefix, QueryType type, boolean isCaseSensitive) {
            this.caretOffset = caretOffset;
            this.result = result;
            this.prefix = prefix;
            this.type = type;
            this.isCaseSensitive = isCaseSensitive;
        }

        @Override
        public int getCaretOffset() {
            return caretOffset;
        }

        @Override
        public ParserResult getParserResult() {
            return result;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public boolean isPrefixMatch() {
            return true;
        }

        @Override
        public QueryType getQueryType() {
            return type;
        }

        @Override
        public boolean isCaseSensitive() {
            return isCaseSensitive;
        }
    }
}