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

package org.netbeans.upgrade;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class CopyTest extends org.netbeans.junit.NbTestCase {
    public CopyTest (String name) {
        super (name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        
        clearWorkDir();
    }    
   
    public void testAppendSelectedLines() throws Exception {
        //setup
        List expectedLines = new ArrayList();
        File wDir = getWorkDir();
        File sFile = new File(wDir,this.getName()+".file");
        assertTrue(sFile.createNewFile());
        File tFolder = new File(wDir,this.getName());
        assertTrue(tFolder.mkdir());
        File tFile = new File(tFolder,this.getName()+".file");
        assertTrue(tFile.createNewFile());
        FileOutputStream fos = new FileOutputStream(tFile);
        try {
            String line = "nbplatform.default.harness.dir=${nbplatform.default.netbeans.dest.dir}/harness \n";
            fos.write(line.getBytes());
            expectedLines.add(line);                        
        } finally {
            fos.close();
        }
        
        fos = new FileOutputStream(sFile);
        try {
            String line = "nbplatform.id.netbeans.dest.dir=/work/nball/nbbuild/netbeans \n";
            fos.write(line.getBytes());
            expectedLines.add(line);
            
            line = "nbplatform.id.netbeans.dest.dir=/work/nbide/netbeans \n";
            fos.write(line.getBytes());
            expectedLines.add(line);
            
            line = "nbplatform.default.netbeans.dest.dir=/work/nbide/netbeans \n";
            fos.write(line.getBytes());
            //lines.add(line); -- should be excluded
        } finally {
            fos.close();
        }
        String[] regexForSelection = new String[] {
            "^nbplatform[.](?![dD]efault).+[.](netbeans[.]dest[.]dir|label|harness[.]dir)=.+$"//NOI18N
        };
        
        Copy.appendSelectedLines(sFile, tFolder, regexForSelection);
        String line = null;
        List resultLines = new ArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(tFile));
        try {
            while ((line = reader.readLine()) != null) {
                resultLines.add(line+"\n");
            }
        } finally {
            reader.close();
        }
        assertEquals(expectedLines,resultLines);
    }
    
    public void testDoesSomeCopy () throws Exception {
        FileSystem fs = createLocalFileSystem (new String[] {
            "root/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("X.txt");
          Copy.copyDeep (fo, tg, set);

        assertEquals ("One file copied", 1, tg.getChildren().length);
        String n = tg.getChildren ()[0].getNameExt();
        assertEquals ("Name is X.txt", "X.txt", n);
        
    }
    
    public void testDoesDeepCopy () throws Exception {
        FileSystem fs = createLocalFileSystem (new String[] {
            "root/subdir/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("subdir/X.txt");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name is X.txt", "subdir", tg.getChildren ()[0].getNameExt());
        assertEquals ("One children of one child", 1, tg.getChildren()[0].getChildren().length);
        assertEquals ("X.txt", "X.txt", tg.getChildren()[0].getChildren()[0].getNameExt());
        
    }
    
    public void testCopyAttributes () throws Exception {
        FileSystem fs = createLocalFileSystem (new String[] {
            "root/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        FileObject x = fs.findResource ("root/X.txt");
        x.setAttribute ("ahoj", "yarda");
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("X.txt");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name is X.txt", "X.txt", tg.getChildren ()[0].getNameExt());
        assertEquals ("attribute copied", "yarda", tg.getChildren()[0].getAttribute("ahoj"));
    }
    
    public void testCopyFolderAttributes () throws Exception {
        FileSystem fs = createLocalFileSystem (new String[] {
            "root/sub/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        FileObject x = fs.findResource ("root/sub");
        x.setAttribute ("ahoj", "yarda");
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("sub");
        set.add ("sub/X.txt");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("One file copied", 1, tg.getChildren().length);
        assertEquals ("Name of the dir is sub", "sub", tg.getChildren ()[0].getNameExt());
        assertEquals ("attribute copied", "yarda", tg.getChildren()[0].getAttribute("ahoj"));
        assertEquals ("X.txt", "X.txt", tg.getChildren()[0].getChildren()[0].getNameExt());
    }
    
    public void testDoNotCopyEmptyDirs () throws Exception {
        FileSystem fs = createLocalFileSystem (new String[] {
            "root/sub/X.txt", 
            "root/Y.txt",
            "nonroot/Z.txt"
        });
        FileObject x = fs.findResource ("root/sub");
        
        FileObject fo = fs.findResource ("root");
        FileObject tg = fs.getRoot().createFolder ("target");
        
        java.util.HashSet set = new java.util.HashSet ();
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("Nothing copied", 0, tg.getChildren().length);
    }
    
    public void testDoNotOverwriteFiles () throws Exception {
        java.util.HashSet set = new java.util.HashSet ();
        set.add ("X.txt");
        
        FileSystem fs = createLocalFileSystem (new String[] {
            "root/project/X.txt", 
            "root/X.txt",
            "nonroot/Z.txt"
        });
        
        writeTo (fs, "root/project/X.txt", "content-project");
        writeTo (fs, "root/X.txt", "content-global");

        FileObject tg = fs.getRoot().createFolder ("target");
        
        FileObject project = fs.findResource ("root/project");
        Copy.copyDeep (project, tg, set);
        
        
        
        FileObject root = fs.findResource ("root");
        Copy.copyDeep (root, tg, set);

        
        FileObject x = tg.getFileObject ("X.txt");
        assertNotNull ("File copied", x);
        
        byte[] arr = new byte[300];
        int len = x.getInputStream ().read (arr);
        String content = new String (arr, 0, len);
        
        assertEquals ("The content is kept from project", content, "content-project");
    }
    
    public void testDoesCopyHiddenFiles () throws Exception {
        String[] res = {
            "root/Yes.txt", 
            "root/X.txt_hidden", 
        };
        LocalFileSystem fs = createLocalFileSystem (res);
        URL url = getClass().getResource("layer5.5.xml");
        assertNotNull("found sample layer", url);
        XMLFileSystem xfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = AutoUpgrade.createLayeredSystem(fs, xfs); 
        
        FileObject fo = mfs.findResource ("root");
        
        FileSystem original = FileUtil.createMemoryFileSystem();
        
        MultiFileSystem tgfs = new MultiFileSystem(new FileSystem[] { fs, original });
        FileObject tg = tgfs.getRoot().createFolder ("target");
        FileObject toBeHidden = FileUtil.createData(original.getRoot(), "target/X.txt");
        
        assertEquals ("One file is there", 1, tg.getChildren().length);
        assertEquals ("X.txt", tg.getChildren()[0].getNameExt());
        
        
        HashSet set = new HashSet ();
        set.add ("Yes.txt");
        set.add ("X.txt_hidden");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals ("After the copy there is still one file", 1, tg.getChildren().length);
        assertEquals ("but the file is Yes.txt, as X.txt is hidden by txt_hidden", "Yes.txt", tg.getChildren()[0].getNameExt());
    }
    
    private static void writeTo (FileSystem fs, String res, String content) throws java.io.IOException {
        FileObject fo = org.openide.filesystems.FileUtil.createData (fs.getRoot (), res);
        org.openide.filesystems.FileLock lock = fo.lock ();
        java.io.OutputStream os = fo.getOutputStream (lock);
        os.write (content.getBytes ());
        os.close ();
        lock.releaseLock ();
    }
    
    public LocalFileSystem createLocalFileSystem(String[] resources) throws IOException {
        File mountPoint = new File(getWorkDir(), "tmpfs"); 
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (mountPoint,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException iex) {
                    throw new IOException ("While creating " + resources[i] + " in " + mountPoint.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath() + " with resource list: " + Arrays.asList(resources));
                }
            }
        }
        
        LocalFileSystem lfs = new LocalFileSystem();
        try {
            lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }
 }
