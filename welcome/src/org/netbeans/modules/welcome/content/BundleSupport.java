/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.openide.util.NbBundle;

public class BundleSupport {

    private static final String BUNDLE_NAME = "org.netbeans.modules.welcome.resources.Bundle"; // NOI18N
    
    private static final String LABEL_PREFIX = "LBL_"; // NOI18N
    private static final String URL_PREFIX = "URL_"; // NOI18N
    private static final String CATEGORY_PREFIX = "CATEGORY_"; // NOI18N
    private static final String TEMPLATE_PREFIX = "TEMPLATE_"; // NOI18N
    private static final String ACN_PREFIX = "ACN_"; // NOI18N
    private static final String ACD_PREFIX = "ACD_"; // NOI18N
    private static final String MNM_PREFIX = "MNM_"; // NOI18N
    
    private static ResourceBundle resources = NbBundle.getBundle(BUNDLE_NAME);
    
    public static String getLabel(String bundleKey) {
        return resources.getString(LABEL_PREFIX + bundleKey);
    }
    
    public static String getURL(String bundleKey) {
        return resources.getString(URL_PREFIX + bundleKey);
    }
    
    public static char getMnemonic(String bundleKey) {
        return resources.getString(MNM_PREFIX + bundleKey).charAt(0);
    }
    
    public static String getSampleCategory(String bundleKey) {
        return resources.getString(CATEGORY_PREFIX + bundleKey);
    }

    public static String getSampleTemplate(String bundleKey) {
        return resources.getString(TEMPLATE_PREFIX + bundleKey);
    }

    public static String getAccessibilityName(String bundleKey) {
        return resources.getString(ACN_PREFIX + bundleKey);
    }
    
    public static void setAccessibilityProperties(JComponent component, String bundleKey) {
        String aName = resources.getString(ACN_PREFIX + bundleKey);  
        String aDescr = resources.getString(ACD_PREFIX + bundleKey);  
      
        component.getAccessibleContext().setAccessibleName(aName);
        component.getAccessibleContext().setAccessibleDescription(aDescr);
    }
}
