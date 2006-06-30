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
