/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author  Jan Pokorsky
 */
public class SerialDataNodeTest extends NbTestCase {
    
    private FileSystem sfs;
    
    /** Creates a new instance of SerialDataNodeTest */
    public SerialDataNodeTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new org.netbeans.junit.NbTestSuite(SerialDataNodeTest.class));
        System.exit(0);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        org.openide.TopManager.getDefault();
        sfs = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
    }
    
    public void testDisplayName() throws Exception {
        String res = "Settings/org-netbeans-modules-settings-convertors-testDisplayName.settings";
        FileObject fo = sfs.findResource(res);
        assertNotNull(res, fo);
        assertNull("name", fo.getAttribute("name"));
        
        DataObject dobj = DataObject.find (fo);
        Node n = dobj.getNodeDelegate();
        assertNotNull(n);
        assertEquals("I18N", n.getDisplayName());
        
        // property sets have to be initialized otherwise the change name would be
        // propagated to the node after some delay (~2s)
        Object garbage = n.getPropertySets();
        
        InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
        assertNotNull (dobj + " does not contain instance cookie", ic);
        
        FooSetting foo = (FooSetting) ic.instanceCreate();
        String newName = "newName";
        foo.setName(newName);
        assertEquals(n.toString(), newName, n.getDisplayName());
        
        newName = "newNameViaNode";
        n.setName(newName);
        assertEquals(n.toString(), newName, n.getDisplayName());
        assertEquals(n.toString(), newName, foo.getName());
    }
}
