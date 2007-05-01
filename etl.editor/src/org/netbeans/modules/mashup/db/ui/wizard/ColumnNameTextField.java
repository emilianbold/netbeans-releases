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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ColumnNameTextField extends JTextField {
    /**
     * Extends a plain document to enforce character limitataions for a field name
     * textfield.
     */
    protected class FieldNameDocument extends PlainDocument {
        /**
         * Inserts a string into the text field.
         * 
         * @param offs is the offset to insert
         * @param str is the string to insert
         * @param a is the attribute.
         * @throws BadLocationException if the string cannot be inserted.
         */
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            char[] source = str.toCharArray();

            if (offs == 0 && !Character.isLetter(source[0])) {
                // First character of field name must be a letter.
                toolkit.beep();
                return;
            } else if (str.length() == 1) {
                // Check individual char if illegal, beep and refuse if true.
                if (!isValidChar(source[0])) {
                    toolkit.beep();
                    return;
                }
            } else {
                // Must be a pasted string, check all characters and display error message
                // if it contains illegal chars.
                boolean isBadString = false;
                for (int i = 0; i < source.length; i++) {
                    if (!isValidChar(source[i])) {
                        isBadString = true;
                        break;
                    }
                }

                if (isBadString) {
                    toolkit.beep();
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(ColumnNameTextField.class, "ERROR_invalid_chars", str)));
                    return;
                }
            }

            super.insertString(offs, str.toUpperCase(), a);
        }

        private boolean isValidChar(final char c) {
            return Character.isDigit(c) || Character.isLetter(c) || ('_' == c) || ('$' == c) || ('#' == c);
        }
    }

    private Toolkit toolkit;

    /**
     * Creates a new instance of ColumnNameTextField.
     * 
     * @param value is the value to create with.
     * @param columns is used to construct this object's subclass
     */
    public ColumnNameTextField() {
        toolkit = Toolkit.getDefaultToolkit();
    }

    /**
     * Creates a FieldNameDocument as the default model.
     * 
     * @return Document that is created.
     */
    protected Document createDefaultModel() {
        return new FieldNameDocument();
    }
}

