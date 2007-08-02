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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.java.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.editor.ext.ExtKit.PrefixMakerAction;
import org.netbeans.editor.ext.ExtKit.UncommentAction;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.codegen.GenerateCodeAction;
import org.netbeans.modules.java.editor.imports.FastImportAction;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports;
import org.netbeans.modules.java.editor.overridden.GoToSuperTypeAction;
import org.netbeans.modules.java.editor.rename.InstantRenameAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.awt.Mnemonics;
import org.openide.util.*;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaKit extends NbEditorKit {

    public static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    private static final String[] getSetIsPrefixes = new String[] {
                "get", "set", "is" // NOI18N
            };

    /** Switch first letter of word to capital and insert 'get'
    * at word begining.
    */
    public static final String makeGetterAction = "make-getter"; // NOI18N

    /** Switch first letter of word to capital and insert 'set'
    * at word begining.
    */
    public static final String makeSetterAction = "make-setter"; // NOI18N

    /** Switch first letter of word to capital and insert 'is'
    * at word begining.
    */
    public static final String makeIsAction = "make-is"; // NOI18N

    /** Add the watch depending on the context under the caret */
    public static final String addWatchAction = "add-watch"; // NOI18N

    /** Toggle the breakpoint of the current line */
    public static final String toggleBreakpointAction = "toggle-breakpoint"; // NOI18N

    /** Debug source and line number */
    public static final String abbrevDebugLineAction = "abbrev-debug-line"; // NOI18N

    /** Menu item for adding all necessary imports in a file */
    public static final String fixImportsAction = "fix-imports"; // NOI18N
    
    /** Open dialog for choosing the import statement to be added */
    public static final String fastImportAction = "fast-import"; // NOI18N
    
    /** Opens Go To Class dialog */
    //public static final String gotoClassAction = "goto-class"; //NOI18N

    public static final String tryCatchAction = "try-catch"; // NOI18N

    public static final String javaDocShowAction = "javadoc-show-action"; // NOI18N
    
    public static final String expandAllJavadocFolds = "expand-all-javadoc-folds"; //NOI18N
    
    public static final String collapseAllJavadocFolds = "collapse-all-javadoc-folds"; //NOI18N

    public static final String expandAllCodeBlockFolds = "expand-all-code-block-folds"; //NOI18N
    
    public static final String collapseAllCodeBlockFolds = "collapse-all-code-block-folds"; //NOI18N
    
    public static final String selectNextElementAction = "select-element-next"; //NOI18N
    
    public static final String selectPreviousElementAction = "select-element-previous"; //NOI18N
    
    /* package */ static final String previousCamelCasePosition = "previous-camel-case-position"; //NOI18N
    
    /* package */ static final String nextCamelCasePosition = "next-camel-case-position"; //NOI18N
    
    /* package */ static final String selectPreviousCamelCasePosition = "select-previous-camel-case-position"; //NOI18N
    
    /* package */ static final String selectNextCamelCasePosition = "select-next-camel-case-position"; //NOI18N
    
    /* package */ static final String deletePreviousCamelCasePosition = "delete-previous-camel-case-position"; //NOI18N
    
    /* package */ static final String deleteNextCamelCasePosition = "delete-next-camel-case-position"; //NOI18N
    
    static final long serialVersionUID =-5445829962533684922L;
    

    public JavaKit(){
        org.netbeans.modules.java.editor.JavaEditorModule.init();
    }
    
    public String getContentType() {
        return JAVA_MIME_TYPE;
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new JavaSyntax(getSourceLevel((BaseDocument)doc));
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return null;
    }

    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return null;
    }

    @Override
    public Document createDefaultDocument() {
        Document doc = new JavaDocument(this.getClass());
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        if (mimeType == null){
            doc.putProperty("mimeType", getContentType()); //NOI18N
        }
        return doc;
    }
    
    public String getSourceLevel(BaseDocument doc) {
        DataObject dob = NbEditorUtilities.getDataObject(doc);
        return dob != null ? SourceLevelQuery.getSourceLevel(dob.getPrimaryFile()) : null;
    }

    /** Create the formatter appropriate for this kit */
    public Formatter createFormatter() {
        return new JavaFormatter(this.getClass());
    }

    protected void initDocument(BaseDocument doc) {
//        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
//                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
        doc.putProperty(SyntaxUpdateTokens.class,
              new SyntaxUpdateTokens() {
                  
                  private List tokenList = new ArrayList();
                  
                  public void syntaxUpdateStart() {
                      tokenList.clear();
                  }
      
                  public List syntaxUpdateEnd() {
                      return tokenList;
                  }
      
                  public void syntaxUpdateToken(TokenID id, TokenContextPath contextPath, int offset, int length) {
                      if (JavaTokenContext.LINE_COMMENT == id) {
                          tokenList.add(new TokenInfo(id, contextPath, offset, length));
                      }
                  }
              }
          );
	  
	  //do not ask why, fire bug in the IZ:
	  CodeTemplateManager.get(doc);
      }

    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        Action[] javaActions = new Action[] {
                                   new JavaDefaultKeyTypedAction(),
                                   new PrefixMakerAction(makeGetterAction, "get", getSetIsPrefixes), // NOI18N
                                   new PrefixMakerAction(makeSetterAction, "set", getSetIsPrefixes), // NOI18N
                                   new PrefixMakerAction(makeIsAction, "is", getSetIsPrefixes), // NOI18N
                                   new AbbrevDebugLineAction(),
                                   new ToggleCommentAction("//"), // NOI18N
                                   new JavaGenerateGoToPopupAction(),
				   new JavaInsertBreakAction(),
				   new JavaDeleteCharAction(deletePrevCharAction, false),
                                   new ExpandAllJavadocFolds(),
                                   new CollapseAllJavadocFolds(),
                                   new ExpandAllCodeBlockFolds(),
                                   new CollapseAllCodeBlockFolds(),
                                   new JavaGenerateFoldPopupAction(),
                                   new JavaGoToDeclarationAction(),
                                   new JavaGoToSourceAction(),
                                   new JavaGotoHelpAction(),
				   new InstantRenameAction(),
                                   new JavaFixImports(),
                                   new GenerateCodeAction(),
                                   new SelectCodeElementAction(selectNextElementAction, true),
                                   new SelectCodeElementAction(selectPreviousElementAction, false),
                                   
                                   new NextCamelCasePosition(findAction(superActions, nextWordAction)),
                                   new PreviousCamelCasePosition(findAction(superActions, previousWordAction)),
                                   new SelectNextCamelCasePosition(findAction(superActions, selectionNextWordAction)),
                                   new SelectPreviousCamelCasePosition(findAction(superActions, selectionPreviousWordAction)),
                                   new DeleteToNextCamelCasePosition(findAction(superActions, removeNextWordAction)),
                                   new DeleteToPreviousCamelCasePosition(findAction(superActions, removePreviousWordAction)),
                                   
                                   new FastImportAction(),
                                   new GoToSuperTypeAction(),
                               };
                               
        return TextAction.augmentList(superActions, javaActions);
    }

    private static Action findAction(Action [] actions, String name) {
        for(Action a : actions) {
            Object nameObj = a.getValue(Action.NAME);
            if (nameObj instanceof String && name.equals(nameObj)) {
                return a;
            }
        }
        return null;
    }
    
    public static class JavaDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        protected void insertString(BaseDocument doc, int dotPos,
                                    Caret caret, String str,
                                    boolean overwrite) throws BadLocationException {
            char insertedChar = str.charAt(0);
            if (insertedChar == '\"' || insertedChar == '\''){
                boolean inserted = BracketCompletion.completeQuote(doc, dotPos, caret, insertedChar);
                if (inserted){
                    caret.setDot(dotPos+1);
                }else{
                    super.insertString(doc, dotPos, caret, str, overwrite);
                    
                }
            } else {
                super.insertString(doc, dotPos, caret, str, overwrite);
                BracketCompletion.charInserted(doc, dotPos, caret, insertedChar);
            }
        }
        
        protected void replaceSelection(JTextComponent target,
                int dotPos,
                Caret caret,
                String str,
                boolean overwrite)
                throws BadLocationException {
            char insertedChar = str.charAt(0);
            Document doc = target.getDocument();
            if (insertedChar == '\"' || insertedChar == '\''){
                if (doc != null) {
                    try {
                        boolean inserted = false;
                        int p0 = Math.min(caret.getDot(), caret.getMark());
                        int p1 = Math.max(caret.getDot(), caret.getMark());
                        if (p0 != p1) {
                            doc.remove(p0, p1 - p0);
                        }
                        int caretPosition = caret.getDot();
                        if (doc instanceof BaseDocument){
                            inserted = BracketCompletion.completeQuote(
                                    (BaseDocument)doc,
                                    caretPosition,
                                    caret, insertedChar);
                        }
                        if (inserted){
                            caret.setDot(caretPosition+1);
                        } else {
                            if (str != null && str.length() > 0) {
                                doc.insertString(p0, str, null);
                            }
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                super.replaceSelection(target, dotPos, caret, str, overwrite);
                if (doc instanceof BaseDocument){
                    BracketCompletion.charInserted((BaseDocument)doc, caret.getDot()-1, caret, insertedChar);
                }
            }
        }
    }


    public static class JavaGenerateGoToPopupAction extends NbGenerateGoToPopupAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

        private void addAcceleretors(Action a, JMenuItem item, JTextComponent target){
            // Try to get the accelerator
            Keymap km = target.getKeymap();
            if (km != null) {
                
                KeyStroke[] keys = km.getKeyStrokesForAction(a);
                if (keys != null && keys.length > 0) {
                    item.setAccelerator(keys[0]);
                }else if (a!=null){
                    KeyStroke ks = (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);
                    if (ks!=null) {
                        item.setAccelerator(ks);
                    }
                }
            }
        }
        
        private void addAction(JTextComponent target, JMenu menu, Action a){
            if (a != null) {
                String actionName = (String) a.getValue(Action.NAME);
                JMenuItem item = null;
                if (a instanceof BaseAction) {
                    item = ((BaseAction)a).getPopupMenuItem(target);
                }
                if (item == null) {
                    // gets trimmed text that doesn' contain "go to"
                    String itemText = (String)a.getValue(ExtKit.TRIMMED_TEXT); 
                    if (itemText == null){
                        itemText = getItemText(target, actionName, a);
                    }
                    if (itemText != null) {
                        item = new JMenuItem(itemText);
                        Mnemonics.setLocalizedText(item, itemText);                        
                        item.addActionListener(a);
                        addAcceleretors(a, item, target);
                        item.setEnabled(a.isEnabled());
                        Object helpID = a.getValue ("helpID"); // NOI18N
                        if (helpID != null && (helpID instanceof String))
                            item.putClientProperty ("HelpID", helpID); // NOI18N
                    }else{
                        if (ExtKit.gotoSourceAction.equals(actionName)){
                            item = new JMenuItem(NbBundle.getBundle(JavaKit.class).getString("goto_source_open_source_not_formatted")); //NOI18N
                            addAcceleretors(a, item, target);
                            item.setEnabled(false);
                        }
                    }
                }

                if (item != null) {
                    menu.add(item);
                }

            }            
        }
        
        protected void addAction(JTextComponent target, JMenu menu,
        String actionName) {
            BaseKit kit = Utilities.getKit(target);
            if (kit == null) return;
            Action a = kit.getActionByName(actionName);
            if (a!=null){
                addAction(target, menu, a);
            } else { // action-name is null, add the separator
                menu.addSeparator();
            }
        }        
        
        protected String getItemText(JTextComponent target, String actionName, Action a) {
            String itemText;
            if (a instanceof BaseAction) {
                itemText = ((BaseAction)a).getPopupMenuText(target);
            } else {
                itemText = actionName;
            }
            return itemText;
        }
        
        public JMenuItem getPopupMenuItem(final JTextComponent target) {
            String menuText = NbBundle.getBundle(JavaKit.class).getString("generate-goto-popup"); //NOI18N
            JMenu jm = new JMenu(menuText);
            addAction(target, jm, ExtKit.gotoSourceAction);
            addAction(target, jm, ExtKit.gotoDeclarationAction);
            addAction(target, jm, gotoSuperImplementationAction);
            addAction(target, jm, ExtKit.gotoAction);
            return jm;
        }
    
    }
    
    
    public static class AbbrevDebugLineAction extends BaseAction {

        public AbbrevDebugLineAction() {
            super(abbrevDebugLineAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                BaseDocument doc = (BaseDocument)target.getDocument();
                StringBuffer sb = new StringBuffer("System.out.println(\""); // NOI18N
                String title = (String)doc.getProperty(Document.TitleProperty);
                if (title != null) {
                    sb.append(title);
                    sb.append(':');
                }
                try {
                    sb.append(Utilities.getLineOffset(doc, target.getCaret().getDot()) + 1);
                } catch (BadLocationException e) {
                }
                sb.append(' ');

                BaseKit kit = Utilities.getKit(target);
                if (kit == null) return;
                Action a = kit.getActionByName(BaseKit.insertContentAction);
                if (a != null) {
                    Utilities.performAction(
                        a,
                        new ActionEvent(target, ActionEvent.ACTION_PERFORMED, sb.toString()),
                        target
                    );
                }
            }
        }

    }
    

    public static class JavaInsertBreakAction extends InsertBreakAction {
        
        static final long serialVersionUID = -1506173310438326380L;
        
        protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
            int dotPos = caret.getDot();
            if (BracketCompletion.posWithinString(doc, dotPos)) {
                try {
                    doc.insertString(dotPos, "\" + \"", null); //NOI18N
                    dotPos += 3;
                    caret.setDot(dotPos);
                    return new Integer(dotPos);
                } catch (BadLocationException ex) {
                }
            } else {
                try {
                    if (BracketCompletion.isAddRightBrace(doc, dotPos)) {
                        int end = BracketCompletion.getRowOrBlockEnd(doc, dotPos);
                        doc.insertString(end, "}", null); // NOI18N
                        doc.getFormatter().indentNewLine(doc, end);                        
                        caret.setDot(dotPos);
                        return Boolean.TRUE;
                    }
                } catch (BadLocationException ex) {
                }
            }
            return null;
        }
        
        protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret, Object cookie) {
            if (cookie != null) {
                if (cookie instanceof Integer) {
                    // integer
                    int nowDotPos = caret.getDot();
                    caret.setDot(nowDotPos+1);
                }
            }
        }

  }


    public static class JavaDeleteCharAction extends ExtDeleteCharAction {
        
        public JavaDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }

        protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch)
        throws BadLocationException {
            BracketCompletion.charBackspaced(doc, dotPos, caret, ch);
        }
    }
    
    public static class ExpandAllJavadocFolds extends BaseAction{
        public ExpandAllJavadocFolds(){
            super(expandAllJavadocFolds);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("expand-all-javadoc-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-expand-all-javadoc-folds"));
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.expand(hierarchy, JavaFoldManager.JAVADOC_FOLD_TYPE);
        }
    }
    
    public static class CollapseAllJavadocFolds extends BaseAction{
        public CollapseAllJavadocFolds(){
            super(collapseAllJavadocFolds);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("collapse-all-javadoc-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-collapse-all-javadoc-folds"));
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.collapse(hierarchy, JavaFoldManager.JAVADOC_FOLD_TYPE);
        }
    }
    
    public static class ExpandAllCodeBlockFolds extends BaseAction{
        public ExpandAllCodeBlockFolds(){
            super(expandAllCodeBlockFolds);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("expand-all-code-block-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-expand-all-code-block-folds"));
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            List types = new ArrayList();
            types.add(JavaFoldManager.CODE_BLOCK_FOLD_TYPE);
            types.add(JavaFoldManager.IMPORTS_FOLD_TYPE);
            FoldUtilities.expand(hierarchy, types);
        }
    }
    
    public static class CollapseAllCodeBlockFolds extends BaseAction{
        public CollapseAllCodeBlockFolds(){
            super(collapseAllCodeBlockFolds);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("collapse-all-code-block-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-collapse-all-code-block-folds"));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            List types = new ArrayList();
            types.add(JavaFoldManager.CODE_BLOCK_FOLD_TYPE);
            types.add(JavaFoldManager.IMPORTS_FOLD_TYPE);
            FoldUtilities.collapse(hierarchy, types);
        }
    }
    
    public static class JavaGenerateFoldPopupAction extends GenerateFoldPopupAction{
        
        protected void addAdditionalItems(JTextComponent target, JMenu menu){
            addAction(target, menu, collapseAllJavadocFolds);
            addAction(target, menu, expandAllJavadocFolds);
            setAddSeparatorBeforeNextAction(true);
            addAction(target, menu, collapseAllCodeBlockFolds);
            addAction(target, menu, expandAllCodeBlockFolds);
        }
        
    }
    
    private static class JavaGoToDeclarationAction extends GotoDeclarationAction {
        public @Override boolean gotoDeclaration(JTextComponent target) {
            GoToSupport.goTo((BaseDocument) target.getDocument(), target.getCaretPosition(), false);
            return true;
        }
    }
    
    private static class JavaGoToSourceAction extends BaseAction {

        static final long serialVersionUID =-6440495023918097760L;

        public JavaGoToSourceAction() {
            super(gotoSourceAction,
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                  | SAVE_POSITION
                 );
            putValue(TRIMMED_TEXT, LocaleSupport.getString("goto-source-trimmed"));  //NOI18N            
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                GoToSupport.goTo((BaseDocument) target.getDocument(), target.getCaretPosition(), true);
            }
        }
        
        public String getPopupMenuText(JTextComponent target) {
            return NbBundle.getBundle(JavaKit.class).getString("goto_source_open_source_not_formatted"); //NOI18N
        }
        
        protected Class getShortDescriptionBundleClass() {
            return BaseKit.class;
        }
    }
    
    private static class JavaFixImports extends BaseAction {

        public JavaFixImports() {
            super(fixImportsAction,
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                 );
            putValue(TRIMMED_TEXT, NbBundle.getBundle(JavaKit.class).getString("fix-imports-trimmed"));
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("desc-fix-imports")); // NOI18N
            putValue(POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-fix-imports")); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Document doc = target.getDocument();
                FileObject fo = ((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile();
                
                JavaFixAllImports.getDefault().fixAllImports(fo);
            }
        }
        
        public static final class GlobalAction extends MainMenuAction {
            private final JMenuItem menuPresenter;

            public GlobalAction() {
                super();
                this.menuPresenter = new JMenuItem(getMenuItemText());
                setMenu();
            }

            protected String getMenuItemText() {
                return NbBundle.getBundle(GlobalAction.class).getString("fix-imports-main-menu-source-item"); //NOI18N
            }

            protected String getActionName() {
                return fixImportsAction;
            }

            public JMenuItem getMenuPresenter() {
                return menuPresenter;
            }
        }
    } // End of JavaFixImports action
    
    private static class JavaGotoHelpAction extends BaseAction {

        public JavaGotoHelpAction() {
            super(gotoHelpAction, ABBREV_RESET | MAGIC_POSITION_RESET
                    | UNDO_MERGE_RESET |SAVE_POSITION);
            putValue ("helpID", JavaGotoHelpAction.class.getName ()); // NOI18N
            // fix of #25090; [PENDING] there should be more systematic solution for this problem
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("java-desc-goto-help"));
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                GoToSupport.goToJavadoc(target.getDocument(), target.getCaretPosition());
            }
        }

        public String getPopupMenuText(JTextComponent target) {
            return NbBundle.getBundle(JavaKit.class).getString("show_javadoc"); // NOI18N
        }

    }
    
}
