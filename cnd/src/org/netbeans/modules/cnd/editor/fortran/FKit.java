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

package org.netbeans.modules.cnd.editor.fortran;

import java.io.Writer;
import java.io.IOException;
import java.io.CharArrayWriter;
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
	int arraySize = 2;
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

	public void actionPerformed(ActionEvent evt, JTextComponent target) {
	    if (target != null) {

		if (!target.isEditable() || !target.isEnabled()) {
		    target.getToolkit().beep();
		    return;
		}

		Caret caret = target.getCaret();
		BaseDocument doc = (BaseDocument)target.getDocument();

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

			CharArrayWriter cw = new CharArrayWriter();
			Writer w = doc.getFormatter().createWriter(doc, pos, cw);
			w.write(doc.getChars(pos, stopPos - pos));
			w.close();
			String out = new String(cw.toCharArray());
			doc.remove(pos, stopPos - pos);
			doc.insertString(pos, out, null);
			pos += out.length(); // go to the end of the area inserted
		    }

		    // Restore the line
		    pos = Utilities.getRowStartFromLineOffset(doc, caretLine);
		    if (pos >= 0) {
			caret.setDot(pos);
		    }
		} catch (BadLocationException e) {
		    if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
			e.printStackTrace();
		    }
		} catch (IOException e) {
		    if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
			e.printStackTrace();
		    }
		} finally {
		    doc.atomicUnlock();
		}
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
    } // end class FDefaultKeyTypedAction
}
