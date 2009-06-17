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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

public class NativeExecutionBaseTestCase extends NbTestCase {
    
    static {
        String dirs = System.getProperty("netbeans.dirs", ""); // NOI18N
        File junitWorkdir = new File(System.getProperty("nbjunit.workdir")); // NOI18N
        
        while (true) {
            String dirName = junitWorkdir.getName();
            junitWorkdir = junitWorkdir.getParentFile();
            if ("dlight.nativeexecution".equals(dirName) || "".equals(dirName)) { // NOI18N
                break;
            }
        }

        File dlightDir = new File(junitWorkdir, "nbbuild/netbeans/dlight1"); // NOI18N
        System.setProperty("netbeans.dirs", dlightDir.getAbsolutePath() + ":" + dirs); // NOI18N

        Logger log = Logger.getLogger("nativeexecution.support"); // NOI18N
        log.setLevel(Level.ALL);

        log.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                System.err.printf("%s [%s]: %s\n", record.getLevel(), record.getLoggerName(), record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }

    private final ExecutionEnvironment testExecutionEnvironment;

    public NativeExecutionBaseTestCase(String name) {
        super(name);
        System.setProperty("nativeexecution.mode.unittest", "true");
        testExecutionEnvironment = null;
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
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Gets execution environment this test was created with.
     * @return
     */
    protected ExecutionEnvironment getTestExecutionEnvironment() {
        return testExecutionEnvironment;
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

    public static File createTempFile(String prefix, String suffix, boolean directory) throws IOException {
        File tmpFile = File.createTempFile(prefix, suffix);
        if (directory) {
            if(!(tmpFile.delete())) {
                throw new IOException("Could not delete temp file: " + tmpFile.getAbsolutePath()); // NOI18N
            }
            if (!(tmpFile.mkdir())) {
                throw new IOException("Could not create temp directory: " + tmpFile.getAbsolutePath()); // NOI18N
            }
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }

}
