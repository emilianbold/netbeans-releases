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

package org.netbeans.modules.editor.html;

import java.beans.BeanDescriptor;
import java.util.MissingResourceException;
import org.netbeans.modules.editor.FormatterIndentEngineBeanInfo;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.NbBundle;

/**
* Beaninfo for LineWrapIndentEngine.
*
* @author Petr Nejedly
*/

public class LineWrapIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    private BeanDescriptor beanDescriptor;

    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(getBeanClass());
            beanDescriptor.setDisplayName(getString("LAB_LineWrapIndentEngine"));
            beanDescriptor.setShortDescription(getString("HINT_LineWrapIndentEngine"));
            beanDescriptor.setValue("global", Boolean.TRUE); // NOI18N
        }
        return beanDescriptor;
    }

    protected Class getBeanClass() {
        return LineWrapIndentEngine.class;
    }

    protected String getString(String key) {
        try {
            return NbBundle.getMessage(LineWrapIndentEngineBeanInfo.class,key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}

