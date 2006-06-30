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

package org.netbeans.modules.j2ee.dd.api.webservices;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.InputSource;

/*
 * DDProviderTest.java
 * JUnit based test
 *
 * Created on 16 December 2005, 14:15
 */

/**
 *
 * @author jungi
 */
public class DDProviderTest extends NbTestCase {

    private DDProvider dd;
    
    public DDProviderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dd = DDProvider.getDefault();
        assertNotNull("DDProvider cannot be null.", dd);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dd = null;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DDProviderTest.class);
        return suite;
    }

    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromFOWHandler() throws Exception {
        File f = new File(getDataDir(), "wHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, true);
    }

    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromInputSourceWHandler() throws Exception {
        File f = new File(getDataDir(), "wHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, false);
    }
    
    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromFOWoHandler() throws Exception {
        File f = new File(getDataDir(), "woHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, true);
    }

    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromInputSourceWoHandler() throws Exception {
        File f = new File(getDataDir(), "woHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, false);
    }
    
    private void readWriteDD(File ddFile, boolean useFO) throws Exception {
        Webservices result = null;
        if (!useFO) {
            InputSource is = new InputSource(new BufferedInputStream(new FileInputStream(ddFile)));
            result = dd.getDDRoot(is);
        } else {
            FileObject fo = FileUtil.toFileObject(ddFile);
            result = dd.getDDRoot(fo);
        }
        assertNotNull("Result cannot be null.", result);
        assertEquals(Webservices.STATE_VALID, result.getStatus());
        File dest = new File(getWorkDir(), "webservices.xml");
        File diff = new File(getWorkDir(), "webservices.xml.diff");
        assertTrue(dest.createNewFile());
        assertTrue(diff.createNewFile());
        result.write(new BufferedOutputStream(new FileOutputStream(dest)));
        assertFile(dest, ddFile, diff);
    }
}
