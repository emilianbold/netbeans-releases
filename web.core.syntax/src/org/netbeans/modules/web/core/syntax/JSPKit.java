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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;


import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.html.editor.coloring.EmbeddingUpdater;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;
import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;
import org.netbeans.modules.web.core.syntax.deprecated.ELDrawLayerFactory;
import org.netbeans.modules.web.core.syntax.formatting.JspFormatter;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.text.*;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.folding.JspFoldTypes;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.netbeans.spi.lexer.TokenHierarchyControl;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.CompletionJavaDoc;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.java.JavaDrawLayerFactory;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.modules.editor.NbEditorKit.GenerateFoldPopupAction;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 * Editor kit implementation for JSP content type
 *
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @author Marek.Fukala@Sun.COM
 * @version 1.5
 */
public class JSPKit extends LanguagesEditorKit implements org.openide.util.HelpCtx.Provider{
    
    public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    //comment folds
    public static final String collapseAllCommentsAction = "collapse-all-comment-folds"; //NOI18N
    public static final String expandAllCommentsAction = "expand-all-comment-folds"; //NOI18N
    
    //scripting folds
    public static final String collapseAllScriptingAction = "collapse-all-scripting-folds"; //NOI18N
    public static final String expandAllScriptingAction = "expand-all-scripting-folds"; //NOI18N
    
    /** serialVersionUID */
    private static final long serialVersionUID = 8933974837050367142L;
    
    public static final boolean debug = false;
    
    /** Default constructor */
    public JSPKit() {
        super(JSP_MIME_TYPE);
    }
    
    public String getContentType() {
        return JSP_MIME_TYPE;
    }
    
    public Object clone() {
        return new JSPKit();
    }
    
