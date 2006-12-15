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

package org.netbeans.modules.web.core.syntax.formatting;

import java.beans.BeanDescriptor;
import java.util.MissingResourceException;
import org.netbeans.modules.editor.FormatterIndentEngineBeanInfo;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.openide.util.NbBundle;

/**
 * Bean info for indent engine for jsp and tag files.
 * @author Petr Pisl
 */
public class JspIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(getBeanClass());
            beanDescriptor.setDisplayName(getString("LAB_JspIndentEngine"));
            beanDescriptor.setShortDescription(getString("HINT_JspIndentEngine"));
            beanDescriptor.setValue("global", Boolean.TRUE); // NOI18N
        }
        return beanDescriptor;
    }

    protected Class getBeanClass() {
        return JspIndentEngine.class;
    }

    protected String[] createPropertyNames() {
        return NbEditorUtilities.mergeStringArrays(super.createPropertyNames(),
            new String[] {
                JspIndentEngine.JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
                JspIndentEngine.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
                JspIndentEngine.JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
                JspIndentEngine.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP
            }
        );
    }

    protected String getString(String key) {
        try {
            return NbBundle.getMessage(JSPKit.class,key);
        } catch (MissingResourceException e) {
            try {
                return NbBundle.getMessage(JavaIndentEngine.class,key);
            }catch(MissingResourceException mre) {
                return super.getString(key);
            }
        }
    }
    
}
