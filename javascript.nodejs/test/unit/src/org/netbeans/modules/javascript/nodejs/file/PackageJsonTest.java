/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class PackageJsonTest extends NbTestCase {

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    private FileObject projectDir;
    private PackageJson packageJson;


    public PackageJsonTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File dummy = new File(getWorkDir(), "dummy");
        assertTrue(dummy.mkdir());
        projectDir = FileUtil.toFileObject(dummy);
        assertNotNull(projectDir);
        packageJson = new PackageJson(new DummyProject(projectDir));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        packageJson.cleanup();
    }

    public void testNoPackageJson() {
        assertFalse(packageJson.exists());
        assertEquals(getFile().getAbsolutePath(), packageJson.getPath());
    }

    public void testPackageJson() throws Exception {
        writeFile(getData(true, false));
        assertTrue(packageJson.exists());
        assertEquals(getFile().getAbsolutePath(), packageJson.getPath());
    }

    public void testNameChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        asyncWriteFile(getData(true, false));
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change name
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(true, false);
        newData.put(PackageJson.NAME, "YourProject");
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_NAME);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("MyProject", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertEquals("MyProject", event.getOldValue());
        assertEquals("YourProject", event.getNewValue());
    }

    public void testStartFileChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        asyncWriteFile(getData(false, true));
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change start file
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(false, true);
        ((Map<String, Object>) newData.get(PackageJson.SCRIPTS)).put(PackageJson.START, "node app.js --port 2080");
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(1, allEvents.size());
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_SCRIPTS_START);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("node server.js", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertEquals("node server.js", event.getOldValue());
        assertEquals("node app.js --port 2080", event.getNewValue());
    }

    public void testFileChange() throws Exception {
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        listener.setCountDownLatch(countDownLatch1);
        packageJson.addPropertyChangeListener(listener);
        asyncWriteFile(getData(true, true));
        // wait
        countDownLatch1.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change name & start file
        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch2);
        Map<String, Object> newData = getData(true, true);
        newData.put(PackageJson.NAME, "YourProject");
        ((Map<String, Object>) newData.get(PackageJson.SCRIPTS)).put(PackageJson.START, "node app.js --port 2080");
        asyncWriteFile(newData);
        // wait
        countDownLatch2.await(1, TimeUnit.MINUTES);
        // needed for FS to notice the change
        Thread.sleep(1000);
        // change start file only
        CountDownLatch countDownLatch3 = new CountDownLatch(1);
        listener.setCountDownLatch(countDownLatch3);
        Map<String, Object> newerData = new HashMap<>(newData);
        ((Map<String, Object>) newerData.get(PackageJson.SCRIPTS)).put(PackageJson.START, "node app.js");
        asyncWriteFile(newerData);
        // wait
        countDownLatch3.await(1, TimeUnit.MINUTES);
        // check events
        Map<String, List<PropertyChangeEvent>> allEvents = listener.getAllEvents();
        assertEquals(2, allEvents.size());
        // name
        List<PropertyChangeEvent> events = allEvents.get(PackageJson.PROP_NAME);
        assertNotNull(events);
        assertEquals(2, events.size());
        PropertyChangeEvent event = events.get(0);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("MyProject", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_NAME, event.getPropertyName());
        assertEquals("MyProject", event.getOldValue());
        assertEquals("YourProject", event.getNewValue());
        // start file
        events = allEvents.get(PackageJson.PROP_SCRIPTS_START);
        assertNotNull(events);
        assertEquals(3, events.size());
        event = events.get(0);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("node server.js", event.getNewValue());
        event = events.get(1);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertEquals("node server.js", event.getOldValue());
        assertEquals("node app.js --port 2080", event.getNewValue());
        event = events.get(2);
        assertEquals(PackageJson.PROP_SCRIPTS_START, event.getPropertyName());
        assertEquals("node app.js --port 2080", event.getOldValue());
        assertEquals("node app.js", event.getNewValue());
    }

    private File getFile() {
        return new File(FileUtil.toFile(projectDir), PackageJson.FILENAME);
    }

    private Map<String, Object> getData(boolean name, boolean startFile) {
        Map<String, Object> data = new HashMap<>();
        if (name) {
            data.put(PackageJson.NAME, "MyProject");
        }
        if (startFile) {
            Map<String, Object> scripts = new HashMap<>();
            scripts.put(PackageJson.START, "node server.js");
            data.put(PackageJson.SCRIPTS, scripts);
        }
        return data;
    }

    private void writeFile(Map<String, Object> data) throws IOException {
        File file = getFile();
        try (Writer out = new FileWriter(file)) {
            JSONObject.writeJSONString(data, out);
        }
        assertTrue(file.isFile());
        FileUtil.refreshFor(file.getParentFile());
    }

    private void asyncWriteFile(Map<String, Object> data) {
        final Map<String, Object> synchronizedData = Collections.synchronizedMap(data);
        EXECUTORS.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    writeFile(synchronizedData);
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        });
    }

    //~ Inner classes

    private static final class DummyProject implements Project {

        private final FileObject projectDir;


        public DummyProject(FileObject projectDir) {
            Assert.assertNotNull(projectDir);
            this.projectDir = projectDir;
        }

        @Override
        public FileObject getProjectDirectory() {
            return projectDir;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

    }

    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {

        private final Map<String, List<PropertyChangeEvent>> allEvents = new HashMap<>();
        private volatile CountDownLatch countDownLatch;


        public void setCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            Assert.assertNotNull(evt);
            String propertyName = evt.getPropertyName();
            Assert.assertNotNull(propertyName);
            List<PropertyChangeEvent> events = allEvents.get(propertyName);
            if (events == null) {
                events = new ArrayList<>();
                allEvents.put(propertyName, events);
            }
            events.add(evt);
            countDownLatch.countDown();
        }

        public Map<String, List<PropertyChangeEvent>> getAllEvents() {
            return new HashMap<>(allEvents);
        }

    }

}
