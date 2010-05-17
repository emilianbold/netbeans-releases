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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.openide.util.NbBundle;

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
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(InsertSemicolonAction.class, name));
    }

    public InsertSemicolonAction(String name, boolean withNewline) {
        this (name, ';', withNewline); //NOI18N
    }
    
    public InsertSemicolonAction(boolean withNewLine) {
        this (withNewLine ? "complete-line-newline" : "complete-line", withNewLine); //NOI18N
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
                try {
                    int eoloffset = Utilities.getRowEnd(target, dotpos);
                    String insertString = "" + what;
                    doc.insertString(eoloffset, insertString, null); //NOI18N
                    if (withNewline) {
                        Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            int eolDot = Utilities.getRowEnd(target, caret.getDot());
                            doc.insertString(eolDot, "\n", null); // NOI18N
                            caret.setDot(eolDot+1);
                            indent.reindent(eolDot+1);
                        } finally {
                            indent.unlock();
                        }
                    }
                } catch (BadLocationException ex) {
                }
            }
        }
        doc.runAtomicAsUser(new R());
    }
}
