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

package org.netbeans.modules.web.core.syntax.formatting;

import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.core.GsfIndentTaskFactory;
import org.netbeans.modules.csl.core.GsfParserFactory;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.formatting.api.support.AbstractIndenter;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.html.editor.gsf.embedding.CssEmbeddingProvider;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.java.source.parsing.ClassParserFactory;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.javascript.editing.embedding.JsEmbeddingProvider;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.gsf.JspEmbeddingProvider;
import org.netbeans.modules.web.core.syntax.indent.ExpressionLanguageIndentTaskFactory;
import org.netbeans.modules.web.core.syntax.indent.JspIndentTaskFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.test.web.core.syntax.TestBase2;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class JspIndenterTest extends TestBase2 {

    private static TestLanguageProvider testLanguageProvider = null;

    public JspIndenterTest(String name) {
        super(name);
        if (testLanguageProvider == null) {
            testLanguageProvider = new TestLanguageProvider();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new TestClassPathProvider(createClassPaths()), testLanguageProvider);
        initParserJARs();
        NbReaderProvider.setupReaders();
        AbstractIndenter.inUnitTestRun = true;

        // init TestLanguageProvider
        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        TestLanguageProvider.register(CssTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());
        TestLanguageProvider.register(JsTokenId.language());

        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-css"), cssFactory);
        JspIndentTaskFactory jspReformatFactory = new JspIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), new JspKit("text/x-jsp"), jspReformatFactory, new EmbeddingProviderImpl.Factory(), new JspEmbeddingProvider.Factory());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"));
        Reformatter.Factory factory = new Reformatter.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), factory, new JavacParserFactory(), new ClassParserFactory());
        ExpressionLanguageIndentTaskFactory elReformatFactory = new ExpressionLanguageIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-el"), elReformatFactory);
        GsfIndentTaskFactory jsFactory = new GsfIndentTaskFactory();
        // adding javascript formatter makes some tests randomly fail;
        // for now I'm disabling
        //MockMimeLookup.setInstances(MimePath.parse("text/javascript"), jsFactory, new GsfParserFactory());
        MockMimeLookup.setInstances(MimePath.EMPTY, new JsEmbeddingProvider.Factory(), new CssEmbeddingProvider.Factory());
    }

    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because I've already done in setUp()
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
    protected boolean runInEQ() {
        return true;
    }

    private Map<String, ClassPath> createClassPaths() throws Exception {
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        ClassPath cp = createServletAPIClassPath();
        cps.put(ClassPath.COMPILE, cp);
        return cps;
    }

    private class TestClassPathProvider implements ClassPathProvider {
        private Map<String, ClassPath> map;
        public TestClassPathProvider(Map<String, ClassPath> map) {
            this.map = map;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            if (map != null) {
                return map.get(type);
            } else {
                return null;
            }
        }
    }


    public void testFormattingCase001() throws Exception {
        reformatFileContents("testfilesformatting/case001.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase002() throws Exception {
        reformatFileContents("testfilesformatting/case002.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase003() throws Exception {
        reformatFileContents("testfilesformatting/case003.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase004() throws Exception {
        reformatFileContents("testfilesformatting/case004.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase005() throws Exception {
        reformatFileContents("testfilesformatting/case005.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase006() throws Exception {
        reformatFileContents("testfilesformatting/case006.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase007() throws Exception {
        reformatFileContents("testfilesformatting/case007.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase008() throws Exception {
        reformatFileContents("testfilesformatting/case008.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase009() throws Exception {
        reformatFileContents("testfilesformatting/case009.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase010() throws Exception {
        reformatFileContents("testfilesformatting/case010.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase011() throws Exception {
        reformatFileContents("testfilesformatting/case011.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase012() throws Exception {
        reformatFileContents("testfilesformatting/case012.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue121102() throws Exception {
        reformatFileContents("testfilesformatting/issue121102.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue129778() throws Exception {
        reformatFileContents("testfilesformatting/issue129778.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue89174() throws Exception {
        reformatFileContents("testfilesformatting/issue89174.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue160098() throws Exception {
        reformatFileContents("testfilesformatting/issue160098.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue160103() throws Exception {
        reformatFileContents("testfilesformatting/issue160103.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue160527() throws Exception {
        reformatFileContents("testfilesformatting/issue160527.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue162017() throws Exception {
        reformatFileContents("testfilesformatting/issue162017.jsp",new IndentPrefs(4,4));
    }

    public void testIndentation() throws Exception {
//        insertNewline("<style>\n     h1 {\n        <%= System.\n   somth() ^%>",
//                      "<style>\n     h1 {\n        <%= System.\n   somth() \n        ^%>", null);

        //#160092:
        insertNewline("^<html>\n</html>\n", "\n^<html>\n</html>\n", null);

        insertNewline("<jsp:useBean>^", "<jsp:useBean>\n    ^", null);
        insertNewline("^<jsp:body>", "\n^<jsp:body>", null);

        insertNewline("<jsp:body>\n    <html>^", "<jsp:body>\n    <html>\n        ^", null);
        insertNewline("<jsp:body>\n^<html>", "<jsp:body>\n\n    ^<html>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n            ^", null);
        insertNewline("<jsp:body>\n    <html>\n^<jsp:useBean>", "<jsp:body>\n    <html>\n\n        ^<jsp:useBean>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n            <table>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n            <table>\n                ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n^<table>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n\n            ^<table>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n        ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>a\n^</jsp:useBean>", "<jsp:body>\n    <html>\n        <jsp:useBean>a\n\n        ^</jsp:useBean>", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>a^</jsp:useBean>", "<jsp:body>\n    <html>\n        <jsp:useBean>a\n        ^</jsp:useBean>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    </html>^",
                      "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    </html>\n    ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^</html>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    ^</html>", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^<table>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n        ^<table>", null);

        insertNewline("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\"\n        prefix=\"c\" %>^",
                      "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\"\n        prefix=\"c\" %>\n^",null);

        // TODO: impl matching of INDENT/RETURN and use it to properly match incorrect document:
        //insertNewline("<jsp:body>\n    <html>^</jsp:body>", "<jsp:body>\n    <html>\n^</jsp:body>", null);

        insertNewline("<!--\n   comment\n^-->\n", "<!--\n   comment\n\n^-->\n", null);
        insertNewline(
            "<html> <!--^comment",
            "<html> <!--\n       ^comment", null);
        insertNewline(
            "<html> <!--\n             ^comment",
            "<html> <!--\n             \n       ^comment", null);

        // expression indentation:
        insertNewline(
            "<html>\n    ${\"expression+\n           exp2\"}^",
            "<html>\n    ${\"expression+\n           exp2\"}\n    ^", null);
        insertNewline(
            "<html>\n    some text ${\"expression+\n                         exp2\"^}",
            "<html>\n    some text ${\"expression+\n                         exp2\"\n    ^}", null);
        insertNewline(
            "<html>\n    ${\"expression+\n           exp2\"\n                }^",
            "<html>\n    ${\"expression+\n           exp2\"\n                }\n                ^", null);

// #128034
//        insertNewline(
//            "<a href=\"${path}\">^</a>",
//            "<a href=\"${path}\">\n    ^\n</a>", null);

// broken: html indenter eliminates SCRIPT tags and previous line (containing wrongly formatted JS)
//         is then used to calcualte line-adjustment causing wrong indentation:
//        insertNewline("<html>\n    <head>\n        <script type=\"text/javascript\">\n            function a() {\n                <%%>\n            }\n        </script>^",
//                      "<html>\n    <head>\n        <script type=\"text/javascript\">\n            function a() {\n                <%%>\n            }\n        </script>\n        ^", null);
   }

}