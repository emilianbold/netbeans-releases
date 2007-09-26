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

import javax.swing.JEditorPane;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public abstract class RhtmlTestBase extends RubyTestBase {

    public RhtmlTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestLanguageProvider.register(RhtmlTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
    }

    @Override
    protected void tearDown() throws Exception {
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

        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(new RhtmlKit());
        pane.setContentType(RubyInstallation.RHTML_MIME_TYPE);
                
        BaseDocument bdoc = (BaseDocument)pane.getDocument();

        bdoc.insertString(0, text, null);
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
}
