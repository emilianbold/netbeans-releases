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
package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * @author pzajac
 */
public class TestEntryTest extends TestBase {
    
    public TestEntryTest(String testName) {
        super(testName);
    }
    
    public void testGetSourcesNbOrgModule() throws IOException {
        File test = new File(nbrootF,"nbbuild/build/testdist/unit/" + CLUSTER_IDE + "/org-netbeans-modules-apisupport-project/tests.jar"); // NOI18N
        TestEntry entry = TestEntry.get(test);
        assertNotNull("TestEntry for aisupport/project tests",entry);
        assertNotNull("Nbcvsroot wasn't found.", entry.getNBCVSRoot());
        URL srcDir = entry.getSrcDir();
        assertEquals(new File(nbrootF,"apisupport/project/test/unit/src").toURI().toURL(),srcDir);
    }
    
    public void testGetSourcesFromExternalModule() throws IOException {
        File test = file(EEP + "/suite4/build/testdist/unit/cluster/module1/tests.jar");
        TestEntry entry = TestEntry.get(test);
        assertNotNull("TestEntry for aisupport/project tests",entry);
        assertNull("Nbcvsroot was found.", entry.getNBCVSRoot());
        URL srcDir = entry.getSrcDir();
        assertEquals(file(EEP + "/suite4/module1/test/unit/src").toURI().toURL().toExternalForm(),srcDir.toExternalForm());
    }
    
}
