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
import java.io.FileReader;
import java.nio.CharBuffer;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.JsTestBase;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class JsModelTest extends JsTestBase {

    public JsModelTest(String testName) {
        super(testName);
    }

//    @SuppressWarnings(value = "unchecked")
//    private static String rhtmlToJs(String rhtml) {
//        TokenHierarchy hi = TokenHierarchy.create(rhtml, RhtmlTokenId.language());
//        TokenSequence<RhtmlTokenId> ts = hi.tokenSequence();
//
//        JsModel model = new JsModel(null);
//
//        StringBuilder buffer = new StringBuilder();
//        model.extractJavaScriptFromRhtml(ts, buffer);
//
//        return buffer.toString();
//    }
    
    @SuppressWarnings(value = "unchecked")
    private String codeToJs(String code, String mimeType) {
//        Language<?> language = LanguageManager.getInstance().findLanguage(mimeType);
        Language<?> language = null;
        if (mimeType.equals(HTMLKit.HTML_MIME_TYPE)) {
            language = HTMLTokenId.language();
        } else if (mimeType.equals(RhtmlTokenId.MIME_TYPE)) {
            language = RhtmlTokenId.language();
        } else {
            fail("Unexpected mimetype " + mimeType);
        }

        TokenHierarchy hi = TokenHierarchy.create(code, language);
        TokenSequence<HTMLTokenId> ts = hi.tokenSequence();

        BaseDocument doc = getDocument(code);
        JsModel model = JsModel.get(doc);

        StringBuilder buffer = new StringBuilder();
        if (mimeType.equals(HTMLKit.HTML_MIME_TYPE)) {
            doc.putProperty("mimeType", HTMLKit.HTML_MIME_TYPE);
            model.extractJavaScriptFromHtml(ts, buffer, false);
        } else if (mimeType.equals(RhtmlTokenId.MIME_TYPE)) {
            doc.putProperty("mimeType", RhtmlTokenId.MIME_TYPE);
            model.extractJavaScriptFromRhtml(ts, buffer);
        } else {
            fail("Unexpected mimetype " + mimeType);
        }

        return buffer.toString();

        // Depend on RhtmlTokenId?

        // Must adjust this test to cope with RHTML, JSP, etc.
//        BaseDocument doc = getDocument(code, mimeType);
//        
//        return JsModel.get(doc).getJsCode();
    }
    
    
    private void checkJavaScript(NbTestCase test, String relFilePath) throws Exception {
        File jsFile = new File(test.getDataDir(), relFilePath);
        if (!jsFile.exists()) {
            NbTestCase.fail("File " + jsFile + " not found.");
        }
        String js = readFile(test, jsFile);

        String RHTML_MIME_TYPE = RhtmlTokenId.MIME_TYPE;
        String HTML_MIME_TYPE = HTMLKit.HTML_MIME_TYPE;
        
        String mimeType;
        if (relFilePath.endsWith(".html")) {
            mimeType = HTML_MIME_TYPE;
        } else if (relFilePath.endsWith(".erb") || relFilePath.endsWith(".rhtml")) {
            mimeType = RHTML_MIME_TYPE;
        } else {
            fail("Unexpected file extension for " + relFilePath);
            return;
        }
        
        String generatedRuby = codeToJs(js, mimeType);

        assertDescriptionMatches(relFilePath, generatedRuby.toString(), false, ".js");
        
        // Make sure the generated file doesn't have errors
        File rubyFile = new File(test.getDataDir(), relFilePath + ".js");
        FileObject rubyFo = FileUtil.toFileObject(rubyFile);
        assertNotNull(rubyFo);
        CompilationInfo info = getInfo(rubyFo);
        assertNotNull(info);
        assertNotNull("Parse error on translated source", AstUtilities.getRoot(info));
        assertTrue(info.getErrors().size() == 0);
    }

    private static String readFile(NbTestCase test, File f) throws Exception {
        FileReader r = new FileReader(f);
        int fileLen = (int)f.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        return cb.toString();
    }

    public void testJs1() throws Exception {
        checkJavaScript(this, "testfiles/embedding/rails-index.html");
    }

    public void testJs2() throws Exception {
        checkJavaScript(this, "testfiles/embedding/mixed.erb");
    }

    public void testJs3() throws Exception {
        checkJavaScript(this, "testfiles/embedding/fileinclusion.html");
    }

    public void testJs124916() throws Exception {
        checkJavaScript(this, "testfiles/embedding/embed124916.erb");
    }
}
