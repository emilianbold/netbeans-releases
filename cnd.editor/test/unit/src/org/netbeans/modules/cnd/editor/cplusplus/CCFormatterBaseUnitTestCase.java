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
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.test.base.BaseDocumentUnitTestCase;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

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

    protected void setDefaultsOptions(){
        // Note due to IZ#130533 the default style is changed. Hence we reset some properties.
        if (isCPP) {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceNamespace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        } else {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C));
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceNamespace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        }
    }
    protected void setDefaultsOptions(String style){
        if (isCPP) {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP), style);
        } else {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C));
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C), style);
        }
    }
    

    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected void indentNewLine() {
        Indent indenter = Indent.get(getDocument());
        indenter.lock();
        try {
            getDocument().atomicLock();
            try {
                int offset = indenter.indentNewLine(getCaretOffset());
                getCaret().setDot(offset);
            } catch (BadLocationException ble) {
                throw new IllegalStateException(ble);
            } finally {
                getDocument().atomicUnlock();
            }
        } finally {
            indenter.unlock();
        }
    }
    
    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        Reformat f = Reformat.get(getDocument());
        f.lock();
        try {
            getDocument().atomicLock();
            try {
                f.reformat(0, getDocument().getLength());
            } finally {
                getDocument().atomicUnlock();
            }
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
        } finally {
	    f.unlock();
	}
    }
}
