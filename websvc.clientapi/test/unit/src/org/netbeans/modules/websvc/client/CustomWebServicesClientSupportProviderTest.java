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

package org.netbeans.modules.websvc.api;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesClientSupportProviderTest extends NbTestCase {
    
    private FileObject datadir;
    private FileObject nows;
    private FileObject ws;
    private FileObject jaxws;
    private FileObject both;
    
    static {
        CustomWebServicesClientSupportProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomWebServicesSupportProviderTest */
    public CustomWebServicesClientSupportProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getWorkDir();
        assertTrue("work dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject(f);
        assertNotNull("no FileObject", datadir);
        nows = datadir.createData("custom", "nows");
        assertNotNull("no ws FileObject", nows);
        ws = datadir.createData("custom", "ws");
        assertNotNull("no ws FileObject", ws);
        jaxws = datadir.createData("custom", "jaxws");
        assertNotNull("no ws FileObject", jaxws);
        both = datadir.createData("custom", "both");
        assertNotNull("no ws FileObject", both);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ws.delete();
        jaxws.delete();
        nows.delete();
        both.delete();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(WebServicesClientSupportProvider.class));
        assertEquals("there should be 2 instances - one from websvc/clientapi and one from tests", 2, res.allInstances ().size ());
    }
    
    public void testGetWebServicesClientSupport() throws Exception {
        WebServicesClientSupport ws1 = WebServicesClientSupport.getWebServicesClientSupport(nows);
        assertNull("not found ws support", ws1);
        WebServicesClientSupport ws2 = WebServicesClientSupport.getWebServicesClientSupport(ws);
        assertNotNull("found ws support", ws2);
        WebServicesClientSupport ws3 = WebServicesClientSupport.getWebServicesClientSupport(jaxws);
        assertNull("not found ws support", ws3);
        WebServicesClientSupport ws4= WebServicesClientSupport.getWebServicesClientSupport(both);
        assertNotNull("found ws support", ws4);
        
        JAXWSClientSupport jaxws1 = JAXWSClientSupport.getJaxWsClientSupport(nows);
        assertNull("not found jaxws support", jaxws1);
        JAXWSClientSupport jaxws2 = JAXWSClientSupport.getJaxWsClientSupport(ws);
        assertNull("not found jaxws support", jaxws2);
        JAXWSClientSupport jaxws3 = JAXWSClientSupport.getJaxWsClientSupport(jaxws);
        assertNotNull("found jaxws support", jaxws3);
        JAXWSClientSupport jaxws4 = JAXWSClientSupport.getJaxWsClientSupport(both);
        assertNotNull("found jaxws support", jaxws4);
    }
}
