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

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.jmi.reflect.JmiException;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.java.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.jmi.javamodel.Catch;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.JavaPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.StatementBlock;
import org.netbeans.jmi.javamodel.TryStatement;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.javacore.internalapi.JavaModelUtil;
import org.netbeans.modules.javacore.TryWrapper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.*;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider {
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

    public static final String expandAllJavadocFolds = "expand-all-javadoc-folds"; //NOI18N
    
    public static final String collapseAllJavadocFolds = "collapse-all-javadoc-folds"; //NOI18N

    public static final String expandAllCodeBlockFolds = "expand-all-code-block-folds"; //NOI18N
    
    public static final String collapseAllCodeBlockFolds = "collapse-all-code-block-folds"; //NOI18N
    
    public static final String selectNextElementAction = "select-element-next"; //NOI18N
    
    public static final String selectPreviousElementAction = "select-element-previous"; //NOI18N
    
    static final long serialVersionUID =-5445829962533684922L;
    
    /**
     * Construct the java kit instance.
     */
    public JavaKit() {
        org.netbeans.modules.java.editor.JavaEditorModule.init();        
    }
    
    public String getContentType() {
        return JAVA_MIME_TYPE;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(JavaKit.class);
    }

    public Document createDefaultDocument() {
        BaseDocument doc = new NbEditorDocument(this.getClass());
        // Force '\n' as write line separator // !!! move to initDocument()
        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, BaseDocument.LS_LF);
        return doc;
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new JavaSyntax(getSourceLevel((BaseDocument)doc));
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new NbJavaJMISyntaxSupport(doc);
    }

