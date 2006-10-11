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
public class JUnitSettingsTest extends BasicTestForImport {
    public JUnitSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-junit-JUnitSettings.settings");
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/junit");
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "fileSystem",
            "version",
            "membersPackage",
            "generateMainMethod",
            "membersProtected",
            "bodyComments",
            "bodyContent",
            "javaDoc",
            "generateTearDown",
            "generateSuiteClasses",
            "membersPublic",
            "includePackagePrivateClasses",
            "rootSuiteClassName",
            "generateSetUp",
            "generateAbstractImpl",
            "generateMainMethodBody",
            "generateExceptionClasses"
        });
    }
    
    public void testVersion() throws Exception {
        assertPropertyType("version","java.lang.Integer");
        assertProperty("version","41");
    }
    
    public void testFileSystem() throws Exception {
        assertPropertyType("fileSystem","java.lang.String");
        assertProperty("fileSystem","");
    }
    
    public void testMembersPublic() throws Exception {
        assertPropertyType("membersPublic","java.lang.Boolean");
        assertProperty("membersPublic","true");
    }
    
    public void testMembersProtected() throws Exception {
        assertPropertyType("membersProtected","java.lang.Boolean");
        assertProperty("membersProtected","true");
    }
    
    public void testMembersPackage() throws Exception {
        assertPropertyType("membersPackage","java.lang.Boolean");
        assertProperty("membersPackage","true");
    }
    
    public void testBodyComments() throws Exception {
        assertPropertyType("bodyComments","java.lang.Boolean");
        assertProperty("bodyComments","true");
    }
    
    public void testBodyContent() throws Exception {
        assertPropertyType("bodyContent","java.lang.Boolean");
        assertProperty("bodyContent","true");
    }
    
    public void testJavaDoc() throws Exception {
        assertPropertyType("javaDoc","java.lang.Boolean");
        assertProperty("javaDoc","true");
    }
    
    public void testGenerateTearDown() throws Exception {
        assertPropertyType("generateTearDown","java.lang.Boolean");
        assertProperty("generateTearDown","true");
    }
    public void testGenerateSuiteClasses() throws Exception {
        assertPropertyType("generateSuiteClasses","java.lang.Boolean");
        assertProperty("generateSuiteClasses","true");
    }
    public void testIncludePackagePrivateClasses() throws Exception {
        assertPropertyType("includePackagePrivateClasses","java.lang.Boolean");
        assertProperty("includePackagePrivateClasses","false");
    }
    public void testRootSuiteClassName() throws Exception {
        assertPropertyType("rootSuiteClassName","java.lang.String");
        assertProperty("rootSuiteClassName","RootSuite");
    }
    public void testGenerateSetUp() throws Exception {
        assertPropertyType("generateSetUp","java.lang.Boolean");
        assertProperty("generateSetUp","true");
    }
    public void testGenerateAbstractImpl() throws Exception {
        assertPropertyType("generateAbstractImpl","java.lang.Boolean");
        assertProperty("generateAbstractImpl","true");
    }
    public void testGenerateMainMethodBody() throws Exception {
        assertPropertyType("generateMainMethodBody","java.lang.String");
        assertProperty("generateMainMethodBody","junit.textui.TestRunner.run(suite());");
    }
    public void testGenerateExceptionClasses() throws Exception {
        assertPropertyType("generateExceptionClasses","java.lang.Boolean");
        assertProperty("generateExceptionClasses","false");
    }
}
