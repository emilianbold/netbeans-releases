/*
 * AttrTest.java
 *
 * Created on August 15, 2002, 4:09 PM
 */

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

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
        clearWorkDir();
        fileSystemDir = new File(getWorkDir(), "testAtt123rDir");
        if(fileSystemDir.mkdir() == false || fileSystemDir.isDirectory() == false) {
            throw new IOException (fileSystemDir.toString() + " is not directory");
        }
        fileSystem = new LocalFileSystem();
        fileSystem.setRootDirectory(fileSystemDir);
    }
        
    private FileObject getAnyFileObject() {
        return fileSystem.getRoot();
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
    }
    
}
