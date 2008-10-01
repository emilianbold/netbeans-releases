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

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import org.netbeans.modules.editor.*;
import org.netbeans.modules.cnd.MIMENames;

/**
* Fortran editor kit with appropriate document
*/

public class FKit extends NbEditorKit {

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
        BaseDocument doc = new NbEditorDocument(this.getClass());
        // Force '\n' as write line separator // !!! move to initDocument()
        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, BaseDocument.LS_LF);
        return doc; 
    }

    /** Create new instance of syntax coloring scanner
     * @param doc document to operate on. It can be null in the cases the syntax
     *   creation is not related to the particular document
     */
    @Override
    public Syntax createSyntax(Document doc) {
        return new FSyntax();
    }

    /** Create the formatter appropriate for this kit */
    @Override
    public Formatter createFormatter() {
        return new FFormatter(this.getClass());
    }

    @Override
    protected Action[] createActions() {
	int arraySize = 4;
	int numAddClasses = 0;
	if (actionClasses != null) {
	    numAddClasses = actionClasses.size();
	    arraySize += numAddClasses;
	}
        Action[] fortranActions = new Action[arraySize];
	int index = 0;
	if (actionClasses != null) {
	    for (int i = 0; i < numAddClasses; i++) {
		Class c = actionClasses.get(i);
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
	fortranActions[index++] = new FDefaultKeyTypedAction();
	fortranActions[index++] = new FFormatAction();
        fortranActions[index++] = new CommentAction("!"); // NOI18N
        fortranActions[index++] = new UncommentAction("!"); // NOI18N 
        return TextAction.augmentList(super.createActions(), fortranActions);
    }

    /** Holds action classes to be created as part of createAction.
        This allows dependent modules to add editor actions to this
        kit on startup.
    */
    private static ArrayList<Class> actionClasses = null;


    public static void addActionClass(Class action) {
	if (actionClasses == null) {
	    actionClasses = new ArrayList<Class>(2);
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

                        Formatter formatter = doc.getFormatter();
                        formatter.reformatLock();
                        try {
                            while (pos < endPosition.getOffset()) {
                                int stopPos = endPosition.getOffset();
                                int reformattedLen = formatter.reformat(doc, pos, stopPos);
                                pos = pos + reformattedLen;
                            }
                        } finally {
                            formatter.reformatUnlock(); 
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
    
    public static class FDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        /** Check and possibly popup, hide or refresh the completion */
        @Override
	protected void checkCompletion(JTextComponent target, String typedText) {
	    Completion completion = ExtUtilities.getCompletion(target);
	    if (completion != null && typedText.length() > 0) {
		if (!completion.isPaneVisible()) { // pane not visible yet
		    if (completion.isAutoPopupEnabled()) {
			boolean pop = false;
			switch (typedText.charAt(0)) {
			case ' ':
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
					pop = true;
				    }
				} catch (BadLocationException e) {
				}
			    }
			    break;
			    
			case '.':
			case ',':
			    pop = true;
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
			completion.setPaneVisible(false);
			break;
			
		    default:
			completion.refresh(true);
			break;
		    }
		}
	    }
	}

         @Override
         protected void checkIndentHotChars(JTextComponent target, String typedText) {
             BaseDocument doc = Utilities.getDocument(target);
             if (doc != null) {
                 super.checkIndentHotChars(target, typedText);
             }
         }

    } // end class FDefaultKeyTypedAction
}
