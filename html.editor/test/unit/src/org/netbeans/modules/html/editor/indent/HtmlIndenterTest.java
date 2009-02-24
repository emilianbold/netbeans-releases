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

package org.netbeans.modules.html.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.html.editor.HTMLKit;
import org.netbeans.modules.html.editor.test.TestBase2;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.indent.JspIndentTaskFactory;
import org.openide.util.Lookup;

/**
 *
 */
public class HtmlIndenterTest extends TestBase2 {

    public HtmlIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

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
    protected void configureIndenters(final BaseDocument document, final Formatter formatter,
            final CompilationInfo compilationInfo, boolean indentOnly, String mimeType) throws BadLocationException {
        // override it because I've already done in setUp()
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testFormatting() throws Exception {
        // misc broken HTML:
        format(
            "<html>\n<xbody>\n<h1>Hello World!</h1>\n<p>text\n</body>",
            "<html>\n    <xbody>\n        <h1>Hello World!</h1>\n        <p>text\n    </body>", null);
        format("<html>\n<body>\n<div>\nSome text\n<!--\n     Some comment\n       * bullet\n       * bullet2\n-->\n</div>\n</body>\n</html>\n",
               "<html>\n    <body>\n        <div>\n            Some text\n            <!--\n                 Some comment\n                   * bullet\n                   * bullet2\n            -->\n        </div>\n    </body>\n</html>\n", null);
        format("<html>\n<body>\n<pre>Some\ntext which\n  should not be formatted.\n \n </pre>\n</body>\n</html>\n",
               "<html>\n    <body>\n        <pre>Some\ntext which\n  should not be formatted.\n \n        </pre>\n    </body>\n</html>\n", null);
        format("<html>\n<head id=someid\nclass=class/>\n<body>",
               "<html>\n    <head id=someid\n          class=class/>\n    <body>",null);

        // there was assertion failure discovered by this test:
        format("<html>\n        <head>\n<title>Localized Dates</title></head>",
               "<html>\n    <head>\n        <title>Localized Dates</title></head>", null);
        // TODO: impl this:
//        format("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n\"http://www.w3.org/TR/html4/loose.dtd\">\n<table>",
//                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n        \"http://www.w3.org/TR/html4/loose.dtd\">\n<table>",null);

    }

    public void testFormattingHTML() throws Exception {
        reformatFileContents("testfiles/simple.html",new IndentPrefs(4,4));
    }

    public void testIndentation() throws Exception {
        insertNewline("  <html><table      color=aaa^", "  <html><table      color=aaa\n                    ^", null);
        // property tag indentation:
        insertNewline("<html>^<table>", "<html>\n    ^<table>", null);
        insertNewline("<html>^<table>\n<p>", "<html>\n    ^<table>\n<p>", null);
        insertNewline("<html>\n           <head>^", "<html>\n           <head>\n               ^", null);
        insertNewline("<html>\n           <body>^<table>", "<html>\n           <body>\n               ^<table>", null);
        insertNewline("<html><div/>^<table>", "<html><div/>\n    ^<table>", null);
        // tab attriutes indentation:
        insertNewline("<html><table^>", "<html><table\n        ^>", null);
        insertNewline("<html>^\n    <table>\n", "<html>\n    ^\n    <table>\n", null);

        // test that returning </body> tag matches opening one:
        insertNewline(
            "<html>\n  <body>\n        <h1>Hello World!</h1>\n                <p>text^</body>",
            "<html>\n  <body>\n        <h1>Hello World!</h1>\n                <p>text\n  ^</body>", null);
        insertNewline(
            "<html><body><table><tr>   <td>^</td></tr></table>",
            "<html><body><table><tr>   <td>\n                ^</td></tr></table>", null);
        insertNewline(
            "<html>\n    <body><table><tr>   <td>\n                ^</td></tr></table>",
            "<html>\n    <body><table><tr>   <td>\n                \n                ^</td></tr></table>", null);

        // misc invalid HTML doc formatting:
        insertNewline(
            "<html>\n    <xbody>\n        <h1>Hello World!</h1>\n        <p>text\n^</body>",
            "<html>\n    <xbody>\n        <h1>Hello World!</h1>\n        <p>text\n\n    ^</body>", null);

        // #149719
        insertNewline(
            "<tr>some text^\n</tr>",
            "<tr>some text\n    ^\n</tr>", null);

        // #120136
        insertNewline(
            "<meta ^http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">",
            "<meta \n    ^http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">", null);
    }

}
