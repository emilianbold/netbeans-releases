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

package org.netbeans.modules.html.editor.options;

import java.beans.*;
import java.awt.Image;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class HTMLOptionsBeanInfo extends BaseOptionsBeanInfo {

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;
    /** Additional beaninfo */
    private static BeanInfo[] additional;

    private static final String[] EXPERT_PROP_NAMES = new String[] {
        HTMLOptions.COMPLETION_INSTANT_SUBSTITUTION_PROP,
        HTMLOptions.COMPLETION_LOWER_CASE_PROP
    };

    public HTMLOptionsBeanInfo() {
        super("/org/netbeans/modules/editor/resources/htmlOptions"); // NOI18N
    }

    protected String[] getPropNames() {
        return HTMLOptions.HTML_PROP_NAMES;
    }

    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();
        setExpert(EXPERT_PROP_NAMES);
    }    

    protected Class getBeanClass() {
        return HTMLOptions.class;
    }

    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(HTMLOptionsBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
