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

package org.netbeans.modules.editor.options;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

/**
* BeanInfo for Coloring class.
*
* @author Ales Novak
*/

public class ColoringBeanBeanInfo extends SimpleBeanInfo {
    /** Prefix of the icon location. */
    private String iconPrefix = "/org/netbeans/editor/resources/coloring"; // NOI18N

    /** Icons for compiler settings objects. */
    private Image icon;
    private Image icon32;

    /*
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] descriptors;
        try {
            descriptors = new PropertyDescriptor[] {
                              new PropertyDescriptor("coloring", ColoringBean.class) // NOI18N
                          };
            descriptors[0].setPropertyEditorClass(ColoringEditor.class);
        } catch (Exception e) {
            descriptors = new PropertyDescriptor[0];
        }
        return descriptors;
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
