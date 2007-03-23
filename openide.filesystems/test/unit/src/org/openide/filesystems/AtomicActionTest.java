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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 * Test proving embeding of AtomicActions
 * @author Radek Matous
 */
public class AtomicActionTest extends NbTestCase {
    
    public AtomicActionTest(String name) {
        super(name);
    }
    
    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testBasic() throws Exception {
        File f = getWorkDir();
        final LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(f);                
        //------------------------
        FileObject root = lfs.getRoot();
        assertNotNull(FileUtil.createData(root, "data"));        
        final FileObject data = root.getFileObject("data");
        assertNotNull(data);
        
        final TestChangeListener tcl = new TestChangeListener();
        assertFalse(tcl.deleteNotification);
        root.addFileChangeListener(tcl);
        assertFalse(tcl.deleteNotification);
        lfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                lfs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        lfs.runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                data.delete();
                                assertFalse(tcl.deleteNotification);
                            }
                        });                        
                        assertFalse(tcl.deleteNotification);
                    }
                });
                assertFalse(tcl.deleteNotification);
            }
        });
        assertTrue(tcl.deleteNotification);
        tcl.reset();
        assertNotNull(FileUtil.createData(root, "data"));        

        final FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        final JarFileSystem jfs = new JarFileSystem();
        
        sfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                jfs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        lfs.runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                data.delete();
                                assertFalse(tcl.deleteNotification);
                            }
                        });                        
                        assertFalse(tcl.deleteNotification);
                    }
                });
                assertFalse(tcl.deleteNotification);
            }
        });
        assertTrue(tcl.deleteNotification);
    }
    
    private static class TestChangeListener extends FileChangeAdapter {
        private boolean deleteNotification;
        @Override
        public void fileDeleted(FileEvent fe) {
            deleteNotification = true;
        }
        public void reset() {
            deleteNotification = false;
        }
    }    
}
