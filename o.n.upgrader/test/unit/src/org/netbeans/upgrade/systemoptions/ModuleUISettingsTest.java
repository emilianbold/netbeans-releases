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

public class ModuleUISettingsTest extends BasicTestForImport {

    public ModuleUISettingsTest(String name) {
        super(name, "org-netbeans-modules-apisupport-project-ui-ModuleUI.settings");
    }

    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/apisupport/project");
    }

    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "lastChosenLibraryLocation",
            "lastUsedNbPlatformLocation",
            "newModuleCounter",
            "newSuiteCounter",
            "confirmReloadInIDE",
            "lastUsedPlatformID",
            "harnessesUpgraded",
        });
    }

    public void testPropertyValues() throws Exception {
        assertPropertyTypeAndValue("lastChosenLibraryLocation", "java.lang.String", "/home/jglick");
        assertPropertyTypeAndValue("lastUsedNbPlatformLocation", "java.lang.String", "/home/jglick");
        assertPropertyTypeAndValue("newModuleCounter", "java.lang.Integer", "0");
        assertPropertyTypeAndValue("newSuiteCounter", "java.lang.Integer", "1");
        assertPropertyTypeAndValue("confirmReloadInIDE", "java.lang.Boolean", "true");
        assertPropertyTypeAndValue("lastUsedPlatformID", "java.lang.String", "default");
        assertPropertyTypeAndValue("harnessesUpgraded", "java.lang.Boolean", "true");
    }

}
