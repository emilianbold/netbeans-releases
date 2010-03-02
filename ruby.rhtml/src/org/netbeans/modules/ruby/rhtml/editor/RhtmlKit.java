/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.rhtml.editor;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ToggleCommentAction;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.DeleteToNextCamelCasePosition;
import org.netbeans.modules.csl.api.DeleteToPreviousCamelCasePosition;
import org.netbeans.modules.csl.api.InstantRenameAction;
import org.netbeans.modules.csl.api.NextCamelCasePosition;
import org.netbeans.modules.csl.api.PreviousCamelCasePosition;
import org.netbeans.modules.csl.api.SelectCodeElementAction;
import org.netbeans.modules.csl.api.SelectNextCamelCasePosition;
import org.netbeans.modules.csl.api.SelectPreviousCamelCasePosition;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rhtml.spi.DtdResolver;
import org.openide.util.Lookup;

/**
 * Editor kit implementation for RHTML content type
 *
 * @todo Automatic bracket matching for RHTML files should probably split Ruby blocks up,
 *  e.g. pressing enter here:  <% if true| %> should take you -outside- of the current
 *  block and insert a matching <% end %> outside!
 * @todo Hook up caret motion commands
 * @todo Hook up refactoring and inline rename operations
 * @todo Pressing newline in an EMPTY rhtml expression should NOT newline me out of the block!
 * 
 * @author Marek Fukala
 * @author Tor Norbye
 * @version 1.00
 */

public class RhtmlKit extends HtmlKit {
    
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
    public Document createDefaultDocument() {
        return super.createDefaultDocument();
    }

    @Override
    public Syntax createSyntax(Document doc) {
        Syntax result = super.createSyntax(doc);
        DtdResolver resolver = Lookup.getDefault().lookup(DtdResolver.class);
        // need to this here instead of createDefaultDocument since the StreamDescriptionProperty
        // property gets set after createDefaultDocument and the DtdResolver impls typically
        // need to be able to get the fo for the document. Another way would be to provide
        // custom DataEditorSupport in RhtmlDataLoader and override createStyledDocument - the
        // drawback of that is that it requires copy-pasting code from SimpleES
        if (resolver != null) {
            String fallbackDtd = resolver.getIdentifier(doc);
            if (fallbackDtd != null) {
                doc.putProperty(HtmlParserResult.FALLBACK_DTD_PROPERTY_NAME, Registry.getDTD(fallbackDtd, null));
            }
        }
        return result;
    }



    @Override
    protected DeleteCharAction createDeletePrevAction() {
        return new RhtmlDeleteCharAction(deletePrevCharAction, false, super.createDeletePrevAction());
    }
    
    @Override
    protected ExtDefaultKeyTypedAction createDefaultKeyTypedAction() {
        return new RhtmlDefaultKeyTypedAction(super.createDefaultKeyTypedAction());
    }

    @Override
    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        
        return TextAction.augmentList(superActions, new Action[] {
            // TODO - also register a Tab key action which tabs out of <% %> if the caret is near the end
            // (Shift Enter inserts a line below the current - perhaps that's good enough)
            new RhtmlToggleCommentAction(),
            new SelectCodeElementAction(SelectCodeElementAction.selectNextElementAction, true),
            new SelectCodeElementAction(SelectCodeElementAction.selectPreviousElementAction, false),
            new NextCamelCasePosition(findAction(superActions, nextWordAction)),
            new PreviousCamelCasePosition(findAction(superActions, previousWordAction)),
            new SelectNextCamelCasePosition(findAction(superActions, selectionNextWordAction)),
            new SelectPreviousCamelCasePosition(findAction(superActions, selectionPreviousWordAction)),
            new DeleteToNextCamelCasePosition(findAction(superActions, removeNextWordAction)),
            new DeleteToPreviousCamelCasePosition(findAction(superActions, removePreviousWordAction)),
            new InstantRenameAction(),
         });
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

    private boolean handleDeletion(BaseDocument doc, int dotPos) {
        if (dotPos > 0) {
            try {
                char ch = doc.getText(dotPos-1, 1).charAt(0);
                if (ch == '%') {
                    TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
                    TokenSequence<?> ts = th.tokenSequence();
                    ts.move(dotPos);
                    if (ts.movePrevious()) {
                        Token<?> token = ts.token();
                        if (token.id() == RhtmlTokenId.DELIMITER && ts.offset()+token.length() == dotPos && ts.moveNext()) {
                            token = ts.token();
                            if (token.id() == RhtmlTokenId.DELIMITER && ts.offset() == dotPos) {
                                doc.remove(dotPos-1, 1+token.length());
                                return true;
                            }
                        }
                    }
                }
            } catch (BadLocationException ble) {
                // do nothing - see #154991
            }
        }
        
        return false;
    }

