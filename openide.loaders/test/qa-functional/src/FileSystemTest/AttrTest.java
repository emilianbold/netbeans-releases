/*
 * AttrTest.java
 *
 * Created on August 15, 2002, 4:09 PM
 */

package FileSystemTest;

import java.beans.PropertyVetoException;
import java.io.*;
import java.util.Enumeration;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.*;

/**
 *
 * @author  pz97949
 */
public class AttrTest extends NbTestCase {
//    File fileSystemFile;
    File fileSystemDir;
    LocalFileSystem fileSystem;
     public AttrTest(String testName) {
        super(testName);
    }
   
     /** tests set/get attribute to fileobject special named: 
      *  "\"
      * see to bug http://installer.netbeans.org/issues/show_bug.cgi?id=8976
      */
    public void testSpecialNamedAttr() throws IOException,PropertyVetoException {
            preprocess();
            FileObject fo = getAnyFileObject() ; 
            setAttribute(fo,"\"", "1");
            setAttribute(fo,"h&", "2");
            setAttribute(fo,"<","3");
            setAttribute(fo,">","4");
            setAttribute(fo,"-", "5");
            setAttribute(fo,"*","6");
            System.gc();

            getAttribute(fo,"\"","1");
            getAttribute(fo,"h&","2");
            getAttribute(fo,"<","3");
            getAttribute(fo,">","4");
            getAttribute(fo,"-","5");
            getAttribute(fo,"*","6");
            postprocess();
    }
 
    /** set attribute to FileObject 
     */ 
    private void setAttribute(FileObject fo,String name,String value) {
        try {
          fo.setAttribute(name, value);
          log ("attribute (name = " + name + ", value = " + value + ") setted" );
        } catch (Exception e) {
            String msg = "failed on set attribute name = " + name + " , value = " + value;
            log (msg); 
            assertTrue(msg,false);
        }
    }
    /** read attribude from fileobject and tests if is correct
     */
    private String  getAttribute(FileObject fo,String name, String refValue) {
        String value = (String) fo.getAttribute(name);
        if (value == null ) {
            assertTrue("File object doesn't contain attribute (name = " + name + ", value = " + value + " ",false);
        } else {
            if (!value.equals(refValue)) {
                assertTrue("FileObject read wrong attr value ( name = " + name + 
                      ",correct value = " + refValue + " , read value = " + value, false );
            }
        }
        return value;
    }
    /** it mounts LocalFileSystem in temorary directory
     */
    private void preprocess() throws IOException,PropertyVetoException {
//        fileSystemFile.mkdir();
        fileSystemDir = new File(getWorkDir(), "testAtt123rDir");
        if(fileSystemDir.mkdir() == false || fileSystemDir.isDirectory() == false) {
            throw new IOException (fileSystemDir.toString() + " is not directory");
        }
        Enumeration en = Repository.getDefault().fileSystems();
        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem) en.nextElement();
            if (fs instanceof LocalFileSystem) {
                LocalFileSystem lfs = (LocalFileSystem) fs;
                if (lfs.getRootDirectory().equals(fileSystemDir)) {
                    fileSystem = lfs;
                    break;
                }
            }
        } 
        if (fileSystem == null ) {
            fileSystem = new LocalFileSystem();
            fileSystem.setRootDirectory(fileSystemDir);
            Repository.getDefault().addFileSystem(fileSystem);
        }
        
    }
        
    /** unmount temporary filesystem
     **/    
    private void postprocess() {
        Repository.getDefault().getDefault().removeFileSystem(fileSystem);
        fileSystemDir.deleteOnExit();
      //  fileSystemFile.deleteOnExit();
    }
        
    private FileObject getAnyFileObject() {
        return fileSystem.getRoot();
    }
   /**This suite*/
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(AttrTest.class);
        return suite;
    }
    
    /** test set "\\" attr value, see to :  8977 in Issuezila
     */
    public void testSetBackslashValue() throws IOException,  PropertyVetoException {
        preprocess();
        FileObject fo = getAnyFileObject();
        try {
             setAttribute(fo, "\\", "2");   
             getAttribute(fo, "\\",  "2");
        } catch(Exception e) {
            assertTrue(" failed:no  attribute setted " + e,false );
        }
            
       
        postprocess();
    }
    
    
      /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    
}
