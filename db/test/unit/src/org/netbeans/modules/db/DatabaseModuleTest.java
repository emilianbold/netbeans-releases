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

package org.netbeans.modules.db;

import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseModuleTest extends TestBase {

    // TODO should also test that connections are disconnected

    // TODO should also test that no errors are only logged to ErrorManager with EM.notify(INFORMATIONAL, e)
    
    public DatabaseModuleTest(String testName) {
        super(testName);
    }

    public void testRuntimesAreStopped() throws Exception {
        FileObject runtimeFolder = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject("Databases/Runtimes");
        FileObject runtime1 = FileUtil.createData(runtimeFolder, "runtime1.instance");
        runtime1.setAttribute("instanceOf", DatabaseRuntime.class.getName());
        runtime1.setAttribute("instanceCreate", new DatabaseRuntimeImpl());
        FileObject runtime2 = FileUtil.createData(runtimeFolder, "runtime2.instance");
        runtime2.setAttribute("instanceOf", DatabaseRuntime.class.getName());
        runtime2.setAttribute("instanceCreate", new DatabaseRuntimeImpl());
        
        new DatabaseModule().close();
        
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
        int checked = 0;
        for (int i = 0; i < runtimes.length; i++) {
            if (runtimes[i] instanceof DatabaseRuntimeImpl) {
                assertTrue(((DatabaseRuntimeImpl)runtimes[i]).stopped);
                checked++;
            }
        }
        // check we have really tested our DatabaseRuntime implementations
        assertTrue(checked == 2);
    }
    
    static final class DatabaseRuntimeImpl implements DatabaseRuntime {
        
        boolean stopped;
        
        public boolean acceptsDatabaseURL(String url) {
            return true;
        }

        public void stop() {
            stopped = true;
        }

        public void start() {
        }

        public boolean isRunning() {
            return true;
        }

        public String getJDBCDriverClass() {
            return null;
        }

        public boolean canStart() {
            return true;
        }
    }
}
