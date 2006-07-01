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

package org.openide.loaders;

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;

/** Test functionality of FilesSet object returned from MultiFileObject.files()
 * method.
 *
 * @author Petr Hamernik
 */
public class MultiDataObjectFilesTest extends NbTestCase {

    public MultiDataObjectFilesTest(String name) {
        super(name);
    }

    public void testFilesSet () throws Exception {
        DataLoader loader = DataLoader.getLoader(SimpleLoader.class);
        AddLoaderManuallyHid.addRemoveLoader(loader, true);

        
        // create directory structur description
        String[] fsstruct = new String[] {
            "A.primary", "A.a", "A.b", 
            "B.x0", "B.zx", "B.secondary", 
            "C.a0", "C.a5", "C.a1", "C.a4", 
            "A.primary0", "A.secondary", "A.zx", "A.x0",
            "C.a2", "C.a3", "C.primary",
            "B.primary", "B.b", "B.primary0", "B.a"
        };
            
        // clean and create new filesystems
        TestUtilHid.destroyLocalFileSystem(getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        DataFolder folder = DataFolder.findFolder(fs.getRoot());

        DataObject[] children = folder.getChildren();
        assertTrue ("DataObjects were not recognized correctly.", children.length == 3);
        for (int i = 0; i < children.length; i++) {
            DataObject obj = children[i];
            Set files = obj.files();
            
            Iterator it = files.iterator();
            FileObject primary = (FileObject) it.next();
            assertEquals("Primary file is not returned first for "+obj.getName(), primary, obj.getPrimaryFile());

            FileObject last = null;
            while (it.hasNext()) {
                FileObject current = (FileObject) it.next();
                if (last != null) {
                    assertTrue("FileObjects are not alphabetically", last.getNameExt().compareTo(current.getNameExt()) < 0);
                }
                last = current;
            }
        }
        
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class);
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                return fo.hasExt("primary") ? fo : FileUtil.findBrother(fo, "primary");
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
    }

}
