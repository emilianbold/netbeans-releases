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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
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
import org.netbeans.editor.TokenItem;

import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.editor.ext.ExtKit.UncommentAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorKit.NbGenerateGoToPopupAction;

import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.cnd.editor.spi.cplusplus.CCSyntaxSupport;
import org.netbeans.modules.cnd.editor.spi.cplusplus.SyntaxSupportProvider;
import org.netbeans.modules.cnd.editor.spi.cplusplus.GotoDeclarationProvider;


/** C++ editor kit with appropriate document */
public class CCKit extends NbEditorKit {

    public String getContentType() {
        return MIMENames.CPLUSPLUS_MIME_TYPE;
    }
    
// Work-in-progress...
//    public HelpCtx getHelpCtx() {
//        System.err.println("CCKit.getHelpCts: Using JavaKit help ID");
//        return new HelpCtx("org.netbeans.modules.editor.java.JavaKit");
//    }
    
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
            sup = new CCSyntaxSupport(doc);
        }
	return sup;        
    }

    /** Create the formatter appropriate for this kit */
    public Formatter createFormatter() {
        return new CCFormatter(this.getClass());
    }

    protected Action getCommentAction() {
        return new CommentAction("//"); // NOI18N
    }
    
    protected Action getUncommentAction() {
        return new UncommentAction("//"); // NOI18N
    }
    
    protected Action getToggleCommentAction() {
        return new ToggleCommentAction("//"); // NOI18N
    }
    
    protected Action[] createActions() {
        Action[] ccActions = new Action[] {
	    new CCDefaultKeyTypedAction(),
	    new CCFormatAction(),
//	    new CppFoldTestAction(),
            new CCInsertBreakAction(),
            new CCDeleteCharAction(deletePrevCharAction, false),
//            new CCGenerateGoToPopupAction(),
            getToggleCommentAction(),
            getCommentAction(),
            getUncommentAction()
	};
        ccActions = TextAction.augmentList(super.createActions(), ccActions);
        GotoDeclarationProvider gotoDeclaration = Lookup.getDefault().lookup(GotoDeclarationProvider.class);
        if (gotoDeclaration == null)  {
            return ccActions;
        } else {
            return TextAction.augmentList(ccActions, new Action[]{gotoDeclaration.getGotoDeclarationAction()});
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
		  MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
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
                    //failed to format
		} finally {
		    doc.atomicUnlock();
                    target.setCursor(origCursor);
		}

	    }
	}
    }

    
    public static class CCDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
      
	protected void checkIndentHotChars(JTextComponent target, String typedText) {
	    boolean reindent = false;
	
	    BaseDocument doc = Utilities.getDocument(target);
	    int dotPos = target.getCaret().getDot();
	    if (doc != null) {
                reindent = CCFormatter.getKeywordBasedReformatBlock(doc, dotPos, typedText) != null;
	  
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
                    doc.insertString(dotPos, "\"\"", null); //NOI18N
                    dotPos += 1;
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
                        CCSyntaxSupport sup = (CCSyntaxSupport)Utilities.getSyntaxSupport(target);
                        TokenItem item = sup.getTokenChain(dotPos - 1, dotPos);
                        while (item != null && item.getTokenID() == CCTokenContext.WHITESPACE) {
                            item = item.getPrevious();
                        }
                        if (item == null || item.getTokenID() != CCTokenContext.LBRACE) {
                            return Boolean.FALSE;
                        }
                        int lBracePos = item.getOffset();
                        int lastSepOffset = sup.getLastCommandSeparator(lBracePos - 1);           
                        if (lastSepOffset == -1 && lBracePos > 0) {
                            lastSepOffset = 0;
                        }
                        if (lastSepOffset != -1 && lastSepOffset < dotPos) {
                            TokenItem keyword = sup.getTokenChain(lastSepOffset, lBracePos);
                            while (keyword != null && keyword.getOffset() < lBracePos) {
                                if (keyword.getTokenID() == CCTokenContext.CLASS ||
                                        keyword.getTokenID() == CCTokenContext.UNION ||
                                        keyword.getTokenID() == CCTokenContext.STRUCT ||
                                        keyword.getTokenID() == CCTokenContext.ENUM) {
                                    insString = "};"; // NOI18N
                                    break;
                                }
                                keyword = keyword.getNext();
                            } 
//                            String text = doc.getText(lastSepOffset, dotPos - lastSepOffset);
//                            if (DEBUG) System.out.println("current text " + text); // NOI18N
//                            String regexp=".*\\b(class|union|struct|enum)\\b.*";//NOI18N
//                            if (text != null && text.matches(regexp)) {
//                                insString = "};"; // NOI18N
//                            }
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
