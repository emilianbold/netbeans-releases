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

package org.openide.filesystems;

/**
 *
 * @author Radek Matous
 */
public class MemoryFSTestHid extends TestBaseHid {

    /** Creates a new instance of MemoryFSTestHid */
    public MemoryFSTestHid(String testName) {
        super(testName);
    }

    protected String[] getResources(String testName) {
        return new String[]{};
    }


    public void test58331() throws Exception {
        FileObject p = this.testedFS.getRoot();
        FileObject fo = p.createData("test58331");//NOI18N
        assertEquals(fo.getParent(), p);
        String n = fo.getName();
        fo.delete();
        fo.refresh();
        fo.isFolder(); 
        p.createData(n);
    }

    public void testRootAttributes () throws Exception {
        FileObject file = FileUtil.createData(this.testedFS.getRoot(), "/folder/file");
        assertNotNull(file);
        FileObject root = this.testedFS.getRoot();
        assertNotNull(root);
        file.setAttribute("name", "value");
        assertEquals(file.getAttribute("name"), "value");
        root.setAttribute("rootName", "rootValue");
        assertEquals(root.getAttribute("rootName"), "rootValue");        
    }        
}
