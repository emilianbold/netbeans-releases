/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;

/**
 * @author pfiala
 */
abstract class NonEditableDocument extends PlainDocument {

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
