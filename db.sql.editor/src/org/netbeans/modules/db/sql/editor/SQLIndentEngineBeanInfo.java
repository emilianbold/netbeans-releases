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

package org.netbeans.modules.db.sql.editor;

import java.beans.BeanDescriptor;
import java.util.MissingResourceException;
import org.netbeans.modules.editor.FormatterIndentEngineBeanInfo;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.NbBundle;

/**
 * The BeanInfo descriptor for the SQLIndentEngine
 *
 * @author Jesse Beaumont
 */
public class SQLIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    private BeanDescriptor beanDescriptor;

    /**
     * Get the bean descriptor
     */
    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(getBeanClass());
            beanDescriptor.setDisplayName(getMessage("LBL_SQLIndentEngine"));
            beanDescriptor.setShortDescription(getMessage("HINT_SQLIndentEngine"));
            beanDescriptor.setValue("global", Boolean.TRUE); // NOI18N
        }
        return beanDescriptor;
    }

    /**
     * Get the class of the bean described by this bean info
     */
    protected Class getBeanClass() {
        return SQLIndentEngine.class;
    }

    /**
     * Look up a resource bundle message, if it is not found locally defer to 
     * the super implementation
     */
    protected String getMessage(String key) {
        try {
            return NbBundle.getMessage(SQLIndentEngineBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}

