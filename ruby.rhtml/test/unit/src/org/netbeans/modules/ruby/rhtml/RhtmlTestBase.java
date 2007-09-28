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
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.editor.indent.IndentTestMimeDataProvider;
import org.netbeans.modules.gsf.GsfIndentTaskFactory;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public abstract class RhtmlTestBase extends RubyTestBase {
   private RhtmlIndentTaskFactory rhtmlReformatFactory;
   private HtmlIndentTaskFactory htmlReformatFactory;
   private GsfIndentTaskFactory rubyReformatFactory;

    public RhtmlTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestLanguageProvider.register(RhtmlTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        
        rhtmlReformatFactory = new RhtmlIndentTaskFactory();
        IndentTestMimeDataProvider.addInstances(RubyInstallation.RHTML_MIME_TYPE, rhtmlReformatFactory);
        htmlReformatFactory = new HtmlIndentTaskFactory();
        IndentTestMimeDataProvider.addInstances("text/html", htmlReformatFactory);
        // Can't do this without LanguageRegistry finding Ruby
        //rubyReformatFactory = new GsfIndentTaskFactory();
        //IndentTestMimeDataProvider.addInstances(RubyInstallation.RUBY_MIME_TYPE, rubyReformatFactory);
        
        Formatter.setFormatter(RhtmlKit.class, new ExtFormatter(RhtmlKit.class));
    }
    
    @Override
    protected void tearDown() throws Exception {
        IndentTestMimeDataProvider.removeInstances(RubyInstallation.RHTML_MIME_TYPE, rhtmlReformatFactory);
        IndentTestMimeDataProvider.removeInstances("text/html", htmlReformatFactory);
        //IndentTestMimeDataProvider.removeInstances(RubyInstallation.RUBY_MIME_TYPE, rubyReformatFactory);
        super.tearDown();
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
    
    protected JEditorPane getPane(String text, int startPos, int endPos) throws Exception {
        String textWithMarkers = text;
        if (startPos > 0 || endPos < text.length()) {
            textWithMarkers = text.substring(0, startPos) + "$start$" + text.substring(startPos, endPos) + "$end$" + text.substring(endPos);
        } else {
            textWithMarkers = "^" + text;
        }
        
        return getPane(textWithMarkers);
    }
    
    protected JEditorPane getPane(String text) throws Exception {
        String BEGIN = "$start$"; // NOI18N
        String END = "$end$"; // NOI18N
        int sourceStartPos = text.indexOf(BEGIN);
        int caretPos = -1;
        int sourceEndPos = -1;
        if (sourceStartPos != -1) {
            text = text.substring(0, sourceStartPos) + text.substring(sourceStartPos+BEGIN.length());
            sourceEndPos = text.indexOf(END);
            assert sourceEndPos != -1;
            text = text.substring(0, sourceEndPos) + text.substring(sourceEndPos+END.length());
        } else {
            caretPos = text.indexOf('^');
            text = text.substring(0, caretPos) + text.substring(caretPos+1);
        }

        JEditorPane.registerEditorKitForContentType(RubyInstallation.RHTML_MIME_TYPE, RhtmlKit.class.getName());
        JEditorPane pane = new JEditorPane(RubyInstallation.RHTML_MIME_TYPE, text);
        //pane.setEditorKit(new RhtmlKit());
        //pane.setContentType(RubyInstallation.RHTML_MIME_TYPE);
                
        BaseDocument bdoc = (BaseDocument)pane.getDocument();

        bdoc.putProperty(org.netbeans.api.lexer.Language.class, RhtmlTokenId.language());
        bdoc.putProperty("mimeType", RubyInstallation.RHTML_MIME_TYPE);

        //bdoc.insertString(0, text, null);
        if (sourceStartPos != -1) {
            assert sourceEndPos != -1;
            pane.setSelectionStart(sourceStartPos);
            pane.setSelectionEnd(sourceEndPos);
        } else {
            assert caretPos != -1;
            pane.getCaret().setDot(caretPos);
        }
        
        return pane;
    }
    
    protected void runKitAction(JEditorPane pane, String actionName, String cmd) {
        RhtmlKit kit = (RhtmlKit)pane.getEditorKit();
        Action a = kit.getActionByName(actionName);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, cmd));
    }
}
