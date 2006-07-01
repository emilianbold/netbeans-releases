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
