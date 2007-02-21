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

package org.netbeans.modules.soa.mapper.basicmapper.literal;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * A dialog which displays a number field
 * @author rshankar
 * @version 1.0
 */
public class NumberField extends JTextField {
    /**
     * @param cols number of columns
     */
    public NumberField(int cols) {
        super(cols);
    }

    /**
     * @return Document document associated with the textfield
     */
    protected Document createDefaultModel() {
        return new NumberDocument();
    }
 
    private static class NumberDocument extends PlainDocument {
        public void insertString(int offs, String str, AttributeSet a) 
          throws BadLocationException {
            if (str == null) {
                return;
            }
            AbstractDocument.Content content = getContent();
            String oldStr = content.getString(0, content.length());
            char[] chars = str.toCharArray();
            String s = "";
            boolean foundNumber = false;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '.') {
                    s += c;
                    if (oldStr.indexOf(".") == -1) {
                        foundNumber = true;
                    }
                } else if ((oldStr.indexOf("-") == -1 && offs == 0 && i == 0 && c == '-') 
                    || (c >= '0' && c <= '9')) {
                    s += c;
                    foundNumber = true;
                } else {
                    break;
                }
            }
            if (!foundNumber) {
                s = "";
            }
            super.insertString(offs, s, a);
        }
    }
}
