/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor;


import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.HTMLLexerFormatter;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.modules.html.editor.HTMLKit;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.php.doc.DocumentationRegistry;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.openide.util.HelpCtx;


/**
 * @author ads
 *
 */
public class PhpKit extends HTMLKit {

    private static final long serialVersionUID = 1858280307247622290L;
    
    private static final String LINE_COMMENT = "//";        // NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.languages.dataobject.LanguagesEditorKit#initDocument(javax.swing.text.Document)
     */
    @Override
    protected void initDocument( Document doc )
    {
        super.initDocument(doc);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PhpKit.class);
    }
    
    public PhpKit(){
        super(PhpTokenId.MIME_TYPE);
    }
    
    public String getContentType() {
        return PhpTokenId.MIME_TYPE;
    } 
    
    public Object clone() {
        return new PhpKit();
    }

    
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        doc.putProperty(HTMLLexerFormatter.HTML_FORMATTER_ACTS_ON_TOP_LEVEL, Boolean.TRUE);
        return new HTMLSyntaxSupport(doc) {
        
            public int[] findMatchingBlock(int offset, boolean simpleSearch)
                    throws BadLocationException 
            {
                OffsetRange range = BracketCompletion.findMatching(
                        getDocument(), offset);
                if (range == null || range == OffsetRange.NONE) {
                    return super.findMatchingBlock( offset, simpleSearch);
                }
                else {
                    return new int[] { range.getStart(), range.getEnd() };
                }
            }
        };
    }
    
    /*protected void initDocument(BaseDocument doc) {

        // XXX This appears in JavaKit, not sure why, but doing it just in case.
        //do not ask why, fire bug in the IZ:
        CodeTemplateManager.get(doc);
    }*/
    
    @Override
    protected Action[] createActions() {
        List<Action> actions = new LinkedList<Action>();

        for (Action htmlAction : super.createActions()){
            actions.add(htmlAction);
        }
        
        actions.add(new PhpDefaultKeyTypedAction());
        actions.add(new PhpInsertBreakAction());
        actions.add(new PhpDeleteCharAction(deletePrevCharAction, false));
        //actions.add(new PhpDeleteCharAction(deleteNextCharAction, true));


        actions.add(new CommentAction( LINE_COMMENT ));
        actions.add(new UncommentAction( LINE_COMMENT ));
        
        /*Collection<? extends EditorAction> extraActions = Lookup.getDefault().lookupAll(EditorAction.class);
        for (EditorAction action : extraActions) {
            actions.add(new EditorActionWrapper(action));
        }*/
        
        /*actions.add(new InstantRenameAction());
        actions.add(new PrettyPrintAction());
        actions.add(new GenericGoToDeclarationAction());
        actions.add(new GenericGenerateGoToPopupAction());

        actions.add(new SelectCodeElementAction(selectNextElementAction, true));
        actions.add(new SelectCodeElementAction(selectPreviousElementAction, false));*/


        return TextAction.augmentList(super.createActions(),
            actions.toArray(new Action[actions.size()]));
    }
    
    /*
     * Start low priority task for accessing to documentation.
     * It loads categories with their function list and loads 
     * names of function .
     * Documentation for each function will load on demand. 
     */
    static {
        Thread thread = new Thread() {
            public void run(){
                DocumentationRegistry.getInstance().getCategories();
            }
        };
        thread.setPriority( Thread.MIN_PRIORITY );
        thread.start();
    }
    
    public static class PhpDefaultKeyTypedAction extends
            ExtDefaultKeyTypedAction
    {

        private static final long serialVersionUID = 8779107741901181392L;
        
        protected void insertString( BaseDocument doc, int dotPos, Caret caret,
                String str, boolean overwrite ) throws BadLocationException
        {
            char insertedChar = str.charAt(0);
            if ( TokenUtils.checkPhp(doc, dotPos) 
                    &&
                    (insertedChar == '\"' 
                    || insertedChar == '\'' 
                    || insertedChar == '`' ) )
            {
                boolean inserted = BracketCompletion.completeQuote(doc, dotPos,
                        caret, insertedChar);
                if (inserted) {
                    caret.setDot(dotPos + 1);
                }
                else {
                    super.insertString(doc, dotPos, caret, str, overwrite);
                }
            } else {
                super.insertString(doc, dotPos, caret, str, overwrite);
                if (TokenUtils.checkPhp(doc, dotPos)) {
                    BracketCompletion.charInserted(doc, dotPos, caret, insertedChar);
                }

                // reformat line when the bracket is opened or closed
                if (insertedChar == '}' || insertedChar == '{') {
                    doc.atomicLock();
                    try {
                        int startOffset = Utilities.getRowStart(doc, dotPos);
                        int endOffset = Utilities.getRowEnd(doc, dotPos);
                        FormattingUtils.reformat(doc, startOffset, endOffset);
                    } finally {
                        doc.atomicUnlock();
                    }
                }
            }
        }

        protected void replaceSelection( JTextComponent target, int dotPos,
                Caret caret, String str, boolean overwrite )
                throws BadLocationException
        {
            char insertedChar = str.charAt(0);
            // the same cast was already performed in parent
            BaseDocument doc = (BaseDocument)target.getDocument();
            if ( TokenUtils.checkPhp(doc, dotPos) 
                    &&
                    ( insertedChar == '\"' 
                    || insertedChar == '\''
                    || insertedChar == '`') )
            {
                removeSelection(doc, dotPos, caret);

                int caretPosition = caret.getDot();
                boolean inserted = BracketCompletion.completeQuote(doc, caretPosition, 
                        caret, insertedChar);
                if (inserted) {
                    caret.setDot(caretPosition + 1);
                } else {
                    doc.insertString(caretPosition, str, null);
                }
            }
            else {
                super.replaceSelection(target, dotPos, caret, str, overwrite);
                if (TokenUtils.checkPhp(doc, dotPos)) {
                    BracketCompletion.charInserted(doc, caret.getDot() - 1, 
                            caret, insertedChar);
                }
            }
        }

        /**
         * removes selected block if any.
         * @returns true if something was removed. false otherwise
         */
        private boolean removeSelection(BaseDocument doc, int dotPos, Caret caret) 
                throws BadLocationException
        {
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            if (p0 != p1) {
                doc.remove(p0, p1 - p0);
                return true;
            }
            return false;
        }
    }

    public static class PhpInsertBreakAction extends InsertBreakAction {

        private static final long serialVersionUID = 1669306776471076647L;

        private static final String STATE_DOT_CONNECTED_STRING 
                = "string-connected-with-dot"; // NOI18N
        private static final String STATE_MULTILINE_STRING 
                = "string-multiline"; // NOI18N
        private static final String STATE_OPENED_BRACE 
                = "opened-brace"; // NOI18N
        private static final String STATE_EOD_STRING 
                = "string-eod"; // NOI18N

        @Override
        protected Object beforeBreak( JTextComponent target, BaseDocument doc,
                Caret caret )
        {
            int dotPos = caret.getDot();
            if (!TokenUtils.checkPhp(doc, dotPos)){
                return null;
            }
            if ( BracketCompletion.posWithinString(doc, dotPos)) {
                if (BracketCompletion.useDotConnectorInStringSetting()) {
                    insertDotConnectorBeforeBreak(doc, caret, dotPos);
                    return STATE_DOT_CONNECTED_STRING;
                } else {
                    return STATE_MULTILINE_STRING;
                }
            }
            else if (BracketCompletion.isNotClosedEOD(doc, dotPos)){
                try {
                    closeEODBeforeBreak(doc, caret, dotPos);
                    return STATE_EOD_STRING;
                } catch (BadLocationException ex) {
                }
            }   
            else {
                try {
                    if (BracketCompletion.isAddRightBrace(doc, dotPos)) {
                        BracketCompletion.addRightBrace(doc, caret, dotPos);
                        return STATE_OPENED_BRACE;
                    }
                } catch (BadLocationException ex) {
                }
            }
            return null;
        }

        @Override
        protected void afterBreak( JTextComponent target, BaseDocument doc,
                Caret caret, Object cookie )
        {
            if (STATE_DOT_CONNECTED_STRING.equals(cookie)){
                if (BracketCompletion.useDotConnectorInStringSetting()) {
                    insertDotConnectorAfterBreak(caret);
                }
            }
            // do nothing for STATE_MULTILINE_STRING and STATE_OPENED_BRACE
        }

        private Integer closeEODBeforeBreak(BaseDocument doc, 
                Caret caret, int dotPos) throws BadLocationException 
        {
            String eodLabel = BracketCompletion.getEODStringLabel(doc, dotPos);
            if (eodLabel.length() > 0) {
                // do not leave trailing spaces on the line
                cursorBeforeTrailingSpaces(doc, caret, dotPos);

                doc.insertString(dotPos, eodLabel + TokenUtils.SEMICOLON, null);
                FormattingUtils.indentNewLine(doc, dotPos);
                caret.setDot(dotPos);
            }
            return dotPos;
        }
        
        private void cursorBeforeTrailingSpaces(BaseDocument doc, 
                Caret caret, int dotPos) throws BadLocationException
        {
            int lastNonWhite = Utilities.getFirstNonWhiteBwd(doc, dotPos);
            if (lastNonWhite < dotPos){
                caret.setDot(lastNonWhite + 1);
                dotPos = caret.getDot();
            }
        }
        
        private Integer insertDotConnectorBeforeBreak(BaseDocument doc, 
                Caret caret, int dotPos) 
        {
            try {
                Token token = TokenUtils.getPhpToken(doc, dotPos);
                char quote = token.text().charAt(0);
                doc.insertString(dotPos, quote+". "+quote, null); // NOI18N
                dotPos += 3;
                caret.setDot(dotPos);
                return dotPos;
            } catch (BadLocationException ex) {
            }
            return null;
        }

        private void insertDotConnectorAfterBreak(Caret caret) {
            int nowDotPos = caret.getDot();
            caret.setDot(nowDotPos + 1);
        }
        
    }


    public static class PhpDeleteCharAction extends ExtDeleteCharAction {

        private static final long serialVersionUID = 7261622068721435459L;

        public PhpDeleteCharAction( String nm, boolean nextChar ) {
            super(nm, nextChar);
        }

        @Override
        protected void charDeleted(BaseDocument doc, int dotPos, 
                Caret caret, char ch) throws BadLocationException 
        {
            charBackspaced(doc, dotPos, caret, ch);
        }

        
        
        @Override
        protected void charBackspaced( BaseDocument doc, int dotPos,
                Caret caret, char ch ) throws BadLocationException
        {
            if ( TokenUtils.checkPhp(doc, dotPos) ) {
                BracketCompletion.charBackspaced(doc, dotPos, caret, ch);
            }
        }
        
        
    }
}
