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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.codegen;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.openide.util.Exceptions;

/**
 * Action which inserts an appropriate character at line-end without moving
 * the caret.
 *
 * @author Tim Boudreau
 */
public final class InsertSemicolonAction extends BaseAction {
    private final boolean withNewline;
    private final char what;
    
    protected InsertSemicolonAction (String name, char what, boolean withNewline) {
        super(name);
        this.withNewline = withNewline;
        this.what = what;
    }

    public InsertSemicolonAction(String name, boolean withNewline) {
        this (name, ';', withNewline);
    }
    
    public InsertSemicolonAction(boolean withNewLine) {
        this (withNewLine ? "complete-line-newline" : "complete-line", ';', 
                withNewLine);
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (!target.isEditable() || !target.isEnabled()) {
            target.getToolkit().beep();
            return;
        }
        final BaseDocument doc = (BaseDocument) target.getDocument();
        final class R implements Runnable {
            public void run() {
                Caret caret = target.getCaret();
                int dotpos = caret.getDot();
                Formatter formatter = doc.getFormatter();
                formatter.indentLock();
                try {
                    int eoloffset = Utilities.getRowEnd(target, dotpos);
                    doc.insertString(eoloffset, "" + what, null);
                    if (withNewline) {
                        //This is code from the editor module, but it is
                        //a pretty strange way to do this:
                        doc.insertString(dotpos, "-", null); //NOI18N
                        doc.remove(dotpos, 1);
                        int eolDot = Utilities.getRowEnd(target, caret.getDot());
                        int newDotPos = formatter.indentNewLine(doc, eolDot);
                        caret.setDot(newDotPos);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    formatter.indentUnlock();
                }
            }
        };
        doc.runAtomicAsUser(new R());
    }
}