    private boolean handleInsertion(BaseDocument doc, Caret caret, char c) {
        int dotPos = caret.getDot();
        // Bracket matching on <% %>
        if (c == ' ' && dotPos >= 2) {
            try {
                String s = doc.getText(dotPos-2, 2);
                if ("%=".equals(s) && dotPos >= 3) { // NOI18N
                    s = doc.getText(dotPos-3, 3);
                }
                if ("<%".equals(s) || "<%=".equals(s)) { // NOI18N
                    doc.insertString(dotPos, "  ", null);
                    caret.setDot(dotPos+1);
                    return true;
                }
            } catch (BadLocationException ble) {
                // do nothing - see #154991
            }
            
            return false;
        }
        
        if ((dotPos > 0) && (c == '%' || c == '>')) {
            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
            TokenSequence<?> ts = th.tokenSequence();
            ts.move(dotPos);
            try {
                if (ts.moveNext() || ts.movePrevious()) {
                    Token<?> token = ts.token();
                    if (token.id() == RhtmlTokenId.HTML && doc.getText(dotPos-1, 1).charAt(0) == '<') {
                        // See if there's anything ahead
                        int first = Utilities.getFirstNonWhiteFwd(doc, dotPos, Utilities.getRowEnd(doc, dotPos));
                        if (first == -1) {
                            doc.insertString(dotPos, "%%>", null); // NOI18N
                            caret.setDot(dotPos+1);
                            return true;
                        }
                    } else if (token.id() == RhtmlTokenId.DELIMITER) {
                        String tokenText = token.text().toString();
                        if (tokenText.endsWith("%>")) { // NOI18N
                            // TODO - check that this offset is right
                            int tokenPos = (c == '%') ? dotPos : dotPos-1;
                            CharSequence suffix = DocumentUtilities.getText(doc, tokenPos, 2);
                            if (CharSequenceUtilities.textEquals(suffix, "%>")) { // NOI18N
                                caret.setDot(dotPos+1);
                                return true;
                            }
                        } else if (tokenText.endsWith("<")) {
                            // See if there's anything ahead
                            int first = Utilities.getFirstNonWhiteFwd(doc, dotPos, Utilities.getRowEnd(doc, dotPos));
                            if (first == -1) {
                                doc.insertString(dotPos, "%%>", null); // NOI18N
                                caret.setDot(dotPos+1);
                                return true;
                            }
                        }
                    } else if ((token.id() == RhtmlTokenId.RUBY || token.id() == RhtmlTokenId.RUBY_EXPR) && dotPos >= 1 && dotPos <= doc.getLength()-3) {
                        // If you type ">" one space away from %> it's likely that you typed
                        // "<% foo %>" without looking at the screen; I had auto inserted %> at the end
                        // and because I also auto insert a space without typing through it, you've now
                        // ended up with "<% foo %> %>". Let's prevent this by interpreting typing a ""
                        // right before %> as a duplicate for %>.   I can't just do this on % since it's
                        // quite plausible you'd have
                        //   <% x = %q(foo) %>  -- if I simply moved the caret to %> when you typed the
                        // % in %q we'd be in trouble.
                        String s = doc.getText(dotPos-1, 4);
                        if ("% %>".equals(s)) { // NOI18N
                            doc.remove(dotPos-1, 2);
                            caret.setDot(dotPos+1);
                            return true;
                        }
                    }
                }
            } catch (BadLocationException ble) {
                // do nothing - see #154991
            }
        }
        
        return false;
    }

    // This code used to customize the break behavior and insert a newline AFTER the closing %> tag which isn't
    // always what people want
    //    private boolean handleBreak(BaseDocument doc, Caret caret) throws BadLocationException {
    //        int dotPos = caret.getDot();
    //
    //        // First see if we're -right- before a %>, if so, just enter out
    //        // of it
    //        if (dotPos <= doc.getLength()-3) {
    //            String text = doc.getText(dotPos, 3);
    //            if (text.equals(" %>") || text.startsWith("%>") || text.equals("-%>") || text.equals("% -%")) { // NOI18N
    //                TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
    //                TokenSequence<?> ts = th.tokenSequence();
    //                ts.move(dotPos);
    //                if (ts.moveNext()) {
    //                    // Go backwards and make sure we have nothing before the previous
    //                    // delimiter
    //                    TokenId id = ts.token().id();
    //                    boolean notJustSpace = false;
    //                    if (id == RhtmlTokenId.RUBY || id == RhtmlTokenId.RUBY_EXPR || id == RhtmlTokenId.DELIMITER) {
    //                        do {
    //                            id = ts.token().id();
    //                            if (id == RhtmlTokenId.DELIMITER && ts.token().text().charAt(0) == '<') {
    //                                if (notJustSpace ) {
    //                                    caret.setDot(dotPos + text.indexOf('>')+1);
    //                                    return true;
    //                                }
    //                                return false;
    //                            } else if (id == RhtmlTokenId.RUBY || id == RhtmlTokenId.RUBY_EXPR) {
    //                                if (!notJustSpace) {
    //                                    TokenSequence<?> ets = ts.embedded();
    //                                    if (ets != null) {
    //                                        ets.moveStart();
    //                                        while (ets.moveNext()) {
    //                                            if (ets.token().id() != RubyTokenId.WHITESPACE) {
    //                                                notJustSpace = true;
    //                                            }
    //                                        }
    //                                    }
    //                                }
    //                            }
    //                        } while (ts.movePrevious());
    //                    }
    //                }
    //            }
    //        }
    //        
    //        return false;
    //    }

