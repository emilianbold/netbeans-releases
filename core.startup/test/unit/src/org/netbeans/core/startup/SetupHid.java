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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.CRC32;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** Some infrastructure for module system tests.
 * @author Jesse Glick
 */
public abstract class SetupHid extends NbTestCase {

    public SetupHid(String name) {
        super(name);
    }

    /** directory of data files for JARs */
    protected File data;
    /** directory full of JAR files to test */
    protected File jars;

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        createTestJARs();
        
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        
        System.setProperty("netbeans.user", ud.getPath());
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
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

    @Deprecated
    protected static void copyStreams(InputStream is, OutputStream os) throws IOException {
        FileUtil.copy(is, os);
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

    public static class FakeModuleInstaller extends ModuleInstaller {
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

    public static final class FakeEvents extends org.netbeans.Events {
        protected void logged(String message, Object[] args) {
            // do nothing
            // XXX is it better to test events or the installer??
        }
    }

    // XXX use MockPropertyChangeListener instead
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

    private void createTestJARs() throws IOException {
        File simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);
        createTestJAR("dep-on-dep-on-simple", null, simpleModule, dependsOnSimpleModule);
        File cyclic1;
        { // cyclic-1
            File cyclic1Src = new File(data, "cyclic-1");
            File cyclic2Src = new File(data, "cyclic-2");
            compile(Arrays.asList(
                    "-sourcepath", cyclic1Src + File.pathSeparator + cyclic2Src,
                    "-d", cyclic1Src.getAbsolutePath()),
                    findSourceFiles(cyclic1Src, cyclic2Src));
            cyclic1 = new File(jars, "cyclic-1.jar");
            OutputStream os = new FileOutputStream(cyclic1);
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(new File(data, "cyclic-1.mf")));
                try {
                    jarUp(jos, new File(cyclic1Src, "org/foo"), "org/foo/");
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
        }
        File cyclic2 = createTestJAR("cyclic-2", null, cyclic1);
        createTestJAR("depends-on-cyclic-1", null, cyclic1, cyclic2);
        File libraryUndecl = createTestJAR("library-undecl", "library-src");
        createTestJAR("library-unvers", "library-src");
        createTestJAR("library-vers", "library-src");
        createTestJAR("library-vers-partial", "library-src");
        createTestJAR("depends-on-lib-undecl", "depends-on-library-src", libraryUndecl);
        createTestJAR("depends-on-lib-unvers", "depends-on-library-src", libraryUndecl);
        createTestJAR("depends-on-lib-vers", "depends-on-library-src", libraryUndecl);
        createTestJAR("depends-on-lib-vers-partial", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-lib-undecl", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-non-existing-package", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-lib-unvers", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-lib-old", "depends-on-library-src", libraryUndecl);
        createTestJAR("prov-foo", null);
        createTestJAR("req-foo", null);
        createTestJAR("prov-foo-bar", null);
        createTestJAR("req-foo-baz", null);
        createTestJAR("prov-baz", null);
        createTestJAR("prov-foo-req-bar", null);
        createTestJAR("prov-bar-req-foo", null);
        createTestJAR("prov-bar-dep-cyclic", null);
        createTestJAR("rel-ver-2", null);
        createTestJAR("dep-on-relvertest-1", null);
        createTestJAR("dep-on-relvertest-1-2", null);
        createTestJAR("dep-on-relvertest-1-2-nospec", null);
        createTestJAR("dep-on-relvertest-2", null);
        createTestJAR("dep-on-relvertest-2-3", null);
        createTestJAR("dep-on-relvertest-2-3-late", null);
        createTestJAR("dep-on-relvertest-2-impl", null);
        createTestJAR("dep-on-relvertest-2-impl-wrong", null);
        createTestJAR("dep-on-relvertest-2-late", null);
        createTestJAR("dep-on-relvertest-3-4", null);
        createTestJAR("dep-on-relvertest-some", null);
        createTestJAR("depends-on-simple-module-2", null);
        createTestJAR("needs-foo", null);
        createTestJAR("recommends-foo", null);
        createTestJAR("prov-foo-depends-needs_foo", "prov-foo");
        createTestJAR("api-mod-export-all", "exposes-api");
        createTestJAR("api-mod-export-none", "exposes-api");
        File exposesAPI = createTestJAR("api-mod-export-api", "exposes-api");
        createTestJAR("api-mod-export-friend", "exposes-api");
        createTestJAR("uses-api-simple-dep", "uses-api", exposesAPI);
        createTestJAR("uses-api-impl-dep", "uses-api", exposesAPI);
        createTestJAR("uses-api-impl-dep-for-friends", "uses-api", exposesAPI);
        createTestJAR("uses-api-spec-dep", "uses-api", exposesAPI);
        createTestJAR("dep-on-two-modules", null);
        File usesAPI = createTestJAR("uses-and-exports-api", "uses-api", exposesAPI);
        createTestJAR("uses-api-transitively", null, exposesAPI, usesAPI);
        createTestJAR("uses-api-directly", "uses-api-transitively", exposesAPI, usesAPI);
        createTestJAR("uses-api-transitively-old", "uses-api-transitively", exposesAPI, usesAPI);
        createTestJAR("uses-api-directly-old", "uses-api-transitively", exposesAPI, usesAPI);
        createTestJAR("look-for-myself", null);
        createTestJAR("uses-api-friend", "uses-api", exposesAPI);
        createTestJAR("little-manifest", null);
        createTestJAR("medium-manifest", null);
        createTestJAR("big-manifest", null);
        createTestJAR("patchable", null);
        { // Make the patch JAR specially:
            File src = new File(data, "patch");
            String srcS = src.getAbsolutePath();
            compile(Arrays.asList("-sourcepath", srcS, "-d", srcS), findSourceFiles(src));
            File jar = new File(jars, "patches/pkg-subpkg/some-patch.jar");
            jar.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(jar);
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    jarUp(jos, src, "");
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
        }
        File locale = new File(jars, "locale");
        locale.mkdirs();
        {
            OutputStream os = new FileOutputStream(new File(jars, "localized-manifest.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(new File(data, "localized-manifest.mf")));
                try {
                    writeJarEntry(jos, "locmani/Bundle.properties", new File(data, "localized-manifest/locmani/Bundle.properties"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            os = new FileOutputStream(new File(locale, "localized-manifest_cs.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    writeJarEntry(jos, "locmani/Bundle_cs.properties", new File(data, "localized-manifest/locmani/Bundle_cs.properties"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
        }
        {
            OutputStream os = new FileOutputStream(new File(jars, "base-layer-mod.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(new File(data, "base-layer-mod.mf")));
                try {
                    writeJarEntry(jos, "baselayer/layer.xml", new File(data, "base-layer-mod/baselayer/layer.xml"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            os = new FileOutputStream(new File(locale, "base-layer-mod_cs.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    writeJarEntry(jos, "baselayer/layer_cs.xml", new File(data, "base-layer-mod/baselayer/layer_cs.xml"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            os = new FileOutputStream(new File(locale, "base-layer-mod_foo.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    writeJarEntry(jos, "baselayer/layer_foo.xml", new File(data, "base-layer-mod/baselayer/layer_foo.xml"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            createTestJAR("override-layer-mod", null);
        }
    }
    private static void compile(List<String> options, Iterable<File> files) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager mgr = compiler.getStandardFileManager(null, null, null);
        List<String> fullOptions = new ArrayList<String>(options);
        fullOptions.addAll(Arrays.asList("-source", "1.5", "-target", "1.5"));
        if (!compiler.getTask(null, mgr, null, fullOptions, null, mgr.getJavaFileObjectsFromFiles(files)).call()) {
            throw new IOException("compilation failed");
        }
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    public static File createTestJAR(File data, File jars, String name, String srcdir, File... classpath) throws IOException {
        File srcdirF = null;
        File d = new File(data, srcdir != null ? srcdir : name);
        if (d.isDirectory()) {
            srcdirF = d;
        }
        File manifestF = null;
        File f = new File(data, name + ".mf");
        if (f.isFile()) {
            manifestF = f;
        }
        if (srcdirF != null) {
            assert srcdirF.isDirectory();
            List<File> sourceFiles = findSourceFiles(srcdirF);
            if (!sourceFiles.isEmpty()) {
                StringBuilder cp = new StringBuilder(System.getProperty("java.class.path")); // o.o.util, o.o.modules
                for (File j : classpath) {
                    cp.append(File.pathSeparatorChar);
                    cp.append(j);
                }
                compile(Arrays.asList(
                        "-classpath", cp.toString(),
                        "-sourcepath", srcdirF.getAbsolutePath(),
                        "-d", srcdirF.getAbsolutePath()),
                        sourceFiles);
            }
        }
        // Cannot trivially use TestFileUtils.writeZipFile here since we have binary content (classes).
        File jar = new File(jars, name + ".jar");
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os, loadManifest(manifestF));
            try {
                if (srcdirF != null) {
                    jarUp(jos, srcdirF, "");
                }
            } finally {
                jos.close();
            }
        } finally {
            os.close();
        }
        return jar;
    }
    private static Manifest loadManifest(File mani) throws IOException {
        Manifest m = new Manifest();
        if (mani != null) {
            InputStream is = new FileInputStream(mani);
            try {
                m.read(is);
            } finally {
                is.close();
            }
        }
        m.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        return m;
    }
    private static List<File> findSourceFiles(File... roots) {
        List<File> sourceFiles = new ArrayList<File>();
        for (File root : roots) {
            doFindSourceFiles(sourceFiles, root);
        }
        return sourceFiles;
    }
    private static void doFindSourceFiles(List<File> sourceFiles, File srcdir) {
        for (File k : srcdir.listFiles()) {
            if (k.getName().endsWith(".java")) {
                sourceFiles.add(k);
            } else if (k.isDirectory()) {
                doFindSourceFiles(sourceFiles, k);
            }
        }
    }
    private static void jarUp(JarOutputStream jos, File dir, String prefix) throws IOException {
        for (File f : dir.listFiles()) {
            String path = prefix + f.getName();
            if (f.getName().endsWith(".java")) {
                continue;
            } else if (f.isDirectory()) {
                jarUp(jos, f, path + "/");
            } else {
                writeJarEntry(jos, path, f);
            }
        }
    }
    private static void writeJarEntry(JarOutputStream jos, String path, File f) throws IOException, FileNotFoundException {
        JarEntry je = new JarEntry(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new FileInputStream(f);
        try {
            FileUtil.copy(is, baos);
        } finally {
            is.close();
        }
        byte[] data = baos.toByteArray();
        je.setSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        je.setCrc(crc.getValue());
        jos.putNextEntry(je);
        jos.write(data);
    }

}
