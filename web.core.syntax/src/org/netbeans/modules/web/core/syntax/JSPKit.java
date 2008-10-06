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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.core.syntax;


import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.HtmlIndenter;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.gsfret.InstantRenameAction;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.SelectCodeElementAction;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.html.editor.HTMLAutoCompletion;
import org.netbeans.modules.html.editor.coloring.EmbeddingUpdater;
import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;
import org.netbeans.modules.web.core.syntax.deprecated.ELDrawLayerFactory;
import java.awt.event.ActionEvent;
import java.beans.*;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.editor.ext.html.HTMLLexerFormatter;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.web.core.syntax.formatting.JSPLexerFormatter;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.Mutex;

/**
 * Editor kit implementation for JSP content type
 *
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @author Marek.Fukala@Sun.COM
 * @version 1.5
 */
public class JSPKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider{
    
    public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    /** serialVersionUID */
    private static final long serialVersionUID = 8933974837050367142L;
    
    public static final boolean debug = false;
    private final String mimeType;

    // called from the XML layer
    private static JSPKit createKitForJsp() {
        return new JSPKit(JSP_MIME_TYPE);
    }
    
    // called from the XML layer
    private static JSPKit createKitForTag() {
        return new JSPKit(TAG_MIME_TYPE);
    }
    
    /** Default constructor */
    public JSPKit(String mimeType) {
        super();
        this.mimeType = mimeType;
    }
    
    @Override
    public String getContentType() {
        return mimeType;
    }
    
    @Override
    public Object clone() {
        return new JSPKit(mimeType);
    }
    
