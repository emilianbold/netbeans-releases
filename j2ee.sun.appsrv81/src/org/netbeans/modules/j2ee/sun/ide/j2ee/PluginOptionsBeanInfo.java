/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
        PropertyDescriptor proot = createPropertyDescriptor("installRoot", "LBL_InstallRoot", "DSC_InstallRoot");//NOI18N
        proot.setValue("files", Boolean.FALSE); // only directories...
        proot.setValue("changeImmediate", Boolean.FALSE); // change on ok only...

        PropertyDescriptor[] retValue = new PropertyDescriptor[] {
            // TODO: from bundle
            createPropertyDescriptor("userList", "LBL_UserList", "DSC_UserList"), //NOI18N
            createPropertyDescriptor("groupList", "LBL_GroupList", "DSC_GroupList"),//NOI18N
            createPropertyDescriptor("charsetDisplayPreference", "LBL_CharsetDispPref", "DSC_CharsetDispPref", CharsetDisplayPreferenceEditor.class),//NOI18N
            createPropertyDescriptor("logLevel", "LBL_PluginLogLevel", "DSC_PluginLogLevel", LoggingLevelEditor.class),//NOI18N
            createPropertyDescriptor("incrementalDeploy", "LBL_INCREMENTAL", "DSC_INCREMENTAL"),//NOI18N
            proot
            
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
