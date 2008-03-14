/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
public class CCFormatterBaseUnitTestCase extends BaseDocumentUnitTestCase {
    private boolean isCPP = true;
    
    public CCFormatterBaseUnitTestCase(String testMethodName) {
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
	try {
	    f.indentLock();
	    int offset = f.indentNewLine(getDocument(), getCaretOffset());
	    getCaret().setDot(offset);
	} finally {
	    f.indentUnlock();
	}
    }
    
    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        Formatter f = getDocument().getFormatter();
        try {
	    f.reformatLock();
            f.reformat(getDocument(), 0, getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
        } finally {
	    f.reformatUnlock();
	}
    }
}
