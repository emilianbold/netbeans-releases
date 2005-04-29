/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;

/**
 * @author  rm111737
 */
public class JarFileSystemTest extends FileSystemFactoryHid {
     JarFileSystem jfs;
    /** Creates new JarFileSystemTest */
    public JarFileSystemTest(Test test) {
        super(test);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }


    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);                
        suite.addTestSuite(FileSystemTestHid.class);        
        suite.addTestSuite(FileObjectTestHid.class);
        /*failing tests*/
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(URLMapperTestInternalHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                        
        
        return new JarFileSystemTest(suite);
    }
    protected void destroyFileSystem (String testName) throws IOException {}
    
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException{
        File jar = TestUtilHid.locationOfTempFolder("jfstest");
        jar.mkdir();
        
        File f = new File (jar,"jfstest.jar");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream (new FileOutputStream (f));        
        
        for (int i = 0; i < resources.length; i++) {
            String entryName = resources[i];
            if (entryName.startsWith("/")) entryName = entryName.substring(1);             
            jos.putNextEntry(new ZipEntry (entryName));
        }
        
        
       jos.close();        
        
        jfs = new JarFileSystem  ();
        try {
            jfs.setJarFile(f);
        } catch (Exception ex) {}
                
        return new FileSystem[] {jfs};
    }
}
