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

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class I18nOptionsTest extends BasicTestForImport {
    public I18nOptionsTest(String testName) {
        super(testName, "org-netbeans-modules-i18n-I18nOptions.settings");
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/i18n");
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "replaceResourceValue",
            "regularExpression",
            "initJavaCode",
            "replaceJavaCode",
            "advancedWizard",
            "lastResource2",
            "i18nRegularExpression"
        });
    }
    
    
    public void testReplaceResourceValue() throws Exception {
        assertPropertyType("replaceResourceValue", "java.lang.Boolean");
        assertProperty("replaceResourceValue","false");
    }
    public void testRegularExpression() throws Exception {
        assertPropertyType("regularExpression", "java.lang.String");
        assertProperty("regularExpression","(getString|getBundle)[:space:]*\\([:space:]*{hardString}|// NOI18N");
    }
    public void testInitJavaCode() throws Exception {
        assertPropertyType("initJavaCode", "java.lang.String");
        assertProperty("initJavaCode","java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\")");
    }
    public void testReplaceJavaCode() throws Exception {
        assertPropertyType("replaceJavaCode", "java.lang.String");
        assertProperty("replaceJavaCode","java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\")");
    }
    public void testAdvancedWizard() throws Exception {
        assertPropertyType("advancedWizard", "java.lang.Boolean");
        assertProperty("advancedWizard","true");
    }
    public void testLastResource2() throws Exception {
        assertProperty("lastResource2","home.local/rmatous/module2/src/org/yourorghere/module2/Bundle.properties");
    }
    public void testI18nRegularExpression() throws Exception {
        assertPropertyType("i18nRegularExpression", "java.lang.String");
        assertProperty("i18nRegularExpression","getString[:space:]*\\([:space:]*{hardString}");
    }
    
}
