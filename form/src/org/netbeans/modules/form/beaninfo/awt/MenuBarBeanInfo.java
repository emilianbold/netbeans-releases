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

package org.netbeans.modules.form.beaninfo.awt;

import java.awt.Image;
import java.beans.*;
import org.openide.util.Utilities;

/** A BeanInfo for java.awt.MenuBar.
*
* @author Ales Novak
*/
public class MenuBarBeanInfo extends MenuComponentBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(java.awt.MenuBar.class);
    }

    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/form/beaninfo/awt/menubar.gif"); // NOI18N
    }

}
