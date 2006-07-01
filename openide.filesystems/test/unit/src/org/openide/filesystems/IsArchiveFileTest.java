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


import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.*;

/**
 *Test behavior of FileUtil.isArchiveFile
 *
 * @author Tomas Zezula
 */
public class IsArchiveFileTest extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private LocalFileSystem lfs;
    private FileObject directory;
    private FileObject brokenArchive;
    private FileObject archive;
    private FileObject file;
    private FileObject emptyFile;

    /**
     * Creates new test
     */
    public IsArchiveFileTest(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(IsArchiveFileTest.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();                        
        lfs = new LocalFileSystem ();
        lfs.setRootDirectory(this.getWorkDir());
        Repository.getDefault().addFileSystem(lfs);
        FileObject root = lfs.getRoot();        
        directory = root.createFolder("dir");
        brokenArchive = root.createData ("brokenArchive.jar");
        archive = root.createData("archive.jar");
        FileLock lock = archive.lock();
        try {
            JarOutputStream out = new JarOutputStream (archive.getOutputStream(lock));
            try {
                out.putNextEntry(new ZipEntry("foo"));
                out.closeEntry();
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        file = root.createData ("file.txt");
        lock = file.lock ();
        try {
            OutputStream out = file.getOutputStream(lock);
            try {
                out.write ("Test file".getBytes());
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        emptyFile = root.createData("emptyFile.txt");
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(lfs);
        super.tearDown();
    }
    

    public void testIsArchivFile () throws Exception {
        assertFalse (FileUtil.isArchiveFile(directory));
        assertFalse (FileUtil.isArchiveFile(brokenArchive));
        assertTrue (FileUtil.isArchiveFile(archive));
        assertFalse (FileUtil.isArchiveFile(file));
        assertFalse (FileUtil.isArchiveFile(emptyFile));
        
        assertFalse (FileUtil.isArchiveFile(new URL("jar:file:/foo.jar!/")));
        assertFalse (FileUtil.isArchiveFile(new URL("file:/foo/")));
        assertTrue (FileUtil.isArchiveFile(new URL("file:/foo.jar")));
    }

   
}
  
  
  
