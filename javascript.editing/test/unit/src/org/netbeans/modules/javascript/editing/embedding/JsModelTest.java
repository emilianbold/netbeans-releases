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

package org.netbeans.modules.javascript.editing.embedding;

import java.io.File;
import java.util.Collections;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.JsTestBase;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class JsModelTest extends JsTestBase {

    public JsModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(RhtmlTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
        try {
            TestLanguageProvider.register(JsTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private Snapshot getTranslatedSource(String relFilePath) throws Exception {
        File jsFile = new File(getDataDir(), relFilePath);
        if (!jsFile.exists()) {
            NbTestCase.fail("File " + jsFile + " not found.");
        }

        BaseDocument doc = getDocument(getTestFile(relFilePath));

        return getTranslatedSource(doc, relFilePath);
    }

    private Snapshot getTranslatedSource(BaseDocument doc, String relFilePath) throws Exception {
        String RHTML_MIME_TYPE = RhtmlTokenId.MIME_TYPE;
        String HTML_MIME_TYPE = HtmlKit.HTML_MIME_TYPE;

        Language lexerLanguage;
        String mimeType;
        if (relFilePath.endsWith(".html")) {
            mimeType = HTML_MIME_TYPE;
            lexerLanguage = HTMLTokenId.language();
        } else if (relFilePath.endsWith(".erb") || relFilePath.endsWith(".rhtml")) {
            mimeType = RHTML_MIME_TYPE;
            lexerLanguage = RhtmlTokenId.language();
        } else {
            fail("Unexpected file extension for " + relFilePath);
            return null;
        }

        doc.putProperty("mimeType", mimeType);
        doc.putProperty(org.netbeans.api.lexer.Language.class, lexerLanguage);

        Source testSource = Source.create(doc);
        final Snapshot [] result = new Snapshot [] { null };
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                int javascriptEmbeddings = 0;
                for(Embedding e : resultIterator.getEmbeddings()) {
                    if (JsTokenId.JAVASCRIPT_MIME_TYPE.equals(e.getMimeType())) {
                        if (result[0] == null) {
                            ResultIterator jri = resultIterator.getResultIterator(e);
                            result[0] = jri.getSnapshot();
                            assertNotNull("Embedded snapshot should not be null", result[0]);
                        }
                        javascriptEmbeddings++;
                    }
                }

                assertEquals("Wrong number of javascript embeddings", 1, javascriptEmbeddings);
            }
        });

        return result[0];
    }

    private void checkJavaScriptTranslation(String relFilePath) throws Exception {
        checkJavaScriptTranslation(relFilePath, true);
    }

    private void checkJavaScriptTranslation(String relFilePath, boolean mustCompile) throws Exception {
        Snapshot translatedSource = getTranslatedSource(relFilePath);
        String generatedJs = translatedSource.getText().toString();

        assertDescriptionMatches(relFilePath, generatedJs.toString(), false, ".js");

        // Make sure the generated file doesn't have errors
        File rubyFile = new File(getDataDir(), relFilePath + ".js");
        FileObject jsFo = FileUtil.toFileObject(rubyFile);
        assertNotNull(jsFo);
        if (mustCompile) {
            Source source = Source.create(jsFo);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result r = resultIterator.getParserResult();
                    JsParseResult jspr = AstUtilities.getParseResult(r);
                    assertNotNull("Expecting JsParseResult, but got " + r, jspr);
                    assertNotNull("Parse error on translated source", jspr.getRootNode());

                    // Warnings are okay:
                    //assertTrue(info.getErrors().toString(), info.getErrors().size() == 0);
                    for (Error error : jspr.getDiagnostics()) {
                        assertTrue(error.toString(), error.getSeverity() != Severity.ERROR);
                    }
                }
            });

        }
    }

    private void checkPositionTranslations(String relFilePath, String checkBeginLine, String checkEndLine) throws Exception {
        // TODO
        // Translate source... then iterate through the source positions and assert
        // that everything in the source matches. Also make sure that the stuff that
        // doesn't match is properly placed...
        Snapshot translatedSource = getTranslatedSource(relFilePath);
        String text = readFile(getTestFile(relFilePath));

        assertNotNull(checkBeginLine);
        assertNotNull(checkEndLine);

        int checkBeginOffset = -1;
        int lineDelta = checkBeginLine.indexOf("^");
        assertTrue(lineDelta != -1);
        checkBeginLine = checkBeginLine.substring(0, lineDelta) + checkBeginLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(checkBeginLine);
        assertTrue(lineOffset != -1);

        checkBeginOffset = lineOffset + lineDelta;

        int checkEndOffset = -1;
        lineDelta = checkEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        checkEndLine = checkEndLine.substring(0, lineDelta) + checkEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(checkEndLine);
        assertTrue(lineOffset != -1);

        checkEndOffset = lineOffset + lineDelta;

        // First, make sure that all positions that are defined work symmetrically
        for (int i = 0; i < text.length(); i++) {
            int astOffset = translatedSource.getEmbeddedOffset(i);
            if (astOffset == -1) {
                continue;
            }
            int lexOffset = translatedSource.getOriginalOffset(astOffset);
            if (lexOffset == -1) {
                fail("Ast offset " + astOffset + " (for lexical position " + i + ") didn't map back properly; " + getSourceWindow(text, i));
            }
            if (lexOffset != i) {
                fail("Lexical position " + i + " mapped to ast offset " + astOffset + " and then mapped back to lexical " + lexOffset + " instead of " + i + "; " + getSourceWindow(text, i));
            }
        }

        // Next check the provided region to make sure we actually define AST offsets there

        for (int i = checkBeginOffset; i < checkEndOffset; i++) {
            int astOffset = translatedSource.getEmbeddedOffset(i);
            if (astOffset == -1) {
                fail("Lexical offset " + i + " didn't map to an ast offset; " + getSourceWindow(text, i));
            }
            // Probably not needed, this should follow from the first check section
            int lexOffset = translatedSource.getOriginalOffset(astOffset);
            if (lexOffset == -1) {
                fail("Ast offset " + astOffset + " (for lexical position " + i + ") didn't map back properly; " + getSourceWindow(text, i));
            }
            if (lexOffset != i) {
                fail("Lexical position " + i + " mapped to ast offset " + astOffset + " and then mapped back to lexical " + lexOffset + " instead of " + i + "; " + getSourceWindow(text, i));
            }
        }
    }

