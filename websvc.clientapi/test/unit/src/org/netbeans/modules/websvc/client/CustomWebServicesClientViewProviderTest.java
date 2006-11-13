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
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesClientViewProviderTest extends NbTestCase {
    
    private FileObject datadir;
    private FileObject ws;
    private FileObject nows;
    
    static {
        CustomWebServicesClientViewProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomJAXWSViewProviderTest */
    public CustomWebServicesClientViewProviderTest(String name) {
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
        ws = datadir.createData("custom", "ws");
        assertNotNull("no ws FileObject", ws);
        nows = datadir.createData("custom", "nows");
        assertNotNull("no ws FileObject", nows);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ws.delete();
        nows.delete();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(WebServicesClientViewProvider.class));
        assertEquals("there should be 1 instance - from websvc/clientapi", 1, res.allInstances().size());
    }
    
    public void testGetWebServicesClientView() {
        WebServicesClientView view = WebServicesClientView.getWebServicesClientView(ws);
        assertNotNull("found view support", view);
        WebServicesClientView view2 = WebServicesClientView.getWebServicesClientView(nows);
        assertNull("no found view support", view2);
    }
    
}
