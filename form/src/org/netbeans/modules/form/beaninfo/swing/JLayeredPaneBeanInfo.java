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

package org.netbeans.modules.form.beaninfo.swing;

import java.beans.*;

/** BeanInfo for JLayeredPane - defines only the icons for now.
*
* @author  Ian Formanek
*/

public class JLayeredPaneBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(javax.swing.JLayeredPane.class);
    }

    public java.awt.Image getIcon(int type) {
        if (type == ICON_COLOR_32x32 || type == ICON_MONO_32x32)
            return org.openide.util.Utilities.loadImage(
                "javax/swing/beaninfo/images/JLayeredPaneColor32.gif"); // NOI18N
        else
            return org.openide.util.Utilities.loadImage(
                "javax/swing/beaninfo/images/JLayeredPaneColor16.gif"); // NOI18N
    }
}
