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

package org.netbeans.core;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** A BeanInfo for global IDE settings.
 *
 * @author Ian Formanek
 */
public class IDESettingsBeanInfo extends SimpleBeanInfo {

    /** Provides an explicit property info. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
    	    ResourceBundle bundleUIMode = NbBundle.getBundle(UIModePropertyEditor.class);

            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (IDESettings.PROP_CONFIRM_DELETE, IDESettings.class,
                                               "getConfirmDelete", "setConfirmDelete"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_HOME_PAGE, IDESettings.class,
                                               "getHomePage", "setHomePage"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_FILE_EXTENSIONS, IDESettings.class,
                                               "getShowFileExtensions", "setShowFileExtensions"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_UIMODE, IDESettings.class, 
                                               "getUIMode", "setUIMode"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_TOOLTIPS_IN_IDE, IDESettings.class,
                                               "getShowToolTipsInIDE", "setShowToolTipsInIDE"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_IGNORED_FILES, IDESettings.class,
                                               "getIgnoredFiles", "setIgnoredFiles"), // NOI18N
                   };

            desc[0].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_CONFIRM_DELETE"));
            desc[0].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_CONFIRM_DELETE"));

            desc[1].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_HOME_PAGE"));
            desc[1].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_HOME_PAGE"));
            
            desc[2].setHidden (true);

            desc[3].setDisplayName(bundleUIMode.getString("PROP_UI_Mode"));
            desc[3].setShortDescription(bundleUIMode.getString("HINT_UI_Mode"));
            desc[3].setPropertyEditorClass(UIModePropertyEditor.class);

            desc[4].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_SHOW_TOOLTIPS_IN_IDE"));
            desc[4].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_SHOW_TOOLTIPS_IN_IDE"));
            
            desc[5].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_ignoredFiles"));
            desc[5].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class,"HINT_ignoredFiles"));            
            
            return desc;
        } catch (IntrospectionException ex) {
	    Exceptions.printStackTrace(ex);
	    return null;
        }

    }

    /** Returns the IDESettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
	    return Utilities.loadImage("org/netbeans/core/resources/ideSettings.gif"); // NOI18N
        else
            return Utilities.loadImage ("org/netbeans/core/resources/ideSettings32.gif"); // NOI18N
    }

}
