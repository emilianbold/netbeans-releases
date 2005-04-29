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

public class MfoOnSFSTestHid extends TestBaseHid {
    FileSystem sfs = null;
    
    public MfoOnSFSTestHid(String testName) {
        super(testName);
        sfs = this.testedFS;        
    }
    
    /** not necessary  */
    protected String[] getResources (String testName) {
        return new String[] {};
    }    
    
    
    public void testMove () throws IOException{        
        String whatRes = "Actions/System/org-openide-actions-GarbageCollectAction.instance";
        String whereRes = "Menu/Tools";                        
                
        FileObject what = sfs.findResource (whatRes);
        if (what == null) {
            what = FileUtil.createData(sfs.getRoot(), whatRes);
        }
        fsAssert("Expected in SystemFileSystem: " + whatRes,what != null);        
        FileObject where = sfs.findResource (whereRes);
        if (where == null) {
            where = FileUtil.createFolder(sfs.getRoot(), whereRes);
        }
        
        fsAssert("Expected in SystemFileSystem: " + whereRes,where != null);        
        
        FileLock flock = what.lock();
        try {
            FileObject moveResult = what.move (flock,where,what.getName(),what.getExt());
            fsAssert("Move error",moveResult != null);
            fsAssert("Move error",sfs.findResource (whatRes) == null);
            fsAssert("Move error",sfs.findResource (whereRes) != null);            
        } finally {
            flock.releaseLock();
        }
        
    }    

    
    protected void setUp() throws Exception {
        this.testedFS = sfs = Repository.getDefault().getDefaultFileSystem();;
    }
    
}
