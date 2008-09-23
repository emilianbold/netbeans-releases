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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.editor.ext.html.parser.SyntaxParserListener;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Marek Fukala
 */
public class HtmlEmbeddingModelTest extends TestBase {

    public HtmlEmbeddingModelTest(String name) {
        super(name);
    }
    
    private Document document;

    @Override
    public void setUp() {
        try {
            super.setUp();

            FileSystem memFS = FileUtil.createMemoryFileSystem();
            FileObject fo = memFS.getRoot().createData("test", "html");
            assertNotNull(fo);

            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);

            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
            assertNotNull(cookie);

            document = cookie.openDocument();
            assertEquals(0, document.getLength());

            LanguageRegistry registry = LanguageRegistry.getInstance();
            Language l = registry.getLanguageByMimeType("text/html");
            assertNotNull(l);
        } catch (IOException ex) {
            throw new IllegalStateException("Error setting up tests", ex);
        }

    }

    public void testVirtualSourceOnPureHTML() throws BadLocationException, IOException {
        String content = "<html><head></head><body>hello</body></html>";
        setDocumentText(content);
        
        Source source = Source.forDocument(document);
        final ParserResult[] result = new ParserResult[1];
        source.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.PARSED);
                result[0] = parameter.getEmbeddedResult("text/html", 0);
            }
        }, false);
        
        ParserResult pr = result[0];
        
        assertNotNull(pr);
        
        TranslatedSource translatedSource = pr.getTranslatedSource();
        assertNull(translatedSource);
        
        
    }
    
    private void setDocumentText(String text) throws BadLocationException {
        document.remove(0, document.getLength());
        document.insertString(0, text, null);
    }
    
}
