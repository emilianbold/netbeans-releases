/*
 * FSTest.java
 *
 * Created on July 15, 2002, 3:59 PM
 */

package FileSystemTest;

import java.beans.PropertyVetoException;
import java.io.*;
import java.util.Iterator;
import java.util.Set;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFilter;
import org.openide.loaders.RepositoryNodeFactory;

import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

/**
 *
 * @author  pz97949
 */
public class FSTest extends NbTestCase {
    File testDir;
    LocalFileSystem testFS;
    String TEST_FILE_OBJECT_NAME = "testedfile.java";
    static class FailException extends Exception {
        FailException () {
            super ();
        }
        FailException (String msg) {
            super(msg);
        }
    }
    
    static class ErrorException extends Exception {
        ErrorException() {
            super();
        }
        ErrorException(String name) {
            super(name);
        }
    }
    
    /** Creates a new instance of FSTest */
    public FSTest(String testName) {
        super(testName);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
     /**This suite*/
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(FSTest.class);
        return suite;
    }
     /** Test if filesystem is hiden or shown in repository 
      * (test is done through nodes of explorer)
      */
     public void testHide () throws IOException {
         Node node = null;
         FileObject fo = null;
         FileSystem fs = null;
         try {
           try {  
//             TopManager tm = TopManager.getDefault();

            // get node of dataObject 

             fo = getRefFO();
             fs = fo.getFileSystem();
             fs.setHidden(false);
             System.out.println(fs);
             failTest("file system is hidden", fs.isHidden() ==false);   
             DataObject dobj = DataObject.find(fo);
             node = dobj.getNodeDelegate(); 
           } catch (DataObjectNotFoundException donfe ) {
               failTest (donfe);
           } catch (FileStateInvalidException fsie ) {
               failTest (fsie);
           }
           Node repNode = RepositoryNodeFactory.getDefault().repository(DataFilter.ALL);
             fs.setHidden(false);
//                     System.out.println("status:" + ((fs.isHidden()) ? "hidden" : "shown"));
             if (findNode(repNode,node,2) == false ) {
                 failTest("filesystem is hidden (must be shown)", false);
             } 
             fs.setHidden(true);     
             if (findNode(repNode,node,2) == true ) {
                 failTest("filesystem is shown (must be hidden)", false);
             }
             fs.setHidden(false);    
             if (findNode(repNode,node,2) == false ) {
                 failTest("filesystem is hidden (must be shown)", false);
             } 
         } catch (FailException fe) {
             System.out.println("failed");
         }
         System.out.println("passed");  
     }
      
     /** @return unique fileobject in tested filesystem 
       */
     protected FileObject getRefFO() throws java.io.IOException {
         Repository rep = Repository.getDefault();
         FileObject fo = rep.findResource(TEST_FILE_OBJECT_NAME);
         if (fo == null) {
             fo = testFS.getRoot().createData(TEST_FILE_OBJECT_NAME) ;
         }
        return fo;
     }
     
     /**
      *  
      */
     private void failTest(Exception e) throws FailException {
         e.printStackTrace ();
         throw new FailException ();
         
     }
     
     private void failTest(String str, boolean status) throws FailException {
         if (status == false ) {
            System.out.println(str);
            throw new FailException();
         }
         
     }
     
     private boolean findNode(Node parent, Node child, int depth) {
         if (parent.getName().equals(child.getName()) ) {
             return true;
         }
         if (depth == 0 ) {
             return false;
         }
         Children children = parent.getChildren();
         Node childNodes[] = children.getNodes();
         for (int i = 0 ; i < childNodes.length ; i++ ) {
             if (childNodes[i] == child || findNode(childNodes[i],child, depth - 1 ) ) {
                return true;
             }
       //      System.out.println(childNodes[i].getName());
         }
         return false;    
     }
             
     private void sleepCurThread(int milis) {
         try {
             Thread.currentThread().sleep(milis);
         } catch (InterruptedException exception) {
             exception.printStackTrace();
         }
     }
             
