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

package org.netbeans.modules.welcome.content;

import java.text.MessageFormat;
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
    
    public static String getLabel(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(LABEL_PREFIX + bundleKey);
    }
    
    public static String getURL(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(URL_PREFIX + bundleKey);
    }
    
    public static char getMnemonic(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(MNM_PREFIX + bundleKey).charAt(0);
    }
    
    public static String getSampleCategory(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(CATEGORY_PREFIX + bundleKey);
    }

    public static String getSampleTemplate(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(TEMPLATE_PREFIX + bundleKey);
    }

    public static String getAccessibilityName(String bundleKey) {
        return NbBundle.getBundle(BUNDLE_NAME).getString(ACN_PREFIX + bundleKey);
    }
    
    public static String getAccessibilityName(String bundleKey, String param) {
        return MessageFormat.format( NbBundle.getBundle(BUNDLE_NAME).getString(ACN_PREFIX + bundleKey), param );
    }
    
    public static String getAccessibilityDescription(String bundleKey, String param) {
        return MessageFormat.format( NbBundle.getBundle(BUNDLE_NAME).getString(ACD_PREFIX + bundleKey), param );
    }
    
    public static void setAccessibilityProperties(JComponent component, String bundleKey) {
        String aName = NbBundle.getBundle(BUNDLE_NAME).getString(ACN_PREFIX + bundleKey);  
        String aDescr = NbBundle.getBundle(BUNDLE_NAME).getString(ACD_PREFIX + bundleKey);  
      
        component.getAccessibleContext().setAccessibleName(aName);
        component.getAccessibleContext().setAccessibleDescription(aDescr);
    }
}
