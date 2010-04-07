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

package org.netbeans.modules.cnd.editor.fortran;

import java.awt.Cursor;
import java.util.ArrayList;

import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.Position;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;

import org.netbeans.modules.cnd.editor.fortran.indent.FortranHotCharIndent;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.util.Exceptions;

/**
* Fortran editor kit with appropriate document
*/

public class FKit extends NbEditorKit {

    private InputAttributes lexerAttrs = null;

    @Override
    public String getContentType() {
        return MIMENames.FORTRAN_MIME_TYPE;
    }

    @Override
    public void install(JEditorPane c) {
        super.install(c);
    }

    @Override
    public Document createDefaultDocument() {
        BaseDocument doc = new NbEditorDocument(MIMENames.FORTRAN_MIME_TYPE);
        // Force '\n' as write line separator // !!! move to initDocument()
        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, BaseDocument.LS_LF);
        return doc; 
    }

    /** Initialize document by adding the draw-layers for example. */
    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        doc.putProperty(InputAttributes.class, getLexerAttributes(doc));
        doc.putProperty(Language.class, getLanguage());
    }

    protected Language<FortranTokenId> getLanguage() {
        return FortranTokenId.languageFortran();
    }

    protected final synchronized InputAttributes getLexerAttributes(BaseDocument doc) {
        // for now use shared attributes for all documents to save memory
        // in future we can make attributes per document based on used compiler info
        if (lexerAttrs == null) {
            lexerAttrs = new InputAttributes();
            lexerAttrs.setValue(getLanguage(), CndLexerUtilities.LEXER_FILTER, getFilter(), true);
            lexerAttrs.setValue(getLanguage(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, FSettingsFactory.MAXIMUM_TEXT_WIDTH, true);
        }
        lexerAttrs.setValue(getLanguage(), CndLexerUtilities.FORTRAN_FREE_FORMAT, FortranCodeStyle.get(doc).isFreeFormatFortran(), true);
        return lexerAttrs;
    }

    protected Filter<FortranTokenId> getFilter() {
        return CndLexerUtilities.getFortranFilter();
    }

    @Override
    protected Action[] createActions() {
	int arraySize = 5;
	int numAddClasses = 0;
	if (actionClasses != null) {
	    numAddClasses = actionClasses.size();
	    arraySize += numAddClasses;
	}
        Action[] fortranActions = new Action[arraySize];
	int index = 0;
	if (actionClasses != null) {
	    for (int i = 0; i < numAddClasses; i++) {
		Class<?> c = actionClasses.get(i);
		try {
		    fortranActions[index] = (Action)c.newInstance();
		} catch (java.lang.InstantiationException e) {
		    e.printStackTrace();
		} catch (java.lang.IllegalAccessException e) {
		    e.printStackTrace();
		}
		index++;
	    }
	}
	fortranActions[index++] = new FFormatAction();
	fortranActions[index++] = new CCDefaultKeyTypedAction();
	fortranActions[index++] = new CommentAction("!"); // NOI18N
	fortranActions[index++] = new UncommentAction("!"); // NOI18N
	fortranActions[index++] = new ToggleCommentAction("!"); // NOI18N
        return TextAction.augmentList(super.createActions(), fortranActions);
    }

    /** Holds action classes to be created as part of createAction.
        This allows dependent modules to add editor actions to this
        kit on startup.
    */
    private static ArrayList<Class<?>> actionClasses = null;


    public static void addActionClass(Class<?> action) {
	if (actionClasses == null) {
	    actionClasses = new ArrayList<Class<?>>(2);
	}
	actionClasses.add(action);
    }

    @Override
    protected void updateActions() {
	super.updateActions();
        addSystemActionMapping(formatAction, FFormatAction.class);
    }
    
    public static class FFormatAction extends BaseAction {

	public FFormatAction() {
	    super(BaseKit.formatAction,
		  MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
	    putValue ("helpID", FFormatAction.class.getName ()); // NOI18N
	}

        
        @Override
   	public void actionPerformed(ActionEvent evt, final JTextComponent target) {
	    if (target != null) {

		if (!target.isEditable() || !target.isEnabled()) {
		    target.getToolkit().beep();
		    return;
		}

		final BaseDocument doc = (BaseDocument)target.getDocument();
                // Set hourglass cursor
                Cursor origCursor = target.getCursor();
                target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                doc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Caret caret = target.getCaret();
                            int caretLine = Utilities.getLineOffset(doc, caret.getDot());
                            int startPos;
                            Position endPosition;
                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = target.getSelectionStart();
                                endPosition = doc.createPosition(target.getSelectionEnd());
                            } else {
                                startPos = 0;
                                endPosition = doc.createPosition(doc.getLength());
                            }

                            int pos = startPos;
                            Reformat reformat = Reformat.get(doc);
                            reformat.lock();
                            try {
                                reformat.reformat(pos, endPosition.getOffset());
                            } finally {
                                reformat.unlock();
                            }

                            // Restore the line
                            pos = Utilities.getRowStartFromLineOffset(doc, caretLine);
                            if (pos >= 0) {
                                caret.setDot(pos);
                            }
                        } catch (BadLocationException e) {
                            //failed to format
                        }
                    }
                });
                target.setCursor(origCursor);

            }
        }
    }    

    private static class CCDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
        @Override
        protected void checkIndentHotChars(JTextComponent target, String typedText) {
            BaseDocument doc = Utilities.getDocument(target);
            int offset = target.getCaretPosition();
            if (FortranHotCharIndent.INSTANCE.getKeywordBasedReformatBlock(doc, offset, typedText)) {
                Indent indent = Indent.get(doc);
                indent.lock();
                try {
                    doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                    indent.reindent(offset);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                } finally{
                    doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                    indent.unlock();
                }
            }
       	}

        @Override
        protected void insertString(BaseDocument doc, int dotPos,
                Caret caret, String str,
                boolean overwrite) throws BadLocationException {
            super.insertString(doc, dotPos, caret, str, overwrite);
            FortranBracketCompletion.charInserted(doc, dotPos, caret, str.charAt(0));
        }
   }
}
