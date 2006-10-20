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

public class JavaSettingsTest extends BasicTestForImport {

    public JavaSettingsTest(String name) {
        super(name, "javaSettings.settings");
    }

    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/java/project");
    }

    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "showAgainBrokenRefAlert",
        });
    }

    public void testShowAgainBrokenRefAlert() throws Exception {
        assertPropertyType("showAgainBrokenRefAlert", "java.lang.Boolean");
        assertProperty("showAgainBrokenRefAlert", "false");
    }

}
