/*
   *                 Sun Public License Notice
   * 
   * The contents of this file are subject to the Sun Public License
   * Version 1.0 (the "License"). You may not use this file except in
   * compliance with the License. A copy of the License is available at
   * http://www.sun.com/
   * 
   * The Original Code is NetBeans. The Initial Developer of the Original
   * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
   * Microsystems, Inc. All Rights Reserved.
   */

package org.openide.filesystems;


import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.junit.*;
import org.openide.util.Task;

/**
 * Simulate deadlock from issue 50137.
 *
 * There is expected not to call any foreign code from 
 * StreamPool.
 *
 * @author Radek Matous
 */
public class StreamPool50137Test extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private FileSystem lfs;
    private FileObject testFo;

    public StreamPool50137Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(StreamPool50137Test.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = new TestFileSystem ((LocalFileSystem)TestUtilHid.createLocalFileSystem(getName(), new String[]{"TestDeadlock" }));        
        testFo = lfs.findResource("TestDeadlock");
        assertNotNull(testFo);        
    }


    public void testStreamPoolDeadlock () throws Exception {
        FileLock fLock = testFo.lock();
        OutputStream os = null;
        InputStream is = null;        
        
        try {
            os = testFo.getOutputStream(fLock);
            os.close(); os = null;
            is = testFo.getInputStream();
            is.close(); is = null;            
        } finally {
            if (fLock != null) fLock.releaseLock();
            if (os != null) os.close();
            if (is != null) is.close();            
        }
    }
    
    private static final class TestFileSystem extends LocalFileSystem {
        TestFileSystem (LocalFileSystem lfs) throws Exception {
            super ();             
            this.info = new TestImplInfo (this);
            setRootDirectory(lfs.getRootDirectory());
        }                            
    }

    private static final class TestImplInfo extends LocalFileSystem.Impl  {
        private final TestFileSystem tfs;
        TestImplInfo (TestFileSystem tfs) {
            super (tfs);
            this.tfs = tfs;
            
        }
        
        public OutputStream outputStream(String name) throws java.io.IOException {
            OutputStream retValue = super.outputStream(name);
            deadlockSimulation ();            
            return retValue;
        }

        public InputStream inputStream(String name) throws java.io.FileNotFoundException {
            InputStream  retValue = super.inputStream(name);
            deadlockSimulation ();
            return retValue;
        }                        
        
        
        private void deadlockSimulation() {
            Task task = org.openide.util.RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    StreamPool.find(tfs);
                }                
            });
            task.waitFinished();                        
        }

    }

}
  
  
  