     /**
      *    test if filesystem is readonly 
      */
     public void testReadOnLocalFS() throws IOException {
         try {
             FileObject fo = getRefFO();
             FileSystem fs = null;
             try {
                 fs = fo.getFileSystem();
             }catch (FileStateInvalidException fse) { 
                 fse.printStackTrace();
                 throw new FailException("fo.getFileSystem() failed");
             }
             if (! (fs instanceof LocalFileSystem))  {
                 throw new ErrorException("tested filesystem is not instance of LocalFileSystem");
             }             
             LocalFileSystem lfs =(LocalFileSystem )fs;
             boolean tmpROValue = lfs.isReadOnly();
             // test read only
             lfs.setReadOnly(true );
             FileObject root =  fs.getRoot();
             
             // test create file object
             try {
                 root.createData("MyNewFile.txt");
                 throw new FailException ("Create FileObject on read only filesystem failed.");
             } catch (IOException ioe) {
                 System.out.println("Create FileObject on read only filesystem file passed.");
             }
             //test create folder 
             try {
                 root.createFolder("MyNewFolder");
                 throw new FailException ("Create folder on read only filesytem failed.");
             } catch (IOException ioe) {
                 System.out.println("Create folder on read only filesystem passed");
             }
             
             // tet write data into file object
             FileLock lock = null;
             try {

                 lock = fo.lock();
                 OutputStream os = fo.getOutputStream(lock);
                 PrintStream ps = new PrintStream(os);
                 ps.println("import ahoj;");
                 lock.releaseLock();
                 throw new FailException("write to file object on read only filesystem failed.");
             } catch (IOException ioe) {
                 System.out.println("write to file object on read only filesystem passed.");
             }
             
             
             // test on readonly = false
             //
             lfs.setReadOnly(false);
             
             // test create FileObject
             try {
                 FileObject fo2 = root.createData("MyNewFile.txt");
                 fo2.delete();
                 System.out.println("Create FileObject on read/write filesystem passed.");
             } catch (IOException ioe) {
                 ioe.printStackTrace();
                 throw new FSTest.FailException("Create FileObject on read/write filesystem file failed.");
             }
             //test create folder 
             try {
                 FileObject folder = root.createFolder("MyNewFolder");
                 folder.delete();
                System.out.println("Create folder on read only filesytem passed.");
             } catch (IOException ioe) {
                 ioe.printStackTrace();
                 throw new FSTest.FailException("Create folder on read/write  filesystem failed.");
             }
             
             // tet write data into file object
            lock = fo.lock();
            try {
                 OutputStream os = fo.getOutputStream(lock);
                 PrintStream ps = new PrintStream(os);
                 ps.println("import ahoj;");
                 System.out.println("write to file object on read/write  filesystem passed.");
                 lock.releaseLock();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
                 lock.releaseLock();      
                 throw new FSTest.FailException("write to file object on read/write  filesystem failed.");
             }
         } catch (FailException fe) {
             fe.printStackTrace();
             assertTrue(fe.getMessage(), false);
             
         } catch (ErrorException ee) {
             ee.printStackTrace();
             assertTrue(ee.getMessage(),false);
         }
     }
     protected void setUp() throws IOException, PropertyVetoException {
         testDir = new File(File.createTempFile("ssadfasdfsadf","6346436").getParentFile(),"fgsagkjasdhgksa");
       //  System.out.println("testDir = " + testDir );         
         if (testDir.mkdir() == false && testDir.isDirectory() == false) {
             throw new IOException ("Error, temporary directory is not created");
         }
         testFS =  new LocalFileSystem();
         testFS.setRootDirectory(testDir);
         FileSystem [] fss = Repository.getDefault().toArray();
         for (int i = 0 ; i < fss.length ; i++ ) {
             FileSystem fs = fss[i];
             if (fs instanceof LocalFileSystem && ((LocalFileSystem) fs).getRootDirectory().equals(testDir)) {
                 // filesystem is allready mounted 
                 testFS = (LocalFileSystem) fs;
                 return;
             }
         }
         FileObject root = testFS.getRoot();
         
         
         Repository.getDefault().addFileSystem(testFS);
     }
     
 }