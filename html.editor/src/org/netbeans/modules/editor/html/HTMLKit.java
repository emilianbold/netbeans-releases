/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.html;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;

import org.netbeans.editor.*;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.html.*;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.modules.html.editor.folding.HTMLFoldTypes;
import org.openide.util.NbBundle;

/**
* Editor kit implementation for HTML content type
*
* @author Miloslav Metelka
* @version 1.00
*/

public class HTMLKit extends org.netbeans.modules.editor.NbEditorKit {

    static final long serialVersionUID =-1381945567613910297L;

    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    public static final String shiftInsertBreakAction = "shift-insert-break"; // NOI18N
    
    //comment folds
    public static final String collapseAllCommentsAction = "collapse-all-comment-folds"; //NOI18N
    
    public static final String expandAllCommentsAction = "expand-all-comment-folds"; //NOI18N
    
    private static boolean setupReadersInitialized = false;


    public HTMLKit(){
        if (!setupReadersInitialized){
            NbReaderProvider.setupReaders();
            setupReadersInitialized = true;
        }
    }
    
    public String getContentType() {
        return HTML_MIME_TYPE;
    }
    
    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return new HTMLCompletionJavaDoc (extEditorUI);
    }
    
    protected void initDocument(BaseDocument doc) {
        /*doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);*/
        doc.addDocumentListener(new HTMLDrawLayerFactory.TagParenWatcher());
    }
    
    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new HTMLSyntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new HTMLSyntaxSupport(doc);
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return new HTMLCompletion(extEditorUI);
    }
    
    public Formatter createFormatter() {
        //return new LineWrapFormatter(this.getClass());
        return new HTMLFormatter(this.getClass());
    }

    protected Action[] createActions() {
        Action[] HTMLActions = new Action[] {
                                   new HTMLShiftBreakAction(),
                                   // replace MatchBraceAction with HtmlEditor own
                                   new MatchBraceAction(ExtKit.matchBraceAction, false),
                                   new MatchBraceAction(ExtKit.selectionMatchBraceAction, true),
                                   new HTMLGenerateFoldPopupAction(),
                                    new CollapseAllCommentsFolds(),
                                    new ExpandAllCommentsFolds()
                               };
        return TextAction.augmentList(super.createActions(), HTMLActions);
    }

    public static class HTMLShiftBreakAction extends BaseAction {

        static final long serialVersionUID =4004043376345356061L;

        public HTMLShiftBreakAction() {
            super( shiftInsertBreakAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Completion completion = ExtUtilities.getCompletion(target);
                if (completion != null && completion.isPaneVisible()) {
                    if (completion.substituteText( true )) {
//                        completion.setPaneVisible(false);
                    } else {
                        completion.refresh(false);
                    }
                }
            }
        }

    }
    
    /** This is implementation for MatchBraceAction for HTML file. 
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
                    // is the token from HTML Syntax??
                    if (token != null && token.getTokenContextPath().contains(HTMLTokenContext.contextPath)){           
                        if (dotPos > 0) {
                            int[] matchBlk = sup.findMatchingBlock(dotPos - 1, false);
                            if (matchBlk != null) {
                                dotPos = matchBlk[0];
                                token = sup.getTokenChain(dotPos, dotPos+1);
                                // find the first html tag in the matching block
                                while (!(token != null
                                        && (token.getTokenID().getNumericID() == HTMLTokenContext.TAG_OPEN_ID ||
                                            token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_ID)
                                        && token.getTokenContextPath().contains(HTMLTokenContext.contextPath))) {
                                    token = token.getNext();
                                }
                                // place the curret after the html tag name     
                                if (select) {
                                    caret.moveDot(token.getOffset()+token.getImage().length());
                                } else {
                                    caret.setDot(token.getOffset()+token.getImage().length());
                                }
                            }
                        }
                    }
                    else{   // If this is not token from HTML Syntax -> call the original action from editor.
                        super.actionPerformed(evt, target);
                    }

                }
                catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }
    
    public static class HTMLGenerateFoldPopupAction extends GenerateFoldPopupAction {
        
        protected void addAdditionalItems(JTextComponent target, JMenu menu){
            addAction(target, menu, collapseAllCommentsAction);
            addAction(target, menu, expandAllCommentsAction);
        }
    }
    
    public static class ExpandAllCommentsFolds extends BaseAction{
        public ExpandAllCommentsFolds(){
            super(expandAllCommentsAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(HTMLKit.class).getString("expand-all-comment-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(HTMLKit.class).getString("popup-expand-all-comment-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.expand(hierarchy, HTMLFoldTypes.COMMENT);
        }
    }
    
    public static class CollapseAllCommentsFolds extends BaseAction{
        public CollapseAllCommentsFolds(){
            super(collapseAllCommentsAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(HTMLKit.class).getString("collapse-all-comment-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(HTMLKit.class).getString("popup-collapse-all-comment-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.collapse(hierarchy, HTMLFoldTypes.COMMENT);
        }
    }  
    
}
