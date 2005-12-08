/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.Formatter;

/**
 * Java formatter tests.
 *
 * @autor Miloslav Metelka
 */
public class JavaFormatterUnitTestCase extends JavaBaseDocumentUnitTestCase {
    
    public JavaFormatterUnitTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected void indentNewLine() {
        Formatter f = getDocument().getFormatter();
        int offset = f.indentNewLine(getDocument(), getCaretOffset());
        getCaret().setDot(offset);
    }
    
    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        Formatter f = getDocument().getFormatter();
        try {
            f.reformat(getDocument(), 0, getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
        }
    }
    
}
