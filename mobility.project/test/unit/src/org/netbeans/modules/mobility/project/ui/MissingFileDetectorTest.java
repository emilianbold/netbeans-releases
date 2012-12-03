/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mobility.project.ui;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tim Boudreau
 */
public class MissingFileDetectorTest extends NbTestCase {

    public MissingFileDetectorTest(String name) {
        super (name);
    }

    static {
        MissingFileDetector.unitTest = true;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private File dir;
    private File root;
    private File subdir;
    private File exists;
    private File doesntExistDir;
    private File doesntExistFile;
    private File sanity;
    private File sanityParent;
    private File sanityAncestor;
    private static Random r = new Random (System.currentTimeMillis());
    @Before
    @Override
    public void setUp() throws Exception {
        MissingFileDetector.unitTest = true;
        MissingFileDetector.INTERVAL = 10;
        root = FileUtil.normalizeFile(new File (System.getProperty("java.io.tmpdir")));
        while (dir == null || (dir != null && dir.exists())) {
            dir = new File (root, "" + (System.currentTimeMillis() * r.nextLong()));
        }
        assertTrue (dir.mkdirs());
        String pth = dir.getPath();
        dir = FileUtil.normalizeFile (dir);
        assertNotNull ("Could not normalize " + pth);
        subdir = new File (dir, "subdir" + File.separator + "subdir");
        if (!subdir.mkdirs()) {
            fail ("Could not create " + subdir.getPath());
        }
        exists = new File (subdir, "somefile");
        createExistsFile();
        doesntExistDir = new File (dir, "nothing" + File.separator + "nowhere");
        doesntExistFile = new File (doesntExistDir, "nofile");
        sanityAncestor = new File (dir, "sanity");
        sanityParent = new File (sanityAncestor, "sanity");
        assertTrue (sanityParent.mkdirs());
        sanity = new File (sanityParent, "sanityFile");
        assertFalse (sanity.exists());
    }

    private void createFile (File file) throws Exception {
        assertNotNull (file);
        assertFalse (file.exists());
        assertTrue (file.createNewFile());
        writeToFile (file, "Hello");
    }

    private void createExistsFile() throws Exception {
        createFile (exists);
    }

    private void createDoesntExistFile() throws Exception {
        if (!doesntExistDir.exists()) {
            if (!doesntExistDir.mkdirs()) {
                fail ("Could not create " + doesntExistDir.getPath());
            }
        }
        createFile (doesntExistFile);
    }

    private void writeToFile (File file, String message) throws Exception {
        FileOutputStream out = new FileOutputStream (file);
        out.write(message.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private void writeToExistsFile (String message) throws Exception {
        writeToFile (exists, message);
    }

    @After
    @Override
    public void tearDown() {
        if (dir.exists()) {
            dir.delete();
        }
        MissingFileDetector.reset();
    }

    @Test
    public void testRemoval() throws Exception {
        System.out.println("testRemoval");
        FM monitor = new FM();
        MissingFileDetector.register(doesntExistFile, monitor);
        createDoesntExistFile();
        monitor.awaitNotification();
        monitor.assertCreated(doesntExistFile.getPath() + " created but no " +
                "notification received.  doesntExistFile.exists() returns " +
                doesntExistFile.exists());
        assertTrue (MissingFileDetector.INSTANCE.active);
        MissingFileDetector.unregister(doesntExistFile, monitor);
        assertTrue(doesntExistFile.delete());
        monitor.awaitNotification();
        monitor.assertNotDeleted("Monitor notified even though it was removed");
        assertFalse (MissingFileDetector.INSTANCE.active);
        WeakReference ref = new WeakReference (monitor);
        monitor = null;
        assertGC("FileMonitor not garbage collected", ref);
    }

    @Test
    public void testLifecycle() throws Exception {
        System.out.println("testLifecycle");
        FM monitor = new FM();
        MissingFileDetector.register(doesntExistFile, monitor);
        createDoesntExistFile();
        monitor.awaitNotification();
        monitor.assertCreated(doesntExistFile.getPath() + " created but no " +
                "notification received");
        assertTrue (MissingFileDetector.INSTANCE.active);
        WeakReference ref = new WeakReference (monitor);
        monitor = null;
        assertGC("FileMonitor not garbage collected", ref);
        assertFalse (MissingFileDetector.INSTANCE.active);
    }

    @Test
    public void testMonitorNonexistentFile() throws Exception {
        System.out.println("testMonitorNonexistentFile");
        FM monitor = new FM();
        MissingFileDetector.register(doesntExistFile, monitor);
        createDoesntExistFile();
        monitor.awaitNotification();
        monitor.assertCreated(doesntExistFile.getPath() + " created but no " +
                "notification received");
        assertTrue (doesntExistFile.delete());
        Thread.yield();
        monitor.awaitNotification();
        monitor.assertDeleted(doesntExistFile.getPath() + " deleted but " +
                "notification receieved");
        createDoesntExistFile();
        monitor.awaitNotification();
        monitor.assertCreated(doesntExistFile.getPath() + " created but " +
                "no notification received");
        writeToFile (doesntExistFile, "Goodbye");
        monitor.awaitNotification();
        monitor.assertChanged("Wrote to " + doesntExistFile.getPath() + " but " +
                "no change notification received");
    }

    @Test
    public void testMonitorExistingFile() throws Exception {
        System.out.println("testMonitorExistingFile");
        FM monitor = new FM();
        FM monitor2 = new FM();
        MissingFileDetector.register(exists, monitor);
        MissingFileDetector.register(exists, monitor2);
        monitor.assertNotChanged();
        monitor.assertNotCreated();
        monitor.assertNotDeleted();
        monitor2.assertNotChanged();
        monitor2.assertNotCreated();
        monitor2.assertNotDeleted();

        exists.delete();

        assertFalse (exists.exists());
        monitor.awaitNotification();
        monitor.assertDeleted("Deleted " + exists.getPath() + " but no notification received");
        monitor2.awaitNotification();
        monitor2.assertDeleted("Deleted " + exists.getPath() + " but no notification received");
        createExistsFile();
        monitor.awaitNotification();
        monitor2.awaitNotification();

        monitor.assertCreated(exists.getPath() + " created but no notification " +
                "received");
        monitor2.assertCreated(exists.getPath() + " created but no notification " +
                "received");

        writeToExistsFile ("Goodbye");
        monitor.awaitNotification();
        monitor2.awaitNotification();
        monitor.assertChanged("No notification of change to " + exists.getPath());
        monitor2.assertChanged("No notification of change to " + exists.getPath());
    }

    private static final class FM implements FileMonitor {
        private boolean changed;
        private boolean created;
        private boolean deleted;

        private void awaitNotification() {
            if (changed || created || deleted) return;
            Thread.yield();
            synchronized (this) {
                try {
                    wait(4000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException (ex);
                }
            }
        }

        private void log (String msg) {
        }

        public void fileCreated() {
            created = true;
            log ("Created");
            synchronized(this) {
                notifyAll();
            }
        }

        public void fileDeleted() {
            deleted = true;
            log ("Deleted");
            synchronized(this) {
                notifyAll();
            }
        }

        public void fileChanged() {
            changed = true;
            log ("Changed");
            synchronized(this) {
                notifyAll();
            }
        }

        public void clear() {
            changed = false;
            deleted = false;
            created = false;
            synchronized(this) {
                notifyAll();
            }
        }

        public void assertChanged() {
            boolean old = changed;
            changed = false;
            assertTrue (old);
        }

        public void assertNotChanged() {
            boolean old = changed;
            changed = false;
            assertFalse (old);
        }

        public void assertCreated() {
            boolean old = created;
            created = false;
            assertTrue (old);
        }

        public void assertNotCreated() {
            boolean old = created;
            created = false;
            assertFalse (old);
        }

        public void assertDeleted() {
            boolean old = deleted;
            deleted = false;
            assertTrue (old);
        }

        public void assertNotDeleted() {
            boolean old = deleted;
            deleted = false;
            assertFalse (old);
        }

        public void assertChanged(String message) {
            boolean old = changed;
            changed = false;
            assertTrue(message, old);
        }

        public void assertNotChanged(String message) {
            boolean old = changed;
            changed = false;
            assertFalse(message, old);
        }

        public void assertCreated(String message) {
            boolean old = created;
            created = false;
            assertTrue(message, old);
        }

        public void assertNotCreated(String message) {
            boolean old = created;
            created = false;
            assertFalse(message, old);
        }

        public void assertDeleted(String message) {
            boolean old = deleted;
            deleted = false;
            assertTrue(message, old);
        }

        public void assertNotDeleted(String message) {
            boolean old = deleted;
            deleted = false;
            assertFalse(message, old);
        }
    }
}
