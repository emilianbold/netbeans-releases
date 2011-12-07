/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fileoperations;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import org.netbeans.modules.remote.impl.fs.*;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fileoperations.FileOperationsProvider.FileOperations;
import org.netbeans.modules.remote.impl.fileoperations.FileOperationsProvider.FileProxyO;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 *
 * @author Alexander Simon
 */
public class FileOperationsTestCase extends RemoteFileTestBase {

    private String script;
    private String localScript;
    private String remoteDir;
    private String localDir;
    private String user;
    private String group;

    public static Test suite() {
        return RemoteApiTest.createSuite(FileOperationsTestCase.class);
    }

    public FileOperationsTestCase(String testName) {
        super(testName);
    }

    public FileOperationsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (execEnv != null) {
            user = execEnv.getUser();
            if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX) {
                group = "wheel"; // don't know the reason, but mac isn't supported, so it's mostly for my own convenien
            } else {
                group = execute("groups").split(" ")[0];
            }
            remoteDir = mkTempAndRefreshParent(true);
            ProcessUtils.execute(execEnv, "umask", "0002");
            script = getScript(remoteDir);
            localDir = mkTemp(ExecutionEnvironmentFactory.getLocal(), true);
            localScript = getScript(localDir);
        } else {
            user = "user_1563";
            group = "staff";
        }
    }

    private String getScript(String dir) {
        return  "cd " + dir + "\n" +
                "echo \"123\" > just_a_file\n" +
                "echo \"123\" > \"file with a space\"\n" +
                "mkdir -p \"dir with a space\"\n" +
                "mkdir -p dir_1\n" +
                "ln -s just_a_file just_a_link\n" +
                "ln -s dir_1 link_to_dir\n" +
                "ln -s \"file with a space\" link_to_file_with_a_space\n" +
                "ln -s \"file with a space\" \"link with a space to file with a space\"\n" +
                "ln -s "+dir+"/dir_1 link_to_abs_dir\n" +
                "mkfifo fifo\n"+
                "cd dir_1\n"+
                "ln -s .. recursive_link\n" +
                "ln -s ../just_a_file back_link\n" +
                "cd ..\n"+
                "ln -s dir_1/back_link double_link\n" +
                "";
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (execEnv != null) {
            removeRemoteDirIfNotNull(remoteDir);
        }
        if (localDir != null) {
            CommonTasksSupport.rmDir(ExecutionEnvironmentFactory.getLocal(), localDir, true, new OutputStreamWriter(System.err)).get();
        }
    }

    private void prepareDirectory() throws Exception {
        ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, script, new LineProcessor() {
            @Override
            public void processLine(String line) {
                System.err.println(line);
            }
            @Override
            public void reset() {}
            @Override
            public void close() {}
        });
        int rc = scriptRunner.execute();
        assertEquals("Error running script", 0, rc);
        scriptRunner = new ShellScriptRunner(ExecutionEnvironmentFactory.getLocal(), localScript, new LineProcessor() {
            @Override
            public void processLine(String line) {
                System.err.println(line);
            }
            @Override
            public void reset() {}
            @Override
            public void close() {}
        });
        rc = scriptRunner.execute();
        assertEquals("Error running local script", 0, rc);
    }

    @ForAllEnvironments
    public void testFileOperations() throws Exception {
        prepareDirectory();
        DirectoryReaderSftp directoryReader = new DirectoryReaderSftp(execEnv, remoteDir);
        directoryReader.readDirectory();
        List<DirEntry> entries = directoryReader.getEntries();
        FileOperations fileOperations = FileOperationsProvider.getDefault().getFileOperations(fs);
        for(DirEntry entry : entries) {
            String name = entry.getName();
            String path = remoteDir+"/"+name;
            FileProxyO file = FileOperationsProvider.toFileProxy(path);
            assertTrue(fileOperations.exists(file));
            assertEquals(entry.canWrite(execEnv), fileOperations.canWrite(file));
            assertEquals(remoteDir, fileOperations.getDir(file));
            assertEquals(name, fileOperations.getName(file));
            assertEquals(path, fileOperations.getPath(file));
            assertEquals(fs.getRoot(), fileOperations.getRoot());
            if (!entry.isLink()) {
                assertEquals(entry.isPlainFile(), fileOperations.isFile(file));
                assertEquals(entry.isDirectory(), fileOperations.isDirectory(file));
            }
            File ioFile = new File(localDir+"/"+name);
            fileEquals(ioFile, fileOperations, file, false);
        }

        {
            // test unexisting file
            String name = "unexisting";
            String path = remoteDir+"/"+name;
            FileProxyO file = FileOperationsProvider.toFileProxy(path);
            File ioFile = new File(localDir+"/"+name);
            fileEquals(ioFile, fileOperations, file, false);
        }
        
        {
            // test of self dir
            FileProxyO file = FileOperationsProvider.toFileProxy(remoteDir);
            File ioFile = new File(localDir);
            fileEquals(ioFile, fileOperations, file, true);
        }
        {
            // test of recursive link
            FileProxyO file = FileOperationsProvider.toFileProxy(remoteDir+"/"+"dir_1/recursive_link");
            File ioFile = new File(localDir+"/"+"dir_1/recursive_link");
            fileEquals(ioFile, fileOperations, file, false);
        }
    }

    private void fileEquals(File ioFile, FileOperations fileOperations, FileProxyO file, boolean skipName) {
        assertEquals(message(ioFile, file, "exist"), ioFile.exists(), fileOperations.exists(file));
        if (!skipName) {
            assertEquals(ioFile.getName(), fileOperations.getName(file));
        }
        absPathEquals(ioFile.getAbsolutePath(), fileOperations.getPath(file));
        assertEquals(message(ioFile, file, "canWrite"), ioFile.canWrite(), fileOperations.canWrite(file));
        assertEquals(message(ioFile, file, "isDirectory"), ioFile.isDirectory(), fileOperations.isDirectory(file));
        assertEquals(message(ioFile, file, "isFile"), ioFile.isFile(), fileOperations.isFile(file));
        listEquals(message(ioFile, file, "isFile"), ioFile.list(), fileOperations.list(file));
        absPathEquals(ioFile.getParent(), fileOperations.getDir(file));
    }
    
    private String message(File ioFile, FileProxyO file, String method) {
        return new StringBuilder().append(method)
                .append("(")
                .append(ioFile.getAbsolutePath())
                .append(") # ")
                .append(method)
                .append("(")
                .append(file.getPath())
                .append(")")
                .toString();
    }
    
    private void absPathEquals(String file, String fo) {
        assertEquals(file == null , fo == null);
        if (file != null) {
            if (file.length() <= localDir.length()) {
                return;
            }
            file = remoteDir+file.substring(localDir.length());
            assertEquals(file, fo);
        }
    }
    
    private void listEquals(String message, String[] file, String[] fo) {
        assertEquals(file == null , fo == null);
        if (file != null) {
            assertEquals(file.length, fo.length);
            loop:for(int i = 0; i < file.length; i++) {
                for(int j = 0; j < fo.length; j++) {
                    if (file[i].equals(fo[j])) {
                        continue loop;
                    }
                }
                assertTrue(message, false);
            }
        }
    }
    
}
