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

public class MultiFileObjectTestHid extends TestBaseHid {
    private static String[] resources = new String [] {
        "/fold10/fold11/fold12",
        "/fold20/fold21/fold22",
        "/fold20/fold23.txt"        
    };
    
    public MultiFileObjectTestHid(String testName) {
        super(testName);
    }
    
    
    protected String[] getResources (String testName) {
        return resources;
    }    
    
    /** #18820*/
    public void testDeleteMask() throws IOException {
        FileSystem mfs = new MultiFileSystem (new FileSystem[] {this.testedFS});
        FileSystem wfs;        
        FileSystem [] allFs = this.allTestedFS;
        if (allFs.length > 1 && !allFs[1].isReadOnly())
            wfs = allFs[1];
        else return; 
        String resource = "/fold20/fold23.txt";
        String resource_hidden = "/fold20/fold23.txt_hidden";
        
        FileObject fo = mfs.findResource(resource);                
        fo.delete();
                
        FileObject hidd = wfs.findResource(resource_hidden);
        if (hidd == null) return;        
       /** only if mask necessary*/
        hidd.delete();        

        fo = mfs.findResource(resource);
        fsAssert(resource+" should be present after deleting mask",fo != null);        
    }
    

    /** */
    public void testDeleteFolder() throws IOException {
        FileSystem mfs = this.testedFS;

        FileObject testFo = mfs.findResource("/fold20/fold21/fold22");
        fsAssert("/fold20/fold21/fold22 should be present",testFo != null);
        
        FileObject toDel = mfs.findResource("/fold20");
        fsAssert("/fold20 should be present",toDel != null);
        
        FileObject parent = toDel.getParent();
        
        toDel.delete();
        
        toDel = mfs.findResource("/fold20");
        fsAssert("/fold20 should not be present",toDel == null);
        

        parent.createFolder("fold20");
        toDel = mfs.findResource("/fold20");
        fsAssert("/fold20 should be present",toDel != null);
        
      
        /** this assert is goal of this test. Not whole hierarchy should not appear 
         * after deleting and recreation of any folder
         */
        testFo = mfs.findResource("/fold20/fold21/fold22");
        fsAssert("/fold20/fold21/fold22 should not be present",testFo == null);
    }    
    
    public void testBug19425 () throws IOException {
        String whereRes = "/fold10";
        String whatRes = "/fold20/fold23.txt";
        
        FileSystem mfs = this.allTestedFS[0];
        FileSystem lfsLayer = this.allTestedFS[1];
        FileSystem xfsLayer = this.allTestedFS[2];                
        
        boolean needsMask = (xfsLayer.findResource (whatRes) != null);
        

        FileObject where = mfs.findResource (whereRes);
        FileObject what = mfs.findResource (whatRes);
        
        fsAssert ("Expected resource: " + whereRes, whereRes != null);
        fsAssert ("Expected resource: " + whatRes, whatRes != null);

        FileLock fLock = what.lock();
        try {
            what.move (fLock,where,what.getName(),what.getExt());
            if (needsMask)
                fsAssert ("Must exist mask", lfsLayer.findResource(whatRes+"_hidden") != null);
            else 
                fsAssert ("Mustn`t exist mask", lfsLayer.findResource(whatRes+"_hidden") == null);
                
        } finally {
            fLock.releaseLock();
        }
    }
    
    /** null delegates are acceptable*/
    public void testSetDelegates() throws IOException {            
        FileSystem mfs = this.testedFS;
        MultiFileSystem mfs2 = new MultiFileSystem (new FileSystem[] {mfs});

        try {
            mfs2.setDelegates(new FileSystem[] {mfs,null});
        } catch (NullPointerException npe) {
            fsFail ("Null delegates should be supported"); 
        }
    }
    
}
