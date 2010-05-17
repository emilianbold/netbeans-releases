/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.loaders;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import org.openide.filesystems.FileSystem;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

/** Creation of data object is said to be slow due to
 * poor implementation of BrokenDataShadow validate functionality.
 * @author Jaroslav Tulach
 */
public class DataFolderExpandIsSlowTest extends NbTestCase {
    /** folder to work with */
    private DataFolder folder;

    private FileObject root;
    /** keep some files */
    private DataObject[] arr;
    
    public DataFolderExpandIsSlowTest (String name) {
        super(name);
    }
    
    public static NbTestSuite suite () {
        return NbTestSuite.linearSpeedSuite(DataFolderExpandIsSlowTest.class, 5, 3);
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "files/",
        });
        
        
        root = FileUtil.toFileObject(FileUtil.toFile(lfs.getRoot()));
        
        
        folder = DataFolder.findFolder(root.getFileObject("files"));
        assertNotNull(folder);
        
        int count = getTestNumber ();
        
        for (int i = 0; i < count; i++) {
            FileUtil.createData(folder.getPrimaryFile(), "empty" + i + ".txt");
        }
    }
    
    protected void tearDown() throws Exception {
        WeakReference<Object> ref = new WeakReference<Object>(root);
        this.root = null;
        this.folder = null;
        
        List<WeakReference<DataObject>> refs = new ArrayList<WeakReference<DataObject>>();
        for (DataObject dataObject : arr) {
            refs.add(new WeakReference<DataObject>(dataObject));
        }
        this.arr = null;
        try {
            assertGC("Make sure the filesystem is gone", ref);
            
            for (WeakReference<DataObject> weakReference : refs) {
                assertGC("All data objects needs to be gone", weakReference);
            }

        } catch (AssertionFailedError ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
        }
    }
    
    private void doArr(boolean fast) {
        DataObjectPool.fastCache(fast);
        arr = folder.getChildren();
        assertEquals("All computed as expected", getTestNumber(), arr.length);
    }
    
    
    public void testNew99 () { doArr(false); }
    public void testNew245 () { doArr(false); }
    public void testNew987 () { doArr(false); }
//    public void testNew9987 () { doArr(false); }
    public void testOld99 () { doArr(true); }
    public void testOld245 () { doArr(true); }
    public void testOld987 () { doArr(true); }
//    public void testOld9987 () { doArr(false); }
}
