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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class DwarfDiscoveryTest  extends NbTestCase {

    public DwarfDiscoveryTest() {
        super("DwarfDiscoveryTest");
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        unzipTestData();
    }

    private void unzipTestData() throws Exception  {
        File dataDir = getDataDir();
        String zip = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/projects/data.zip";
        new File(zip).exists();
        ZipInputStream in = new ZipInputStream(new FileInputStream(zip));
        while (true) {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                break;
            }
            String outFilename = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/projects/"+entry.getName();
            if (entry.isDirectory()) {
                File f = new File(outFilename);
                f.mkdir();
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

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testDll_RHEL55_x64_gcc() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "libhello3lib.so", "libhello4lib.so","libstdc++.so.6", "libm.so.6", "libgcc_s.so.1", "libc.so.6");
    }

    public void testDll_Ubuntu1010_x64_gcc() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "libhello3lib.so", "libhello4lib.so","libstdc++.so.6", "libm.so.6", "libgcc_s.so.1", "libc.so.6");
    }

    public void testDll_macosx32() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/main/dist/Debug/GNU-MacOSX/main",
                "libhello3lib.dylib", "libhello4lib.dylib", "/usr/lib/libstdc++.6.dylib", "/usr/lib/libSystem.B.dylib");
    }

    public void testDll_macosx64() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/main/dist/Debug/GNU-MacOSX/main",
                "libhello3lib.dylib", "libhello4lib.dylib", "/usr/lib/libstdc++.6.dylib", "/usr/lib/libSystem.B.dylib");
    }

    public void testDll_windows7_cygwin() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/main/dist/Debug/Cygwin-Windows/main.exe",
                "libhello3lib.dll", "libhello4lib.dll", "cygwin1.dll");
    }

    public void testDll_windowsxp_mingw() {
        dumpDlls("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/main/dist/Debug/MinGW-Windows/main.exe",
                "libhello3lib.dll", "libhello4lib.dll");
    }

    public void testApplicable_RHEL55_x64_gcc() {
        dumpSources("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "GNU C++ 4.1.2 20080704 (Red Hat 4.1.2-48)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_RHEL55_x64_gcc/");
    }

    public void testApplicable_Ubuntu1010_x64_gcc() {
        dumpSources("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/main/dist/Debug/GNU-Linux-x86/main",
                "GNU C++ 4.4.5",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_Ubuntu1010_x64_gcc/");
    }

    public void testApplicable_macosx32() {
        dumpSources("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/main/dist/Debug/GNU-MacOSX/main",
                "GNU C++ 4.2.1 (Apple Inc. build 5664)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx32/main/");
    }

    public void testApplicable_macosx64() {
        dumpSources("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/main/dist/Debug/GNU-MacOSX/main",
                "GNU C++ 4.2.1 (Apple Inc. build 5664)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_macosx64/main/");
    }

    public void testApplicable_windows7_cygwin() {
        dumpSources("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/main/dist/Debug/Cygwin-Windows/main.exe",
                "GNU C++ 3.4.4 (cygming special, gdc 0.12, using dmd 0.125)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windows7_cygwin/");
    }

    public void testApplicable_windowsxp_mingw() {
        dumpSources("/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/main/dist/Debug/MinGW-Windows/main.exe",
                "GNU C++ 3.4.5 (mingw-vista special r3)",
                "/org/netbeans/modules/cnd/dwarfdiscovery/projects/SubProjects_windowsxp_mingw/");
    }

    private void dumpSources(String path, String compiler, String root) {
        AnalyzeExecutable provider = new AnalyzeExecutable();
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+path;
        root = dataDir.getAbsolutePath()+root;
        provider.getProperty(AnalyzeExecutable.EXECUTABLE_KEY).setValue(objFileName);
        Applicable canAnalyze = provider.canAnalyze(new ProjectProxy() {

            @Override
            public boolean createSubProjects() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Project getProject() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getMakefile() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getSourceRoot() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getExecutable() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getWorkingFolder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        String compilerName = canAnalyze.getCompilerName();
        String sourceRoot = canAnalyze.getSourceRoot();
        String mainFunction = canAnalyze.getMainFunction().getFilePath();
        System.err.println(compilerName);
        System.err.println(sourceRoot);
        System.err.println(mainFunction);
        assertEquals(compiler, compilerName);
        assertEquals(root, sourceRoot);
        assertNotNull(mainFunction);
        assertTrue(mainFunction.startsWith(root));
        assertTrue(canAnalyze.isApplicable());
        canAnalyze.getMainFunction().getFilePath();
        assertTrue(canAnalyze.getDependencies().size()>=2);
    }

    private void dumpDlls(String path, String...dlls) {
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+path;
        List<String> res =  null;
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            res = dump.readPubNames();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WrongFileFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        assertNotNull(res);
        System.err.println(res);
        assertEquals(dlls.length, res.size());
        int i = 0;
        for(String dll: res) {
            assertEquals(dlls[i], dll);
            i++;
        }
    }
}
