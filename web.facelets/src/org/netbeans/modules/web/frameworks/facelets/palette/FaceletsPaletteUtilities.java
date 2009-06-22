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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.palette;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
/**
 *
 * @author Petr Pisl
 */


public class FaceletsPaletteUtilities {
    
    /** Creates a new instance of jbossddPaletteUtilities */
    public FaceletsPaletteUtilities() {
    }
    
    public static void insert(String text, JTextComponent target)
    throws BadLocationException {
        insert(text, target, true);
    }
    
    public static void insert(final String text, final JTextComponent target, boolean reformat)
    throws BadLocationException {

        if (text == null)
            ;

        Document doc = target.getDocument();
        if (doc == null)
            return;

       if (doc instanceof BaseDocument) {
            final BaseDocument baseDoc = (BaseDocument) doc;
            Runnable edit = new Runnable() {

                public void run() {
                    try {
                        int start = insert(text, target, baseDoc);
                    } catch (Exception e) {
                    }
                }
            };
            baseDoc.runAtomic(edit);
        }

    }

    private static int insert(String text, JTextComponent target, Document doc)
    throws BadLocationException {

        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int startPossition = Math.min(caret.getDot(), caret.getMark());
            int endPossition = Math.max(caret.getDot(), caret.getMark());
            doc.remove(startPossition, endPossition - startPossition);

            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, text, null);
        } catch (BadLocationException ble) {}

        return start;
    }
    
}
