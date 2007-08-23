/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rhtml;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.html.HTMLFormatter;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.IndentPrefs;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.html.editor.indent.HtmlIndentTask;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;
import org.netbeans.spi.editor.indent.Context;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlFormattingTest extends RubyTestBase {

    public RhtmlFormattingTest(String testName) {
        super(testName);
    }

    @Override
    protected BaseDocument getDocument(String s) {
        BaseDocument doc = super.getDocument(s);
        doc.putProperty(org.netbeans.api.lexer.Language.class, RhtmlTokenId.language());
        doc.putProperty("mimeType", RubyInstallation.RHTML_MIME_TYPE);
        
        return doc;
    }
    
    @Override
    protected BaseDocument getDocument(FileObject fo) {
        BaseDocument doc = super.getDocument(fo);
        doc.putProperty(org.netbeans.api.lexer.Language.class, RhtmlTokenId.language());
        doc.putProperty("mimeType", RubyInstallation.RHTML_MIME_TYPE);
        
        return doc;
    }
    
    public String format(BaseDocument doc, int startPos, int endPos, FormattingPreferences preferences) throws Exception {
        //ParserResult result = parse(fo);
        ParserResult result = null;

        // First format HTML, since the Ruby formatter relies on that
        //new FormattingContext()
        //HtmlIndentTask htmlTask = new HtmlIndentTask();
        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(startPos);

        // Use the HtmlIndentTask to format
//        JEditorPane pane = new JEditorPane();
//        pane.setDocument(doc);
//        RhtmlKit kit = new RhtmlKit();
//        pane.setEditorKit(kit); 
//        assertEquals(RubyInstallation.RHTML_MIME_TYPE, pane.getDocument().getProperty("mimeType"));
//        Action a = kit.getActionByName(BaseKit.formatAction);
//        assertNotNull(a);
//        a.actionPerformed(new ActionEvent(pane, 0, ""));

        // ... that doesn't work, so do what the indent task currently does anyway:
        HTMLFormatter f = new HTMLFormatter(HTMLKit.class);
        String unformatted = doc.getText(0, doc.getLength());
        JEditorPane pane = new JEditorPane();
        pane.setContentType(HTMLKit.HTML_MIME_TYPE);
        HTMLKit kit = new HTMLKit();
        pane.setEditorKit(kit);
        BaseDocument bdoc = (BaseDocument)pane.getDocument();
        bdoc.insertString(0, unformatted, null);
        f.reformat(bdoc, startPos, endPos, true);
        
        
        Formatter formatter = getFormatter(preferences);

        String htmlFormatted = bdoc.getText(0, bdoc.getLength());
        
        doc.remove(0, doc.getLength());
        doc.insertString(0, htmlFormatted, null);
        
        formatter.reindent(doc, startPos, endPos, result, preferences);

        String formatted = doc.getText(0, doc.getLength());
        
        return formatted;
    }
    
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    public void format(String source, String reformatted, FormattingPreferences preferences) throws Exception {
        // Must run in AWT thread (BaseKit.install() checks for that)
        String BEGIN = "%<%"; // NOI18N
        int startPos = source.indexOf(BEGIN);
        if (startPos != -1) {
            source = source.substring(0, startPos) + source.substring(startPos+BEGIN.length());
        } else {
            startPos = 0;
        }
        
        String END = "%>%"; // NOI18N
        int endPos = source.indexOf(END);
        if (endPos != -1) {
            source = source.substring(0, endPos) + source.substring(endPos+END.length());
        }

        BaseDocument doc = getDocument(source);

        if (endPos == -1) {
            endPos = doc.getLength();
        }
        
        String formatted = format(doc, startPos, endPos, preferences);
        assertEquals(reformatted, formatted);
    }
    
    public void reformatFileContents(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);

        FormattingPreferences preferences = new IndentPrefs(2,2);
        String formatted = format(doc, 0, doc.getLength(), preferences);
        assertDescriptionMatches(file, formatted, false, ".formatted");
    }
    
    public void testFormat1() throws Exception {
        reformatFileContents("testfiles/format1.rhtml");
    }
    
    public void testFormat2() throws Exception {
        reformatFileContents("testfiles/format2.rhtml");
    }
    
    public void testFormat3() throws Exception {
        reformatFileContents("testfiles/format3.rhtml");
    }
    
    public void testFormat4() throws Exception {
        reformatFileContents("testfiles/format4.rhtml");
    }
    
    public void testFormat5() throws Exception {
        format("<%\ndef foo\nwhatever\nend\n%>\n",
                "<%\ndef foo\n  whatever\nend\n%>\n", null);
    }
    
//    public void testFormat6() throws Exception {
//        format("<% if true %>\nhello\n%<= foo %>\n<% end %>\n",
//                "<% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
//    }
//
//    public void testFormat7() throws Exception {
//        format("<% foo %><% if true %>\nhello\n%<= foo %>\n<% end %>\n",
//                "<% foo %><% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
//    }
}
