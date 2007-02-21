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
package org.netbeans.modules.xslt.core;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import java.beans.IntrospectionException;
import org.openide.loaders.DataLoader;
import org.openide.util.Utilities;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XSLTDataLoaderBeanInfo extends SimpleBeanInfo {

    public static final String PATH_TO_IMAGE = 
        "org/netbeans/modules/xslt/core/resources/xslt_file.gif";   // NOI18N

    /** {@inheritDoc} */
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {Introspector.getBeanInfo(DataLoader.class)};
        } catch (IntrospectionException e) {
//            ErrorManager.getDefault().notify(ie);
//            return null;
            throw new AssertionError(e);
        }
    }

    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage(PATH_TO_IMAGE);
        } else {
            return null;
        }

    }
}
