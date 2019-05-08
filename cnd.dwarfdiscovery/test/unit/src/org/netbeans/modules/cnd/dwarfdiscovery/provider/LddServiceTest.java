/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import junit.framework.Test;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.support.RemoteJarServiceProvider;
import org.netbeans.modules.cnd.dwarfdump.LddService;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService.AbstractFunctionToLine;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.util.Exceptions;

/**
 *
 */
public class LddServiceTest extends NativeExecutionBaseTestCase {

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(LddServiceTest.class);
    }
    
    public LddServiceTest(String name) {
        super(name);
    }

    public void testSubprojectUbuntu() throws IOException {
        String executable = getResource("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main");
        SharedLibraries res1 = LddService.getPubNames(executable);
        assertEquals(6, res1.getDlls().size());
        assertEquals(1, res1.getPaths().size());
        for(String java : javaPaths()) {
            ProcessUtils.ExitStatus status = getJavaProcess(java, LddService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
            assertNotNull(status);
            assertTrue("Cannot execute "+java, status.isOK());
            BufferedReader br = new BufferedReader(new StringReader(status.getOutputString()));
            SharedLibraries res2 = LddService.getPubNames(br);
            assertEquals(res1.getDlls().size(), res2.getDlls().size());
            assertEquals(res1.getPaths().size(), res2.getPaths().size());
            for(int i = 0; i < res1.getDlls().size(); i++) {
                assert  res1.getDlls().get(i).equals(res2.getDlls().get(i));
            }
            for(int i = 0; i < res1.getPaths().size(); i++) {
                assert  res1.getPaths().get(i).equals(res2.getPaths().get(i));
            }
        }
    }

    public void testSubprojectRedHat() throws IOException {
        String executable = getResource("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/main/dist/Debug/GNU-Linux-x86/main");
        SharedLibraries res1 = LddService.getPubNames(executable);
        assertEquals(6, res1.getDlls().size());
        assertEquals(1, res1.getPaths().size());
        for(String java : javaPaths()) {
            ProcessUtils.ExitStatus status = getJavaProcess(java, LddService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
            assertNotNull(status);
            assertTrue("Cannot execute "+java, status.isOK());
            BufferedReader br = new BufferedReader(new StringReader(status.getOutputString()));
            SharedLibraries res2 = LddService.getPubNames(br);
            assertEquals(res1.getDlls().size(), res2.getDlls().size());
            assertEquals(res1.getPaths().size(), res2.getPaths().size());
            for(int i = 0; i < res1.getDlls().size(); i++) {
                assert  res1.getDlls().get(i).equals(res2.getDlls().get(i));
            }
            for(int i = 0; i < res1.getPaths().size(); i++) {
                assert  res1.getPaths().get(i).equals(res2.getPaths().get(i));
            }
        }
    }

    public void testSubprojectSparc64() throws IOException {
        String executable = getResource("/org/netbeans/modules/cnd/dwarfdiscovery/projects/Subproject_sparc64/main/dist/Debug/OracleSolarisStudio-Solaris-Sparc/main");
        SharedLibraries res1 = LddService.getPubNames(executable);
        assertEquals(6, res1.getDlls().size());
        assertEquals(1, res1.getPaths().size());
        for(String java : javaPaths()) {
            ProcessUtils.ExitStatus status = getJavaProcess(java, LddService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
            assertNotNull(status);
            assertTrue("Cannot execute "+java, status.isOK());
            BufferedReader br = new BufferedReader(new StringReader(status.getOutputString()));
            SharedLibraries res2 = LddService.getPubNames(br);
            assertEquals(res1.getDlls().size(), res2.getDlls().size());
            assertEquals(res1.getPaths().size(), res2.getPaths().size());
            for(int i = 0; i < res1.getDlls().size(); i++) {
                assert  res1.getDlls().get(i).equals(res2.getDlls().get(i));
            }
            for(int i = 0; i < res1.getPaths().size(); i++) {
                assert  res1.getPaths().get(i).equals(res2.getPaths().get(i));
            }
        }
    }

    public void testSubprojectWindows() throws IOException {
        String executable = getResource("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/main/dist/Debug/Cygwin-Windows/main.exe");
        SharedLibraries res1 = LddService.getPubNames(executable);
        assertEquals(3, res1.getDlls().size());
        assertEquals(0, res1.getPaths().size());
        for(String java : javaPaths()) {
            ProcessUtils.ExitStatus status = getJavaProcess(java, LddService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
            assertNotNull(status);
            assertTrue("Cannot execute "+java, status.isOK());
            BufferedReader br = new BufferedReader(new StringReader(status.getOutputString()));
            SharedLibraries res2 = LddService.getPubNames(br);
            assertEquals(res1.getDlls().size(), res2.getDlls().size());
            assertEquals(res1.getPaths().size(), res2.getPaths().size());
            for(int i = 0; i < res1.getDlls().size(); i++) {
                assert  res1.getDlls().get(i).equals(res2.getDlls().get(i));
            }
            for(int i = 0; i < res1.getPaths().size(); i++) {
                assert  res1.getPaths().get(i).equals(res2.getPaths().get(i));
            }
        }
    }
    
    public void testProfilingdemo() throws IOException {
        String executable = getResource("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main");
        Map<String, AbstractFunctionToLine> res1 = Offset2LineService.getOffset2Line(executable);
        res1 = new TreeMap<String, AbstractFunctionToLine>(res1);
        for(String java : javaPaths()) {
            ProcessUtils.ExitStatus status = getJavaProcess(java, Offset2LineService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
            assertNotNull(status);
            assertTrue("Cannot execute "+java, status.isOK());
            BufferedReader br = new BufferedReader(new StringReader(status.getOutputString()));
            Map<String, AbstractFunctionToLine> res2 = Offset2LineService.getOffset2Line(br);
            assertEquals(res1.size(), res2.size());
            res2 = new TreeMap<String, AbstractFunctionToLine>(res2);
            for(String function : res1.keySet()) {
                AbstractFunctionToLine line1 = res1.get(function);
                AbstractFunctionToLine line2 = res2.get(function);
                assertNotNull(line1);
                assertNotNull(line2);
                assertEquals(line1, line2);
            }
        }
    }

    private ProcessUtils.ExitStatus getJavaProcess(String java, Class<?> clazz, ExecutionEnvironment env, String[] arguments) throws IOException{
        return RemoteJarServiceProvider.getJavaProcess(java, clazz, env, arguments);
    }

    private String getResource(String resource) {
        File dataDir = getDataDir();
        return dataDir.getAbsolutePath() + resource;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        unzipTestData();
    }
    
    private List<String> javaPaths() {
        List<String> res = new ArrayList<String>();
        try {
            for(String s: new String[]{"java1_5","java1_6", "java1_7", "java1_8", "java1_9"}) {
                String path = NativeExecutionTestSupport.getRcFile().get("jdk.paths", s);
                if (path != null) {
                    if (!new File(path).exists()) {
                        System.err.println("Skip test because "+s+" ("+path+") not found");
                        continue;
                    }
                    res.add(path);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (FormatException ex) {
            Exceptions.printStackTrace(ex);
        }
        return res;
    }

    private void unzipTestData() throws Exception  {
        File dataDir = getDataDir();
        String zip = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/projects/data.zip";
        ZipInputStream in = new ZipInputStream(new FileInputStream(zip));
        while (true) {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                break;
            }
            String outFilename = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/projects/"+entry.getName();
            if (entry.isDirectory()) {
                File f = new File(outFilename);
                if (!f.exists()) {
                    f.mkdir();
                }
                continue;
            }
            if (new File(outFilename).exists()) {
                continue;
            }
            OutputStream out = new FileOutputStream(outFilename);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        }
    }
}