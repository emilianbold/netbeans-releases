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
package org.netbeans.modules.tasklist.impl;

import java.util.Collections;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner.Callback;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class TaskManagerImplTest extends NbTestCase {
    
    public TaskManagerImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workDir);
        
        file1 = workDir.createData("file1.txt");
        super.setUp();
    }

    private FileObject file1;
    
    /**IZ #100463
     */
    public void testProviderCanStartImmediatelly() throws Exception {
        final PushTaskScanner scanner = new PushTaskScanner("", "", null) {
            public void setScope(TaskScanningScope scope, Callback callback) {
                callback.setTasks(file1, Collections.singletonList(Task.create(file1, "unknown", "x", 2)));
            }
        };
        
        TaskManagerImpl impl = new TaskManagerImpl() {
            @Override
            Iterable<? extends FileTaskScanner> getFileScanners() {
                return Collections.<FileTaskScanner>emptyList();
            }

            @Override
            Iterable<? extends PushTaskScanner> getPushScanners() {
                return Collections.singletonList(scanner);
            }
        };
        
        impl.observe(new TaskScanningScope("", "", null) {
            public boolean isInScope(FileObject resource) {
                return (resource == file1);
            }
            
            public void attach(Callback callback) {
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(file1);
            }
            
            public Iterator<FileObject> iterator() {
                return Collections.singletonList(file1).iterator();
            }
        }, TaskFilter.EMPTY);

        impl.waitFinished();
        
        assertEquals(1, impl.getTasks().getTasks().size());
    }

    public void testProviderCanRemoveTasks() throws Exception {
        final Callback[] cb = new Callback[1];
        final PushTaskScanner scanner = new PushTaskScanner("", "", null) {
            public void setScope(TaskScanningScope scope, Callback callback) {
                callback.setTasks(file1, Collections.singletonList(Task.create(file1, "unknown", "x", 2)));
                cb[0] = callback;
            }
        };
        
        TaskManagerImpl impl = new TaskManagerImpl() {
            @Override
            Iterable<? extends FileTaskScanner> getFileScanners() {
                return Collections.<FileTaskScanner>emptyList();
            }
            
            @Override
            Iterable<? extends PushTaskScanner> getPushScanners() {
                return Collections.singletonList(scanner);
            }
        };
        
        impl.observe(new TaskScanningScope("", "", null) {
            public boolean isInScope(FileObject resource) {
                return (resource == file1);
            }
            
            public void attach(Callback callback) {
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(file1);
            }
            
            public Iterator<FileObject> iterator() {
                return Collections.singletonList(file1).iterator();
            }
        }, TaskFilter.EMPTY);
        
        impl.waitFinished();
        
        assertEquals(1, impl.getTasks().getTasks().size());
        
        cb[0].setTasks(file1, Collections.<Task>emptyList());
        
        assertTrue(impl.getTasks().getTasks().isEmpty());
    }
    
}
