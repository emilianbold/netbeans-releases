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
 * @author Radek Matous, Jesse Glick
 */
public class AntSettingsTest extends BasicTestForImport {
    public AntSettingsTest(String testName) {
        super(testName, "org-apache-tools-ant-module-AntSettings.settings");
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/apache/tools/ant/module");
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "saveAll",
            "alwaysShowOutput",
            "extraClasspath",
            "antHome",
            "verbosity",
            "autoCloseTabs",
            "properties",
            // "customDefs" not imported
        });
    }
    public void testSaveAll() throws Exception {
        assertPropertyType("saveAll","java.lang.Boolean");
        assertProperty("saveAll","true");
    }
    public void testAlwaysShowOutput() throws Exception {
        assertPropertyType("alwaysShowOutput","java.lang.Boolean");
        assertProperty("alwaysShowOutput","false");
    }
    public void testExtraClasspath() throws Exception {
        assertPropertyType("extraClasspath","org.openide.execution.NbClassPath");
        assertProperty("extraClasspath","/home/jglick/NetBeansProjects:/home/jglick/NetBeansProjects/foo/dist/foo.jar");
    }
    public void testAntHome() throws Exception {
        assertPropertyType("antHome","java.io.File");
        assertProperty("antHome","/space/src/ant/dist");
    }
    public void testVerbosity() throws Exception {
        assertPropertyType("verbosity","java.lang.Integer");
        assertProperty("verbosity","4");
    }
    public void testAutoCloseTabs() throws Exception {
        assertPropertyType("autoCloseTabs","java.lang.Boolean");
        assertProperty("autoCloseTabs","true");
    }
    public void testProperties() throws Exception {
        assertPropertyType("properties", "java.util.HashMap");
        assertProperty("properties", "hello=kitty\nmuscular=midget");
    }
}
