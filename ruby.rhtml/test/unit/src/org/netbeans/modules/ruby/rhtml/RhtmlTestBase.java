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

package org.netbeans.modules.ruby.rhtml;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.ruby.lexer.RubyTokenId;

/**
 *
 * @author Tor Norbye
 */
public abstract class RhtmlTestBase extends RubyTestBase {
   private RhtmlIndentTaskFactory rhtmlReformatFactory;
   private HtmlIndentTaskFactory htmlReformatFactory;
//   private GsfIndentTaskFactory rubyReformatFactory;

    public RhtmlTestBase(String testName) {
        super(testName);
    }

    @Override
    @SuppressWarnings("deprecation") // For setFormatter()
    protected void setUp() throws Exception {
        super.setUp();
        try {
            TestLanguageProvider.register(RhtmlTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
        try {
            TestLanguageProvider.register(RubyTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }

        rhtmlReformatFactory = new RhtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse(RubyInstallation.RHTML_MIME_TYPE), rhtmlReformatFactory);
        htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory);
        // Can't do this without LanguageRegistry finding Ruby
        //rubyReformatFactory = new GsfIndentTaskFactory();
        //IndentTestMimeDataProvider.addInstances(RubyInstallation.RUBY_MIME_TYPE, rubyReformatFactory);

        Formatter.setFormatter(RhtmlKit.class, new ExtFormatter(RhtmlKit.class));

    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new RhtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return RhtmlTokenId.MIME_TYPE;
    }

    @Override
    protected void tearDown() throws Exception {
        MockMimeLookup.setLookup(MimePath.parse(RubyInstallation.RHTML_MIME_TYPE));
        MockMimeLookup.setLookup(MimePath.parse("text/html"));
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
    public BaseDocument getDocument(FileObject fo) {
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
        pane.getCaret().setSelectionVisible(true);
        
        return pane;
    }
    
    protected void runKitAction(JEditorPane pane, String actionName, String cmd) {
        RhtmlKit kit = (RhtmlKit)pane.getEditorKit();
        Action a = kit.getActionByName(actionName);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, cmd));
    }

//    // Ensure Ruby is registered too
//    @Override
//    protected void initializeRegistry() {
//        super.initializeRegistry();
//        if (!LanguageRegistry.getInstance().isSupported(RubyInstallation.RUBY_MIME_TYPE)) {
//            List<Action> actions = Collections.emptyList();
//            RubyLanguage l = new RubyLanguage();
//            Language dl = new Language("unknown", "text/x-ruby", actions,
//                    l, l.getCompletionHandler(),
//                    l.getInstantRenamer(), l.getDeclarationFinder(),
//                    l.getFormatter(), l.getKeystrokeHandler(),
//                    l.getIndexerFactory(), l.getStructureScanner(), null,
//                    l.isUsingCustomEditorKit());
//            List<Language> languages = new ArrayList<Language>();
//            languages.add(dl);
//            CslCorePackageAccessor.get().languageRegistryAddLanguages(languages);
//        }
//    }
}