    private class RhtmlDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
        private ExtDefaultKeyTypedAction htmlAction;

        RhtmlDefaultKeyTypedAction(ExtDefaultKeyTypedAction htmlAction) {
            this.htmlAction = htmlAction;
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            String cmd = evt.getActionCommand();
            if (cmd.length() > 0) {
                char c = cmd.charAt(0);
                if (handleInsertion(doc, caret, c)) {
                    return;
                }
            }

            htmlAction.actionPerformed(evt, target);
        }
    }
    
    private class RhtmlDeleteCharAction extends ExtDeleteCharAction {
        //implements NextCharProvider { XXX - Parsing API
        
        private DeleteCharAction htmlAction;

        public RhtmlDeleteCharAction(String nm, boolean nextChar, DeleteCharAction htmlAction) {
            super(nm, nextChar);
            this.htmlAction = htmlAction;
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            target.putClientProperty(ExtDeleteCharAction.class, this);
            try {
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                int dotPos = caret.getDot();
                if (handleDeletion(doc, dotPos)) {
                    return;
                }

                htmlAction.actionPerformed(evt, target);
            } finally {
                target.putClientProperty(ExtDeleteCharAction.class, null);
            }
        }

        public boolean getNextChar() {
            return nextChar;
        }
    }
    
    @Override
    public Object clone() {
        return new RhtmlKit();
    }
    
    private static Token<?> getToken(BaseDocument doc, int offset, boolean checkEmbedded) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        TokenSequence<?> ts = th.tokenSequence();
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        if (checkEmbedded) {
            TokenSequence<?> es = ts.embedded();
            if (es != null) {
                es.move(offset);
                if (es.moveNext() || es.movePrevious()) {
                    return es.token();
                }
            }
        }
        return ts.token();
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
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            commentUncomment(evt, target, null);
        }

        /** See if this line looks commented */
        private static boolean isLineCommented(BaseDocument doc, int textBegin) throws BadLocationException  {
            assert textBegin != -1;
            
            Token<?> token = getToken(doc, textBegin, false);
            if (token != null) {
                TokenId id = token.id();
                if (id == RhtmlTokenId.DELIMITER) {
                    // Could be either <% or <%# or even <% #
                    if (token.text().toString().endsWith("#")) {
                        return true;
                    }
                    // Handle "<% #" etc.
                    int first = Utilities.getFirstNonWhiteFwd(doc, textBegin+token.length(), 
                            Utilities.getRowEnd(doc, textBegin));
                    if (first == -1) {
                        return false;
                    } else {
                        char c = DocumentUtilities.getText(doc, first, 1).charAt(0);
                        return c == '#';
                    }
                } else if (id == RhtmlTokenId.RUBY || id == RhtmlTokenId.RUBY_EXPR) {
                    // We're in the middle of some Ruby - check it
                    token = getToken(doc, textBegin, true);
                    return token.id() == RubyTokenId.LINE_COMMENT;
                } else if (id == RhtmlTokenId.RUBYCOMMENT) {
                    return true;
                } else {
                    // We don't consider HTML comments commented out - want RHTML commenting
                    return false;
                }
            } 

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
        
        private void commentUncomment(ActionEvent evt, final JTextComponent target, final Boolean forceComment) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomic(new Runnable() {
                    public @Override void run() {
                        try {
                            Caret caret = target.getCaret();
                            int startPos;
                            int endPos;

                            if (caret.isSelectionVisible()) {
                                startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos && endPos > startPos) {
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
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                });
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

                Token<?> token = getToken(doc, textBegin, false);
                if (token != null) {
                    TokenId id = token.id();
                    if (id == RhtmlTokenId.DELIMITER) {
                        if (!token.text().toString().endsWith("#")) {
                            doc.insertString(textBegin+ERB_PREFIX_LEN, "#", null); // NOI18N
                        }
                    } else if (id == RhtmlTokenId.RUBY || id == RhtmlTokenId.RUBY_EXPR) {
                        // We're in the middle of some Ruby - check it
                        token = getToken(doc, textBegin, true);
                        doc.insertString(textBegin, "#", null); // NOI18N
                    } else if (id == RhtmlTokenId.RUBYCOMMENT) {
                        //return true;
                    } else {
                        // Plain text or HTML
                        doc.insertString(textBegin, ERB_TEXT, null); // NOI18N
                        doc.insertString(Utilities.getRowLastNonWhite(doc, offset)+1, ERB_SUFFIX, null); // NOI18N
                    }
                    continue;
                } 
                
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
                int textBegin = Utilities.getRowFirstNonWhite(doc, offset);

                Token<?> token = getToken(doc, textBegin, false);
                if (token != null) {
                    TokenId id = token.id();
                    if (id == RhtmlTokenId.DELIMITER) {
                        // Perhaps something like - todo <% #"
                        // TODO!
                        if (token.text().toString().endsWith("#")) {
                            int textEnd = Utilities.getRowLastNonWhite(doc, textBegin)+1;
                            if (textEnd-textBegin >= ERB_TEXT_LEN) {
                                CharSequence maybeLineComment = DocumentUtilities.getText(doc, textBegin, ERB_TEXT_LEN);
                                String maybeLineEnd =
                                        DocumentUtilities.getText(doc, textEnd-ERB_SUFFIX_LEN, ERB_SUFFIX_LEN).toString();
                                if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_TEXT)) {
                                    doc.remove(textBegin, ERB_TEXT_LEN);
                                    if (CharSequenceUtilities.textEquals(maybeLineEnd, ERB_SUFFIX)) {
                                        doc.remove(textEnd-ERB_SUFFIX_LEN-ERB_TEXT_LEN, ERB_SUFFIX_LEN);
                                    }
                                    continue;
                                }
                            }

                            // Else it is probably a regular Ruby expression; we remove ONLY the "#" inside
                            if (textEnd-textBegin >= ERB_COMMENT_LEN) {
                                CharSequence maybeLineComment = DocumentUtilities.getText(doc, textBegin, ERB_COMMENT_LEN);
                                if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_COMMENT)) {
                                    // Remove just the #
                                    doc.remove(textBegin+2, 1);
                                    continue;
                                }
                            }
                        } else {
                            int first = Utilities.getFirstNonWhiteFwd(doc, textBegin+token.length(), 
                                    Utilities.getRowEnd(doc, textBegin));
                            if (first != -1) {
                                char c = DocumentUtilities.getText(doc, first, 1).charAt(0);
                                if (c == '#') {
                                    doc.remove(first, 1);
                                }
                            }
                            
                        }
                    } else if (id == RhtmlTokenId.RUBY || id == RhtmlTokenId.RUBY_EXPR) {
                        // We're in the middle of some Ruby - check it
                        token = getToken(doc, textBegin, true);
                        if (token.id() == RubyTokenId.LINE_COMMENT) {
                            doc.remove(textBegin, 1);
                        }
                    //} else if (id == RhtmlTokenId.RUBYCOMMENT) {
                    }
                    continue;
                } 
                
                // Is this a "text" line, or a Ruby line?
                // Text lines have an additional "*" in them
                int textEnd = Utilities.getRowLastNonWhite(doc, textBegin)+1;
                if (textEnd-textBegin >= ERB_TEXT_LEN) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, textBegin, ERB_TEXT_LEN);
                    CharSequence maybeLineEnd = DocumentUtilities.getText(doc, textEnd-ERB_SUFFIX_LEN, ERB_SUFFIX_LEN);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_TEXT)) {
                        doc.remove(textBegin, ERB_TEXT_LEN);
                        if (CharSequenceUtilities.textEquals(maybeLineEnd, ERB_SUFFIX)) {
                            doc.remove(textEnd-ERB_SUFFIX_LEN-ERB_TEXT_LEN, ERB_SUFFIX_LEN);
                        }
                        continue;
                    }
                }
                
                // Else it is probably a regular Ruby expression; we remove ONLY the "#" inside
                if (textEnd-textBegin >= ERB_COMMENT_LEN) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, textBegin, ERB_COMMENT_LEN);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, ERB_COMMENT)) {
                        // Remove just the #
                        doc.remove(textBegin+2, 1);
                        continue;
                    }
                }
            }
        }
    }
}

