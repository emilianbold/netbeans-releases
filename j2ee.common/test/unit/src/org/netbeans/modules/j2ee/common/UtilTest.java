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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 *
 * @author sherold
 */
public class UtilTest extends NbTestCase {
    
    /** Creates a new instance of UtilTest */
    public UtilTest(String testName) {
        super(testName);
    }
    
    
    public void testContainsClass() throws IOException {
        File dataDir = getDataDir();
        File[] classpath1 = new File[] { 
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/libs/org.netbeans.test.dbdriver.jar") 
        }; 
        File[] classpath2 = new File[] { 
            new File(dataDir, "testcp/libs/org.netbeans.nondriver.jar"),
            new File(dataDir, "testcp/classes") ,
            new File(dataDir, "testcp/shared/classes"), 
        };
        
        assertFalse(Util.containsClass(Arrays.asList(classpath1), "com.mysql.Driver"));
        assertFalse(Util.containsClass(Arrays.asList(classpath2), "com.mysql.Driver"));
        
        // the driver is in the jar file
        assertTrue(Util.containsClass(Arrays.asList(classpath1), "org.netbeans.test.db.driver.TestDriver"));
        // the driver is among the classes
        assertTrue(Util.containsClass(Arrays.asList(classpath2), "org.netbeans.test.db.driver.TestDriver"));
    } 
    
    public void testGetJ2eeSpecificationLabel() {
        assertNotNull(Util.getJ2eeSpecificationLabel(J2eeModule.J2EE_13));
        assertNotNull(Util.getJ2eeSpecificationLabel(J2eeModule.J2EE_14));
        assertNotNull(Util.getJ2eeSpecificationLabel(J2eeModule.JAVA_EE_5));
        
        try {
            Util.getJ2eeSpecificationLabel("nothing");
            fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            //expected
        }
                     
        try {
            Util.getJ2eeSpecificationLabel(null);
            fail("Expected exception not thrown");
        } catch (NullPointerException ex) {
            //expected
        }
    }
}
