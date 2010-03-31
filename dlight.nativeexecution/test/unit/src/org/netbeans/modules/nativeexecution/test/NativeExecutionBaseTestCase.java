/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class NativeExecutionBaseTestCase extends NbTestCase {

    protected static class TestLogHandler extends Handler {

        protected final Logger log;

        public TestLogHandler(Logger log) {
            this.log = log;
        }
        
        @Override
        public void publish(LogRecord record) {
            // Log if parent cannot log the message ONLY.
            if (!log.getParent().isLoggable(record.getLevel())) {
                String message;
                Object[] params = record.getParameters();
                if (params == null || params.length == 0) {
                    message = record.getMessage();
                } else {
                    message =  MessageFormat.format(record.getMessage(), record.getParameters());
                }
                System.err.printf("%s: %s\n", record.getLevel(), message); // NOI18N
                if (record.getThrown() != null) {
                    record.getThrown().printStackTrace(System.err);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    static {
        final Logger log = Logger.getLogger("nativeexecution.support"); // NOI18N
        log.setLevel(Level.ALL);
        log.addHandler(new TestLogHandler(log));
    }

    private final ExecutionEnvironment testExecutionEnvironment;
    private String remoteTmpDir;

    public NativeExecutionBaseTestCase(String name) {
        super(name);
        System.setProperty("nativeexecution.mode.unittest", "true");
        testExecutionEnvironment = null;
        setupUserDir();
    }

    /**
     * A special constructor for use with NativeExecutionBaseTestSuite
     * @param name
     * @param testExecutionEnvironment
     */
    /*protected - feel free to make it public in the case you REALLY need this */
    protected NativeExecutionBaseTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name);
        System.setProperty("nativeexecution.mode.unittest", "true");
        this.testExecutionEnvironment = testExecutionEnvironment;
        assertNotNull(testExecutionEnvironment);
        setupUserDir();
    }

    @Override
    protected void setUp() throws Exception {
        setupProperties();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void setupUserDir() {
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage").setLevel(Level.SEVERE);
        File userDir = getUserDir();
        userDir.mkdirs();
        System.setProperty("netbeans.user", userDir.getAbsolutePath());
    }

    protected File getUserDir() {
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage").setLevel(Level.SEVERE);
        File dataDir = getDataDir();
        File dataDirParent = dataDir.getParentFile();
        File userDir = new File(dataDirParent, "userdir");
        return userDir;
    }

    private void setupProperties() throws IOException, FormatException {
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String section = getClass().getSimpleName() + ".properties";
        Collection<String> keys = rcFile.getKeys(section);
        for (String key : keys) {
            String value = rcFile.get(section, key);
            System.setProperty(key, value);
        }
    }    

    @Override
    protected int timeOut() {
        return 500000;
    }

    /**
     * Gets execution environment this test was created with.
     * @return
     */
    protected ExecutionEnvironment getTestExecutionEnvironment() {
        return testExecutionEnvironment;
    }


    protected String getTestHostName() {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        return (env == null) ? null : env.getHost();
    }

    @Override
    public String getName() {
        String name = super.getName();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env == null) {
            return name;
        } else {
            return String.format("%s [%s]", name, env);
        }
    }

    public static void writeFile(File file, CharSequence content) throws IOException {
        Writer writer = new FileWriter(file);
        writer.write(content.toString());
        writer.close();
    }

    public String readFile(File file) throws IOException {
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rdr.readLine()) != null) {
            sb.append(line + "\n");
        }
        rdr.close();
        return sb.toString();
    }

    /**
     * Removes directory recursively
     * @param dir directory  to remove
     * @return true in the case the directory was removed sucessfully, otherwise false
     */
    public static boolean removeDirectory(File dir) {
        return removeDirectory(dir, true);
    }

    /**
     * Removes directory content (recursively)
     * @param dir directory  to remove
     * @return true in the case the directory content was removed sucessfully, otherwise false
     */
    public static boolean removeDirectoryContent(File dir) {
        return removeDirectory(dir, false);
    }

    /**
     * Removes directory recursively
     * @param dir directory  to remove
     * @return true in the case the directory was removed sucessfully, otherwise false
     */
    private static boolean removeDirectory(File dir, boolean removeItself) {
        boolean success = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                if (!removeDirectory(new File(dir, children[i]), true)) {
                    success = false;
                }
            }
        }
        if (success && removeItself) {
            success = dir.delete();
        }
        return success;
    }
    public static File createTempFile(String prefix, String suffix, boolean directory) throws IOException {
        File tmpFile = File.createTempFile(prefix, suffix);
        if (directory) {
            if (!(tmpFile.delete())) {
                throw new IOException("Could not delete temp file: " + tmpFile.getAbsolutePath()); // NOI18N
            }
            if (!(tmpFile.mkdir())) {
                throw new IOException("Could not create temp directory: " + tmpFile.getAbsolutePath()); // NOI18N
            }
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    protected File getNetBeansDir() throws URISyntaxException {
        return getNetBeansPlatformDir().getParentFile();
    }

    protected File getNetBeansPlatformDir() throws URISyntaxException {
        File result = getIdeUtilJar(). // should be ${NBDIST}/platform/lib/org-openide-util.jar
                getParentFile().  // platform/lib
                getParentFile();  // platform
        return result;
    }

    protected File getIdeUtilJar() throws URISyntaxException  {
        return new File(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    protected void copyFile(File srcFile, File dstFile) throws IOException  {
        InputStream in = new FileInputStream(srcFile);
        OutputStream out = new FileOutputStream(dstFile);
        byte[] buf = new byte[8*1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    protected void copyDirectory(File srcDir, File dstDir) throws IOException {
        assertTrue(srcDir.getPath() + " should exist and be a directory", srcDir.isDirectory());
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        assertTrue("Can't create directory " + dstDir.getAbsolutePath(), dstDir.exists());
        for (File child : srcDir.listFiles()) {
            File dst = new File(dstDir, child.getName());
            if (child.isDirectory()) {
                copyDirectory(child, dst);
            } else {
                copyFile(child, dst);
            }
        }
    }

    /** A convenience wrapper for Thread.sleep */
    protected static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String createRemoteTmpDir() throws Exception {
        String dir = getRemoteTmpDir();
        int rc = CommonTasksSupport.mkDir(getTestExecutionEnvironment(), dir, new PrintWriter(System.err)).get().intValue();
        assertEquals("Can not create directory " + dir, 0, rc);
        return dir;
    }

    protected void clearRemoteTmpDir() throws Exception {
        String dir = getRemoteTmpDir();
        int rc = CommonTasksSupport.rmDir(getTestExecutionEnvironment(), dir, true, new PrintWriter(System.err)).get().intValue();
        if (rc != 0) {
            System.err.printf("Can not delete directory %s\n", dir);
        }
    }

    protected synchronized  String getRemoteTmpDir() {
        if (remoteTmpDir == null) {
            final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
            MacroExpander expander = MacroExpanderFactory.getExpander(local);
            String id;
            try {
                id = expander.expandPredefinedMacros("${hostname}-${osname}-${platform}${_isa}"); // NOI18N
            } catch (ParseException ex) {
                id = local.getHost();
                Exceptions.printStackTrace(ex);
            }
            remoteTmpDir = "/tmp/" + id + "-" + System.getProperty("user.name") + "-" + getTestExecutionEnvironment().getUser();
        }
        return remoteTmpDir;
    }
}
