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
import org.netbeans.modules.html.editor.test.TestBase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Marek Fukala
 */
public class EmbeddingUpdaterTest extends TestBase {

    public EmbeddingUpdaterTest(String name) {
        super(name);
    }

    public void testLazyEmbeddingCreation() throws IOException, BadLocationException, InterruptedException {
        FileSystem memFS = FileUtil.createMemoryFileSystem();
        FileObject fo = memFS.getRoot().createData("test", "html");
        assertNotNull(fo);

        DataObject dobj = DataObject.find(fo);
        assertNotNull(dobj);

        EditorCookie cookie = dobj.getCookie(EditorCookie.class);
        assertNotNull(cookie);

        Document document = cookie.openDocument();
        assertEquals(0, document.getLength());

        LanguageRegistry registry = LanguageRegistry.getInstance();
        Language l = registry.getLanguageByMimeType("text/html");
        assertNotNull(l);

        final long start = System.currentTimeMillis();
        
        //wait for the parser to finish so we can shorten the delay during
        //we wait for the embedding to be created.
        SyntaxParser parser = SyntaxParser.get(document, LanguagePath.get(HTMLTokenId.language()));
        final Object lock = new Object();
        parser.addSyntaxParserListener(new SyntaxParserListener() {
            public void parsingFinished(List<SyntaxElement> elements) {
                synchronized (lock) {
                    System.out.println("parsed in " + (System.currentTimeMillis() - start) + "ms.");
                    //wait for a while so the probability that the embedding updater haven't run yet is smaller
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                    lock.notifyAll();
                }
            }
        });
        document.insertString(0, "<a href=\"javascript:alert('hello')\"/> <div style=\"color: red\"/>", null);
        //                        0123456789012345678901234567890123456789012345678901234567890123456789
        //                        0         1         2         3         4         5         6
        
        synchronized (lock) {
            lock.wait(5000); //5 sec timeout
        }
        
        //test the embeddings
        TokenHierarchy th = TokenHierarchy.get(document);
        List<TokenSequence> embedded = th.embeddedTokenSequences(22, false);
        boolean found = false;
        for (TokenSequence ts : embedded) {
            if (ts.language().mimeType().equals("text/javascript")) {
                found = true;
                break;
            }
        }

        assertTrue("No javascript embedding created", found);

        embedded = th.embeddedTokenSequences(52, false);
        found = false;
        for (TokenSequence ts : embedded) {
            if (ts.language().mimeType().equals("text/x-css")) {
                found = true;
                break;
            }
        }

        assertTrue("No css embedding created", found);

    }
}
