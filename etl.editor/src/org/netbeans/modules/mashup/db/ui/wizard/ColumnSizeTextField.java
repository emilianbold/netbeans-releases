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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.sun.sql.framework.utils.Logger;

/**
 * Used in ColumnMetadata table to render scale and precision
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ColumnSizeTextField extends JTextField {
    /**
     * class IntegerDocument extends a plain document.
     */
    protected class IntegerDocument extends PlainDocument {

        /**
         * method insertString inserts a string into the text field.
         * 
         * @param offs is the offset to insert
         * @param str is the string to insert
         * @param a is the attribute.
         * @throws BadLocationException if the string cannot be inserted.
         */
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;

            for (int i = 0; i < result.length; i++) {
                if (Character.isDigit(source[i])) {
                    result[j++] = source[i];
                } else {
                    toolkit.beep();
                    Logger.print(Logger.DEBUG, getClass().getName(), "insertString()", "insertString: " + source[i]);
                }
            }
            super.insertString(offs, new String(result, 0, j), a);
        }
    }

    private NumberFormat integerFormatter;

    private Toolkit toolkit;

    /**
     * Creates a new instance of IntegerField.
     * 
     * @param columns number of columns used to calculate preferred width
     */
    public ColumnSizeTextField(int columns) {
        super(columns);
        setHorizontalAlignment(SwingConstants.RIGHT);

        toolkit = Toolkit.getDefaultToolkit();
        integerFormatter = NumberFormat.getNumberInstance(Locale.US);
        integerFormatter.setParseIntegerOnly(true);
    }

    /**
     * Creates a new instance of IntegerField.
     * 
     * @param value is the initial value to display
     * @param columns number of columns used to calculate preferred width
     */
    public ColumnSizeTextField(int value, int columns) {
        this(columns);
        setValue(value);
    }

    /**
     * Gets an integer value from the text field.
     * 
     * @return int value retrieved
     */
    public int getValue() {
        int retVal = 0;
        try {
            retVal = integerFormatter.parse(getText()).intValue();
        } catch (ParseException e) {
            toolkit.beep();
        }
        return retVal;
    }

    /**
     * Sets the given integer value into the text field.
     * 
     * @param value is the value to use.
     */
    public void setValue(int value) {
        setText(integerFormatter.format(value));
    }

    /**
     * Creates an IntegerDocument as the default model.
     * 
     * @return Document that is created.
     */
    protected Document createDefaultModel() {
        return new IntegerDocument();
    }
}

