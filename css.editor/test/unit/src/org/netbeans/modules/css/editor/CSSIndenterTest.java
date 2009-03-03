/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor;

import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.formatting.api.support.AbstractIndenter;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.gsf.GsfTestBase.IndentPrefs;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.html.editor.HTMLKit;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.html.editor.coloring.EmbeddingUpdater;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.indent.JspIndentTaskFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

public class CSSIndenterTest extends TestBase {

    public CSSIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        NbReaderProvider.setupReaders();
        AbstractIndenter.inUnitTestRun = true;

        MockServices.setServices(TestLanguageProvider.class, MockMimeLookup.class);
        // init TestLanguageProvider
        Lookup.getDefault().lookup(TestLanguageProvider.class);
        TestLanguageProvider.register(CSSTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());

        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-css"), cssFactory);
        JspIndentTaskFactory jspReformatFactory = new JspIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), new JSPKit("text/x-jsp"), jspReformatFactory, new EmbeddingProviderImpl.Factory());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HTMLKit("text/x-jsp"));
        Reformatter.Factory factory = new Reformatter.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), factory);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
             DataObject dobj = DataObject.find(fo);
             assertNotNull(dobj);

             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
             assertNotNull(ec);

             return (BaseDocument)ec.openDocument();
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    @Override
    protected void configureIndenters(final BaseDocument document, final Formatter formatter,
            final CompilationInfo compilationInfo, boolean indentOnly, String mimeType) throws BadLocationException {
        // override it because I've already done in setUp()
    }

    private static class Listener extends EmbeddingUpdater {
        private Semaphore s;
        public Listener(Document doc, Semaphore s) throws InterruptedException {
            super(doc);
            this.s = s;
            s.acquire();
        }
        @Override
        public void parsingFinished(List<SyntaxElement> elements) {
            super.parsingFinished(elements);
            s.release();
        }

    }

    public void testFormatting() throws Exception {
        format("a{\nbackground: red,\nblue;\n  }\n",
               "a{\n    background: red,\n        blue;\n}\n", null);
        format("a{     background: green,\nyellow;\n      }\n",
               "a{     background: green,\n           yellow;\n}\n", null);

        // comments formatting:
        format("a{\n/*comme\n  *dddsd\n nt*/\nbackground: red;\n}",
               "a{\n    /*comme\n      *dddsd\n     nt*/\n    background: red;\n}", null);

        // even though lines are preserved they will be indented according to indent of previous line;
        // I'm not sure it is OK but leaving like that for now:
        format("a{\ncolor: red; /* start\n   comment\n end*/ background: blue;\n}",
               "a{\n    color: red; /* start\n       comment\n     end*/ background: blue;\n}", null);

        // formatting of last line:
        format("a{\nbackground: red,",
               "a{\n    background: red,", null);
        format("a{\nbackground: red,\n",
               "a{\n    background: red,\n", null);
    }

    private void forceHTMLParsingAndWait(String file, String mimeType, Language language) throws Exception {
        FileObject fo = getTestFile(file);
        BaseDocument doc = getDocument(fo, mimeType, language);
        LanguagePath htmlLP = LanguagePath.get(language);
        Semaphore s = new Semaphore(1);
        Listener l = new Listener(doc, s);
        SyntaxParser.get(doc, htmlLP).addSyntaxParserListener(l);
        s.acquire();
        s.release();
    }

    public void testNativeEmbeddingFormattingCase1() throws Exception {
        forceHTMLParsingAndWait("testfiles/format1.html", "text/html", HTMLTokenId.language());
        reformatFileContents("testfiles/format1.html", "text/html", HTMLTokenId.language(), new IndentPrefs(4,4));
    }

    public void testNativeEmbeddingFormattingCase2() throws Exception {
        forceHTMLParsingAndWait("testfiles/format2.html", "text/html", HTMLTokenId.language());
        reformatFileContents("testfiles/format2.html", "text/html", HTMLTokenId.language(), new IndentPrefs(4,4));
    }

    public void testFormattingNetBeansCSS() throws Exception {
        reformatFileContents("testfiles/netbeans.css",new IndentPrefs(4,4));
    }

    public void testIndentation() throws Exception {
        // property indentation:
        insertNewline("a{^background: red;\n  }\n", "a{\n    ^background: red;\n  }\n", null);
        insertNewline("a{\n    background: red;\n}\na{\n    background: red;^\n}", "a{\n    background: red;\n}\na{\n    background: red;\n    ^\n}", null);
        insertNewline("a{background: red;}\nb{^", "a{background: red;}\nb{\n    ^", null);
        insertNewline("a {       background: red;^a: b;", "a {       background: red;\n          ^a: b;", null);
        insertNewline("a {       background: red,^blue", "a {       background: red,\n              ^blue", null);
        // value indentation:
        insertNewline("a{\n    background:^red;\n  }\n", "a{\n    background:\n        ^red;\n  }\n", null);
        insertNewline("a { background: red,^blue; }", "a { background: red,\n        ^blue; }", null);
        // indentation of end of line:
        insertNewline("a { background: red,^", "a { background: red,\n        ^", null);
        insertNewline("a { background: red,^\n", "a { background: red,\n        ^\n", null);
        // property indentation:
        insertNewline("a{\n    background: red;^\n  }\n", "a{\n    background: red;\n    ^\n  }\n", null);
        // new rule indentation:
        insertNewline("a{\n    background: red;\n  }^", "a{\n    background: red;\n  }\n  ^", null);
        // check that indentation cooperates with bracket insertion:
        insertNewline("a{^}", "a{\n    ^\n}", null);
    }

}
