/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    /** Tests FileAlreadyLockedException is thrown and 'locked by' is reported. */
    public void testAlreadyLocked() throws IOException {
        FileSystem mfs = this.testedFS;
        FileObject testFo = mfs.findResource("/fold20/fold23.txt");
        fsAssert("/fold20/fold23.txt should be present", testFo != null);

        FileLock lock = testFo.lock();
        try {
            testFo.lock();
            fail("FileAlreadyLockedException not thrown if already locked.");
        } catch (FileAlreadyLockedException e) {
            // OK
            boolean asserts = false;
            assert asserts = true;
            if (asserts) {
                assertNotNull("Init cause not set.", e.getCause());
            }
        } finally {
            lock.releaseLock();
        }
    }
}
