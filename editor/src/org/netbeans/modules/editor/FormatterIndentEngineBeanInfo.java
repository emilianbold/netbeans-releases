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

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** BeanInfo for the FormatterIndentEngine class
*
* @author Miloslav Metelka
*/
public abstract class FormatterIndentEngineBeanInfo extends SimpleBeanInfo {

    /** Prefix of the icon location. */
    private String iconPrefix;

    /** Icons for compiler settings objects. */
    private Image icon;
    private Image icon32;

    private PropertyDescriptor[] propertyDescriptors;

    private String[] propertyNames;

    public FormatterIndentEngineBeanInfo(String iconPrefix) {
        this.iconPrefix = iconPrefix;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            propertyDescriptors = createPropertyDescriptors();
        }
        return propertyDescriptors;
    }

    protected PropertyDescriptor[] createPropertyDescriptors() {
        String[] propNames = getPropertyNames();
        PropertyDescriptor[] pd;
        try {
            pd = new PropertyDescriptor[propNames.length];

            for (int i = 0; i < propNames.length; i++) {
                pd[i] = new PropertyDescriptor(propNames[i], getBeanClass());

                pd[i].setDisplayName(getString(
                        "PROP_indentEngine_" + propNames[i])); // NOI18N

                pd[i].setShortDescription(getString(
                        "HINT_indentEngine_" + propNames[i])); // NOI18N
            }

        } catch (IntrospectionException e) {
            e.printStackTrace();
            pd = new PropertyDescriptor[0];
        }

        return pd;
    }

    protected abstract Class getBeanClass();

    protected String[] getPropertyNames() {
        if (propertyNames == null) {
            propertyNames = createPropertyNames();
        }
        return propertyNames;
    }

    protected String[] createPropertyNames() {
        return new String[0];
    }

    protected PropertyDescriptor getPropertyDescriptor(String propertyName) {
        String[] propNames = getPropertyNames();
        for (int i = 0; i < propNames.length; i++) {
            if (propertyName.equals(propNames[i])) {
                return getPropertyDescriptors()[i];
            }
        }
        return null;
    }

    protected void setPropertyEditor(String propertyName, Class propertyEditor) {
        PropertyDescriptor pd = getPropertyDescriptor(propertyName);
        if (pd != null) {
            pd.setPropertyEditorClass(propertyEditor);
        }
    }

    protected void setExpert(String[] expertPropertyNames) {
        for (int i = 0; i < expertPropertyNames.length; i++) {
            PropertyDescriptor pd = getPropertyDescriptor(expertPropertyNames[i]);
            if (pd != null) {
                pd.setExpert(true);
            }
        }
    }

    protected void setHidden(String[] hiddenPropertyNames) {
        for (int i = 0; i < hiddenPropertyNames.length; i++) {
            PropertyDescriptor pd = getPropertyDescriptor(hiddenPropertyNames[i]);
            if (pd != null) {
                pd.setHidden(true);
            }
        }
    }

    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage(iconPrefix + ".gif"); // NOI18N
            return icon;

        } else {
            if (icon32 == null)
                icon32 = loadImage(iconPrefix + "32.gif"); // NOI18N
            return icon32;
        }
    }

    /** 
     * Get the localized string. This method must be overriden
     * in children if they add new properties or other stuff
     * that needs to be localized.
     * @param key key to find in a bundle
     * @return localized string
     */
    protected String getString(String key) {
        return key;
    }

}

