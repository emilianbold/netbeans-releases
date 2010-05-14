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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor.model;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * A PlainDocument subclass that restricts values to digits only.
 *
 * @author Noel.Ang@sun.com
 */
public class NumberFieldDocument
        extends PlainDocument {

    private String filter(String value) {
        StringBuffer buffer = new StringBuffer(value);
        int i = 0;
        while (i < buffer.length()) {
            if (!Character.isDigit(buffer.charAt(i))) {
                buffer.delete(i, i + 1);
            } else {
                ++i;
            }
        }
        return buffer.toString();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a)
            throws
            BadLocationException {
        super.insertString(offs, filter(str), a);
    }
}
