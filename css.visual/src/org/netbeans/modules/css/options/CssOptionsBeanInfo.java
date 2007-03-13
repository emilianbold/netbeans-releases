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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CssOptionsBeanInfo.java
 *
 * Created on February 9, 2005, 5:02 PM
 */

package org.netbeans.modules.css.options;
import java.awt.Image;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import org.openide.util.Utilities;

/**
 * BeanInfo of Options for the CSS Editor
 * @author Winston Prakash
 * @version 1.0
 */

public class CssOptionsBeanInfo extends BaseOptionsBeanInfo {

    public CssOptionsBeanInfo() {
        super("/org/netbeans/modules/css/resources/CssOptions"); // NOI18N
    }

    protected Class getBeanClass() {
        return CssOptions.class;
    }

    /** @param type Desired type of the icon
     * @return returns the Image loader's icon
     */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("org/netbeans/modules/css/resources/css.gif"); // NOI18N
        } else {
            return Utilities.loadImage("org/netbeans/modules/css/resources/css.gif"); // NOI18N
        }
    }
}
