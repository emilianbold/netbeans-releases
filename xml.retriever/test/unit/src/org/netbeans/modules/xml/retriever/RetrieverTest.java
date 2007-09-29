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
 * RetrieverTest.java
 * JUnit based test
 *
 * Created on August 16, 2006, 5:02 PM
 */

package org.netbeans.modules.xml.retriever;

import junit.framework.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class RetrieverTest extends TestCase {
    
    public RetrieverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RetrieverTest.class);
        
        return suite;
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.retriever.Retriever.
     */
    public void testGetDefault() throws URISyntaxException, UnknownHostException, IOException {
        System.out.println("getDefault");
        
        Retriever expResult = null;
        Retriever result = Retriever.getDefault();
        //uncomment the following to test the retriever method
        /*File destFolder = new File(System.getProperty("java.io.tmpdir")+File.separator+"RetrieverTest");
        if(destFolder.isDirectory())
            destFolder.renameTo(new File(destFolder.toString()+System.currentTimeMillis()));
        destFolder.mkdirs();
        URI catFileURI = null;
        catFileURI = new URI(destFolder.toURI().toString() + "/catalogfile.xml");
        FileObject dstFO = FileUtil.toFileObject(FileUtil.normalizeFile(destFolder));
        
        result.retrieveResource(dstFO, catFileURI, new URI("http://localhost:8084/grt/maindoc/UBL-Order-1.0"));
         **/
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.retriever.Retriever.
     */
    public void testRelativize() throws Exception {
        System.out.println("getDefault");
        URI masterURI = new URI("A/B/C");
        URI slaveURI = new URI("A/B/C/D/E");
        String result = Utilities.relativize(masterURI, slaveURI);
        assert(result.equals("D/E"));        
    }
    
    
}
