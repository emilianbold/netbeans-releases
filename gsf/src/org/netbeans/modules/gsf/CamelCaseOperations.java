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

package org.netbeans.modules.gsf;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.gsf.BracketCompletion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;

/**
 * CamelCase operations - based on Java ones but rewritten to delegate all logic
 * to language plugins
 * 
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 * @author Tor Norbye
 */
/* package */ class CamelCaseOperations {

    static int nextCamelCasePosition(JTextComponent textComponent, Language language) {
        int offset = textComponent.getCaretPosition();
        Document doc = textComponent.getDocument();

        // Are we at the end of the document?
        if (offset == doc.getLength()) {
            return -1;
        }

        BracketCompletion bc = language.getBracketCompletion();
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(doc, offset, false);
            if (nextOffset != -1) {
                return nextOffset;
            }
        }
        
        try {
            return Utilities.getNextWord(textComponent, offset);
        } catch (BadLocationException ble) {
            // something went wrong :(
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static int previousCamelCasePosition(JTextComponent textComponent, Language language) {
        int offset = textComponent.getCaretPosition();

        // Are we at the beginning of the document?
        if (offset == 0) {
            return -1;
        }
        
        BracketCompletion bc = language.getBracketCompletion();
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(textComponent.getDocument(), 
                    offset, true);
            if (nextOffset != -1) {
                return nextOffset;
            }
        }
        
        try {
            return Utilities.getPreviousWord(textComponent, offset);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static void replaceChar(JTextComponent textComponent, int offset, char c) {
        if (!textComponent.isEditable()) {
            return;
        }
        replaceText(textComponent, offset, 1, String.valueOf(c));
    }

    static void replaceText(JTextComponent textComponent, int offset, int length, String text) {
        if (!textComponent.isEditable()) {
            return;
        }
        Document document = textComponent.getDocument();
        if (document instanceof BaseDocument) {
            ((BaseDocument)document).atomicLock();
        }
        try {
            if (length > 0) {
                document.remove(offset, length);
            }
            document.insertString(offset, text, null);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        } finally {
            if (document instanceof BaseDocument) {
                ((BaseDocument)document).atomicUnlock();
            }
        }
    }
}
