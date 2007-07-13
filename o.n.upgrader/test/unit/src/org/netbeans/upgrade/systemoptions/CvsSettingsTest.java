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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.upgrade.systemoptions;

/**
 *
 * @author tomas
 */
public class CvsSettingsTest extends BasicTestForImport {

    public CvsSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-versioning-system-cvss-settings-CvsModuleConfig.settings");
    }

    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/versioning/system/cvss"); 
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {"commitExclusions.0", "commitExclusions.1", "commitExclusions.2", "defaultValues", "ignoredFilePatterns", "textAnnotationsFormat"});        
    }

    public void testPropertyTypes() throws Exception {
        assertPropertyType("commitExclusions", "org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig.PersistentHashSet");                
    }
    
    public void testProperties() throws Exception {
        assertProperty("commitExclusions.0", "/home/tomas/JavaApplication2/src/javaapplication2/NewClass.java");                
        assertProperty("commitExclusions.1", "/home/tomas/JavaApplication2/src/javaapplication2/NewClass1.java");                
        assertProperty("commitExclusions.2", "/home/tomas/JavaApplication2/src/javaapplication2/Main.java");                                        
    }
    
}
