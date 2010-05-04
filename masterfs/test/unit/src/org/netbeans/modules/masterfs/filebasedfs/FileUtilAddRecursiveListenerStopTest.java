/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FileUtilAddRecursiveListenerStopTest extends NbTestCase
implements Callable<Boolean>, FileChangeListener {
    private FileObject root;
    private List<FileEvent> events = new ArrayList<FileEvent>();
    private int cnt;

    public FileUtilAddRecursiveListenerStopTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("Root found", root);

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                root.createData("" + i, "txt");
            } else {
                root.createFolder("" + i);
            }
        }
    }

    public void testAddListenerGetsFiveCallbacks() throws IOException {
        cnt = 10;
        FileUtil.addRecursiveListener(this, getWorkDir(), this);
        assertEquals("Counter decreased five times to five", 5, cnt);

        FileObject fourth = root.getFileObject("5");
        assertNotNull("Folder found", fourth);
        fourth.createData("Ahoj");
        assertEquals("One event delivered: " + events, 1, events.size());
    }

    public void testAddListenerCanStop() throws IOException {
        cnt = 2;
        CharSequence log = Log.enable("org.netbeans.modules.masterfs", Level.INFO);
        FileUtil.addRecursiveListener(this, getWorkDir(), this);
        assertEquals("Counter is zero", 0, cnt);
        if (!log.toString().contains("addRecursiveListener")) {
            fail("There shall be info about interruption:\n" + log);
        }

        FileObject fourth = root.getFileObject("5");
        assertNotNull("Folder found", fourth);
        fourth.createData("Ahoj");
        assertTrue("No events delivered: " + events, events.isEmpty());
    }

    @Override
    public Boolean call() throws Exception {
        if (--cnt == 0) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        events.add(fe);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        events.add(fe);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        events.add(fe);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        events.add(fe);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        events.add(fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        events.add(fe);
    }

}
