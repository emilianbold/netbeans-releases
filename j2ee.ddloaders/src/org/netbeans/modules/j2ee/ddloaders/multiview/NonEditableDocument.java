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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;

/**
 * @author pfiala
 */
public abstract class NonEditableDocument extends PlainDocument {

    String text = null;

    protected abstract String retrieveText();

    protected NonEditableDocument() {
        init();
    }

    public void init() {
        String s = retrieveText();
        if (s == null) {
            s = "";
        }
        if (!s.equals(text)) {
            text = s;
            try {
                super.remove(0, super.getLength());
                super.insertString(0, s, null);
            } catch (BadLocationException e) {

            }
        }
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {

    }

    public void remove(int offs, int len) throws BadLocationException {

    }
}