//    public Completion createCompletion(ExtEditorUI extEditorUI) {
//        return null;
//        return compatibleCompletion
//            ? new NbJavaCompletion(extEditorUI)
//            : new NbJavaJMICompletion(extEditorUI, getSourceLevel(extEditorUI.getDocument()));
//    }

    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
        return new NbJMICompletionJavaDoc(extEditorUI);
    }

    public String getSourceLevel(BaseDocument doc) {
        DataObject dob = NbEditorUtilities.getDataObject(doc);
        return dob != null ? SourceLevelQuery.getSourceLevel(dob.getPrimaryFile()) : null;
    }

    /** Create the formatter appropriate for this kit */
    public Formatter createFormatter() {
        return new JavaFormatter(this.getClass());
    }

    protected void toolTipAnnotationsLock(Document doc) {
        JavaModel.getJavaRepository().beginTrans(false);
    }
    
    protected void toolTipAnnotationsUnlock(Document doc) {
        JavaModel.getJavaRepository().endTrans();
    }
    

    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
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
      }

    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
                                   new JavaDefaultKeyTypedAction(),
                                   new PrefixMakerAction(makeGetterAction, "get", getSetIsPrefixes), // NOI18N
                                   new PrefixMakerAction(makeSetterAction, "set", getSetIsPrefixes), // NOI18N
                                   new PrefixMakerAction(makeIsAction, "is", getSetIsPrefixes), // NOI18N
                                   new AbbrevDebugLineAction(),
                                   new CommentAction("//"), // NOI18N
                                   new UncommentAction("//"), // NOI18N
                                   new FastImportAction(),
                                   //new GotoClassAction(),
                                   
                                   new JavaGenerateGoToPopupAction(),
                                   new JavaGotoSuperImplementation(),
				   new JavaInsertBreakAction(),
				   new JavaDeleteCharAction(deletePrevCharAction, false),
                                   new ExpandAllJavadocFolds(),
                                   new CollapseAllJavadocFolds(),
                                   new ExpandAllCodeBlockFolds(),
                                   new CollapseAllCodeBlockFolds(),
                                   new JavaGenerateFoldPopupAction(),
                                   new SelectCodeElementAction(selectNextElementAction, true),
                                   new SelectCodeElementAction(selectPreviousElementAction, false)
                               };
                               
        Action[] jmiAction = new Action[] {
                                   new JavaJMIGotoHelpAction(),
                                   new JavaJMIGotoSourceAction(),
                                   new JavaJMIGotoDeclarationAction(),
                                   new JavaFixAllImports(),
                                   new TryCatchAction(),
                                };
        
                                // PENDING merge these classes
                                /*
        Action[] jcAction = new Action[] {
                                   new JavaGotoHelpAction(),
                                   new JavaGotoSourceAction(),
                                   new JavaGotoDeclarationAction(),
                                   new JavaDocShowAction()                                   
                                };
                                 */
        
        
        Action[] mergedActions = TextAction.augmentList(javaActions, jmiAction);
        return TextAction.augmentList(super.createActions(), mergedActions);
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


    public static class JavaGotoDeclarationAction extends GotoDeclarationAction {

        public JavaGotoDeclarationAction () {
            putValue ("helpID", JavaGotoDeclarationAction.class.getName ()); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Completion completion = ExtUtilities.getCompletion(target);
                SyntaxSupport sup = Utilities.getSyntaxSupport(target);
                NbJavaSyntaxSupport nbJavaSup = (NbJavaSyntaxSupport)sup.get(NbJavaSyntaxSupport.class);
                if (completion != null) {
                    String itemDesc = null;
                    Object item = null;
                    if (completion.isPaneVisible()) {
                        item = JCExtension.findItemAtCaretPos(target);
                        itemDesc = nbJavaSup.openSource(item, true);
                    }else{
                        boolean found = false;
                        int dotPos = target.getCaret().getDot();
                        BaseDocument doc = (BaseDocument)target.getDocument();
                        try {
                            int[] idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock(doc, dotPos);
                            if (idFunBlk != null && idFunBlk.length == 2) { // id but not function
                                int pos = Utilities.getFirstNonWhiteBwd(doc, idFunBlk[0]);
                                if (pos < 0 || doc.getChars(pos, 1)[0] != '.') { // because 'this.var' could search for local var
                                    found = gotoDeclaration(target);
                                }
                            }
                            
                            if (!found) {
                                item = JCExtension.findItemAtCaretPos(target);
                                itemDesc = nbJavaSup.openSource(item, true);
                            }
                        } catch (BadLocationException e) {
                        }
                    }
                    if (itemDesc != null) {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        boolean isPkg = (item instanceof JCPackage);
                        String msg = NbBundle.getBundle(JavaKit.class).getString(
                                  isPkg ? "goto_source_package_not_found" : "goto_source_source_not_found"); // NOI18N
                        org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { itemDesc } ));
                    }
                }
            }
        }
    }
    
    public static class JavaJMIGotoDeclarationAction extends JavaGotoDeclarationAction {

        public JavaJMIGotoDeclarationAction () {
            putValue ("helpID", JavaJMIGotoDeclarationAction.class.getName ()); // NOI18N
        }

        protected boolean asynchonous() {
            return false;
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            Runnable run = new Runnable() {
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        RequestProcessor.getDefault().post(this);
                        return;
                    }
                    if (target != null) {
                        SyntaxSupport sup = Utilities.getSyntaxSupport(target);
                        NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);
                        
                        BaseDocument doc = (BaseDocument)target.getDocument();
                        JMIUtils jmiUtils = JMIUtils.get(doc);
                        
                        Object item = null;
                        String itemDesc = null;
                        jmiUtils.beginTrans(false);
                        try {
                            item = jmiUtils.findItemAtCaretPos(target);
                            if (item instanceof NbJMIResultItem.VarResultItem) {
                                int pos = nbJavaSup.findLocalDeclarationPosition(((NbJMIResultItem.VarResultItem)item).getItemText(), target.getCaretPosition());
                                target.setCaretPosition(pos);
                                JumpList.checkAddEntry(target);
                            } else {
                                if (item instanceof ClassDefinition)
                                    item = JMIUtils.getSourceElementIfExists((ClassDefinition)item);
                                itemDesc = nbJavaSup.openSource(item, true);
                            }
                        } finally {
                            jmiUtils.endTrans(false);
                        }
                        if (itemDesc != null) { // not found
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            boolean isPkg = (item instanceof JavaPackage);
                            String msg = NbBundle.getBundle(JavaKit.class).getString(
                                    isPkg ? "goto_source_package_not_found" : "goto_source_source_not_found"); // NOI18N
                            org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { itemDesc } ));
                        }
                    }
                }
            };
            JavaMetamodel.getManager().invokeAfterScanFinished(run, NbBundle.getMessage(BaseAction.class, "goto-declaration"));
        }
    }
    
    public static class JavaGotoSourceAction extends BaseAction {
        
        public JavaGotoSourceAction() {
            super(gotoSourceAction, SAVE_POSITION);
            putValue ("helpID", JavaGotoSourceAction.class.getName ()); // NOI18N
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/gotosource.gif"); // NOI18N
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            Runnable run = new Runnable() {
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        RequestProcessor.getDefault().post(this);
                        return;
                    }
                    if (target != null) {
                        String msg = openSource(target);
                        if (msg != null) { // not found
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
                        }
                    }
                }
            };
            JavaMetamodel.getManager().invokeAfterScanFinished(run, NbBundle.getMessage(BaseAction.class, "goto-source"));
        }

        public String openSource(JTextComponent target) {
            String itemDesc = null;
            SyntaxSupport sup = Utilities.getSyntaxSupport(target);
            NbJavaSyntaxSupport nbJavaSup = (NbJavaSyntaxSupport)sup.get(NbJavaSyntaxSupport.class);

            Object item = JCExtension.findItemAtCaretPos(target);
            itemDesc = nbJavaSup.openSource(item, false);
            if (itemDesc != null) {
                boolean isPkg = (item instanceof JCPackage);
                String msg = NbBundle.getBundle(JavaKit.class).getString(
                          isPkg ? "goto_source_package_not_found" : "goto_source_source_not_found"); // NOI18N
                return MessageFormat.format(msg, new Object [] { itemDesc } );
            }
            return null;
        }
        
        public String getPopupMenuText(JTextComponent target) {
            return NbBundle.getBundle(JavaKit.class).getString("goto_source_open_source_not_formatted"); //NOI18N//openSource(target, true); // simulate open
        }
    }
    
    public static class JavaJMIGotoSourceAction extends JavaGotoSourceAction {

        protected boolean asynchonous() {
            return false;
        }
        
        public String openSource(JTextComponent target) {
            String itemDesc = null;
            SyntaxSupport sup = Utilities.getSyntaxSupport(target);
            NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);

            BaseDocument doc = (BaseDocument)target.getDocument();
            JMIUtils jmiUtils = JMIUtils.get(doc);

            Object item = null;
            jmiUtils.beginTrans(false);
            try {
                item = jmiUtils.findItemAtCaretPos(target);
                if (item instanceof NbJMIResultItem.VarResultItem) {
                    item = ((NbJMIResultItem.VarResultItem)item).getType();
                }
                if (item instanceof ClassDefinition)
                    item = JMIUtils.getSourceElementIfExists((ClassDefinition)item);
                itemDesc = nbJavaSup.openSource(item, false);
            } finally {
                jmiUtils.endTrans(false);
            }
            if (itemDesc != null) {
                boolean isPkg = (item instanceof JavaPackage);
                String msg = NbBundle.getBundle(JavaKit.class).getString(
                          isPkg ? "goto_source_package_not_found" : "goto_source_source_not_found"); // NOI18N
                return MessageFormat.format(msg, new Object [] { itemDesc } );
            }
            return null;
        }

        public String getPopupMenuText(JTextComponent target) {
            return NbBundle.getBundle(JavaKit.class).getString("goto_source_open_source_not_formatted"); //NOI18N//openSource(target, true); // simulate open
        }

        protected Class getShortDescriptionBundleClass() {
            return BaseKit.class;
        }
        
    }

    public static class JavaGotoSuperImplementation extends BaseAction {
        
        public JavaGotoSuperImplementation() {
            super(gotoSuperImplementationAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("goto-super-implementation"));
            String name = NbBundle.getBundle(JavaKit.class).getString("goto-super-implementation-trimmed");
            putValue(ExtKit.TRIMMED_TEXT,name);
            putValue(POPUP_MENU_TEXT, name);
        }
  
//        public String getPopupMenuText(JTextComponent target) {
//            return openSource(target, true); // simulate open
//        }
        

        protected boolean asynchonous() {
            return false;
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            Runnable run = new Runnable() {
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        RequestProcessor.getDefault().post(this);
                        return;
                    }
                    Node selNode = NbEditorUtilities.getTopComponent(target).getActivatedNodes()[0];
                    Element feature = (Element) selNode.getLookup().lookup(Element.class);
                    
                    if (!(feature instanceof Method)) {
                        return;
                    }
                    
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    JMIUtils jmiUtils = JMIUtils.get(doc);
                    FileObject fo = NbEditorUtilities.getDataObject(doc).getPrimaryFile();
                    
                    Method f = null;
                    jmiUtils.beginTrans(false);
                    try {
                        JavaMetamodel.getManager().setClassPath(fo,true);
                        Collection methods = JavaModelUtil.getOverriddenMethods((Method) feature);
                        if (!methods.isEmpty()) {
                            f = (Method)methods.iterator().next();
                        }
                    } finally {
                        jmiUtils.endTrans(false);
                    }
                    
                    SyntaxSupport sup = Utilities.getSyntaxSupport(target);
                    NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);
                    
                    nbJavaSup.openSource(f, false);
                }
            };
            JavaMetamodel.getManager().invokeAfterScanFinished(run, NbBundle.getMessage(BaseAction.class, "goto-super-implementation"));
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
        
        protected void addAction(JTextComponent target, JMenu menu, Action a){
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
            //addAction(target, jm, ExtKit.gotoSourceAction);
            //addAction(target, jm, ExtKit.gotoDeclarationAction);
            //addAction(target, jm, gotoSuperImplementationAction);
            //addAction(target, jm, ExtKit.gotoAction);
            //addAction(target, jm, JavaFastOpenAction.getInstance());
            return jm;
        }
    
    }
    
    public static class JavaGotoHelpAction extends BaseAction {

        public JavaGotoHelpAction() {
            super(gotoHelpAction, SAVE_POSITION);
            putValue ("helpID", JavaGotoHelpAction.class.getName ()); // NOI18N
            // fix of #25090; [PENDING] there should be more systematic solution for this problem
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("java-desc-goto-help"));
        }

        public URL[] getJavaDocURLs(JTextComponent target) {
            SyntaxSupport sup = Utilities.getSyntaxSupport(target);
            NbJavaSyntaxSupport nbJavaSup = (NbJavaSyntaxSupport)sup.get(NbJavaSyntaxSupport.class);

            Object item = JCExtension.findItemAtCaretPos(target);
            return (item == null) ? null : nbJavaSup.getJavaDocURLs(item);                    
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                URL[] urls = getJavaDocURLs(target);
                if (urls != null && urls.length > 0) {
                    org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(urls[0]); // show first URL
                } else {
                    Utilities.setStatusText (target, NbBundle.getBundle(JavaKit.class).getString("cannot_find_javadoc"));
                    java.awt.Toolkit.getDefaultToolkit ().beep ();
                }
            }
        }

        public String getPopupMenuText(JTextComponent target) {
            return NbBundle.getBundle(JavaKit.class).getString("show_javadoc"); // NOI18N
        }

    }

    public static class JavaJMIGotoHelpAction extends JavaGotoHelpAction {

        protected boolean asynchonous() {
            return true;
        }

        public URL[] getJavaDocURLs(JTextComponent target) {
            SyntaxSupport sup = Utilities.getSyntaxSupport(target);
            NbJavaJMISyntaxSupport nbJavaSup = (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class);

            BaseDocument doc = (BaseDocument)target.getDocument();
            JMIUtils jmiUtils = JMIUtils.get(doc);

            jmiUtils.beginTrans(false);
            try {
                Object item = jmiUtils.findItemAtCaretPos(target);
                return (item == null) ? null : nbJavaSup.getJavaDocURLs(item);
            } finally {
                jmiUtils.endTrans(false);
            }
        }

    }
    
    
    public static class AbbrevDebugLineAction extends BaseAction {

        public AbbrevDebugLineAction() {
            super(abbrevDebugLineAction);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
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
    
    public static class FastImportAction extends BaseAction {
    
        public FastImportAction() {
            super(fastImportAction);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            Runnable run = new Runnable() {
                public void run() {
                    if (target != null) {
                        new NbJavaJMIFastImport(target).setDialogVisible(true);
                    }
                }
            };
            JavaMetamodel.getManager().invokeAfterScanFinished(run, NbBundle.getMessage(BaseAction.class, "fast-import"));
        }
        
    }

    /*
    public static class GotoClassAction extends BaseAction {

        static final long serialVersionUID =8425585413146373256L;

        public GotoClassAction() {
            super(gotoClassAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("NAME_JavaFastOpenAction"));
            putValue(ExtKit.TRIMMED_TEXT, NbBundle.getBundle(JavaKit.class).getString("goto-class-trimmed"));
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            NbJavaJMIFastOpen.showFastOpen();
        }

    }
    
     */

    public static class TryCatchAction extends BaseAction {
        
        public TryCatchAction() {
            super(tryCatchAction);
        }

        protected boolean asynchonous() {
            return true;
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                BaseDocument doc = (BaseDocument)target.getDocument();
                int caretPosition = -1;
                TryStatement t;
                int selectionStart = target.getSelectionStart();
                int selectionEnd = target.getSelectionEnd();
                if (selectionStart == selectionEnd) {
                    //no selection - select current row
                    try {
                        selectionStart = Utilities.getRowFirstNonWhite(doc, selectionStart);
                        selectionEnd = Utilities.getRowLastNonWhite(doc,selectionEnd)+1;
                    } catch (BadLocationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
                JavaModel.getJavaRepository().beginTrans(true);
                try {
                    //create a wrapper and wrap selected text
                    TryWrapper wrapper;
                    FileObject fo = NbEditorUtilities.getDataObject(doc).getPrimaryFile();
                    JavaModel.setClassPath(fo);
                    
                    wrapper = new TryWrapper(fo, selectionStart, selectionEnd);
                    t = wrapper.wrap(); 
                } catch (JmiException e) {
                    //if error - write it on status line
                    Utilities.setStatusBoldText(target, e.getLocalizedMessage());
                    return ;
                } finally {
                    JavaModel.getJavaRepository().endTrans();
                }
                    
                JavaModel.getJavaRepository().beginTrans(false);
                try {
                    StatementBlock finalizer = t.getFinalizer();
                    if (finalizer != null) {
                        caretPosition = finalizer.getEndOffset() - 1;
                    } else {
                        Catch cat = (Catch) t.getCatches().get(0);
                        caretPosition = cat.getEndOffset() - 1;
                    }
                }
                finally {
                    JavaModel.getJavaRepository().endTrans();
                }
                if (caretPosition != -1) {
                    target.setCaretPosition(caretPosition);
                    target.setSelectionStart(caretPosition);
                    target.setSelectionEnd(caretPosition);
                }

            }
        }
        
        protected Class getShortDescriptionBundleClass() {
            return BaseKit.class;
        }
        
        /** Surround With try-catch action in Source main menu, wrapper for TryCatchAction
         */ 
        public static final class MainMenuWrapper extends MainMenuAction {

            private JMenuItem menuItem;

            public MainMenuWrapper () {
                super();
                menuItem = new JMenuItem(getMenuItemText());
                setMenu();
            }

            protected String getMenuItemText () {
                return NbBundle.getBundle(MainMenuWrapper.class).getString(
                    "try_catch_main_menu_item"); //NOI18N
            }

            public JMenuItem getMenuPresenter () {
                return menuItem;
            }

            protected String getActionName () {
                return JavaKit.tryCatchAction;
            }

        } // end of MainMenuWrapper
        
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

}
