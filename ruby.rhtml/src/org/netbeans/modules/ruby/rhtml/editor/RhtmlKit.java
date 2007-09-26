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
                    new RhtmlDeleteCharAction(deletePrevCharAction, false)
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
            if (shouldDelegateToHtml(doc, caret.getDot())) {
                return super.beforeBreak(target, doc, caret);
            }

            try {
                int newOffset = rubyCompletion.beforeBreak(doc, caret.getDot(), target);

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
}

