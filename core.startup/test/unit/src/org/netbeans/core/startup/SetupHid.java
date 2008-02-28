/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.startup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.Repository;

/** Some infrastructure for module system tests.
 * @author Jesse Glick
 */
abstract class SetupHid extends NbTestCase {

    public SetupHid(String name) {
        super(name);
    }

    /** directory full of JAR files to test */
    protected File jars;

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        jars = new File(ModuleManagerTest.class.getResource("jars").getFile());
        clearWorkDir();
        
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        
        System.setProperty("netbeans.user", ud.getPath());
    }

    protected static void deleteRec(File f) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) throw new IOException("Could not list: " + f);
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i]);
            }
        }
        if (! f.delete()) throw new IOException("Could not delete: " + f);
    }

    protected static void copyStreams(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        try {
            int i;
            while ((i = is.read(buf)) != -1) {
                os.write(buf, 0, i);
            }
        } finally {
            is.close();
        }
    }

    protected static void copy(File a, File b) throws IOException {
        OutputStream os = new FileOutputStream(b);
        try {
            copyStreams(new FileInputStream(a), os);
        } finally {
            os.close();
        }
    }

    protected static void copy(File a, FileObject b) throws IOException {
        OutputStream os = b.getOutputStream();
        try {
            copyStreams(new FileInputStream(a), os);
        } finally {
            os.close();
        }
    }

    protected static String slurp(String path) throws IOException {
        Main.getModuleSystem(); // #26451
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(path);
        if (fo == null) return null;
        InputStream is = fo.getInputStream();
        StringBuffer text = new StringBuffer((int)fo.getSize());
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            text.append(new String(buf, 0, read, "US-ASCII"));
        }
        return text.toString();
    }

    protected static class FakeModuleInstaller extends ModuleInstaller {
        // For examining results of what happened:
        public final List<String> actions = new ArrayList<String>();
        public final List<Object> args = new ArrayList<Object>();
        public void clear() {
            actions.clear();
            args.clear();
        }
        // For adding invalid modules:
        public final Set<Module> delinquents = new HashSet<Module>();
        // For adding modules that don't want to close:
        public final Set<Module> wontclose = new HashSet<Module>();
        public void prepare(Module m) throws InvalidException {
            if (delinquents.contains(m)) throw new InvalidException(m, "not supposed to be installed");
            actions.add("prepare");
            args.add(m);
        }
        public void dispose(Module m) {
            actions.add("dispose");
            args.add(m);
        }
        public void load(List<Module> modules) {
            actions.add("load");
            args.add(new ArrayList<Module>(modules));
        }
        public void unload(List<Module> modules) {
            actions.add("unload");
            args.add(new ArrayList<Module>(modules));
        }
        public boolean closing(List<Module> modules) {
            actions.add("closing");
            args.add(new ArrayList<Module>(modules));
            Iterator<Module> it = modules.iterator();
            while (it.hasNext()) {
                if (wontclose.contains(it.next())) return false;
            }
            return true;
        }
        public void close(List<Module> modules) {
            actions.add("close");
            args.add(new ArrayList<Module>(modules));
        }
    }

    protected static final class FakeEvents extends org.netbeans.Events {
        protected void logged(String message, Object[] args) {
            // do nothing
            // XXX is it better to test events or the installer??
        }
    }

    protected static final class LoggedPCListener implements PropertyChangeListener {
        private final Set<PropertyChangeEvent> changes = new HashSet<PropertyChangeEvent>(100);
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            changes.add(evt);
            notify();
        }
        public synchronized void waitForChanges() throws InterruptedException {
            wait(5000);
        }
        public synchronized boolean hasChange(Object source, String prop) {
            for (PropertyChangeEvent ev : changes) {
                if (source == ev.getSource ()) {
                    if (prop.equals (ev.getPropertyName ())) {
                        return true;
                    }
                }
            }
            return false;
        }
        public synchronized boolean waitForChange(Object source, String prop) throws InterruptedException {
            while (! hasChange(source, prop)) {
                long start = System.currentTimeMillis();
                waitForChanges();
                if (System.currentTimeMillis() - start > 4000) {
                    //System.err.println("changes=" + changes);
                    return false;
                }
            }
            return true;
        }
    }

    protected static class LoggedFileListener implements FileChangeListener {
        /** names of files that have changed: */
        private final Set<String> files = new HashSet<String>(100);
        private synchronized void change(FileEvent ev) {
            files.add(ev.getFile().getPath());
            notify();
        }
        public synchronized void waitForChanges() throws InterruptedException {
            wait(5000);
        }
        public synchronized boolean hasChange(String fname) {
            return files.contains(fname);
        }
        public synchronized boolean waitForChange(String fname) throws InterruptedException {
            while (! hasChange(fname)) {
                long start = System.currentTimeMillis();
                waitForChanges();
                if (System.currentTimeMillis() - start > 4000) {
                    //System.err.println("changes=" + changes);
                    return false;
                }
            }
            return true;
        }
        public void fileDeleted(FileEvent fe) {
            change(fe);
        }
        public void fileFolderCreated(FileEvent fe) {
            change(fe);
        }
        public void fileDataCreated(FileEvent fe) {
            change(fe);
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore?
        }
        public void fileRenamed(FileRenameEvent fe) {
            change(fe);
        }
        public void fileChanged(FileEvent fe) {
            change(fe);
        }
    }

    /**
     * Create a fresh JAR file.
     * @param jar the file to create
     * @param contents keys are JAR entry paths, values are text contents (will be written in UTF-8)
     * @param manifest a manifest to store (key/value pairs for main section)
     */
    public static void createJar(File jar, Map<String,String> contents, Map<String,String> manifest) throws IOException {
        // XXX use TestFileUtils.writeZipFile
        Manifest m = new Manifest();
        m.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        for (Map.Entry<String,String> line : manifest.entrySet()) {
            m.getMainAttributes().putValue(line.getKey(), line.getValue());
        }
        jar.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os, m);
            Iterator it = contents.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String path = (String) entry.getKey();
                byte[] data = ((String) entry.getValue()).getBytes("UTF-8");
                JarEntry je = new JarEntry(path);
                je.setSize(data.length);
                CRC32 crc = new CRC32();
                crc.update(data);
                je.setCrc(crc.getValue());
                jos.putNextEntry(je);
                jos.write(data);
            }
            jos.close();
        } finally {
            os.close();
        }
    }

}
