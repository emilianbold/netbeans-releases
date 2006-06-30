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

import java.beans.BeanDescriptor;
import java.util.MissingResourceException;
import org.netbeans.modules.editor.FormatterIndentEngineBeanInfo;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.NbBundle;

/**
* Beaninfo for JavaIndentEngine.
*
* @author Miloslav Metelka
*/

public class JavaIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    private BeanDescriptor beanDescriptor;

    public JavaIndentEngineBeanInfo() {
    }

    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(getBeanClass());
            beanDescriptor.setDisplayName(getString("LAB_JavaIndentEngine"));
            beanDescriptor.setShortDescription(getString("HINT_JavaIndentEngine"));
            beanDescriptor.setValue("global", Boolean.TRUE); // NOI18N
        }
        return beanDescriptor;
    }

    protected Class getBeanClass() {
        return JavaIndentEngine.class;
    }

    protected String[] createPropertyNames() {
        return NbEditorUtilities.mergeStringArrays(super.createPropertyNames(),
            new String[] {
                JavaIndentEngine.JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
                JavaIndentEngine.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
                JavaIndentEngine.JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
                JavaIndentEngine.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP
            }
        );
    }

    protected String getString(String key) {
        try {
            return NbBundle.getBundle(JavaIndentEngineBeanInfo.class).getString(key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}

