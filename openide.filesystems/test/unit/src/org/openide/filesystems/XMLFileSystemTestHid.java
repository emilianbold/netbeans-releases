/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.io.IOException;
import java.io.File;

public class XMLFileSystemTestHid extends TestBaseHid {
    private String[] resources = new String[] {"a/b/c"};
    XMLFileSystem xfs = null;
    
    public XMLFileSystemTestHid(String testName) {
        super(testName);
    }
    
    protected String[] getResources (String testName) {        
        return resources;
    }    
    
   
    public void testReset () throws Exception {        
        FileObject a = xfs.findResource("a");
        assertNotNull(a);
        

        FileChangeAdapter fcl = new FileChangeAdapter();
        a.addFileChangeListener(fcl);
        
        resources = new String[] {"a/b/c","a/b1/c"};        
        xfs.setXmlUrl(createXMLLayer().toURL());
        
        FileObject b1 = xfs.findResource("a/b1");
        assertNotNull(b1);                
        assertTrue(b1.isFolder());        
    }
    
    protected void setUp() throws Exception {
        File f = createXMLLayer();
        xfs = new XMLFileSystem ();
        xfs.setXmlUrl(f.toURL());
        this.testedFS = xfs;
    }

    private File createXMLLayer() throws IOException {
        String testName = getName();
        File f = TestUtilHid.createXMLLayer(testName, getResources(testName));
        return f;
    }

}