//    private Pair<JsTranslatedSource,String> checkIncrementalUpdate(String relFilePath, UpdateState expectedState, String... edits) throws Exception {
//        // TODO
//        // Translate source... then iterate through the source positions and assert
//        // that everything in the source matches. Also make sure that the stuff that
//        // doesn't match is properly placed...
//        TranslatedSource translatedSource = getTranslatedSource(relFilePath);
//        translatedSource.getSource(); // ensure initialized
//        String text = readFile(getTestFile(relFilePath));
//
//        Pair<EditHistory,String> pair = getEditHistory(text, edits);
//        EditHistory history = pair.getA();
//        String modifiedText = pair.getB();
//
//        assertTrue(translatedSource instanceof JsTranslatedSource);
//        JsTranslatedSource jts = (JsTranslatedSource)translatedSource;
//        UpdateState state = jts.incrementalUpdate(history);
//        assertEquals(expectedState, state);
//
//        if (state != UpdateState.FAILED) {
//            // Check that offsets are what they should be
//            // First, make sure that all positions that are defined work symmetrically
//            for (int i = 0; i < text.length(); i++) {
//                int astOffset = translatedSource.getAstOffset(i);
//                if (astOffset == -1) {
//                    continue;
//                }
//                int lexOffset = translatedSource.getLexicalOffset(astOffset);
//                if (lexOffset == -1) {
//                    fail("Ast offset " + astOffset + " (for lexical position " + i + ") didn't map back properly; " + getSourceWindow(text, i));
//                }
//                if (lexOffset != i) {
//                    fail("Lexical position " + i + " mapped to ast offset " + astOffset + " and then mapped back to lexical " + lexOffset + " instead of " + i + "; " + getSourceWindow(text, i));
//                }
//            }
//        }
//
//        // For additional checks
//        return new Pair<JsTranslatedSource,String>(jts, modifiedText);
//    }

    public void testJs1() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/rails-index.html");
    }

    public void testJs2() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/mixed.erb");
    }

    public void testJs3() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/fileinclusion.html");
    }

    public void testJs124916() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/embed124916.erb");
    }

    public void testYuiSample() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/yuisample.html");
    }

    public void testConvertScript() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/convertscript.html");
    }

    public void testSideEffects() throws Exception {
        // Scenario for 131667 - Overly aggressive "code has no side effects" warning
        checkJavaScriptTranslation("testfiles/embedding/sideeffects.html");
    }

    public void testIssue136495() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/issue136495.erb");
    }

    public void testPositions1() throws Exception {
        checkPositionTranslations("testfiles/embedding/rails-index.html", "f^unction about", " ^</script>");
    }

    public void testPositions2() throws Exception {
        checkPositionTranslations("testfiles/embedding/rails-index.html",
                "<script type=\"text/javascript\" src=\"javascripts/prototype.js\">^</script>",
                "<script type=\"text/javascript\" src=\"javascripts/prototype.js\">^</script>");
    }

    public void testInlinedJavascriptWithTemplatings() throws Exception {
        checkJavaScriptTranslation("testfiles/embedding/inlined-javascript-templating.rhtml");
    }

