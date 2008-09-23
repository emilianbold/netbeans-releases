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

package org.netbeans.modules.ruby.rhtml.editor;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.rhtml.RhtmlTestBase;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlKitTest extends RhtmlTestBase {
    
    public RhtmlKitTest(String testName) {
        super(testName);
    }

    public void toggleComment(String text, String expected) throws Exception {
        JEditorPane pane = getPane(text);

        runKitAction(pane, "toggle-comment", "");

        String toggled = pane.getText();
        assertEquals(expected, toggled);
    }
    
    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    private void insertChar(String original, char insertText, String expected) throws BadLocationException, Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws BadLocationException, Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    @Override
    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws Exception {
        JEditorPane pane;
        try {
            pane = getPane(original);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.toString());
            return;
        }
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        Caret caret = pane.getCaret();
        caret.setDot(insertOffset);
        BaseDocument doc = (BaseDocument)pane.getDocument();
        if (selection != null) {
            int start = original.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    original.indexOf(selection, start+1) == -1);
            pane.setSelectionStart(start);
            pane.setSelectionEnd(start+selection.length());
            assertEquals(selection, pane.getSelectedText());
        }
        runKitAction(pane, DefaultEditorKit.defaultKeyTypedAction, ""+insertText);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(expected, formatted);
        if (finalCaretPos != -1) {
            assertEquals(finalCaretPos, caret.getDot());
        }
    }

    @Override
    protected void deleteChar(String original, String expected) throws Exception {
        JEditorPane pane;
        try {
            pane = getPane(original);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.toString());
            return;
        }
        int afterRemoveOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, afterRemoveOffset) + original.substring(afterRemoveOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        Caret caret = pane.getCaret();
        caret.setDot(afterRemoveOffset);
        BaseDocument doc = (BaseDocument)pane.getDocument();
        char ch = doc.getChars(afterRemoveOffset-1, 1)[0];

        runKitAction(pane, DefaultEditorKit.deletePrevCharAction, ""+ch);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(expected, formatted);
        if (finalCaretPos != -1) {
            assertEquals(finalCaretPos, caret.getDot());
        }
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
}
