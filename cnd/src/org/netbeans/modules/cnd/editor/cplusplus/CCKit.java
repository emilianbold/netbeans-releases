/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;
import javax.swing.JEditorPane;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.Keymap;
import javax.swing.text.Position;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;

import java.io.Writer;
import java.io.IOException;
import java.io.CharArrayWriter;
import org.netbeans.modules.cnd.editor.spi.cplusplus.GotoDeclarationProvider;
import org.openide.awt.Mnemonics;

import org.openide.util.Lookup;
import org.openide.loaders.DataObject;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.modules.editor.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.ext.CompletionJavaDoc;

import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.cnd.editor.CppFoldManagerBase;

import org.netbeans.modules.cnd.editor.spi.cplusplus.CompletionProvider;
import org.netbeans.modules.cnd.editor.spi.cplusplus.SyntaxSupportProvider;
import org.openide.util.NbBundle;

/**
* C++ editor kit with appropriate document
*/

public class CCKit extends NbEditorKit {

    public String getContentType() {
        return MIMENames.CPLUSPLUS_MIME_TYPE;
    }
    
    public void install(JEditorPane c) {
        super.install(c);
    }

    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
	CompletionProvider cc = (CompletionProvider) Lookup.getDefault().lookup(CompletionProvider.class);
	CompletionJavaDoc doc = null;
	if (cc != null) {
	    doc = cc.createCompletionJavaDoc(extEditorUI);
	}
        if (doc == null) {
            doc = super.createCompletionJavaDoc(extEditorUI);
        }        
	return doc;
    } 
   
    public Completion createCompletion(ExtEditorUI extEditorUI) {
	CompletionProvider cc = (CompletionProvider) Lookup.getDefault().lookup(CompletionProvider.class);
	Completion compl = null;
	if (cc != null) {
	    compl = cc.createCompletion(extEditorUI);
	}
        if (compl == null) {
            compl = super.createCompletion(extEditorUI);
        }
        return compl;
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
        return new CCSyntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
	SyntaxSupportProvider ss = (SyntaxSupportProvider) Lookup.getDefault().lookup(SyntaxSupportProvider.class);
	SyntaxSupport sup = null;
	if (ss != null) { 
	    sup = ss.createSyntaxSupport(doc);
	}
        if (sup == null) {
            sup = super.createSyntaxSupport(doc);
        }
	return sup;        
    }

    /** Create the formatter appropriate for this kit */
    public Formatter createFormatter() {
        return new CCFormatter(this.getClass());
    }

    protected Action[] createActions() {
        Action[] ccActions = new Action[] {
	    new CCDefaultKeyTypedAction(),
	    new CCFormatAction(),
//	    new CppFoldTestAction(),
            new CCInsertBreakAction(),
            new CCDeleteCharAction(deletePrevCharAction, false),
            new CCGenerateGoToPopupAction(),
            new CommentAction("//"), // NOI18N
            new UncommentAction("//") // NOI18N
	};
        ccActions = TextAction.augmentList(super.createActions(), ccActions);
        GotoDeclarationProvider gotoDeclaration = (GotoDeclarationProvider) Lookup.getDefault().lookup(GotoDeclarationProvider.class);
        if (gotoDeclaration == null)  {
            return ccActions;
        } else {
            return TextAction.augmentList(ccActions, new Action[]{gotoDeclaration.getGotoDeclarationAction()});
        }
    }

    public static class CCGenerateGoToPopupAction extends NbGenerateGoToPopupAction {

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
//                        if (ExtKit.gotoSourceAction.equals(actionName)){
//                            item = new JMenuItem(NbBundle.getBundle(JavaKit.class).getString("goto_source_open_source_not_formatted")); //NOI18N
//                            addAcceleretors(a, item, target);
//                            item.setEnabled(false);
//                        }
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
            String menuText = NbBundle.getBundle(CCKit.class).getString("generate-goto-popup"); //NOI18N
            JMenu jm = new JMenu(menuText);
            //addAction(target, jm, ExtKit.gotoSourceAction);
            //addAction(target, jm, ExtKit.gotoDeclarationAction);
            //addAction(target, jm, gotoSuperImplementationAction);
            //addAction(target, jm, ExtKit.gotoAction);
            //addAction(target, jm, JavaFastOpenAction.getInstance());
            return jm;
        }
    
    }
    