// XXX: parsingapi
//    public void testIncrementalUpdate1() throws Exception {
//        Pair<JsTranslatedSource,String> pair = checkIncrementalUpdate("testfiles/embedding/rails-index.html", UpdateState.COMPLETED,
//                "font-^family:", INSERT+"foo",
//                "pa^dding: 0", REMOVE+"dd"
//                );
//
//        // Check offsets
//        JsTranslatedSource source = pair.getA();
//        String text = pair.getB();
//
//        int offset = getCaretOffset(text, "window^.onload");
//        assertTrue(offset != -1);
//        int astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        int lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
//    }
//
//    public void testIncrementalUpdate2() throws Exception {
//        Pair<JsTranslatedSource,String> pair = checkIncrementalUpdate("testfiles/embedding/rails-index.html", UpdateState.UPDATED,
//                "function ^about", REMOVE+"about",
//                "function ^", INSERT+"aboooot"
//                );
//
//        // Check offsets
//        JsTranslatedSource source = pair.getA();
//        String text = pair.getB();
//
//        int offset = getCaretOffset(text, "^function aboo");
//        assertTrue(offset != -1);
//        int astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        int lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
//    }
//
//    public void testIncrementalUpdate3() throws Exception {
//        Pair<JsTranslatedSource,String> pair = checkIncrementalUpdate("testfiles/embedding/emptyattr.html", UpdateState.UPDATED,
//                "<input onclick=\"^\"/>", INSERT+"f"
//                );
//
//        // Check offsets
//        JsTranslatedSource source = pair.getA();
//        String text = pair.getB();
//
//        int offset = getCaretOffset(text, "<input onclick=\"f^\"/>");
//        assertTrue(offset != -1);
//        int astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        int lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
//
//        offset = getCaretOffset(text, "<input onclick=\"^f\"/>");
//        assertTrue(offset != -1);
//        astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
//    }
//
//    public void testIncrementalUpdate4() throws Exception {
//        // Edits outside should be completed without parse result updates
//        checkIncrementalUpdate("testfiles/embedding/emptyattr.html", UpdateState.COMPLETED,
//                "<input onclick=\"\"/^>", INSERT+"f"
//                );
//    }
//
//    public void testIncrementalUpdate5() throws Exception {
//        // Edits outside should be completed without parse result updates
//        checkIncrementalUpdate("testfiles/embedding/emptyattr.html", UpdateState.COMPLETED,
//                "<input onclick=^\"\"/>", INSERT+"f"
//                );
//    }
//
//    // Not sure about this one
//    //public void testIncrementalUpdate6() throws Exception {
//    //    // Edits outside should be completed without parse result updates
//    //    checkIncrementalUpdate("testfiles/embedding/emptyattr.html", UpdateState.COMPLETED,
//    //            "<input onclick=\"\"^/>", INSERT+"f"
//    //            );
//    //}
//
//    public void testIncrementalUpdate7() throws Exception {
//        // Edits outside should be completed without parse result updates
//        Pair<JsTranslatedSource,String> pair = checkIncrementalUpdate("testfiles/embedding/emptyattr.html", UpdateState.UPDATED,
//                "<form onsubmit=\"C^\"/>", INSERT+"D"
//                );
//
//        // Check offsets
//        JsTranslatedSource source = pair.getA();
//        String text = pair.getB();
//
//        int offset = getCaretOffset(text, "C^D");
//        assertTrue(offset != -1);
//        int astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        int lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
//
//        offset = getCaretOffset(text, "CD^");
//        assertTrue(offset != -1);
//        astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
//
//        // Ensure that D is the end of the block
//        int blockEnd = lexOffset + 1; // +1: The end-quote seem to be included
//        assertEquals(blockEnd, source.getLexicalOffset(astOffset+1));
//        assertEquals(blockEnd, source.getLexicalOffset(astOffset+2));
//        assertEquals(blockEnd, source.getLexicalOffset(astOffset+3));
//    }
//
//    // Test incremental updates: new areas such as <script>, onclick=, etc.
//    public void testIncrementalUpdate8() throws Exception {
//        String relFilePath = "testfiles/embedding/rails-index.html";
//        BaseDocument doc = getDocument(getTestFile(relFilePath));
//        final TranslatedSource ts = getTranslatedSource(doc, relFilePath);
//        assertNotNull(ts);
//
//        // Now apply some updates
//        final EditHistory history = new EditHistory();
//        getEditHistory(doc, history,
//                "background^-color: #f0f0f0;", INSERT+"d",
//                "backgroundd^-color: #f0f0f0;", REMOVE+"-"
//                );
//        // HACK -- events don't seem to get fired synchronously... I've tried
//        // EventQueue.invokeLater, overriding runInEq, and some other tricks
//        // but without success. For now, access it directly
//        history.testHelperNotifyToken(false, HTMLTokenId.STYLE);
//        history.testHelperNotifyToken(true, HTMLTokenId.STYLE);
//
//        assertTrue(history.wasModified(HTMLTokenId.STYLE));
//        assertTrue(!history.wasModified(HTMLTokenId.TEXT));
//        assertTrue(!history.wasModified(HTMLTokenId.SCRIPT));
//        assertTrue(!history.wasModified(HTMLTokenId.VALUE_JAVASCRIPT));
//
//        // Assert the translated source model is correctly updated
//        assertTrue(ts instanceof JsTranslatedSource);
//        JsTranslatedSource jts = (JsTranslatedSource)ts;
//        UpdateState state = jts.incrementalUpdate(history);
//        assertEquals(IncrementalEmbeddingModel.UpdateState.COMPLETED, state);
//    }
//
//    public void testIncrementalUpdate9() throws Exception {
//        // Insert a new <script> block near the top
//
//        String relFilePath = "testfiles/embedding/rails-index.html";
//        BaseDocument doc = getDocument(getTestFile(relFilePath));
//
//        final TranslatedSource ts = getTranslatedSource(doc, relFilePath);
//        assertNotNull(ts);
//
//
//        final EditHistory history = new EditHistory();
//        getEditHistory(doc, history,
//                "</title>^\n", INSERT+"<script>\nfunction foo() { }\n</script>"
//                );
//        // HACK -- events don't seem to get fired synchronously... I've tried
//        // EventQueue.invokeLater, overriding runInEq, and some other tricks
//        // but without success. For now, access it directly
//        history.testHelperNotifyToken(true, HTMLTokenId.SCRIPT);
//        history.testHelperNotifyToken(true, HTMLTokenId.TEXT);
//
//
//        assertTrue(history.wasModified(HTMLTokenId.TEXT)); // The \n before the script
//        assertTrue(history.wasModified(HTMLTokenId.SCRIPT));
//        assertTrue(!history.wasModified(HTMLTokenId.STYLE));
//        assertTrue(!history.wasModified(HTMLTokenId.VALUE_JAVASCRIPT));
//
//        // Assert the translated source model is correctly updated
//        assertTrue(ts instanceof JsTranslatedSource);
//        JsTranslatedSource jts = (JsTranslatedSource)ts;
//        UpdateState state = jts.incrementalUpdate(history);
//        // New JavaScript block -- can't update this incrementally (yet!)
//        assertEquals(IncrementalEmbeddingModel.UpdateState.FAILED, state);
//    }
}
