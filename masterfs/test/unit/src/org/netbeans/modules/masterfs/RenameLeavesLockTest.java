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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

/** Simulating issue 109462.
 */
public class RenameLeavesLockTest extends NbTestCase {
    Logger LOG;
    
    public RenameLeavesLockTest(String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.WARNING;
    }
    
    

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
    }
    public void testRenameBehaviour() throws Exception {
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File fJava = new File(dir, "F.java");
        fJava.createNewFile();

        //LocalFileSystem lfs = new LocalFileSystem();
        //lfs.setRootDirectory(getWorkDir());
        FileObject root = FileUtil.toFileObject(getWorkDir());
        //FileObject root = lfs.getRoot();
        assertNotNull("root found", root);
        
        FileObject f = root.getFileObject("dir/F.java");
        assertNotNull("file found", f);
        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("F.java", all[0]);
        }
        FileLock lock = f.lock();
        assertTrue(f.isLocked());
        f.rename(lock, "Jarda", "java");
        f.rename(lock, "F", "java");
        f.rename(lock, "Jarda", "java");
        assertTrue(f.isLocked());
        lock.releaseLock();
        assertFalse(f.isLocked());
        

        //Issue 109462, this is failing:
        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("Jarda.java", all[0]);
        }
    }
    
}
