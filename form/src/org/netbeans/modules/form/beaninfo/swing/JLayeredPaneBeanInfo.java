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
