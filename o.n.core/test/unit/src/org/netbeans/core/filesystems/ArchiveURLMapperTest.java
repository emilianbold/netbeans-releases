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

package org.netbeans.core.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
/**
 *
 * @author  tomas zezula
 */
public class ArchiveURLMapperTest extends NbTestCase {
    
    private static final String RESOURCE = "test.txt"; //NOI18N
    private static final String JAR_FILE = "test.jar";          //NOI18N
    
    public ArchiveURLMapperTest(String testName) {
        super(testName);
    }
    
    private URL createJarFile () throws IOException {
	File workDir = FileUtil.normalizeFile(this.getWorkDir());
        File jarFile = new File(workDir,JAR_FILE);
        JarOutputStream out = new JarOutputStream ( new FileOutputStream (jarFile));
        ZipEntry entry = new ZipEntry (RESOURCE);        
        out.putNextEntry(entry);
        out.write (RESOURCE.getBytes());
        out.close();
        return jarFile.toURI().toURL();
    }
    
    private boolean removeJarFile () {
        try {
            File workDir = FileUtil.normalizeFile(this.getWorkDir());
            File tmp = new File (workDir,JAR_FILE);
            tmp.delete();
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }
    
    private FileSystem mountFs () throws Exception {    
        File f = FileUtil.normalizeFile(this.getWorkDir());
        String parentName;
        while ((parentName=f.getParent())!=null) {
            f = new File (parentName);
        }
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory(f);
        assertTrue (lfs!=null);
        Repository.getDefault().addFileSystem(lfs);
        return lfs;
    }
    
    private void umountFs (FileSystem fs) {
        Repository.getDefault().removeFileSystem (fs);
    }
    
    
    public void testURLMapper () throws Exception {
        URL jarFileURL = createJarFile ();
        FileSystem fs = mountFs();
        assertTrue (jarFileURL != null);
        URL url = new URL (MessageFormat.format("jar:{0}!/{1}", new Object[] {jarFileURL.toExternalForm(),  //NOI18N
            RESOURCE}));
        FileObject[] fos = URLMapper.findFileObjects(url);
        assertEquals ("There is one found file object", 1, fos.length);
        assertTrue (fos[0].getPath().equals(RESOURCE));
        URL newUrl = URLMapper.findURL(fos[0], URLMapper.EXTERNAL);
        assertEquals(url, newUrl);
        removeJarFile ();
        umountFs(fs);
    }

	public void testArchiveToRootURL () throws Exception {
		URL jarFileURL = createJarFile ();
		assertTrue (jarFileURL != null);
                assertTrue (FileUtil.isArchiveFile(jarFileURL));
                URL jarRootURL = FileUtil.getArchiveRoot(jarFileURL);
                assertTrue ("jar".equals(jarRootURL.getProtocol()));        //NOI18N
                String path = jarRootURL.getPath();
                int index = path.lastIndexOf ("!/");                        //NOI18N
                assertTrue (index==path.length()-2);
                URL innerURL = new URL(path.substring(0,index));
                assertTrue (innerURL.equals(jarFileURL));
		removeJarFile ();
	}
        
        
        public void testArchiveToRootFileObject () throws Exception {
            FileSystem fs = mountFs ();
            URL jarFileURL = createJarFile ();
            FileObject fo = URLMapper.findFileObject(jarFileURL);
            assertTrue (fo != null);   
            assertTrue (FileUtil.isArchiveFile(fo));
            FileObject rootFo = FileUtil.getArchiveRoot (fo);
            assertTrue (rootFo!=null);
            assertTrue ("".equals(rootFo.getPath()));   //NOI18N
            assertTrue (rootFo.getFileSystem() instanceof JarFileSystem);
            File jarFile = ((JarFileSystem)rootFo.getFileSystem()).getJarFile();
            assertTrue (jarFileURL.equals(jarFile.toURI().toURL()));
            removeJarFile ();
            umountFs (fs);
        }
    
}
