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

package org.netbeans.modules.mobility.cldcplatform;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.awt.*;
import org.netbeans.modules.mobility.cldcplatform.editors.FileObjectPropertyEditor;

public class J2MEPlatformBeanInfo extends SimpleBeanInfo {
    
    private static final String PROP_DISPLAY_NAME="displayName";                //NOI18N
    private static final String PROP_SOURCES_FODLER ="sourceFolder";            //NOI18N
    private static final String PROP_JAVADOC_FOLDER = "javadocFolder";          //NOI18N
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyDescriptor[] descs =  new PropertyDescriptor[] {
                new PropertyDescriptor(PROP_DISPLAY_NAME, J2MEPlatform.class),
                new PropertyDescriptor(PROP_SOURCES_FODLER, J2MEPlatform.class, "getSourceFolders", null), //NOI18N
                new PropertyDescriptor(PROP_JAVADOC_FOLDER, J2MEPlatform.class),
            };
            descs[0].setDisplayName(NbBundle.getMessage(J2MEPlatformBeanInfo.class,"TXT_Name")); //NOI18N
            descs[1].setDisplayName(NbBundle.getMessage(J2MEPlatformBeanInfo.class,"TXT_SourcesFolder")); //NOI18N
            descs[1].setPropertyEditorClass(FileObjectPropertyEditor.class);
            descs[2].setDisplayName(NbBundle.getMessage(J2MEPlatformBeanInfo.class,"TXT_JavaDocFolder")); //NOI18N
            descs[2].setPropertyEditorClass(FileObjectPropertyEditor.class);
            return descs;
        } catch (IntrospectionException ie) {
            return new PropertyDescriptor[0];
        }
    }
    
    
    public Image getIcon(final int iconKind) {
        if ((iconKind == BeanInfo.ICON_COLOR_16x16) || (iconKind == BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("org/netbeans/modules/mobility/cldcplatform/resources/platform.gif"); // NOI18N
        } 
        return null;
    }
    
}
