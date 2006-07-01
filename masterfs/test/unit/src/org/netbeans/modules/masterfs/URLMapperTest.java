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

package org.netbeans.modules.masterfs;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * @author Radek Matous
 */
public class URLMapperTest extends NbTestCase {
    public URLMapperTest(String name) {
        super(name);
    }


    public void testURLMapperCallingFromMetaInfLookup() {
        Lookup lkp = Lookups.metaInfServices(Thread.currentThread().getContextClassLoader());
        Object obj = lkp.lookup(Object.class);
        assertNotNull(obj);
        assertEquals(MyInstance2.class, obj.getClass());
    }

    public static class MyInstance2 {
        public MyInstance2() {
            super();
            testURLMapper();
        }

        private static void testURLMapper() {
            MasterFileSystem mfs = MasterFileSystem.getDefault();
            FileObject[] children = mfs.getRoot().getChildren();
            for (int i = 0; i < children.length; i++) {
                java.io.File file = FileUtil.toFile(children[i]);
                assertNotNull(file);
                assertNotNull(FileUtil.toFileObject(file));
            }
        }

    }

}
