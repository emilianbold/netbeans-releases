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

package org.netbeans.modules.editor;

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

public class SimpleIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    private BeanDescriptor beanDescriptor;

    public SimpleIndentEngineBeanInfo() {
        super("/org/netbeans/modules/editor/resources/simpleIndentEngine");
    }

    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(getBeanClass());
            beanDescriptor.setDisplayName(getString("LAB_SimpleIndentEngine"));
            beanDescriptor.setShortDescription(getString("HINT_SimpleIndentEngine"));
        }
        return beanDescriptor;
    }

    protected Class getBeanClass() {
        return SimpleIndentEngine.class;
    }

    protected String getString(String key) {
        try {
            return NbBundle.getBundle(SimpleIndentEngineBeanInfo.class).getString(key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}

