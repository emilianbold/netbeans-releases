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
 * CssFileLoaderBeanInfo.java
 *
 * Created on December 8, 2004, 10:06 PM
 */

package org.netbeans.modules.css.loader;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import org.openide.loaders.UniFileLoader;
import org.openide.util.Utilities;

/**
 * CSS Data loader bean info
 * @author Winston Prakash
 * @version 1.0
 */
public class CssFileLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
	    throw new AssertionError(ie);
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon(final int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
	    return Utilities.loadImage("org/netbeans/modules/css/resources/css.gif"); // NOI18N
        } else {
	    return null;
        }
    }

}
