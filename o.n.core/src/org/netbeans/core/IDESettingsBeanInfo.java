/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Image;
import java.beans.*;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;

import org.openide.util.NbBundle;

import org.netbeans.core.windows.nodes.TabbedContainerUIPropertyEditor;
import org.netbeans.core.windows.nodes.UIModePropertyEditor;

/** A BeanInfo for global IDE settings.
*
* @author Ian Formanek
*/
public class IDESettingsBeanInfo extends SimpleBeanInfo {
    /** Icons for compiler settings objects. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        ResourceBundle bundleUIMode = NbBundle.getBundle(UIModePropertyEditor.class);
        ResourceBundle bundleTabbedContainerUI = NbBundle.getBundle(TabbedContainerUIPropertyEditor.class);
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (IDESettings.PROP_SHOW_TIPS_ON_STARTUP, IDESettings.class,
                                               "getShowTipsOnStartup", "setShowTipsOnStartup"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_LAST_TIP, IDESettings.class,
                                               "getLastTip", "setLastTip"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_CONFIRM_DELETE, IDESettings.class,
                                               "getConfirmDelete", "setConfirmDelete"), // NOI18N
                       new PropertyDescriptor ("loadedBeans", IDESettings.class, // NOI18N
                                               "getLoadedBeans", "setLoadedBeans"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_HOME_PAGE, IDESettings.class,
                                               "getHomePage", "setHomePage"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_USE_PROXY, IDESettings.class,
                                               "getUseProxy", "setUseProxy"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_PROXY_HOST, IDESettings.class,
                                               "getProxyHost", "setProxyHost"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_PROXY_PORT, IDESettings.class,
                                               "getProxyPort", "setProxyPort"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_FILE_EXTENSIONS, IDESettings.class,
                                               "getShowFileExtensions", "setShowFileExtensions"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_MODULES_SORT_MODE, IDESettings.class,
                                               "getModulesSortMode", "setModulesSortMode"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_WWWBROWSER, IDESettings.class, 
                                               "getWWWBrowser", "setWWWBrowser"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_UIMODE, IDESettings.class, 
                                               "getUIMode", "setUIMode"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_TABBEDCONTAINERUI, IDESettings.class, 
                                               "getTabbedContainerUI", "setTabbedContainerUI") // NOI18N
                   };

            desc[0].setDisplayName (Main.getString ("PROP_SHOW_TIPS_ON_STARTUP"));
            desc[0].setShortDescription (Main.getString ("HINT_SHOW_TIPS_ON_STARTUP"));

            desc[1].setHidden (true);

            desc[2].setDisplayName (Main.getString ("PROP_CONFIRM_DELETE"));
            desc[2].setShortDescription (Main.getString ("HINT_CONFIRM_DELETE"));

            desc[3].setHidden(true);

            desc[4].setDisplayName (Main.getString ("PROP_HOME_PAGE"));
            desc[4].setShortDescription (Main.getString ("HINT_HOME_PAGE"));

            desc[5].setDisplayName (Main.getString ("PROP_USE_PROXY"));
            desc[5].setShortDescription (Main.getString ("HINT_USE_PROXY"));

            desc[6].setDisplayName (Main.getString ("PROP_PROXY_HOST"));
            desc[6].setShortDescription (Main.getString ("HINT_PROXY_HOST"));

            desc[7].setDisplayName (Main.getString ("PROP_PROXY_PORT"));
            desc[7].setShortDescription (Main.getString ("HINT_PROXY_PORT"));

            desc[8].setDisplayName (Main.getString ("PROP_SHOW_FILE_EXTENSIONS"));
            desc[8].setShortDescription (Main.getString ("HINT_SHOW_FILE_EXTENSIONS"));

            desc[9].setHidden (true);

            desc[10].setDisplayName (Main.getString ("PROP_WWW_BROWSER"));
            desc[10].setShortDescription (Main.getString ("HINT_WWW_BROWSER"));

            desc[11].setDisplayName(bundleUIMode.getString("PROP_UI_Mode"));
            desc[11].setShortDescription(bundleUIMode.getString("HINT_UI_Mode"));
            desc[11].setPropertyEditorClass(UIModePropertyEditor.class);

            desc[12].setDisplayName(bundleTabbedContainerUI.getString("PROP_TabbedContainerUI"));
            desc[12].setShortDescription(bundleTabbedContainerUI.getString("HINT_TabbedContainerUI"));
            desc[12].setPropertyEditorClass(TabbedContainerUIPropertyEditor.class);

        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
    }

    /** Provides an explicit property info. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return desc;
    }

    /** Returns the IDESettings' icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/ideSettings.gif"); // NOI18N
            icon32 = loadImage ("/org/netbeans/core/resources/ideSettings32.gif"); // NOI18N
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

}
