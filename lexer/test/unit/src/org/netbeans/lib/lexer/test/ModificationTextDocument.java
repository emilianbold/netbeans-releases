/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

public class ModificationTextDocument extends PlainDocument {

    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        super.insertUpdate(chng, attr);
        DocumentUtilities.addEventPropertyStorage(chng);
        try {
            DocumentUtilities.putEventProperty(chng, String.class,
                    getText(chng.getOffset(), chng.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
    }

    protected void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        DocumentUtilities.addEventPropertyStorage(chng);
        try {
            DocumentUtilities.putEventProperty(chng, String.class,
                    getText(chng.getOffset(), chng.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
    }

}