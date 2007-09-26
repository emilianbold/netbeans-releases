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

package org.netbeans.modules.ruby.rhtml.editor;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ToggleCommentAction;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.ruby.BracketCompleter;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rhtml.loaders.BackgroundParser;
import org.openide.util.Exceptions;

/**
 * Editor kit implementation for RHTML content type
 *
 * @todo Automatic bracket matching for RHTML files should probably split Ruby blocks up,
 *  e.g. pressing enter here:  <% if true| %> should take you -outside- of the current
 *  block and insert a matching <% end %> outside!
 * @todo Add in a toggle comment action that -works- !
 * 
 * @author Marek Fukala
 * @author Tor Norbye
 * @version 1.00
 */

public class RhtmlKit extends HTMLKit {
    /** Completion assistance for Ruby */
    private BracketCompleter rubyCompletion = new BracketCompleter();
    /** Index in parent kit's action array where the default key action is found. */
    private static int defaultKeyActionIndex = -1;
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(RhtmlKit.class);
    }
    
    static final long serialVersionUID =-1381945567613910297L;
        
    public RhtmlKit(){
        super(RhtmlTokenId.MIME_TYPE);
    }
    
    @Override
    public String getContentType() {
        return RhtmlTokenId.MIME_TYPE;
    }
    
    @Override
    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        if (defaultKeyActionIndex == -1 || 
                (defaultKeyActionIndex < superActions.length && 
                 !(superActions[defaultKeyActionIndex] instanceof ExtDefaultKeyTypedAction))) {
            for (int i = 0; i < superActions.length; i++) {
                Action action = superActions[i];
                if (action instanceof ExtDefaultKeyTypedAction) {
                    defaultKeyActionIndex = i;
                }

                // This code assumes that HTML hasn't provided custom implementations of insert break
                // and ext delete
                assert !(action instanceof InsertBreakAction && action.getClass() != InsertBreakAction.class);
                assert !(action instanceof ExtDeleteCharAction && action.getClass() != ExtDeleteCharAction.class);
            }
        }

        assert defaultKeyActionIndex != -1;
        superActions[defaultKeyActionIndex] = new RhtmlDefaultKeyTypedAction((ExtDefaultKeyTypedAction)superActions[defaultKeyActionIndex]);
        
        return TextAction.augmentList(superActions, new Action[] {
                    new RhtmlInsertBreakAction(), 
                    new RhtmlDeleteCharAction(deletePrevCharAction, false),
                    new RhtmlToggleCommentAction(),
                 });
    }

    /** Return true if the given offset is inside Ruby code (where ruby completion should kick in */
    private static boolean shouldDelegateToHtml(BaseDocument doc, int offset) {
        return LexUtilities.getRubyTokenSequence(doc, offset) == null;
    }

    private class RhtmlDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
        private JTextComponent currentTarget;
        private ExtDefaultKeyTypedAction htmlAction;

        RhtmlDefaultKeyTypedAction(ExtDefaultKeyTypedAction htmlAction) {
            this.htmlAction = htmlAction;
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            // Delegate to HTML?
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            if (caret != null && shouldDelegateToHtml(doc, caret.getDot())) {
                htmlAction.actionPerformed(evt, target);
                return;
            }

            currentTarget = target;
            super.actionPerformed(evt, target);
            currentTarget = null;
        }

        @Override
        protected void insertString(BaseDocument doc, int dotPos, Caret caret, String str,
            boolean overwrite) throws BadLocationException {
            boolean handled =
                rubyCompletion.beforeCharInserted(doc, dotPos, currentTarget,
                    str.charAt(0));

            if (!handled) {
                super.insertString(doc, dotPos, caret, str, overwrite);
                handled = rubyCompletion.afterCharInserted(doc, dotPos, currentTarget,
                        str.charAt(0));
            }
        }

        @Override
        protected void replaceSelection(JTextComponent target, int dotPos, Caret caret,
            String str, boolean overwrite) throws BadLocationException {
            char insertedChar = str.charAt(0);
            Document document = target.getDocument();

            if (document instanceof BaseDocument) {
                BaseDocument doc = (BaseDocument)document;

                try {
                    int caretPosition = caret.getDot();

                    boolean handled =
                        rubyCompletion.beforeCharInserted(doc, caretPosition,
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

                        rubyCompletion.afterCharInserted(doc, caret.getDot() - 1,
                            target, insertedChar);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            super.replaceSelection(target, dotPos, caret, str, overwrite);
        }
    }
    
    private class RhtmlInsertBreakAction extends InsertBreakAction {
        static final long serialVersionUID = -1506173310438326380L;

        @Override
        protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
            int dotPos = caret.getDot();
            if (shouldDelegateToHtml(doc, dotPos)) {
                return super.beforeBreak(target, doc, caret);
            }

            try {
                // First see if we're -right- before a %>, if so, just enter out
                // of it
                if (dotPos < doc.getLength()-3) {
                    String text = doc.getText(dotPos, 3);
                    if (text.equals(" %>") || text.startsWith("%>")) {
                        // TODO - double check at the lexical level!
                        caret.setDot(dotPos + text.indexOf('>')+1);
                        return super.beforeBreak(target, doc, caret);
                    }
                }
                
                int newOffset = rubyCompletion.beforeBreak(doc, dotPos, target);

                if (newOffset >= 0) {
                    return new Integer(newOffset);
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            return null;
        }

        @Override
        protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret,
            Object cookie) {
            if (shouldDelegateToHtml(doc, caret.getDot())) {
                super.afterBreak(target, doc, caret, cookie);
                return;
            }

            if (cookie != null) {
                if (cookie instanceof Integer) {
                    // integer
                    int dotPos = ((Integer)cookie).intValue();
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

    private class RhtmlDeleteCharAction extends ExtDeleteCharAction {
        private JTextComponent currentTarget;

        public RhtmlDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            currentTarget = target;
            super.actionPerformed(evt, target);
            currentTarget = null;
        }

        @Override
        protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch)
            throws BadLocationException {
            if (shouldDelegateToHtml(doc, dotPos)) {
                super.charBackspaced(doc, dotPos, caret, ch);
                return;
            }

            boolean success = rubyCompletion.charBackspaced(doc, dotPos, currentTarget, ch);
        }
    }
    
    @Override
    public Object clone() {
        return new RhtmlKit();
    }
    
    @Override
    protected void initDocument(Document doc) {
        super.initDocument(doc);
        
        /** Attach error listener */
        new BackgroundParser(doc);
    }
    
    /**
     * Toggle comment action. Doesn't actually reuse much of the implementation
     * but subclasses to inherit the icon and description
     */
    public static class RhtmlToggleCommentAction extends ToggleCommentAction  {
        static final long serialVersionUID = -1L;

        private static final String ERB_PREFIX = "<%"; // NOI18N
        private static final String ERB_COMMENT = "<%#"; // NOI18N
        private static final String ERB_TEXT = "<%#*"; // NOI18N
        private static final String ERB_SUFFIX = "%>"; // NOI18N
        private static final int ERB_PREFIX_LEN = ERB_PREFIX.length();
        private static final int ERB_SUFFIX_LEN = ERB_SUFFIX.length();
        private static final int ERB_COMMENT_LEN = ERB_COMMENT.length();
        private static final int ERB_TEXT_LEN = ERB_TEXT.length();
        
        public RhtmlToggleCommentAction() {
            super(ERB_COMMENT);
            // "Rebind" the action name here since Schliemann has bound the
            // toggle-comment keybinding to an action named "comment"
            putValue(Action.NAME, "comment");
            
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            commentUncomment(evt, target, null);
        }

        /** See if this line looks commented */
        private static boolean isLineCommented(BaseDocument doc, int textBegin) throws BadLocationException  {
            assert textBegin != -1;
            //int start = Utilities.getRowFirstNonWhite(doc, start);
            //if (start == -1) { 
            int textEnd = Utilities.getRowLastNonWhite(doc, textBegin)+1;
            
            if (textEnd - textBegin < ERB_COMMENT_LEN) {
                return false;
            }
            
            CharSequence maybeLineComment = DocumentUtilities.getText(doc, textBegin, ERB_COMMENT_LEN);
            if (!CharSequenceUtilities.textEquals(maybeLineComment, ERB_COMMENT)) {
                return false;
            }

            return true;
        }
        
        private void commentUncomment(ActionEvent evt, JTextComponent target, Boolean forceComment) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    doc.atomicLock();
                    try {
                        int startPos;
                        int endPos;
                        
                        if (caret.isSelectionVisible()) {
                            startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                            endPos = target.getSelectionEnd();
                            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                endPos--;
                            }
                            endPos = Utilities.getRowEnd(doc, endPos);
                        } else { // selection not visible
                            startPos = Utilities.getRowStart(doc, caret.getDot());
                            endPos = Utilities.getRowEnd(doc, caret.getDot());
                        }
                        
                        int lineCount = Utilities.getRowCount(doc, startPos, endPos);
                        boolean comment = forceComment != null ? forceComment : !allComments(doc, startPos, lineCount);
                        
                        if (comment) {
                            comment(doc, startPos, lineCount);
                        } else {
                            uncomment(doc, startPos, lineCount);
                        }
                    } finally {
                        doc.atomicUnlock();
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
        
        private boolean allComments(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
                if (firstNonWhitePos != -1) { // Ignore empty lines
                    if (!isLineCommented(doc, firstNonWhitePos)) {
                        return false;
                    }
                }
                
                offset = Utilities.getRowStart(doc, offset, +1);
            }
            return true;
        }
        
        private void comment(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--, offset = Utilities.getRowStart(doc, offset, +1)) {
                // TODO - if the line starts with "<%", put the "#" inside!
                if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
                    continue;
                }
                
                int textBegin = Utilities.getRowFirstNonWhite(doc, offset);
                int textEnd = Utilities.getRowEnd(doc, offset);
                if (textEnd-offset >= ERB_PREFIX_LEN) {
                    // See if it's a <% prefix
                    // TODO - handle nested <%# applications!
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, textBegin, ERB_PREFIX_LEN);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_PREFIX)) {
                        doc.insertString(textBegin+ERB_PREFIX_LEN, "#", null); // NOI18N
                        continue;
                    }
                }
                doc.insertString(textBegin, ERB_TEXT, null); // NOI18N
                doc.insertString(Utilities.getRowLastNonWhite(doc, offset)+1, ERB_SUFFIX, null); // NOI18N
            }
        }
        
        private void uncomment(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--, offset = Utilities.getRowStart(doc, offset, +1)) {
                if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
                    continue;
                }

                // Get the first non-whitespace char on the current line
                int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);

                // Is this a "text" line, or a Ruby line?
                // Text lines have an additional "*" in them
                int textEnd = Utilities.getRowLastNonWhite(doc, firstNonWhitePos)+1;
                if (textEnd-firstNonWhitePos >= ERB_TEXT_LEN) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, ERB_TEXT_LEN);
                    CharSequence maybeLineEnd = DocumentUtilities.getText(doc, textEnd-ERB_SUFFIX_LEN, ERB_SUFFIX_LEN);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_TEXT)) {
                        doc.remove(firstNonWhitePos, ERB_TEXT_LEN);
                        if (CharSequenceUtilities.textEquals(maybeLineEnd, ERB_SUFFIX)) {
                            doc.remove(textEnd-ERB_SUFFIX_LEN-ERB_TEXT_LEN, ERB_SUFFIX_LEN);
                        }
                        continue;
                    }
                }
                
                // Else it is probably a regular Ruby expression; we remove ONLY the "#" inside
                if (textEnd-firstNonWhitePos >= ERB_COMMENT_LEN) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, ERB_COMMENT_LEN);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_COMMENT)) {
                        // Remove just the #
                        doc.remove(firstNonWhitePos+2, 1);
                        continue;
                    }
                }
            }
        }
    }
}

