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
            //"customDefs", skipped
            "classpath",
            "antHome",
            "verbosity",
            "autoCloseTabs",
            "loadFactor",
            "threshold"
        });
    }
    public void testSaveAll() throws Exception {
        assertPropertyType("saveAll","java.lang.Boolean");
        assertProperty("saveAll","true");
    }
    public void testAlwaysShowOutput() throws Exception {
        assertPropertyType("alwaysShowOutput","java.lang.Boolean");
        assertProperty("alwaysShowOutput","true");
    }
    public void testClasspath() throws Exception {
        assertPropertyType("extraClasspath","org.openide.execution.NbClassPath");
        assertProperty("classpath","/home.local/rmatous/JavaApplication2:/home.local/rmatous/org-netbeans-modules-masterfs.jar");
    }
    public void testAntHome() throws Exception {
        assertPropertyType("antHome","java.io.File");
        assertProperty("antHome","/space/ant/apache-ant-1.6.5");
    }
    public void testVerbosity() throws Exception {
        assertPropertyType("verbosity","java.lang.Integer");
        assertProperty("verbosity","2");
    }
    public void testAutoCloseTabs() throws Exception {
        assertPropertyType("autoCloseTabs","java.lang.Boolean");
        assertProperty("autoCloseTabs","true");
    }
    public void testLoadFactor() throws Exception {
        assertProperty("loadFactor","0.75");
    }
    public void testThreshold() throws Exception {
        assertProperty("threshold","12");
    }        
}
