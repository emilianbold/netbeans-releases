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

package org.netbeans.modules.refactoring.spi;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Becicka
 */
public class BackupFacilityTest extends NbTestCase {
    
    public BackupFacilityTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        f = FileUtil.toFileObject(getDataDir()).getFileObject("projects/default/src/defaultpkg/Main.java");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetDefault() {
        assertNotNull(BackupFacility.getDefault());
    }

    long transactionId;
    FileObject f;
    public void testBackup() throws Exception {
        transactionId = BackupFacility.getDefault().backup(f);
    }

    public void testRestore() throws Exception {
        f.delete();
        assertFalse(f.isValid());
        BackupFacility.getDefault().restore(transactionId);
        FileObject newone = FileUtil.toFileObject(new File(f.getPath()));
        assertTrue(newone.isValid());
    }

    public void testClear() throws IOException {
        BackupFacility.getDefault().clear();
        try {
            BackupFacility.getDefault().restore(transactionId);
        } catch (IllegalArgumentException iae) {
            return;
        }
        fail("clear failed");
    }

}
