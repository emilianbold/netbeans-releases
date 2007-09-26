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

package unit.org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author  pz97949
 */
public class FilesystemBugs extends NbTestCase {
    final String FOLDER1 = "F";
final String FO1     ="testNFOADelete";
    private int counter;
    /** Creates a new instance of FilesystemBugs */
    public FilesystemBugs(String name) {
        super(name);
    }
    /**
    *        Let's have a file hierarchy A/B/C. If you decide to delete the folder A then listeners registered to 
    *       file objects B and C do not have any notion these file objects were deleted. IMO it is incorrect 
    *    behavior.
    *    It also hurts the org.netbeans.core.xml.FileEntityResolver.update implementation.
   */

    public void testNotifyOfSubFoldersAfterDelete23929 () throws IOException {
        counter = 0;
        if (canGenWriteFolder()) {
            // create tree an register listener
            //
           FileObject folder = getWriteFolder();
           log(folder.toString());
           folder = getSubFolder(folder, FOLDER1);
           FileObject tmpFolder = folder;
           System.out.println(folder);
           for (int i = 0 ; i  <  10 ; i++ ) {
               FileObject subFolder = getSubFolder(tmpFolder,FOLDER1 + i);
               subFolder.addFileChangeListener(new TestFileChangeListener());
               for (int j = 0 ; j < 10 ; j++ ) {
                   FileObject fo = getFileObject(tmpFolder, FO1 + j);
                   fo.addFileChangeListener(new TestFileChangeListener());
               }
               tmpFolder = subFolder;
           }
           // delete tree and check counts of calls (must be 209)
           //
           try {
              folder.delete();
               assertTrue ("test failed, deleted  " + counter + " != 209"  , counter == 209);
           } catch (Exception e) {
               assertTrue("cannot delete folder",false);
           }
           
        } else {
             log ("[OK]  cannot get write folder on " + getFSType());
        }
    }
    /** get/create subfolder in folder
     */
    protected FileObject getSubFolder(FileObject folder, String name) {
        try {
            FileObject fo = folder.getFileObject(name);
            if (fo == null) {
                return folder.createFolder(name);
            }
            return fo;
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("cannot get subFolder " + name + " in  " + folder.toString(),false);
        }
        return null;
    }
    /** get/create FilObject in folder 
     */
    protected FileObject getFileObject(FileObject folder, String name) {
        try {
            FileObject fo = folder.getFileObject(name);
            if (fo == null) {
                return folder.createData(name);
            }
            return fo;
        } catch (Exception e) {
            assertTrue("cannot get subFolder " + name + " in  " + folder.toString(),false);
        }
        return null;
    }
    
    private class TestFileChangeListener extends FileChangeAdapter {
        public void fileDeleted(FileEvent ev) {
            log("Delete: " + ev.getFile().getPath());
            counter++;
        }
    }
       
        
    
    protected boolean canGenWriteFolder() {
        return true;
    }
    protected FileObject getWriteFolder() throws IOException {   
        String folderName = "unit/org/openide/filesystems/";
        FileObject folder =  Repository.getDefault().findResource(folderName);
        assertNotNull("folder : " + folder + " doesn't exists.",folder);
        FileObject myFolder = folder.getFileObject("data");
        if (myFolder == null) {
           myFolder = folder.createFolder("data");
        }
        return folder;
    }
    
    protected String getFSType() {
        return "LocalFileSystem";
    }

     /** #8124 When attributes are deleted from file objects, so that no attributes remain set
      *  on any file objects in a folder, the .nbattrs file should be deleted. When no
      *   attributes remain on a particular file object, that <fileobject> tag should be
      *   deleted from the .nbattrs even if others remain.
      */
     public void testDeleteAttrsFileAfterDeleteAttrib() throws IOException {
         FileObject folder  = getWriteFolder();
         FileObject fo = folder.getFileObject("testAttr");
         if (fo == null) {
            fo = folder.createData("testAttr");
         }
         // set any attribute
         fo.setAttribute("blbost","blbost");
         // flush
         System.gc();
         System.gc();
         // delete all attributes
         FileObject fos[] = folder.getChildren();
         for (int i = 0 ; i < fos.length ; i++ ) {
             Enumeration keys = fos[i].getAttributes();
             while (keys.hasMoreElements()) {
                 fos[i].setAttribute((String)keys.nextElement(),null);
             }
         }
         // flush
         System.gc();
         System.gc();
         // test if exists .nbattrs
         File nbattrs = new File(FileUtil.toFile(folder),".nbattrs");
         assertTrue("Empty nbattrs exists in folder:" + FileUtil.toFile(folder) , nbattrs.exists() == false);
         
         
     }
/**#30397 FileURL.encodeFileObject(FileSystem fs, FileObject
fo) breakes the public contract of java.net.URL by
setting the hostname as null value. Even if the
URL constructor implementation permits passing
null value the javadoc does not. It has fatal
impact on comparisons of URL objects then (see
URLStreamHandler.hostsEqual). Following code
always fails*/

     public void testURLContract() throws IOException {
         FileObject fo = getWriteFolder();
          URL u = fo.getURL();
         assertEquals(u, new URL(u.toExternalForm()));
     }
/** #10507    After implementation of issue 18220 it has been
realized that pluggable mapping is also needed
from URL to FileObject, I suggest to implement it
by adding methods:

static FileObject findFileObject (URL url);
abstract FileObject getFileObject (URL url);

both able to return null. Please do it now before
the URLMapper appears in the 3.4 release.
*/