    /** Creates a new instance of the syntax coloring parser */
    @Override
    public Syntax createSyntax(Document doc) {
        //TODO - place the coloring listener initialization to
        //more appropriate place. The createSyntax method is likely
        //going to be removed.
        initLexerColoringListener(doc);
        
        Syntax contentSyntax   = getSyntaxForLanguage(doc, JspUtils.getContentLanguage());
        Syntax scriptingSyntax = getSyntaxForLanguage(doc, JspUtils.getScriptingLanguage());
        final Jsp11Syntax newSyntax = new Jsp11Syntax(contentSyntax, scriptingSyntax);

        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        
        // tag library coloring data stuff
        JSPColoringData data = data = JspUtils.getJSPColoringData(doc, fobj);
        // construct the listener
        PropertyChangeListener pList = new ColoringListener(doc, data, newSyntax);
        // attach the listener
        // PENDING - listen on the language
        //jspdo.addPropertyChangeListener(WeakListeners.propertyChange(pList, jspdo));
        if (data != null) {
            data.addPropertyChangeListener(WeakListeners.propertyChange(pList, data));
        }
        
        return newSyntax;
    }
    
    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
            new JspInsertBreakAction(),
            new JspDefaultKeyTypedAction(),
            new JspDeleteCharAction(deletePrevCharAction, false),
            new JspDeleteCharAction(deleteNextCharAction, true),
            new SelectCodeElementAction(SelectCodeElementAction.selectNextElementAction, true),
            new SelectCodeElementAction(SelectCodeElementAction.selectPreviousElementAction, false),
            new InstantRenameAction(),
        };
        
        return TextAction.augmentList(super.createActions(), javaActions);
    }
    
    private static class ColoringListener implements PropertyChangeListener {
        private Document doc;
        private Object parsedDataRef; // NOPMD: hold a reference to the data we are listening on
        // so it does not get garbage collected
        private Jsp11Syntax syntax;
        //private JspDataObject jspdo;
        
        public ColoringListener(Document doc, JSPColoringData data, Jsp11Syntax syntax) {
            this.doc = doc;
            // we must keep the reference to the structure we are listening on so it's not gc'ed
            this.parsedDataRef = data;
            this.syntax = syntax;
            // syntax must keep a reference to this object so it's not gc'ed
            syntax.listenerReference = this;
            syntax.data = data;
            /* jspdo = (JspDataObject)NbEditorUtilities.getDataObject(doc);*/
        }
        
        private void recolor() {
            if (doc instanceof BaseDocument)
                ((BaseDocument)doc).invalidateSyntaxMarks();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            //            System.out.println("**************** PCHL - propertyChange()");
            if (syntax == null)
                return;
            if (syntax.listenerReference != this) {
                syntax = null; // should help garbage collection
                return;
            }
           /* if (JspDataObject.PROP_CONTENT_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setContentSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getContentLanguage()));
                recolor();
            }
            if (JspDataObject.PROP_SCRIPTING_LANGUAGE.equals(evt.getPropertyName())) {
                syntax.setScriptingSyntax(JSPKit.getSyntaxForLanguage(doc, jspdo.getScriptingLanguage()));
                recolor();
            }*/
            if (JSPColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NbEditorDocument nbdoc = (NbEditorDocument)doc;
                        nbdoc.extWriteLock();
                        try {
                            recolor();
                        } finally {
                            nbdoc.extWriteUnlock();
                        }
                    }
                });
                
            }
        }
    }
    
    
    private static class LexerColoringListener implements PropertyChangeListener {
        
        private Document doc;
        private JSPColoringData data;
        private JspParseData jspParseData;
        
        private LexerColoringListener(Document doc, JSPColoringData data, JspParseData jspParseData) {
            this.doc = doc;
            this.data = data; //hold ref to JSPColoringData so LCL is not GC'ed
            this.jspParseData = jspParseData;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (JSPColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                //THC.rebuild() must run under document write lock. Since it is not guaranteed that the
                //event from the JSPColoringData is not fired under document read lock, synchronous call
                //to write lock could deadlock. So the rebuild is better called asynchronously.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NbEditorDocument nbdoc = (NbEditorDocument)doc;
                        nbdoc.extWriteLock();
                        try {
                            recolor();
                        } finally {
                            nbdoc.extWriteUnlock();
                        }
                    }
                });
            }
        }
        private void recolor() {
            jspParseData.updateParseData((Map<String,String>)data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax());

            MutableTextInput mti = (MutableTextInput)doc.getProperty(MutableTextInput.class);
            if(mti != null) {
                mti.tokenHierarchyControl().rebuild();
            }
        }
        
    }
    
    public static Syntax getSyntaxForLanguage(Document doc, String language) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(language);
        if (kit instanceof JavaKit) {
            JavaKit jkit = (JavaKit)kit;
            String sourceLevel = jkit.getSourceLevel((BaseDocument)doc);
            //create a special javasyntax patched for use in JSPs (fix of #55628)
            return new JavaSyntax(sourceLevel, true);
        } else {
            return new HTMLSyntax();
        }
    }

    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new ELDrawLayerFactory.ELLayer(),
                ELDrawLayerFactory.EL_LAYER_VISIBILITY);
        
        //listen on the HTML parser and create javascript and css embeddings
        LanguagePath jspLP = LanguagePath.get(JspTokenId.language());
        LanguagePath htmlLP = LanguagePath.get(jspLP, HTMLTokenId.language());
        
        SyntaxParser.get(doc, htmlLP).addSyntaxParserListener(new EmbeddingUpdater(doc));
        //initialize JSP embedding updater
        //just a prototype - better disable it for 6.0
        //JspColoringUpdater.init(doc);
    }
    
    private void initLexerColoringListener(Document doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        JSPColoringData data = JspUtils.getJSPColoringData(doc, fobj);
        
        if(data == null) {
            return ;
        }
        
        JspParseData jspParseData = new JspParseData();
        jspParseData.updateParseData((Map<String,String>)data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax());
        PropertyChangeListener lexerColoringListener = new LexerColoringListener(doc, data, jspParseData);
        
        data.addPropertyChangeListener(WeakListeners.propertyChange(lexerColoringListener, data));
        //reference LCL from document to prevent LCL to be GC'ed
        doc.putProperty(LexerColoringListener.class, lexerColoringListener);
        
        //add an instance of InputAttributes to the document property,
        //lexer will use it to read coloring information
        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(JspTokenId.language(), JspParseData.class, jspParseData, false);
        doc.putProperty(InputAttributes.class, inputAttributes);
    }
    
    // <RAVE> #62993
    // Implement HelpCtx.Provider to provide help for CloneableEditor
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(JSPKit.class);
    }
    
    static KeystrokeHandler getBracketCompletion(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument) doc;
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (Language l : list) {
            if (l.getBracketCompletion() != null) {
                return l.getBracketCompletion();
            }
        }

        return null;
    }

    /**
     * Returns true if bracket completion is enabled in options.
     */
    private static boolean completionSettingEnabled() {
        //return ((Boolean)Settings.getValue(JSPKit.class, JavaSettingsNames.PAIR_CHARACTERS_COMPLETION)).booleanValue();
        return true;
    }
    
    public static class JspInsertBreakAction extends InsertBreakAction {

        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target != null) {
                TokenSequence javaTokenSequence;
                AbstractDocument adoc = (AbstractDocument)target.getDocument();
                adoc.readLock();
                try {
                    javaTokenSequence = JspSyntaxSupport.tokenSequence(TokenHierarchy.get(target.getDocument()), JavaTokenId.language(), target.getCaret().getDot() - 1);
                } finally {
                    adoc.readUnlock();
                }

                if (javaTokenSequence != null) {
                    JavaKit jkit = (JavaKit) getKit(JavaKit.class);
                    if (jkit != null) {
                        Action action = jkit.getActionByName(DefaultEditorKit.insertBreakAction);
                        if (action != null && action instanceof JavaKit.JavaInsertBreakAction) {
                            ((JavaKit.JavaInsertBreakAction) action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
        
        @Override
        protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = getBracketCompletion(doc, caret.getDot());

                if (bracketCompletion != null) {
                    try {
                        int newOffset = bracketCompletion.beforeBreak(doc, caret.getDot(), target);

                        if (newOffset >= 0) {
                            return new Integer(newOffset);
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            }

            // return Boolean.TRUE;
            return null;
        }

        @Override
        protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret,
                Object cookie) {
            if (completionSettingEnabled()) {
                if (cookie != null) {
                    if (cookie instanceof Integer) {
                        // integer
                        int dotPos = ((Integer) cookie).intValue();
                        if (dotPos != -1) {
                            caret.setDot(dotPos);
                        } else {
                            int nowDotPos = caret.getDot();
                            caret.setDot(nowDotPos + 1);
                        }
                    }
                }
            }
        }
        
    }

    public static class JspDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        private JTextComponent currentTarget;
        
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            currentTarget = target;
            if (target != null) {
                TokenSequence javaTokenSequence;
                AbstractDocument adoc = (AbstractDocument)target.getDocument();
                adoc.readLock();
                try {
                    javaTokenSequence = JspSyntaxSupport.tokenSequence(TokenHierarchy.get(target.getDocument()), JavaTokenId.language(), target.getCaret().getDot() - 1);
                } finally {
                    adoc.readUnlock();
                }

                if (javaTokenSequence != null) {
                    JavaKit jkit = (JavaKit) getKit(JavaKit.class);
                    if (jkit != null) {
                        Action action = jkit.getActionByName(DefaultEditorKit.defaultKeyTypedAction);
                        if (action != null && action instanceof JavaKit.JavaDefaultKeyTypedAction) {
                            ((JavaKit.JavaDefaultKeyTypedAction) action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
            currentTarget = null;
        }
        
        /** called under document atomic lock */
        @Override
        protected void insertString(BaseDocument doc, int dotPos,
                Caret caret, String str,
                boolean overwrite) throws BadLocationException {
            
            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = getBracketCompletion(doc, dotPos);

                if (bracketCompletion != null) {
                    // TODO - check if we're in a comment etc. and if so, do nothing
                    boolean handled =
                            bracketCompletion.beforeCharInserted(doc, dotPos, currentTarget,
                            str.charAt(0));

                    if (!handled) {
                        super.insertString(doc, dotPos, caret, str, overwrite);
                        handled = bracketCompletion.afterCharInserted(doc, dotPos, currentTarget,
                                str.charAt(0));
                    }

                    return;
                }
            }
            
            super.insertString(doc, dotPos, caret, str, overwrite);
            //handle reformat
            handleTagClosingSymbol(doc, dotPos, str.charAt(0));
            //handle html quotations completion
            HTMLAutoCompletion.charInserted(doc, dotPos, caret, str.charAt(0));
        }
        
        @Override
        protected void replaceSelection(JTextComponent target, int dotPos, Caret caret,
                String str, boolean overwrite) throws BadLocationException {
            char insertedChar = str.charAt(0);
            Document document = target.getDocument();

            if (document instanceof BaseDocument) {
                BaseDocument doc = (BaseDocument) document;

                if (completionSettingEnabled()) {
                    KeystrokeHandler bracketCompletion = getBracketCompletion(doc, dotPos);

                    if (bracketCompletion != null) {
                        try {
                            int caretPosition = caret.getDot();

                            boolean handled =
                                    bracketCompletion.beforeCharInserted(doc, caretPosition,
                                    target, insertedChar);

                            int p0 = Math.min(caret.getDot(), caret.getMark());
                            int p1 = Math.max(caret.getDot(), caret.getMark());

                            if (p0 != p1) {
                                doc.remove(p0, p1 - p0);
                            }

                            if (!handled) {
                                if ((str != null) && (str.length() > 0)) {
                                    doc.insertString(p0, str, null);
                                }

                                bracketCompletion.afterCharInserted(doc, caret.getDot() - 1,
                                        target, insertedChar);
                            }
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }

                        return;
                    }
                }
            }

            super.replaceSelection(target, dotPos, caret, str, overwrite);
        }

        /** called under document atomic lock */
        private void handleTagClosingSymbol(final BaseDocument doc, final int dotPos, char lastChar) throws BadLocationException {
            if (lastChar == '>') {
                LanguagePath jspLanguagePath = LanguagePath.get(JspTokenId.language());
                final LanguagePath htmlInJSPPath = LanguagePath.get(jspLanguagePath, HTMLTokenId.language());
                HTMLLexerFormatter htmlFormatter = new HTMLLexerFormatter(htmlInJSPPath);

                if (htmlFormatter.isJustAfterClosingTag(doc, dotPos)) {
                      HtmlIndenter.indentEndTag(doc, htmlInJSPPath, dotPos, null);
//                    reformat(doc, dotPos);
                } else {
                    //We cannot run the jsp reformatter from this thread since the document
                    //is already atomically locked so doing the source lock here is deadlock prone.
                    //There doesn't seem to be an elegant way how to do this so usign a workaround
                    JSPLexerFormatter jspFormatter = new JSPLexerFormatter();
                    if (jspFormatter.isJustAfterClosingTag(doc, dotPos)) {
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    reformat(doc, doc.createPosition(dotPos));
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                        
                    }
                }
            }
        }
        
        private void reformat(final BaseDocument doc, final Position dotPos) {
            final Reformat reformat = Reformat.get(doc);
            reformat.lock();

            try {
                doc.runAtomic(new Runnable() {
                    public void run() {
                        try {
                            int offset = dotPos.getOffset();
                            int startOffset = org.netbeans.editor.Utilities.getRowStart(doc, offset);
                            int endOffset = org.netbeans.editor.Utilities.getRowEnd(doc, offset);
                            reformat.reformat(startOffset, endOffset);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

            } finally {
                reformat.unlock();
            }
        }
    }
    
    public static class JspDeleteCharAction extends ExtDeleteCharAction {
        
        JTextComponent currentTarget;
        
        public JspDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }
        
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            currentTarget = target;
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(nextChar ? DefaultEditorKit.deleteNextCharAction : DefaultEditorKit.deletePrevCharAction);
                        if (action != null && action instanceof JavaKit.JavaDeleteCharAction){
                            ((JavaKit.JavaDeleteCharAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
            currentTarget = null;
        }
        
         protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch) throws BadLocationException {
              if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = getBracketCompletion(doc, dotPos);

                if (bracketCompletion != null) {
                    boolean success = bracketCompletion.charBackspaced(doc, dotPos, currentTarget, ch);
                    return;
                }
            }
            
            super.charBackspaced(doc, dotPos, caret, ch);
        }
        
    }
    
}

