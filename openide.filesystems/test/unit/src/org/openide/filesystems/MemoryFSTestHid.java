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
    
}
