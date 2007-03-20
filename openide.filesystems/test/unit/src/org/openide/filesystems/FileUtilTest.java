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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.io.File;
import java.net.URL;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 * @author Jesse Glick
 */
public class FileUtilTest extends NbTestCase {

    public FileUtilTest(String n) {
        super(n);
    }

    public void testToFileObjectSlash() throws Exception { // #98388
        if (!Utilities.isUnix()) {
            return;
        }
        File root = new File("/");
        assertTrue(root.isDirectory());
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(root);
        LFS_ROOT = lfs.getRoot();
        MockServices.setServices(TestUM.class);
        assertEquals(LFS_ROOT, FileUtil.toFileObject(root));
    }

    private static FileObject LFS_ROOT;
    public static class TestUM extends URLMapper {
        public URL getURL(FileObject fo, int type) {
            throw new UnsupportedOperationException();
        }
        public FileObject[] getFileObjects(URL url) {
            if (url.toExternalForm().equals("file:/")) {
                return new FileObject[] {LFS_ROOT};
            } else {
                return null;
            }
        }
    }

}
