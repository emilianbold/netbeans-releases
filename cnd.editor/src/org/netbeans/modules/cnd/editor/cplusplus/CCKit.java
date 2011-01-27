/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;


import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.editor.ext.ExtKit.UncommentAction;
import org.netbeans.modules.editor.NbEditorKit;

import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Reformat;

/** C++ editor kit with appropriate document */
public class CCKit extends NbEditorKit {
    /* package */ static final String previousCamelCasePosition = "previous-camel-case-position"; //NOI18N
    /* package */ static final String nextCamelCasePosition = "next-camel-case-position"; //NOI18N
    /* package */ static final String selectPreviousCamelCasePosition = "select-previous-camel-case-position"; //NOI18N
    /* package */ static final String selectNextCamelCasePosition = "select-next-camel-case-position"; //NOI18N
    /* package */ static final String deletePreviousCamelCasePosition = "delete-previous-camel-case-position"; //NOI18N
    /* package */ static final String deleteNextCamelCasePosition = "delete-next-camel-case-position"; //NOI18N

    private InputAttributes lexerAttrs = null;

    public CCKit() {
        // default constructor needed to be created from services
    }
    
    @Override
    public String getContentType() {
        return MIMENames.CPLUSPLUS_MIME_TYPE;
    }

    @Override
    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument();
        return doc;
    }

    /** Initialize document by adding the draw-layers for example. */
    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        doc.putProperty(InputAttributes.class, getLexerAttributes());
        doc.putProperty(Language.class, getLanguage());
    }

    protected Language<CppTokenId> getLanguage() {
        return CppTokenId.languageCpp();
    }

    protected final synchronized InputAttributes getLexerAttributes() {
        // for now use shared attributes for all documents to save memory
        // in future we can make attributes per document based on used compiler info
        if (lexerAttrs == null) {
            lexerAttrs = new InputAttributes();
            lexerAttrs.setValue(getLanguage(), CndLexerUtilities.LEXER_FILTER, getFilter(), true);  // NOI18N
        }
        return lexerAttrs;
    }

    protected Filter<CppTokenId> getFilter() {
        return CndLexerUtilities.getGccCppFilter();
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

    protected 
    @Override
    Action[] createActions() {
        Action[] superActions = super.createActions();
        Action[] ccActions = new Action[]{
            new CCFormatAction(),
            getToggleCommentAction(),
            getCommentAction(),
            getUncommentAction(),

            new NextCamelCasePosition(findAction(superActions, nextWordAction)),
            new PreviousCamelCasePosition(findAction(superActions, previousWordAction)),
            new SelectNextCamelCasePosition(findAction(superActions, selectionNextWordAction)),
            new SelectPreviousCamelCasePosition(findAction(superActions, selectionPreviousWordAction)),
            new DeleteToNextCamelCasePosition(findAction(superActions, removeNextWordAction)),
            new DeleteToPreviousCamelCasePosition(findAction(superActions, removePreviousWordAction)),

            new InsertSemicolonAction(true),
            new InsertSemicolonAction(false),};
        ccActions = TextAction.augmentList(superActions, ccActions);

        return ccActions;
    }

    private static Action findAction(Action[] actions, String name) {
        for (Action a : actions) {
            Object nameObj = a.getValue(Action.NAME);
            if (nameObj instanceof String && name.equals(nameObj)) {
                return a;
            }
        }
        return null;
    }

    /** Holds action classes to be created as part of createAction.
    This allows dependent modules to add editor actions to this
    kit on startup.
     */
    @Override
    protected void updateActions() {
        super.updateActions();
        addSystemActionMapping(formatAction, CCFormatAction.class);
    }

    public class CCFormatAction extends BaseAction {

        public CCFormatAction() {
            super(BaseKit.formatAction,
                    MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            putValue("helpID", CCFormatAction.class.getName()); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if (target != null) {

                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

		final BaseDocument doc = (BaseDocument)target.getDocument();
                final Reformat formatter = Reformat.get(doc);

                // Set hourglass cursor
                Cursor origCursor = target.getCursor();
                target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                formatter.lock();
                try {
                    doc.runAtomic(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Caret caret = target.getCaret();

                                int caretLine = Utilities.getLineOffset(doc, caret.getDot());
                                int start;
                                int end;
                                //if (caret.isSelectionVisible()) {
                                if (Utilities.isSelectionShowing(caret)) {
                                    start = target.getSelectionStart();
                                    end = target.getSelectionEnd();
                                } else {
                                    start = 0;
                                    end = doc.getLength();
                                }

                                formatter.reformat(start, end);

                                // Restore the line
                                int pos = Utilities.getRowStartFromLineOffset(doc, caretLine);
                                if (pos >= 0) {
                                    caret.setDot(pos);
                                }
                            } catch (BadLocationException e) {
                                //failed to format
                            }
                        }
                    });
                } finally {
                    formatter.unlock();
                }

                target.setCursor(origCursor);
	    }
	}
    }

}
