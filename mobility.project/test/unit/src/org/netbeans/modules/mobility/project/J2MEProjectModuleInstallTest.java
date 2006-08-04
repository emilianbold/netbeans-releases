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

/*
 * J2MEProjectModuleInstallTest.java
 * JUnit based test
 *
 * Created on 06 February 2006, 18:55
 */
package org.netbeans.modules.mobility.project;

import java.io.IOException;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author lukas
 */
public class J2MEProjectModuleInstallTest extends NbTestCase {
    
    public J2MEProjectModuleInstallTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEProjectModuleInstallTest.class);
        
        return suite;
    }
    
    /**
     * Test of restored method, of class org.netbeans.modules.mobility.project.J2MEProjectModuleInstall.
     */
    public void testRestored() throws IOException {
        System.out.println("restored");
        
        System.setProperty("netbeans.user","test/tiredTester");
        J2MEProjectModuleInstall instance = new J2MEProjectModuleInstall();
        
        EditableProperties pr1=PropertyUtils.getGlobalProperties();
        instance.restored();
        EditableProperties pr2=PropertyUtils.getGlobalProperties();
        assertEquals(pr1,pr2);
    }
}
