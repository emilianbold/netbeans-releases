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


package org.openide.loaders;

import java.util.logging.Level;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Tests for internals of FolderList as there seems to be some
 * inherent problems.
 *
 * @author Jaroslav Tulach
 */
public class FolderListTest extends NbTestCase {
    private FileObject folder;
    private FolderList list;
    
    
    public FolderListTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        
        folder = FileUtil.createFolder(lfs.getRoot(), "folder");

        FileUtil.createData(folder, "A.txt");
        FileUtil.createData(folder, "B.txt");
        FileUtil.createData(folder, "C.txt");
        
        list = FolderList.find(folder, true);
    }

    protected void tearDown() throws Exception {
    }

    public void testComputeChildrenList() throws Exception {
        class L implements FolderListListener {
            private int cnt;
            private boolean finished;
            
            public void process(DataObject obj, List arr) {
                cnt++;
            }

            public void finished(List arr) {
                assertTrue(arr.isEmpty());
                finished = true;
            }
        }
        
        L listener = new L();       
        RequestProcessor.Task t = list.computeChildrenList(listener);
        t.waitFinished();
        
        assertEquals("Three files", 3, listener.cnt);
        assertTrue("finished", listener.finished);
    }
    
}
