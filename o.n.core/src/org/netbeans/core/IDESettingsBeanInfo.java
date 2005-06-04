/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.openide.ErrorManager;

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
                       new PropertyDescriptor (IDESettings.PROP_USE_PROXY, IDESettings.class,
                                               "getUseProxy", "setUseProxy"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_PROXY_TYPE, IDESettings.class,
                                               "getProxyType", "setProxyType"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_PROXY_HOST, IDESettings.class,
                                               "getUserProxyHost", "setUserProxyHost"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_PROXY_PORT, IDESettings.class,
                                               "getUserProxyPort", "setUserProxyPort"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_FILE_EXTENSIONS, IDESettings.class,
                                               "getShowFileExtensions", "setShowFileExtensions"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_MODULES_SORT_MODE, IDESettings.class,
                                               "getModulesSortMode", "setModulesSortMode"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_WWWBROWSER, IDESettings.class, 
                                               "getWWWBrowser", "setWWWBrowser"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_UIMODE, IDESettings.class, 
                                               "getUIMode", "setUIMode"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_TOOLTIPS_IN_IDE, IDESettings.class,
                                               "getShowToolTipsInIDE", "setShowToolTipsInIDE"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_IGNORED_FILES, IDESettings.class,
                                               "getIgnoredFiles", "setIgnoredFiles"), // NOI18N
                       // needs to add fake properties to import proxy settings from older IDE
                       new PropertyDescriptor ("proxyHost", IDESettings.class,
                                               null, "readOldProxyHost"), // NOI18N
                       new PropertyDescriptor ("proxyPort", IDESettings.class,
                                               null, "readOldProxyPort"), // NOI18N
                   };

            desc[0].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_CONFIRM_DELETE"));
            desc[0].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_CONFIRM_DELETE"));

            desc[1].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_HOME_PAGE"));
            desc[1].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_HOME_PAGE"));
            
            desc[2].setHidden (true);

            desc[3].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_PROXY_TYPE"));
            desc[3].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_PROXY_TYPE"));
            desc[3].setPropertyEditorClass (ProxyTypePropertyEditor.class);

            desc[4].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_PROXY_HOST"));
            desc[4].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_PROXY_HOST"));

            desc[5].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_PROXY_PORT"));
            desc[5].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_PROXY_PORT"));

            desc[6].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_SHOW_FILE_EXTENSIONS"));
            desc[6].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_SHOW_FILE_EXTENSIONS"));

            desc[7].setHidden (true);

            desc[8].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_WWW_BROWSER"));
            desc[8].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_WWW_BROWSER"));

            desc[9].setDisplayName(bundleUIMode.getString("PROP_UI_Mode"));
            desc[9].setShortDescription(bundleUIMode.getString("HINT_UI_Mode"));
            desc[9].setPropertyEditorClass(UIModePropertyEditor.class);

            desc[10].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_SHOW_TOOLTIPS_IN_IDE"));
            desc[10].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_SHOW_TOOLTIPS_IN_IDE"));
            
            desc[11].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_ignoredFiles"));
            desc[11].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class,"HINT_ignoredFiles"));            
            
            desc[12].setHidden (true);
            desc[13].setHidden (true);
            
            return desc;
        } catch (IntrospectionException ex) {
	    ErrorManager.getDefault().notify(ex);
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
