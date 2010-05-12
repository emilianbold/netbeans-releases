/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.rhtml.editor;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
//import org.netbeans.modules.gsf.api.CompilationInfo;
//import org.netbeans.modules.gsf.api.Formatter;
//import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.ruby.RubyFormatter;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.RhtmlIndentTaskFactory;
import org.netbeans.modules.ruby.rhtml.RhtmlLanguage;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlKitTest extends RubyTestBase {
    
    public RhtmlKitTest(String testName) {
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

        RhtmlIndentTaskFactory rhtmlReformatFactory = new RhtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse(RubyInstallation.RHTML_MIME_TYPE), rhtmlReformatFactory);
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory);
    }

//    @Override
//    protected void configureIndenters(final BaseDocument document, final Formatter formatter,
//            final CompilationInfo compilationInfo, boolean indentOnly, String mimeType) throws BadLocationException {
//        super.configureIndenters(null, new RubyFormatter(), null, true, RubyInstallation.RUBY_MIME_TYPE);
//    }

    @Override
    public BaseDocument getDocument(String s, final String mimeType, final Language language) {
        BaseDocument doc = super.getDocument(s, mimeType, language);
        doc.putProperty("mimeType", RubyInstallation.RHTML_MIME_TYPE);
        doc.putProperty(org.netbeans.api.lexer.Language.class, RhtmlTokenId.language());

        return doc;
    }

    @Override
    protected String getPreferredMimeType() {
        return RubyInstallation.RHTML_MIME_TYPE;
    }

    @Override
    protected BaseKit getEditorKit(String mimeType) {
        return new RhtmlKit();
    }


    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new RhtmlLanguage();
    }


    private void insertChar(String original, char insertText, String expected) throws BadLocationException, Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws BadLocationException, Exception {
        insertChar(original, insertText, expected, selection, false);
    }
    public void testInsertTag() throws Exception {
        insertChar("<^", '%', "<%^%>");
    }

    public void testInsertTag2a() throws Exception {
        insertChar("<^\n", '%', "<%^%>\n");
    }

    public void testInsertTag2b() throws Exception {
        insertChar("<%^", '%', "<%%^");
    }

    public void testInsertTag2c() throws Exception {
        insertChar("<^f\n", '%', "<%^f\n");
    }

    public void testInsertTag3() throws Exception {
        insertChar("<%%^", '>', "<%%>^");
    }

    public void testInsertTag4() throws Exception {
        insertChar("<%^ ", '%', "<%%^ ");
    }

    // Fails - problem with Ruby completion itself
    //public void testInsertTag5() throws Exception {
    //    insertChar("<%%^ ", '>', "<%%>^ ");
    //}

    public void testInsertTag5b() throws Exception {
        insertChar("<%#%^ ", '>', "<%#%>^ ");
    }
    public void testInsertTag6() throws Exception {
        insertChar("<%^% ", '%', "<%%^% ");
    }

    public void testInsertTag8() throws Exception {
        insertChar("<%^%>", '%', "<%%^>");
    }

    public void testInsertTag9() throws Exception {
        insertChar("<%%^>", '>', "<%%>^");
    }

    public void testInsertTag10() throws Exception {
        insertChar("<%%^> ", '>', "<%%>^ ");
    }

    public void testInsertTag11() throws Exception {
        insertChar("<%foo^%>", '%', "<%foo%^>");
    }

    public void testInsertTag12() throws Exception {
        insertChar("<%foo%^>", '>', "<%foo%>^");
    }

    public void testInsertTag13() throws Exception {
        insertChar("<%foo%^> ", '>', "<%foo%>^ ");
    }

    public void testDeleteTag() throws Exception {
        deleteChar("<%^%>", "<^");
    }

    public void testDeleteTag2() throws Exception {
        deleteChar("<%^%> ", "<^ ");
    }

    public void testDeleteTag3() throws Exception {
        deleteChar("<%^%><div>", "<^<div>");
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testDeleteX() throws Exception {
        deleteChar("cl^ass", "c^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testInsertX3() throws Exception {
        insertChar("<% ^ %>", '"', "<% \"^\" %>");
    }

    public void testInsertX4() throws Exception {
        insertChar(" ^ ", '"', " \"^ ");
    }

    public void testDeleteX3() throws Exception {
        deleteChar("<% \"^\" %>", "<% ^ %>");
    }

    public void testNoMatchInComments() throws Exception {
        insertChar("<% # Hello^ %>", '\'', "<% # Hello'^ %>");
        insertChar("<% # Hello^ %>", '"', "<% # Hello\"^ %>");
        insertChar("<% # Hello^ %>", '[', "<% # Hello[^ %>");
        insertChar("<% # Hello^ %>", '(', "<% # Hello(^ %>");
    }

    public void testSingleQuotes1() throws Exception {
        insertChar("<% x = ^ %>", '\'', "<% x = '^' %>");
    }

    public void testSingleQuotes2() throws Exception {
        insertChar("<% x = '^' %>", '\'', "<% x = ''^ %>");
    }

    public void testSingleQuotes3() throws Exception {
        insertChar("<% x = '^' %>", 'a', "<% x = 'a^' %>");
    }

    public void testSingleQuotes4() throws Exception {
        insertChar("<% x = '\\^' %>", '\'', "<% x = '\\'^' %>");
    }

    public void testDoubleQuotes1() throws Exception {
        insertChar("<% x = ^ %>", '"', "<% x = \"^\" %>");
    }

    public void testBrackets1() throws Exception {
        insertChar("<% x = ^ %>", '[', "<% x = [^] %>");
    }

    public void testBrackets2() throws Exception {
        insertChar("<% x = [^] %>", ']', "<% x = []^ %>");
    }

    public void testBracketsSpecialName() throws Exception {
        insertChar("<% def ^ %>", '[', "<% def [^] %>");
    }

    public void testBracketsSpecialName2() throws Exception {
        insertChar("<% def [^] %>", ']', "<% def []^ %>");
    }

    public void testBrackets3() throws Exception {
        insertChar("<% x = [^] %>", 'a', "<% x = [a^] %>");
    }

    public void testBrackets4() throws Exception {
        insertChar("<% x = [^] %>", '[', "<% x = [[^]] %>");
    }

    public void testBrackets5() throws Exception {
        insertChar("<% x = [[^]] %>", ']', "<% x = [[]^] %>");
    }

    public void testBrackets6() throws Exception {
        insertChar("<% x = [[]^] %>", ']', "<% x = [[]]^ %>");
    }

    public void testParens1() throws Exception {
        insertChar("<% x = ^ %>", '(', "<% x = (^) %>");
    }

    public void testParens2() throws Exception {
        insertChar("<% x = (^) %>", ')', "<% x = ()^ %>");
    }

    public void testParens3() throws Exception {
        insertChar("<% x = (^) %>", 'a', "<% x = (a^) %>");
    }

    public void testParens4() throws Exception {
        insertChar("<% x = (^) %>", '(', "<% x = ((^)) %>");
    }

    public void testParens5() throws Exception {
        insertChar("<% x = ((^)) %>", ')', "<% x = (()^) %>");
    }

    public void testParens6() throws Exception {
        insertChar("<% x = (()^) %>", ')', "<% x = (())^ %>");
    }

    public void testRegexp1() throws Exception {
        insertChar("<% x = ^ %>", '/', "<% x = /^/ %>");
    }

    public void testRegexp2() throws Exception {
        insertChar("<% x = /^/ %>", '/', "<% x = //^ %>");
    }

    public void testRegexp3() throws Exception {
        insertChar("<% x = /^/ %>", 'a', "<% x = /a^/ %>");
    }

    public void testRegexp4() throws Exception {
        insertChar("<% x = /\\^/ %>", '/', "<% x = /\\/^/ %>");
    }

    public void testRegexp5() throws Exception {
        insertChar("<%     regexp = /fofo^\n      # Subsequently, you can make calls to it by name with <tt>yield</tt> in %>", '/',
                "<%     regexp = /fofo/^\n      # Subsequently, you can make calls to it by name with <tt>yield</tt> in %>");
    }

    public void testRegexp6() throws Exception {
        insertChar("<%     regexp = /fofo^\n %>", '/',
                "<%     regexp = /fofo/^\n %>");
    }

    public void testRegexp7() throws Exception {
        insertChar("<% x = ^\n %>", '/', "<% x = /^/\n %>");
    }

    public void testRegexp8() throws Exception {
        insertChar("<% x = /^/\n %>", '/', "<% x = //^\n %>");
    }

    public void testRegexp9() throws Exception {
        insertChar("<% x = /^/\n %>", 'a', "<% x = /a^/\n %>");
    }

    public void testRegexp10() throws Exception {
        insertChar("<% x = /\\^/\n %>", '/', "<% x = /\\/^/\n %>");
    }

    public void testRegexp11() throws Exception {
        insertChar("<% /foo^ %>", '/',
                "<% /foo/^ %>");
    }

    public void testNotRegexp1() throws Exception {
        insertChar("<% x = 10 ^ %>", '/', "<% x = 10 /^ %>");
    }

    public void testNotRegexp2() throws Exception {
        insertChar("<% x = 3.14 ^ %>", '/', "<% x = 3.14 /^ %>");
    }

    public void testNotRegexp4() throws Exception {
        insertChar("<% x = y^ %>", '/', "<% x = y/^ %>");
    }

    public void testRegexpPercent1() throws Exception {
        insertChar("<% x = %r^ %>", '(', "<% x = %r(^) %>");
    }

    public void testRegexpPercent2() throws Exception {
        insertChar("<% x = %r(^) %>", ')', "<% x = %r()^ %>");
    }

    public void testSinglePercent1() throws Exception {
        insertChar("<% x = %q^ %>", '(', "<% x = %q(^) %>");
    }

    public void testSinglePercent2() throws Exception {
        insertChar("<% x = %q(^) %>", ')', "<% x = %q()^ %>");
    }

    public void testSinglePercent5() throws Exception {
        insertChar("<% x = %q((^)) %>", 'a', "<% x = %q((a^)) %>");
    }

    public void testSinglePercent6() throws Exception {
        insertChar("<% x = %q^ %>", '-', "<% x = %q-^- %>");
    }

    public void testSinglePercent7() throws Exception {
        insertChar("<% x = %q-^- %>", '-', "<% x = %q--^ %>");
    }

    public void testSinglePercent8() throws Exception {
        insertChar("<% x = %q^ %>", ' ', "<% x = %q ^  %>");
    }

    public void testSinglePercent10() throws Exception {
        insertChar("<% x = %q ^  %>", 'x', "<% x = %q x^  %>");
    }

    public void testSinglePercent11() throws Exception {
        insertChar("<% x = %q-\\^- %>", '-', "<% x = %q-\\-^- %>");
    }

    public void testNoInsertPercentElsewhere() throws Exception {
        insertChar("<% x = ^ %>", '#', "<% x = #^ %>");
    }

    public void testInsertPercentInRegexp() throws Exception {
        insertChar("<% x = /foo ^/ %>", '#', "<% x = /foo #{^}/ %>");
    }

    public void testInsertPercentInRegexp2() throws Exception {
        // Make sure type-through works
        insertChar("<% x = /foo #{^}/ %>", '}', "<% x = /foo #{}^/ %>");
    }

    public void testInsertPercentInRegexp3() throws Exception {
        insertChar("<% x = /foo #{^}/ %>", '{', "<% x = /foo #{^}/ %>");
    }

    public void testInsertPercentInRegexp4() throws Exception {
        insertChar("<% x = /foo #{^a}/ %>", '}', "<% x = /foo #{}^a}/ %>");
    }

    public void testInsertPercentInRegexp5() throws Exception {
        insertChar("<% x = /foo {^}/ %>", '}', "<% x = /foo {}^}/ %>");
    }

    public void testInsertPercentInRegexp6() throws Exception {
        insertChar("<% x = /foo {^}/ %>", '{', "<% x = /foo {{^}/ %>");
    }

    public void testReplaceSelection1() throws Exception {
        insertChar("<% x = foo^ %>", 'y', "<% x = y^ %>", "foo");
    }

    public void testReplaceSelection2() throws Exception {
        insertChar("<% x = foo^ %>", '"', "<% x = \"foo\"^ %>", "foo");
    }

    public void testReplaceSelection3() throws Exception {
        insertChar("<% x = \"foo^bar\" %>", '#', "<% x = \"#{foo}^bar\" %>", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("<% x = 'foo^bar' %>", '#', "<% x = '#^bar' %>", "foo");
    }

    public void testReplaceCommentSelectionBold() throws Exception {
        insertChar("<% # foo^ %>", '*', "<% # *foo*^ %>", "foo");
    }

    public void testReplaceCommentSelectionTerminal() throws Exception {
        insertChar("<% # foo^ %>", '+', "<% # +foo+^ %>", "foo");
    }

    public void testReplaceCommentSelectionItalic() throws Exception {
        insertChar("<% # foo^ %>", '_', "<% # _foo_^ %>", "foo");
    }

    public void testReplaceCommentSelectionWords() throws Exception {
        // No replacement if it contains multiple lines
        insertChar("<% # foo bar^ %>", '*', "<% # *^ %>", "foo bar");
    }

    public void testReplaceCommentOther() throws Exception {
        // No replacement if it's not one of the three chars
        insertChar("<% # foo^ %>", 'x', "<% # x^ %>", "foo");
    }

    public void test108889() throws Exception {
        // Reproduce 108889: AIOOBE and AE during editing
        // NOTE: While the test currently throws an exception, when the
        // exception is fixed the test won't actually pass; that's an expected
        // fail I will deal with later
        insertChar("<% x = %q((^)) %>", 'a', "<% x = %q((a^)) %>");
    }

    public void testPipes1() throws Exception {
        insertChar("<% 5.each { ^ %>", '|', "<% 5.each { |^| %>");
    }

    public void testPipes2() throws Exception {
        insertChar("<% 5.each { ^} %>", '|', "<% 5.each { |^|} %>");
    }

    public void testPipes3() throws Exception {
        insertChar("<% 5.each { |^|} %>", '|', "<% 5.each { ||^} %>");
    }

    public void testPipes4() throws Exception {
        insertChar("<% 5.each { |foo^|} %>", '|', "<% 5.each { |foo|^} %>");
    }

    public void testNegativePipes1() throws Exception {
        insertChar("<% '^' %>", '|', "<% '|^' %>");
    }

    public void testNegativePipes2() throws Exception {
        insertChar("<% /^/ %>", '|', "<% /|^/ %>");
    }

    public void testNegativePipes3() throws Exception {
        insertChar("<% #^ %>", '|', "<% #|^ %>");
    }

    public void testNegativePipes4() throws Exception {
        insertChar("<% \"^\" %>", '|', "<% \"|^\" %>");
    }

    public void testNegativePipes5() throws Exception {
        insertChar("<% 5.each { |f^oo|} %>", '|', "<% 5.each { |f|^oo|} %>");
    }

    public void testNegativePipes6() throws Exception {
        insertChar("<% 5.each { |^|foo|} %>", '|', "<% 5.each { ||^foo|} %>");
    }

    public void testNegativePipes7() throws Exception {
        insertChar("<% x = true ^ %>", '|', "<% x = true |^ %>");
    }

    public void testNegativePipes8() throws Exception {
        insertChar("<% x = true |^ %>", '|', "<% x = true ||^ %>");
    }

    // ----------- Comment toggling

    public void testToggleComment1() throws Exception {
        toggleComment("fo^o", "<%#*foo%>");
    }

    public void testToggleComment2() throws Exception {
        toggleComment("<%#*f^oo%>", "foo");
    }

    public void testToggleComment3() throws Exception {
        toggleComment("<% ruby^ %>", "<%# ruby %>");
    }

    public void testToggleComment4() throws Exception {
        toggleComment("<%# ruby^ %>", "<% ruby %>");
    }

    public void testToggleComment5() throws Exception {
        toggleComment("foo\n<div>\n  <span>^</span>  \n  <%= rubyexp %> \n",
                      "foo\n<div>\n  <%#*<span></span>%>  \n  <%= rubyexp %> \n");
    }

    public void testToggleComment6() throws Exception {
        toggleComment("foo\n<div>\n  $start$<span></span>$end$  \n  \n  <%= rubyexp %> \n",
                      "foo\n<div>\n  <%#*<span></span>%>  \n  \n  <%= rubyexp %> \n");
    }

    public void testToggleComment7() throws Exception {
        toggleComment("$start$foo\n<div>\n  <span></span>  \n  \n  <%= rubyexp %> \n$end$",
                      "<%#*foo%>\n<%#*<div>%>\n  <%#*<span></span>%>  \n  \n  <%#= rubyexp %> \n");
    }

    public void testToggleComment8() throws Exception {
        toggleComment("$start$<%#*foo%>\n<%#*<div>%>\n  <%#*<span></span>%>  \n  \n  <%#= rubyexp %> \n$end$",
                     "foo\n<div>\n  <span></span>  \n  \n  <%= rubyexp %> \n");
    }

    public void testToggleComment9() throws Exception {
        toggleComment("<div>\n  <% ruby1\n  ru^by2\n  ruby3 %> \n</div>\n",
                      "<div>\n  <% ruby1\n  #ruby2\n  ruby3 %> \n</div>\n");
    }

    public void testToggleComment10() throws Exception {
        toggleComment("<div>\n  <% ruby1\n  #ru^by2\n  ruby3 %> \n</div>\n",
                      "<div>\n  <% ruby1\n  ruby2\n  ruby3 %> \n</div>\n");
    }

    public void testToggleComment11() throws Exception {
        toggleComment("$start$<div>\n  <% ruby1\n  ruby2\n  ruby3 %> \n</div>\n$end$",
                             "<%#*<div>%>\n  <%# ruby1\n  ruby2\n  ruby3 %> \n<%#*</div>%>\n");
    }

    public void testToggleComment12() throws Exception {
        toggleComment("$start$<%#*<div>%>\n  <%# ruby1\n  ruby2\n  ruby3 %> \n<%#*</div>%>\n$end$",
                             "<div>\n  <% ruby1\n  ruby2\n  ruby3 %> \n</div>\n");
    }

    public void testToggleComment13() throws Exception {
        toggleComment("<% #ruby^ %>", "<% ruby %>");
    }

    // test insert break; in particular, entering out of <% else %> doesn't work at the end of input!

    public void testNewline1() throws Exception {
        insertNewline("<div>^", "<div>\n  ^", new IndentPrefs(2, 2));
    }

    public void testNewline2() throws Exception {
        insertNewline("<div></div>^", "<div></div>\n^", new IndentPrefs(2, 2));
    }

    public void testNewline3a() throws Exception {
        insertNewline("<div>^</div>", "<div>\n  ^\n</div>", new IndentPrefs(2, 2));
    }

    public void testNewline3b() throws Exception {
        insertNewline("<div>^</div>\n", "<div>\n  ^\n</div>\n", new IndentPrefs(2, 2));
    }

    public void testNewline4a() throws Exception {
        failingDueToIssue160612("<% if true %>^\n", "<% if true %>\n  ^\n", new IndentPrefs(2, 2));
//        insertNewline("<% if true %>^\n", "<% if true %>\n  ^\n", new IndentPrefs(2, 2));
    }

    public void testNewline4b() throws Exception {
        failingDueToIssue160612("    <% if true %>^\n", "    <% if true %>\n      ^\n", new IndentPrefs(2, 2));
//        insertNewline("    <% if true %>^\n", "    <% if true %>\n      ^\n", new IndentPrefs(2, 2));
    }

    public void testNewline5a() throws Exception {
        failingDueToIssue160612("<% if true %>^", "<% if true %>\n  ^", new IndentPrefs(2, 2));
//        insertNewline("<% if true %>^", "<% if true %>\n  ^", new IndentPrefs(2, 2));
    }

    public void testNewline5b() throws Exception {
        failingDueToIssue160612("    <% if true %>^", "    <% if true %>\n      ^", new IndentPrefs(2, 2));
//        insertNewline("    <% if true %>^", "    <% if true %>\n      ^", new IndentPrefs(2, 2));
    }

    public void testNewline5c() throws Exception {
        insertNewline("    <% if ^true %>", "    <% if \n      ^true %>", new IndentPrefs(2, 2));
    }

    public void testNewline6() throws Exception {
        failingDueToIssue160612("    <% foo %>^\n", "    <% foo %>\n    ^\n", new IndentPrefs(2, 2));
//        insertNewline("    <% foo %>^\n", "    <% foo %>\n    ^\n", new IndentPrefs(2, 2));
    }

    public void testNewline7() throws Exception {
        failingDueToIssue160612("    <% foo %>^", "    <% foo %>\n    ^", new IndentPrefs(2, 2));
    }

    public void testNewline8() throws Exception {
        failingDueToIssue160612("    <% foo %>^<span>", "    <% foo %>\n    ^<span>", new IndentPrefs(2, 2));
//        insertNewline("    <% foo %>^<span>", "    <% foo %>\n    ^<span>", new IndentPrefs(2, 2));
    }

    private void failingDueToIssue160612(String source, String reformatted, IndentPrefs prefs) throws Exception {
        try {
            insertNewline(source, reformatted, prefs);
        } catch (AssertionError ae) {
            String msg = "Skipping failing test: " + getName() + " -- see https://netbeans.org/bugzilla/show_bug.cgi?id=160612. Error: " + ae.getMessage();
            log(msg);
            System.out.println(msg);
        }
    }
}
