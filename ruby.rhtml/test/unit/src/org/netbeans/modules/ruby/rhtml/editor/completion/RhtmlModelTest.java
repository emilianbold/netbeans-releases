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

package org.netbeans.modules.ruby.rhtml.editor.completion;

import java.io.File;
import java.util.Collections;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
//import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditHistory;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.editor.RubyEmbeddingProvider;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class RhtmlModelTest extends RubyTestBase {

    public RhtmlModelTest(String testName) {
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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

//    private TranslatedSource getTranslatedSource(String relFilePath) throws Exception {
//        File jsFile = new File(getDataDir(), relFilePath);
//        if (!jsFile.exists()) {
//            NbTestCase.fail("File " + jsFile + " not found.");
//        }
//
//        BaseDocument doc = getDocument(getTestFile(relFilePath));
//
//        return getTranslatedSource(doc, relFilePath);
//    }

//    private TranslatedSource getTranslatedSource(BaseDocument doc, String relFilePath) throws Exception {
//        File jsFile = new File(getDataDir(), relFilePath);
//        if (!jsFile.exists()) {
//            NbTestCase.fail("File " + jsFile + " not found.");
//        }
//        String RHTML_MIME_TYPE = RhtmlTokenId.MIME_TYPE;
//        String HTML_MIME_TYPE = HTMLKit.HTML_MIME_TYPE;
//
//        Language lexerLanguage;
//        String mimeType;
//        if (relFilePath.endsWith(".html")) {
//            mimeType = HTML_MIME_TYPE;
//            lexerLanguage = HTMLTokenId.language();
//        } else if (relFilePath.endsWith(".erb") || relFilePath.endsWith(".rhtml")) {
//            mimeType = RHTML_MIME_TYPE;
//            lexerLanguage = RhtmlTokenId.language();
//        } else {
//            fail("Unexpected file extension for " + relFilePath);
//            return null;
//        }
//
////        EmbeddingModel model = LanguageRegistry.getInstance().getEmbedding(RubyInstallation.RUBY_MIME_TYPE, mimeType);
////        assertNotNull(model);
//
//        doc.putProperty("mimeType", mimeType);
//        doc.putProperty(org.netbeans.api.lexer.Language.class, lexerLanguage);
//
//        Collection<? extends TranslatedSource> translations = model.translate(doc);
//        assertNotNull(translations);
//        assertEquals(1, translations.size());
//        TranslatedSource translatedSource = translations.iterator().next();
//
//        return translatedSource;
//    }
//
    private void checkEruby(String relFilePath) throws Exception {
        ParserResult pr = getParserResult(relFilePath);
        String generatedRuby = getTranslatedSource(relFilePath);

        assertDescriptionMatches(relFilePath, generatedRuby.toString(), false, ".rb");

        // Make sure the generated file doesn't have errors
        File rubyFile = new File(getDataDir(), relFilePath + ".rb");
        FileObject rubyFo = FileUtil.toFileObject(rubyFile);
        assertNotNull(rubyFo);
        assertNotNull("Parse error on translated source; " + pr.getDiagnostics(), pr);
        // Warnings are okay:
        //assertTrue(info.getErrors().toString(), info.getErrors().size() == 0);
        for (org.netbeans.modules.csl.api.Error error : pr.getDiagnostics()) {
            assertTrue(error.toString(), error.getSeverity() != Severity.ERROR);
        }
    }

    private String getTranslatedSource(String relFilePath) {
        Source source = Source.create(getTestFile(relFilePath));
        final String[] resultHolder = new String[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator ri) throws Exception {
                    resultHolder[0] = "";
                    for (Embedding each : ri.getEmbeddings()) {
                        if (RubyEmbeddingProvider.RUBY_MIMETYPE.equals(each.getMimeType())) {
                            resultHolder[0] += each.getSnapshot().getText();
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return resultHolder[0];

    }

    private void checkPositionTranslations(String relFilePath, String checkBeginLine, String checkEndLine) throws Exception {
        // TODO
        // Translate source... then iterate through the source positions and assert
        // that everything in the source matches. Also make sure that the stuff that
        // doesn't match is properly placed...
        
        ParserResult pr = getParserResult(relFilePath);
//        String text = getTranslatedSource(relFilePath);
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
            int astOffset = pr.getSnapshot().getOriginalOffset(i);
            if (astOffset == -1) {
                continue;
            }
            int lexOffset = pr.getSnapshot().getEmbeddedOffset(astOffset);
            if (lexOffset == -1) {
                fail("Ast offset " + astOffset + " (for lexical position " + i + ") didn't map back properly; " + getSourceWindow(text, i));
            }
            if (lexOffset != i) {
                fail("Lexical position " + i + " mapped to ast offset " + astOffset + " and then mapped back to lexical " + lexOffset + " instead of " + i + "; " + getSourceWindow(text, i));
            }
        }

        // Next check the provided region to make sure we actually define AST offsets there

        for (int i = checkBeginOffset; i < checkEndOffset; i++) {
            int astOffset = pr.getSnapshot().getOriginalOffset(i);
            if (astOffset == -1) {
                fail("Lexical offset " + i + " didn't map to an ast offset; " + getSourceWindow(text, i));
            }
            // Probably not needed, this should follow from the first check section
            int lexOffset = pr.getSnapshot().getEmbeddedOffset(i);
            if (lexOffset == -1) {
                fail("Ast offset " + astOffset + " (for lexical position " + i + ") didn't map back properly; " + getSourceWindow(text, i));
            }
            if (lexOffset != i) {
                fail("Lexical position " + i + " mapped to ast offset " + astOffset + " and then mapped back to lexical " + lexOffset + " instead of " + i + "; " + getSourceWindow(text, i));
            }
        }
    }

//    private Pair<RubyTranslatedSource,String> checkIncrementalUpdate(String relFilePath, UpdateState expectedState, String... edits) throws Exception {
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
//        assertTrue(translatedSource instanceof RubyTranslatedSource);
//        RubyTranslatedSource jts = (RubyTranslatedSource)translatedSource;
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
//        return new Pair<RubyTranslatedSource,String>(jts, modifiedText);
//    }

    public void testEruby() throws Exception {
        checkEruby("testfiles/conv.rhtml");
    }

    public void testEruby2() throws Exception {
        checkEruby("testfiles/test2.rhtml");
    }

    public void testEruby108990() throws Exception {
        checkEruby("testfiles/quotes.rhtml");
    }

    public void testEruby112877() throws Exception {
        checkEruby("testfiles/other-112877.rhtml");
    }
    public void testDashes121229() throws Exception {
        checkEruby("testfiles/dashes.rhtml");
    }

    public void testEruby146875() throws Exception {
        checkEruby("testfiles/issue146875.erb");
    }

    public void testPositions1() throws Exception {
        checkPositionTranslations("testfiles/conv.rhtml", "<li><%=^ link_to", "^ %></li>");
    }

    public void testPositions2() throws Exception {
        checkPositionTranslations("testfiles/conv.rhtml", "^<div id=\"page-nav\">", "<div id=\"page-nav\">^");
    }

    public void testIncrementalUpdate1() throws Exception {
        warn("not migrated to CSL - incremental upate not supported in CSL");
//        Pair<RubyTranslatedSource,String> pair = checkIncrementalUpdate("testfiles/conv.rhtml", UpdateState.UPDATED,
//                "Create ^new article", REMOVE+"new ",
//                "Create ^article", INSERT+"old"
//                );
//
//        // Check offsets
//        RubyTranslatedSource source = pair.getA();
//        String text = pair.getB();
//
//        int offset = getCaretOffset(text, "Create ^old");
//        assertTrue(offset != -1);
//        int astOffset = source.getAstOffset(offset);
//        assertTrue(astOffset != -1);
//        int lexOffset = source.getLexicalOffset(astOffset);
//        assertEquals(offset, lexOffset);
    }

    public void testIncrementalUpdate4() throws Exception {
        warn("not migrated to CSL - incremental upate not supported in CSL");
        // Edits outside should be completed without parse result updates
//        checkIncrementalUpdate("testfiles/conv.rhtml", UpdateState.UPDATED,
//                "^begin action", INSERT+"bbb"
//                );
    }

    public void testIncrementalUpdate8() throws Exception {
        String relFilePath = "testfiles/conv.rhtml";
        BaseDocument doc = getDocument(getTestFile(relFilePath));
//        final TranslatedSource ts = getTranslatedSource(doc, relFilePath);
//        assertNotNull(ts);

        ParserResult pr = getParserResult(relFilePath);
        // Now apply some updates
        final EditHistory history = new EditHistory();
        getEditHistory(doc, history,
                "<div id=\"page-nav\">^", INSERT+"<% foo %>"
                );
        // HACK -- events don't seem to get fired synchronously... I've tried
        // EventQueue.invokeLater, overriding runInEq, and some other tricks
        // but without success. For now, access it directly
        history.testHelperNotifyToken(true, RhtmlTokenId.DELIMITER);
        history.testHelperNotifyToken(true, RhtmlTokenId.RUBY);


        warn("not migrated to CSL - incremental upate not supported in CSL");
        // Assert the translated source model is correctly updated
//        assertTrue(ts instanceof RubyTranslatedSource);
//        RubyTranslatedSource jts = (RubyTranslatedSource)ts;
//        UpdateState state = jts.incrementalUpdate(history);
//        assertEquals(IncrementalEmbeddingModel.UpdateState.FAILED, state);
    }

    private void warn(String msg) {
        System.err.println(getName() + " - " + msg);
    }
}
