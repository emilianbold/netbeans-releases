/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;

import java.awt.event.ActionEvent;
import java.beans.*;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.text.*;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLTokenContext;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.folding.JspFoldTypes;
import org.openide.ErrorManager;
import org.openide.cookies.*;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.CompletionJavaDoc;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.html.HTMLDrawLayerFactory;
import org.netbeans.editor.ext.java.JavaDrawLayerFactory;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;

/**
 * Editor kit implementation for JSP content type
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @versiob 1.5
 */
public class JSPKit extends NbEditorKit {
    
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
        super();
    }
    
    public String getContentType() {
        return JSP_MIME_TYPE;
    }
    
    /** Creates a new instance of the syntax coloring parser */
    public Syntax createSyntax(Document doc) {
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
            new JavaKit.JavaDocShowAction(),
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
                    new JspDeleteCharAction(deletePrevCharAction, false),
        };
        
        return TextAction.augmentList(super.createActions(), javaActions);
    }
    
    private static class ColoringListener implements PropertyChangeListener {
        private Document doc;
        private Object parsedDataRef; // hold a reference to the data we are listening on
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
        EditorKit kit = JEditorPane.createEditorKitForContentType(language);
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
            return new JspSyntaxSupport(doc,
                    JspContextInfo.getContextInfo().getCachedOpenInfo(doc, dobj.getPrimaryFile(), false).isXmlSyntax());
        }
        return new JspSyntaxSupport(doc, false);
        
    }
    
    /** Returns completion for given language (MIME type).
     * Note that JspJavaCompletion competion is returned instead of
     * NbJavaCompletion to add specific stuff related to Java class generated
     * from JSP.
     */
    private static Completion getCompletionForLanguage(
            ExtEditorUI extEditorUI, String language) {
        Completion compl = null;
        if (JavaKit.JAVA_MIME_TYPE.equals(language)) {
            compl = new JspJavaCompletion(extEditorUI);
        } else {
            EditorKit kit = JEditorPane.createEditorKitForContentType(language);
            if (kit instanceof ExtKit)
                compl = ((ExtKit)kit).createCompletion(extEditorUI);
        }
        return compl;
    }
    
    public Completion createCompletion(ExtEditorUI extEditorUI) {
        BaseDocument doc = extEditorUI.getDocument();
        FileObject fobj = null;
        if (doc != null){
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile(): null;
        }
        
        String mimeType = NbEditorUtilities.getMimeType(doc);
        Completion contentCompletion = (!(mimeType.equals(JSP_MIME_TYPE) || mimeType.equals(TAG_MIME_TYPE))) ?
            null :
            getCompletionForLanguage(extEditorUI, JspUtils.getContentLanguage());
        Completion scriptingCompletion = (!(mimeType.equals(JSP_MIME_TYPE) || mimeType.equals(TAG_MIME_TYPE))) ?
            null :
            getCompletionForLanguage(extEditorUI, JspUtils.getScriptingLanguage());
        final JspCompletion completion =
                new JspCompletion(extEditorUI, contentCompletion, scriptingCompletion);
        return completion;
        /*final JspDataObject jspdo = (dobj instanceof JspDataObject) ? (JspDataObject)dobj : null;
        Completion contentCompletion = (jspdo == null) ?
            null :
            getCompletionForLanguage(extEditorUI, jspdo.getContentLanguage());
        Completion scriptingCompletion = (jspdo == null) ?
            null :
            getCompletionForLanguage(extEditorUI, jspdo.getScriptingLanguage());
         
        final JspCompletion completion =
            new JspCompletion(extEditorUI, contentCompletion, scriptingCompletion);
        return completion;*/
        
    }
    
    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return new JspCompletionJavaDoc(extEditorUI);
    }
    
    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
        doc.addLayer(new ELDrawLayerFactory.ELLayer(),
                ELDrawLayerFactory.EL_LAYER_VISIBILITY);
        doc.addDocumentListener(new ELDrawLayerFactory.LParenWatcher());
        doc.addDocumentListener(new HTMLDrawLayerFactory.TagParenWatcher());
    }
    
    public Formatter createFormatter() {
        return new JspFormatter(this.getClass());
        
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
                    ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
                    
                    TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
                    /*if (token != null && token.getTokenContextPath().contains(JspTagTokenContext.contextPath)
                            && token.getTokenID().getNumericID() != JspTagTokenContext.TAG_ID)
                        token = token.getPrevious();*/
                    if (token != null && token.getTokenContextPath().contains(JspTagTokenContext.contextPath)
                    /*&& token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID*/){
                        boolean isScriptletDelimiter = token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID;
                        
                        if (dotPos > 0) {
                            int[] matchBlk = sup.findMatchingBlock(dotPos - 1, false);
                            if (matchBlk != null) {
                                dotPos = matchBlk[0];
                                if (!isScriptletDelimiter){
                                    // Find out, where the opossite close/open tag ends.
                                    token = sup.getTokenChain(dotPos, dotPos+1);
                                    while (token != null
                                            && !(token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID
                                            && token.getTokenContextPath().contains(JspTagTokenContext.contextPath))) {
                                        token = token.getNext();
                                    }
                                    if (select) {
                                        caret.moveDot(token.getOffset()+token.getImage().length());
                                    } else {
                                        caret.setDot(token.getOffset()+token.getImage().length());
                                    }
                                } else{
                                    if (select) {
                                        caret.moveDot(matchBlk[1]);
                                    } else {
                                        caret.setDot(matchBlk[1]);
                                    }
                                }
                            }
                        }
                    } else{
                        BaseKit kit = null;
                        try {
                            if (token != null && token.getTokenContextPath().contains(HTMLTokenContext.contextPath)){
                                kit = getKit(getClass().forName("org.netbeans.modules.editor.html.HTMLKit"));      //NOI18N
                            } else{
                                if (token != null && token.getTokenContextPath().contains(JavaTokenContext.contextPath)){
                                    kit = getKit(getClass().forName("org.netbeans.modules.editor.java.JavaKit"));  //NOI18N
                                }
                            }
                        } catch (java.lang.ClassNotFoundException e){
                            kit = null;
                            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                        }
                        if (kit != null){
                            Action action = kit.getActionByName(select ? ExtKit.selectionMatchBraceAction : ExtKit.matchBraceAction);
                            if (action != null && action instanceof ExtKit.MatchBraceAction){
                                ((ExtKit.MatchBraceAction)action).actionPerformed(evt, target);
                                return;
                            }
                        }
                        super.actionPerformed(evt, target);
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
            FoldUtilities.expand(hierarchy, JspFoldTypes.HTML_COMMENT);
            
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
            FoldUtilities.collapse(hierarchy, JspFoldTypes.HTML_COMMENT);
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

    private static TokenContextPath getTokenContextPath(Caret caret, Document doc){
        if (doc instanceof BaseDocument){
            int dotPos = caret.getDot();
            ExtSyntaxSupport sup = (ExtSyntaxSupport)((BaseDocument)doc).getSyntaxSupport();
            if (dotPos>0){
                try{
                    TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
                    if (token != null){
                        return token.getTokenContextPath();
                    }
                }catch(BadLocationException ble){
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ble);
                }
            }
        }
        return null;
    }
    
    public static class JspInsertBreakAction extends InsertBreakAction {
        public void actionPerformed(ActionEvent e, JTextComponent target) {
            if (target!=null){
                TokenContextPath path = getTokenContextPath(target.getCaret(), target.getDocument());

                if (path != null && path.contains(JavaTokenContext.contextPath)){
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
                TokenContextPath path = getTokenContextPath(target.getCaret(), target.getDocument());

                if (path != null && path.contains(JavaTokenContext.contextPath)){
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
                TokenContextPath path = getTokenContextPath(target.getCaret(), target.getDocument());

                if (path != null && path.contains(JavaTokenContext.contextPath)){
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
    
}
    
