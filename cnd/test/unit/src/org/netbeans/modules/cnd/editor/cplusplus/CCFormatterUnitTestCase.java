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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.cnd.test.base.BaseDocumentUnitTestCase;

/**
 *
 * @author Alexander Simon
 */
public class CCFormatterUnitTestCase extends BaseDocumentUnitTestCase {
    private boolean isCPP = true;
    
    public CCFormatterUnitTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    @Override
    protected EditorKit createEditorKit() {
        if (isCPP) {
            return new CCKit();
        } else {
            return new CKit();
        }
    }

    protected void setCppEditorKit(boolean isCPP){
        this.isCPP = isCPP;
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