     public void testFindFOfromURL() throws IOException {
         FileObject fo = getWriteFolder();
          URL u = fo.getURL();
          URLMapper urlMapper = (URLMapper) Lookup.getDefault().lookup(URLMapper.class);
          FileObject fos[] = urlMapper.findFileObjects(u);
          assertTrue("url " + u + " must to only one FileObject",fos.length==1);
          assertTrue("The references must be equal.", fo == fos[0]);
          
     }
     /* http://installer.netbeans.org/issues/show_bug.cgi?id=26400
      *If I have a multifilesystem and set an attribute
    on one of its fileobjects, I will get strange new
    attributes on all parent folders of this fileobject.

    Example will make it clear:

    If I set attribute "version" on fileobject fileA
    which lies in folders /folder1/folder2, then if
    you ask for all attributes on folder2, you will
    get an attribute with name "fileA\version".
    Similarly on folder1 you will get attribute
    "folder2\fileA\version". If I look into .nbattrs
    file there is just one attribute with name
    "folder1\folder2\fileA\version".
      */
    public void testMultiAttrsBug26400() throws Exception {
            File f1,f2;
            File dir = new File(File.createTempFile("fsdf","eew").getParentFile(),"aret");
            dir.mkdirs();
            assertTrue(dir.isDirectory());
            f1 = new File(dir,"tm26400a");
            f2 = new File(dir,"tm26400b");
            f1.mkdir();
            f2.mkdir();
            LocalFileSystem lfs1 = new LocalFileSystem();
            LocalFileSystem lfs2 = new LocalFileSystem();
            lfs1.setRootDirectory(f1);
            lfs2.setRootDirectory(f2);
            ///
          
            MultiFileSystem mfs = new MultiFileSystem(new FileSystem[]{lfs1,lfs2});
            FileObject rootMfs = mfs.getRoot();
        //    FileObject fomc = rootMfs.createData("c");
            FileObject folder = getSubFolder(rootMfs,"a");
            FileObject fo = getFileObject(folder, "b");
            fo.setAttribute("attr","value");
            assertTrue("folder contains attribute",  folder.getAttributes().hasMoreElements() == false);
            assertTrue("FileObject doesn't contain attribute attr.",   fo.getAttribute("attr").equals("value"));
    }
/* 
 URL returned must be terminated by "/" if fileobject represents folder.
Othervise it violated URL specs and it causes troubles while contructing contexted URL:

  new URL(folder.getURL(), "test.txt");

is now always searched in parent folder.
 */


    public void testFolderSlashUrl () throws Exception {
          URL u = getWriteFolder().getURL();
          assertTrue("invalid url of directory",u.getPath().endsWith("/"));
    }
     
  ////////////////////////   
     
      public void testOpenJarManifestAsResource()  throws Exception {
        Repository repo = Repository.getDefault ();
        JarFileSystem jfs = new JarFileSystem ();
        File nbHome = new File(System.getProperty("netbeans.home"));
        File jarName = new File(new File (nbHome,"lib"), "openide.jar");
        jfs.setJarFile (jarName);
        repo.addFileSystem (jfs);
        repo.removeFileSystem (jfs);
        //try {
            //FileObject fo = jfs.findResource ("META-INF/MANIFEST.MF");
            FileObject fo = jfs.findResource ("org/openide/filesystems/Repository.class");
            assertTrue("FileObject must to be valid",   fo.isValid ());
            try {
              assertNotNull("FileObject must to have InputStream (fo.getInputStream ()");
            } catch (Exception e) {
                e.printStackTrace(getLog());
                fail("Exception " + e);
            }
                
        //} finally {
        //    repo.removeFileSystem (jfs);
        //}
    
    }
    public void testBackSlashAttribute33459() throws IOException {
        FileObject fo = getWriteFolder();
        String attribName = "y\\u2dasfas";
        System.gc();
        System.gc();
        try {
           fo.setAttribute(attribName,attribName);
           System.gc();
           System.gc();
           assertTrue("Attribute is not equal", fo.getAttribute(attribName).equals(attribName));
           System.out.println("ok");
        } catch (Exception e) {
            log(e.toString());
            e.printStackTrace();
            fail("Exception :" + e ) ;
        }
    }
    
     public static NbTestSuite suite() {
         NbTestSuite suite = new NbTestSuite();
         suite.addTest(new FilesystemBugs("testDeleteAttrsFileAfterDeleteAttrib"));
         suite.addTest(new FilesystemBugs("testMultiAttrsBug26400"));
         suite.addTest(new FilesystemBugs("testNotifyOfSubFoldersAfterDelete23929"));
         suite.addTest(new FilesystemBugs("testURLContract"));
         suite.addTest(new FilesystemBugs("testFindFOfromURL"));
         suite.addTest(new FilesystemBugs("testOpenJarManifestAsResource"));
         suite.addTest(new FilesystemBugs("testFolderSlashUrl"));
         suite.addTest(new FilesystemBugs("testBackSlashAttribute33459"));
         return suite;
     }
    
     public  void testMultiLayers19725() throws Exception {
         XMLFileSystem origFs = new XMLFileSystem(FilesystemBugs.class.getResource("layer1.xml"));
         XMLFileSystem brandedFs = new XMLFileSystem(FilesystemBugs.class.getResource("layer2.xml"));
         MultiFileSystem mfs = new MultiFileSystem(new FileSystem[]{brandedFs,origFs});
         FileObject fo = mfs.findResource("myfolder");
         log("file1/file2 = " +  fo.getAttribute("file1/file2").toString());
         log("file2/file1 = " + fo.getAttribute("file2/file1").toString());
         assertTrue(fo.getAttribute("file1/file2").equals(Boolean.FALSE));
         assertTrue(fo.getAttribute("file2/file1").equals(Boolean.TRUE));
     }
     
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new FilesystemBugs("testMultiLayers19725"));
    }
    
}
