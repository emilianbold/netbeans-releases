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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BasePrintOptionsBeanInfo extends SimpleBeanInfo {

    private ResourceBundle bundle;

    /** Prefix of the icon location. */
    private String iconPrefix;

    /** Icons for compiler settings objects. */
    private Image icon;
    private Image icon32;

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;

    public BasePrintOptionsBeanInfo() {
        this("/org/netbeans/modules/editor/resources/baseOptions"); // NOI18N
    }

    public BasePrintOptionsBeanInfo(String iconPrefix) {
        this.iconPrefix = iconPrefix;
    }

    /*
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null) {
            String[] propNames = getPropNames();
            try {
                descriptors = new PropertyDescriptor[propNames.length];

                for (int i = 0; i < propNames.length; i++) {
                    descriptors[i] = new PropertyDescriptor(propNames[i], getBeanClass());
                    descriptors[i].setDisplayName(getString("PROP_" + propNames[i])); // NOI18N
                    descriptors[i].setShortDescription(getString("HINT_" + propNames[i])); // NOI18N
                }

                getPD(BasePrintOptions.PRINT_COLORING_MAP_PROP).setPropertyEditorClass(ColoringArrayEditor.class);

            } catch (IntrospectionException e) {
                descriptors = new PropertyDescriptor[0];
            }
        }
        return descriptors;
    }

    protected String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(BasePrintOptionsBeanInfo.class);
        }
        return bundle.getString(s);
    }

    protected Class getBeanClass() {
        return BasePrintOptions.class;
    }

    protected String[] getPropNames() {
        return BasePrintOptions.BASE_PROP_NAMES;
    }

    protected PropertyDescriptor getPD(String prop) {
        String[] propNames = getPropNames();
        for (int i = 0; i < descriptors.length; i++) {
            if (prop.equals(propNames[i])) {
                return descriptors[i];
            }
        }
        return null;
    }

    /* @param type Desired type of the icon
    * @return returns the Java loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage(iconPrefix + ".gif"); // NOI18N
            return icon;
        }
        else {
            if (icon32 == null)
                icon32 = loadImage(iconPrefix + "32.gif"); // NOI18N
            return icon32;
        }
    }
}