    /** Creates a new instance of the syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        //TODO - place the coloring listener initialization to
        //more appropriate place. The createSyntax method is likely
        //going to be removed.
        initLexerColoringListener(doc);
        
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        
        //String mimeType = NbEditorUtilities.getMimeType(doc);
        
        Syntax contentSyntax   = getSyntaxForLanguage(doc, JspUtils.getContentLanguage());
        Syntax scriptingSyntax = getSyntaxForLanguage(doc, JspUtils.getScriptingLanguage());
        final Jsp11Syntax newSyntax = new Jsp11Syntax(contentSyntax, scriptingSyntax);
        
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
            //            new JspJavaGenerateGotoPopupAction(),
            //                    new JavaKit.JavaJMIGotoSourceAction(),
            //                    new JavaKit.JavaJMIGotoDeclarationAction(),
            //                    new JavaKit.JavaGotoSuperImplementation(),
            // the jsp editor has own action for switching beetween matching blocks
            new MatchBraceAction(ExtKit.matchBraceAction, false),
            new MatchBraceAction(ExtKit.selectionMatchBraceAction, true),
            new JspGenerateFoldPopupAction(),
            new CollapseAllCommentsFolds(),
            new ExpandAllCommentsFolds(),
            new CollapseAllScriptingFolds(),
            new ExpandAllScriptingFolds(),
            new JspInsertBreakAction(),
            new JspDefaultKeyTypedAction(),
            new JspDeleteCharAction(deletePrevCharAction, false)
        };
        
        return TextAction.augmentList(super.createActions(), javaActions);
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
                recolor();
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
                recolor();
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
    
    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            if (dobj.getPrimaryFile() != null) {
                JspOpenInfo jspOpenInfo = JspContextInfo.getContextInfo(dobj.getPrimaryFile()).getCachedOpenInfo(doc, dobj.getPrimaryFile(), false);
                if(jspOpenInfo == null) {
                    Logger.getLogger(JSPKit.class.getName()).log(Level.INFO, "Cannot obtain JspOpenInfo for file " + dobj.getPrimaryFile().getNameExt());
                }
                return new JspSyntaxSupport(doc, jspOpenInfo != null ? jspOpenInfo.isXmlSyntax() : false);
                
            }
        }
        return new JspSyntaxSupport(doc, false);
    }
    
    /** This method now returns null since the code completion is got from
     * code completion providers declared in the module layer.
     */
    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return null;
    }
    
    /** This method now returns null since the code completion is got from
     * code completion providers declared in the module layer.
     */
    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return null;
    }
    
    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
        doc.addLayer(new ELDrawLayerFactory.ELLayer(),
                ELDrawLayerFactory.EL_LAYER_VISIBILITY);
        doc.addDocumentListener(new ELDrawLayerFactory.LParenWatcher());
        
        //listen on the HTML parser and create javascript and css embeddings
        SyntaxParser.get(doc).addSyntaxParserListener(new EmbeddingUpdater(doc));
        //initialize JSP embedding updater
        JspColoringUpdater.init(doc);
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
    
    public Formatter createFormatter() {
        return new JspFormatter(this.getClass());
        
    }
    
    // <RAVE> #62993
    // Implement HelpCtx.Provider to provide help for CloneableEditor
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(JSPKit.class);
    }

    /** Implementation of MatchBraceAction, whic move the cursor in the matched block.
     */
    public static class MatchBraceAction extends ExtKit.MatchBraceAction {
        
        private boolean select; // whether the text between matched blocks should be selected
        public MatchBraceAction(String name, boolean select) {
            super(name, select);
            this.select = select;
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = Utilities.getDocument(target);
                    int dotPos = caret.getDot();
                    dotPos--; //adjust position
                    ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
                    
                    TokenHierarchy hi = TokenHierarchy.get(doc);
                    BaseKit kit = null;
                    try {
                        //are we in HTML or Java?
                        if(JspSyntaxSupport.tokenSequence(hi, HTMLTokenId.language(), dotPos) != null)  {
                            kit = getKit(getClass().forName("org.netbeans.modules.editor.html.HTMLKit"));      //NOI18N
                        }
                        if(JspSyntaxSupport.tokenSequence(hi, JavaTokenId.language(), dotPos) != null)  {
                            kit = getKit(getClass().forName("org.netbeans.modules.editor.java.JavaKit"));  //NOI18N
                        }
                    } catch (java.lang.ClassNotFoundException e){
                        kit = null;
                        Logger.getLogger("global").log(Level.WARNING, null, e);
                    }
                    
                    if (kit == null) {
                        //just JSP - do the JSP brace match
                        if (dotPos > 0) {
                            int[] matchBlk = sup.findMatchingBlock(dotPos - 1, false);
                            if (matchBlk != null) {
                                if (select) {
                                    caret.moveDot(matchBlk[1]);
                                } else {
                                    caret.setDot(matchBlk[1]);
                                }
                            }
                        }
                    } else {
                        //we are in HTML or Java
                        Action action = kit.getActionByName(select ? ExtKit.selectionMatchBraceAction : ExtKit.matchBraceAction);
                        if (action != null && action instanceof ExtKit.MatchBraceAction){
                            ((ExtKit.MatchBraceAction)action).actionPerformed(evt, target);
                        } else {
                            super.actionPerformed(evt, target);
                        }
                    }
                    
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }
    
    public static class JspGenerateFoldPopupAction extends GenerateFoldPopupAction {
        
        protected void addAdditionalItems(JTextComponent target, JMenu menu){
            addAction(target, menu, collapseAllCommentsAction);
            addAction(target, menu, expandAllCommentsAction);
            setAddSeparatorBeforeNextAction(true);
            addAction(target, menu, collapseAllScriptingAction);
            addAction(target, menu, expandAllScriptingAction);
        }
    }
    
    public static class ExpandAllCommentsFolds extends BaseAction{
        public ExpandAllCommentsFolds(){
            super(expandAllCommentsAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JSPKit.class).getString("expand-all-comment-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JSPKit.class).getString("popup-expand-all-comment-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.expand(hierarchy, JspFoldTypes.COMMENT);
            
        }
    }
    
    public static class CollapseAllCommentsFolds extends BaseAction{
        public CollapseAllCommentsFolds(){
            super(collapseAllCommentsAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JSPKit.class).getString("collapse-all-comment-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JSPKit.class).getString("popup-collapse-all-comment-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.collapse(hierarchy, JspFoldTypes.COMMENT);
        }
    }
    
    public static class ExpandAllScriptingFolds extends BaseAction{
        public ExpandAllScriptingFolds(){
            super(expandAllScriptingAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JSPKit.class).getString("expand-all-scripting-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JSPKit.class).getString("popup-expand-all-scripting-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.expand(hierarchy, JspFoldTypes.SCRIPTLET);
            FoldUtilities.expand(hierarchy, JspFoldTypes.DECLARATION);
        }
    }
    
    public static class CollapseAllScriptingFolds extends BaseAction{
        public CollapseAllScriptingFolds(){
            super(collapseAllScriptingAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JSPKit.class).getString("collapse-all-scripting-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JSPKit.class).getString("popup-collapse-all-scripting-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.collapse(hierarchy, JspFoldTypes.SCRIPTLET);
            FoldUtilities.collapse(hierarchy, JspFoldTypes.DECLARATION);
        }
    }
    
    //    private static TokenContextPath getTokenContextPath(Caret caret, Document doc){
    //        if (doc instanceof BaseDocument){
    //            int dotPos = caret.getDot();
    //            ExtSyntaxSupport sup = (ExtSyntaxSupport)((BaseDocument)doc).getSyntaxSupport();
    //            if (dotPos>0){
    //                try{
    //                    TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
    //                    if (token != null){
    //                        return token.getTokenContextPath();
    //                    }
    //                }catch(BadLocationException ble){
    //                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ble);
    //                }
    //            }
    //        }
    //        return null;
    //    }
    
    public static class JspInsertBreakAction extends InsertBreakAction {
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(DefaultEditorKit.insertBreakAction);
                        if (action != null && action instanceof JavaKit.JavaInsertBreakAction){
                            ((JavaKit.JavaInsertBreakAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
    }
    
    public static class JspDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(DefaultEditorKit.defaultKeyTypedAction);
                        if (action != null && action instanceof JavaKit.JavaDefaultKeyTypedAction){
                            ((JavaKit.JavaDefaultKeyTypedAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
    }
    
    public static class JspDeleteCharAction extends ExtDeleteCharAction {
        
        public JspDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }
        
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenSequence javaTokenSequence = JspSyntaxSupport.tokenSequence(
                        TokenHierarchy.get(target.getDocument()),
                        JavaTokenId.language(),
                        target.getCaret().getDot() - 1);
                
                if (javaTokenSequence != null){
                    JavaKit jkit = (JavaKit)getKit(JavaKit.class);
                    if (jkit!=null){
                        Action action = jkit.getActionByName(DefaultEditorKit.deletePrevCharAction);
                        if (action != null && action instanceof JavaKit.JavaDeleteCharAction){
                            ((JavaKit.JavaDeleteCharAction)action).actionPerformed(e, target);
                            return;
                        }
                    }
                }
            }
            super.actionPerformed(e, target);
        }
    }
    
    //    public static class JspJavaGenerateGotoPopupAction extends JavaKit.JavaGenerateGoToPopupAction {
    //
    //        protected void addAction(JTextComponent target, JMenu menu,
    //                String actionName) {
    //            BaseKit kit = Utilities.getKit(target);
    //            if (kit == null) return;
    //            Action a = kit.getActionByName(actionName);
    //            if (a!=null){
    //                //test context only for context-aware actions
    //                if(ExtKit.gotoSourceAction.equals(actionName) ||
    //                        ExtKit.gotoDeclarationAction.equals(actionName) ||
    //                        ExtKit.gotoSuperImplementationAction.equals(actionName))
    //                    a.setEnabled(isJavaContext(target));
    //
    //                addAction(target, menu, a);
    //            } else { // action-name is null, add the separator
    //                menu.addSeparator();
    //            }
    //        }
    //
    //        private boolean isJavaContext(JTextComponent target) {
    //            JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(target);
    //            int carretOffset = target.getCaret().getDot();
    //            try {
    //                TokenItem tok = sup.getTokenChain(carretOffset, carretOffset + 1);
    //                return tok.getTokenContextPath().contains(JavaTokenContext.contextPath);
    //            }catch(BadLocationException e) {
    //                //do nothing
    //                return true;
    //            }
    //        }
    //
    //    }

    
}

