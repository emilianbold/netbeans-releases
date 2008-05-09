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
package org.netbeans.modules.soa.ui.form;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author nk160297
 */
public class RangeDoubleDocument extends PlainDocument {
    
    double myMinValue;
    double myMaxValue;
    
    public RangeDoubleDocument(double minValue, double maxValue) {
        super();
        myMinValue = minValue;
        myMaxValue = maxValue;
    }
    
    public void replace(int offset, int length, String text, AttributeSet attrs)
    throws BadLocationException {
        //
        String fullText = getText(0, getLength());
        StringBuffer sb = new StringBuffer(Math.max(fullText.length(), 10));
        sb.append(fullText.substring(0, offset));
        sb.append(text);
        sb.append(fullText.substring(offset + length));
        //
        if (sb.length() == 0) {
            super.replace(offset, length, text, attrs);
        } else {
            try {
                double newValue = Double.parseDouble(sb.toString());
                //
                if (newValue >= myMinValue && newValue <= myMaxValue) {
                    super.replace(offset, length, text, attrs);
                }
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
    }
    
    public void remove(int offset, int length) throws BadLocationException {
        //
        String fullText = getText(0, getLength());
        StringBuffer sb = new StringBuffer(fullText.length());
        sb.append(fullText.substring(0, offset));
        sb.append(fullText.substring(offset + length));
        //
        if (sb.length() == 0) {
            super.remove(offset, length);
        } else {
            try {
                double newValue = Double.parseDouble(sb.toString());
                //
                if (newValue >= myMinValue && newValue <= myMaxValue) {
                    super.remove(offset, length);
                }
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
    }
    
    public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException {
        //
        String fullText = getText(0, getLength());
        StringBuffer sb = new StringBuffer(fullText.length() + str.length());
        sb.append(fullText.substring(0, offset));
        sb.append(str);
        sb.append(fullText.substring(offset));
        //
        if (sb.length() == 0) {
            super.insertString(offset, str, a);
        } else {
            try {
                double newValue = Double.parseDouble(sb.toString());
                //
                if (newValue >= myMinValue && newValue <= myMaxValue) {
                    super.insertString(offset, str, a);
                }
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
    }
}
