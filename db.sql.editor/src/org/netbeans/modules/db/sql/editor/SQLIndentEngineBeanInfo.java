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

