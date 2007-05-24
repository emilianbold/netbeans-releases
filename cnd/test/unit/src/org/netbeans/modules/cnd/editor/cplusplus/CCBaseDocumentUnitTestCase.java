/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.EditorKit;
import org.netbeans.modules.cnd.test.base.BaseDocumentUnitTestCase;

/**
 * base test case for C++ language based document
 * @author Vladimir Voskresensky
 */
public class CCBaseDocumentUnitTestCase 
        extends BaseDocumentUnitTestCase 
{
    private boolean isPlusPlus;
    private EditorKit editorKit;
    
    public CCBaseDocumentUnitTestCase(String testMethodName) {
        super(testMethodName);
        this.isPlusPlus = true;
    }
    
    protected void setLanguage(String lang) {
        if (lang.equals(CCSyntax.IS_CPLUSPLUS)) {
            this.isPlusPlus = true;
        } else if (lang.equals(CSyntax.IS_C)) {
            this.isPlusPlus = false;
        } else {
            fail("Unsupported language " + lang);
            this.isPlusPlus = false;
        }        
    }
    
    /**
     * Create editor kit instance to be returned
     * by {@link #getEditorKit()}.
     * <br>
     * The returned editor kit should return
     * <code>BaseDocument</code> instances
     * from its {@link javax.swing.text.EditorKit.createDefaultDocument()}.
     */    
    protected EditorKit createEditorKit() {
        if (isCPlusPlus()) {
            return new CCKit();
        } else {
            return new CKit();
        }
    }

    protected boolean isCPlusPlus() {
        return this.isPlusPlus;
    }
}
