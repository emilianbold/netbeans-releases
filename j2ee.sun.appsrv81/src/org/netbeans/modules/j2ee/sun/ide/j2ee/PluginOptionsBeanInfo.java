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
/*
 * PluginOptionsBeanInfo.java
 *
 * Created on December 13, 2004, 3:17 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.editors.CharsetDisplayPreferenceEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.LoggingLevelEditor;

/**
 *
 * @author Andrei Badea
 */
public class PluginOptionsBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
//        PropertyDescriptor proot = createPropertyDescriptor("installRoot", "LBL_InstallRoot", "DSC_InstallRoot");//NOI18N
//        proot.setValue("files", Boolean.FALSE); // only directories...
//        proot.setValue("changeImmediate", Boolean.FALSE); // change on ok only...

        PropertyDescriptor[] retValue = new PropertyDescriptor[] {
            // TODO: from bundle
            createPropertyDescriptor("userList", "LBL_UserList", "DSC_UserList"), //NOI18N
            createPropertyDescriptor("groupList", "LBL_GroupList", "DSC_GroupList"),//NOI18N
            createPropertyDescriptor("charsetDisplayPreference", "LBL_CharsetDispPref", "DSC_CharsetDispPref", CharsetDisplayPreferenceEditor.class),//NOI18N
            createPropertyDescriptor("logLevel", "LBL_PluginLogLevel", "DSC_PluginLogLevel", LoggingLevelEditor.class),//NOI18N
            createPropertyDescriptor("incrementalDeploy", "LBL_INCREMENTAL", "DSC_INCREMENTAL")//NOI18N
           // proot
            
        };
        return retValue;
    }
    
    private PropertyDescriptor createPropertyDescriptor(String name, String displayName, String shortDescription) {
        return createPropertyDescriptor(name, displayName, shortDescription, null);
    }
    
    private PropertyDescriptor createPropertyDescriptor(String name, String displayName, String shortDescription, Class editor) {
        try {
            PropertyDescriptor result = new PropertyDescriptor(name, PluginOptions.class);
            result.setDisplayName(NbBundle.getMessage(PluginOptionsBeanInfo.class, displayName));
            result.setShortDescription(NbBundle.getMessage(PluginOptionsBeanInfo.class, shortDescription));
            if (editor != null)
                result.setPropertyEditorClass(editor);
            return result;
        }
        catch (IntrospectionException e) {
            return null;
        }
    }
}
