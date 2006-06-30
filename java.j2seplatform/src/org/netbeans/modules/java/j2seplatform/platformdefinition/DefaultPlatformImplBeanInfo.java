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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.awt.*;

public class DefaultPlatformImplBeanInfo extends SimpleBeanInfo {

    public DefaultPlatformImplBeanInfo () {
    }

    public Image getIcon(int iconKind) {
        if ((iconKind == BeanInfo.ICON_COLOR_16x16) || (iconKind == BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("org/netbeans/modules/java/j2seplatform/resources/platform.gif"); // NOI18N
        } else {
            return null;
        }
    }


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                new PropertyDescriptor (DefaultPlatformImpl.PROP_DISPLAY_NAME, DefaultPlatformImpl.class),
                new PropertyDescriptor (DefaultPlatformImpl.PROP_SOURCE_FOLDER, DefaultPlatformImpl.class),                
                new PropertyDescriptor (DefaultPlatformImpl.PROP_JAVADOC_FOLDER, DefaultPlatformImpl.class),
            };
            pds[0].setDisplayName(NbBundle.getMessage(DefaultPlatformImplBeanInfo.class,"TXT_Name"));
            pds[0].setBound(true);
            pds[1].setDisplayName(NbBundle.getMessage(DefaultPlatformImplBeanInfo.class,"TXT_SourcesFolder"));
            pds[1].setPropertyEditorClass(FileObjectPropertyEditor.class);
            pds[1].setBound(true);
            pds[2].setDisplayName(NbBundle.getMessage(DefaultPlatformImplBeanInfo.class,"TXT_JavaDocFolder"));
            pds[2].setPropertyEditorClass(FileObjectPropertyEditor.class);
            pds[2].setBound(true);
            return pds;
        } catch (IntrospectionException ie) {
            return new PropertyDescriptor[0];
        }
    }

}