//    public static class CppFoldTestAction extends BaseAction {
//	public CppFoldTestAction() {
//	    super("cpp-fold-test-action"); // NOI18N
//            String sdesc = NbBundle.getBundle(CCKit.class).getString("CppFoldTest"); //NOI18N
//            String menutext = NbBundle.getBundle(CCKit.class).getString("menu_CppFoldTest"); //NOI18N
//
//	    putValue(SHORT_DESCRIPTION, sdesc);
//	    putValue(BaseAction.POPUP_MENU_TEXT, menutext);
//	}
//
//	public void actionPerformed(ActionEvent evt, JTextComponent target) {
//	    FoldHierarchy hierarchy = FoldHierarchy.get(target);
//
//	    // Hierarchy locking done in the utility method
//            List types = new ArrayList();
//            types.add(CppFoldManagerBase.CODE_BLOCK_FOLD_TYPE);
//            types.add(CppFoldManagerBase.INCLUDES_FOLD_TYPE);
//            FoldUtilities.expand(hierarchy, types);
//	}
//    }

    /** Holds action classes to be created as part of createAction.
        This allows dependent modules to add editor actions to this
        kit on startup.
    */

    protected void updateActions() {
 	super.updateActions();
	addSystemActionMapping(formatAction, CCFormatAction.class);
    }   

    public class CCFormatAction extends BaseAction {

	public CCFormatAction() {
	    super(BaseKit.formatAction,
		  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
	    putValue ("helpID", CCFormatAction.class.getName ()); // NOI18N
	}

	public void actionPerformed(ActionEvent evt, JTextComponent target) {
	    if (target != null) {

		if (!target.isEditable() || !target.isEnabled()) {
		    target.getToolkit().beep();
		    return;
		}

		Caret caret = target.getCaret();
		BaseDocument doc = (BaseDocument)target.getDocument();
                // Set hourglass cursor
                Cursor origCursor = target.getCursor();
                target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		doc.atomicLock();
		try {

		    int caretLine = Utilities.getLineOffset(doc, caret.getDot());
		    int startPos;
		    Position endPosition;
		    if (caret.isSelectionVisible()) {
			startPos = target.getSelectionStart();
			endPosition = doc.createPosition(target.getSelectionEnd());
		    } else {
			startPos = 0;
			endPosition = doc.createPosition(doc.getLength());
		    }

		    int pos = startPos;

		    while (pos < endPosition.getOffset()) {
			int stopPos = endPosition.getOffset();
                        int reformattedLen = doc.getFormatter().reformat(doc, pos, stopPos);
                        pos = pos + reformattedLen;
		    }

		    // Restore the line
		    pos = Utilities.getRowStartFromLineOffset(doc, caretLine);
		    if (pos >= 0) {
			caret.setDot(pos);
		    }
		} catch (BadLocationException e) {
                    Utilities.annotateLoggable(e);
		} finally {
		    doc.atomicUnlock();
                    target.setCursor(origCursor);
		}

	    }
	}
    }

    
    public static class CCDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        /** Check and possibly popup, hide or refresh the completion */
	protected void checkCompletion(JTextComponent target, String typedText) {
	    Completion completion = ExtUtilities.getCompletion(target);
	    if (completion != null && typedText.length() > 0) {
		if (!completion.isPaneVisible()) { // pane not visible yet
		    if (completion.isAutoPopupEnabled()) {
			boolean pop = false;
			switch (typedText.charAt(0)) {
			case ' ':
                        {
			    int dotPos = target.getCaret().getDot();
			    BaseDocument doc = (BaseDocument)target.getDocument();
		
			    if (dotPos >= 2) { // last char before inserted space
				int pos = Math.max(dotPos - 5, 0);
				try {
				    String txtBeforeSpace = doc.getText(pos, dotPos - pos);
				    if (txtBeforeSpace.endsWith("new ")) { // NOI18N
					//XXX  && !Character.isCCIdentifierPart(txtBeforeSpace.charAt(0))) {
					pop = true;
				    } else if (txtBeforeSpace.endsWith(", ")) { // NOI18N
                                        // commented out due to IZ#76210
//					pop = true; 
				    }
				} catch (BadLocationException e) {
				}
			    }
                        }
			    break;
			case '>':
                        {
			    int dotPos = target.getCaret().getDot();
			    BaseDocument doc = (BaseDocument)target.getDocument();
		
			    if (dotPos >= 2) { // last char before inserted space
				int pos = Math.max(dotPos - 2, 0);
				try {
				    String txtBeforeSpace = doc.getText(pos, dotPos - pos);
				    if (txtBeforeSpace.endsWith("->")) { // NOI18N
					pop = true;
				    }
				} catch (BadLocationException e) {
				}
			    }
                        }
                        case ':':
                        {
			    int dotPos = target.getCaret().getDot();
			    BaseDocument doc = (BaseDocument)target.getDocument();
		
			    if (dotPos >= 2) { // last char before inserted space
				int pos = Math.max(dotPos - 2, 0);
				try {
				    String txtBeforeSpace = doc.getText(pos, dotPos - pos);
				    if (txtBeforeSpace.endsWith("::")) { // NOI18N
					pop = true;
				    }
				} catch (BadLocationException e) {
				}
			    }
                        }                        
			    break;		
			case '.':
                        {
			    int dotPos = target.getCaret().getDot();
			    BaseDocument doc = (BaseDocument)target.getDocument();
		
			    if (dotPos >= 2) { // last char before inserted space
				int pos = Math.max(dotPos - 2, 0);
				try {
				    String txtBeforeSpace = doc.getText(pos, dotPos - pos);
				    if (!txtBeforeSpace.endsWith("..")) { // NOI18N
					pop = true;
				    }
				} catch (BadLocationException e) {
				}
			    }
                        }                             
			    break;
                        case ',':
                            // commented out due to IZ#76210
//			    pop = true;
			    break;
		
			}
	      
			if (pop) {
			    completion.popup(true);
			} else {
			    completion.cancelRequest();
			}
		    }
	    
		} else { // the pane is already visible
		    switch (typedText.charAt(0)) {
		    case '=':
		    case '{':
		    case ';':
                    case ')':
			completion.setPaneVisible(false);
			break;
	      
		    default:
			completion.refresh(true);
			break;
		    }
		}
	    }
	}
      
	protected void checkIndentHotChars(JTextComponent target, String typedText) {
	    boolean reindent = false;
	
	    BaseDocument doc = Utilities.getDocument(target);
	    int dotPos = target.getCaret().getDot();
	    if (doc != null) {
		/* Check whether the user has written the ending 'e'
		 * of the first 'else' on the line.
		 */
		if ("e".equals(typedText)) { // NOI18N
		    try {
			int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
			if (fnw >= 0 && fnw + 4 == dotPos
			    && "else".equals(doc.getText(fnw, 4)) // NOI18N
			    ) {
			    reindent = true;
			}
		    } catch (BadLocationException e) {
		    }
	    
		} else if (":".equals(typedText)) { // NOI18N
		    try {
			int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
			if (fnw >= 0 && fnw + 4 <= doc.getLength()
			    && "case".equals(doc.getText(fnw, 4)) // NOI18N
			    ) {
			    reindent = true;
			}
		    } catch (BadLocationException e) {
		    }
		}
	  
		// Reindent the line if necessary
		if (reindent) {
		    try {
			Utilities.reformatLine(doc, dotPos);
		    } catch (BadLocationException e) {
		    }
		}
	    }
	
	    super.checkIndentHotChars(target, typedText);
	}
        
        protected void insertString(BaseDocument doc, int dotPos,
                                    Caret caret, String str,
                                    boolean overwrite) throws BadLocationException {
            super.insertString(doc, dotPos, caret, str, overwrite);
            BracketCompletion.charInserted(doc, dotPos, caret, str.charAt(0));
        }
        
    } // end class CCDefaultKeyTypedAction
    
    public static class CCInsertBreakAction extends InsertBreakAction {

        static final long serialVersionUID = -1506173310438326380L;
        static final boolean DEBUG = false;

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
                        String insString = "}"; // NOI18N
                        // XXX: vv159170 simplest hack
                        // insert "};" for "{" when in "enum", "class", "struct" and union completion
                        int stLine = Utilities.getRowFirstNonWhite(doc, dotPos);
                        if (stLine != -1 && stLine < dotPos) {
                            String text = doc.getText(stLine, dotPos - stLine);
                            if (DEBUG) System.out.println("current text " + text); // NOI18N
                            String regexp=".*\\b(class|union|struct|enum)\\b.*";//NOI18N
                            if (text != null && text.matches(regexp)) {
                                insString = "};"; // NOI18N
                            }
                        }
                        doc.insertString(end, insString, null); // NOI18N
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

    } // end class CCInsertBreakAction


    public static class CCDeleteCharAction extends ExtDeleteCharAction {

        public CCDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }

        protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch)
        throws BadLocationException {
            BracketCompletion.charBackspaced(doc, dotPos, caret, ch);
        }
    } // end class CCDeleteCharAction    
    
}
