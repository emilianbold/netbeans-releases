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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

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
    
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
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
