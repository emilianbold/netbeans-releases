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

package org.netbeans.modules.j2me.cdc.platform;

import org.netbeans.modules.j2me.cdc.platform.platformdefinition.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.awt.*;

public class CDCPlatformBeanInfo extends SimpleBeanInfo {

    public CDCPlatformBeanInfo () {
    }


    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] descs =  new PropertyDescriptor[] {
                new PropertyDescriptor (CDCPlatform.PROP_DISPLAY_NAME, CDCPlatform.class),
                new PropertyDescriptor (CDCPlatform.PROP_ANT_NAME, CDCPlatform.class),
                new PropertyDescriptor (CDCPlatform.PROP_SOURCE_FOLDER, CDCPlatform.class),
                new PropertyDescriptor (CDCPlatform.PROP_JAVADOC_FOLDER, CDCPlatform.class),
            };
            descs[0].setDisplayName(NbBundle.getMessage(CDCPlatformBeanInfo.class,"TXT_Name"));
            descs[0].setBound(true);
            descs[1].setDisplayName(NbBundle.getMessage(CDCPlatformBeanInfo.class,"TXT_AntName"));
            descs[1].setWriteMethod(null);
            descs[2].setDisplayName(NbBundle.getMessage(CDCPlatformBeanInfo.class,"TXT_SourcesFolder"));
            descs[2].setPropertyEditorClass(FileObjectPropertyEditor.class);
            descs[2].setBound(true);
            descs[3].setDisplayName(NbBundle.getMessage(CDCPlatformBeanInfo.class,"TXT_JavaDocFolder"));
            descs[3].setPropertyEditorClass(FileObjectPropertyEditor.class);
            descs[3].setBound(true);
            return descs;
        } catch (IntrospectionException ie) {
            return new PropertyDescriptor[0];
        }
    }


    public Image getIcon(int iconKind) {
        if ((iconKind == BeanInfo.ICON_COLOR_16x16) || (iconKind == BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("org/netbeans/modules/j2me/cdc/platform/resources/cdcPlatform.png"); // NOI18N
        return null;
    }
}
