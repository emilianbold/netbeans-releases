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
package org.netbeans.modules.cnd.toolchain.api;

import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerPattern;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainManagerImpl;

/**
 * @author Alexey Vladykin
 */
public class ScannerTestCase extends NbTestCase {

    public ScannerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testGNUpatterns() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "lifedialog.cpp:458: warning: comparison between signed and unsigned integer expressions";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("lifedialog.cpp"));
                assertTrue(m.group(2).equals("458"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }

    public void testGNUpatterns_01() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "../src/CLucene/config/gunichartables.cpp:132:3: warning: #warning \"===== Using internal character function =====\"";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("../src/CLucene/config/gunichartables.cpp"));
                assertTrue(m.group(2).equals("132"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }
    public void testGNUpatterns_02() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "main.cc:41: warning: ISO C++ forbids declaration of `main' with no type";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.cc"));
                assertTrue(m.group(2).equals("41"));
                assertTrue(m.group(3).indexOf("error")<0);
                break;
            }
        }
        assertTrue(find);
    }
    public void testGNUpatterns_03() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "main.cc:53: error: 'gtk_main' was not declared in this scope";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("main.cc"));
                assertTrue(m.group(2).equals("53"));
                assertTrue(m.group(3).indexOf("error")>=0);
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("SunStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"life.cpp\", line 550: Warning: prior hides Life::prior.";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("life.cpp"));
                assertTrue(m.group(2).equals("550"));
                assertEquals("warning", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns_01() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("SunStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"../src/CLucene/util/bufferedstream.h\", line 96:     Where: Instantiated from jstreams::BufferedInputStream<char>::read(const char*&, int, int).";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("../src/CLucene/util/bufferedstream.h"));
                assertTrue(m.group(2).equals("96"));
                assertEquals("warning", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }

    public void testSUNpatterns_02() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("SunStudio", PlatformTypes.PLATFORM_SOLARIS_INTEL);
	ScannerDescriptor scanner = toolchain.getScanner();
      	String s = "\"./CLucene/util/inputstreambuffer.h\", line 45: Error: Non-virtual function jstreams::InputStreamBuffer<char>::read(const char*&, int) declared pure.";
        boolean find = false;
        for(ScannerPattern p : scanner.getPatterns()) {
            Pattern pattern = Pattern.compile(p.getPattern());
            Matcher m = pattern.matcher(s);
	    if (m.matches()){
                find = true;
                assertTrue(m.group(1).equals("./CLucene/util/inputstreambuffer.h"));
                assertTrue(m.group(2).equals("45"));
                assertEquals("error", p.getSeverity());
                break;
            }
        }
        assertTrue(find);
    }


    public void testMSVCpatterns() throws Exception {
	String s = "../../../hbver.c(308) : error C2039: 'wProductType' : is not a member of";
	Pattern pattern = Pattern.compile("^([^\\($]*)\\(([0-9]+)\\) : ([^:$]*):([^$]*)"); // NOI18N
	Matcher m = pattern.matcher(s);
	assertTrue(m.matches());
	assertTrue(m.group(1).equals("../../../hbver.c"));
	assertTrue(m.group(2).equals("308"));
	assertTrue(m.group(3).indexOf("error")>=0);
    }

    public void testCygwinLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testDJGPPLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_WINDOWS);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testGnuFortranLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    public void testGnuCluceneLogs() throws Exception {
        ToolchainDescriptor toolchain = ToolchainManagerImpl.getImpl().getToolchain("GNU", PlatformTypes.PLATFORM_LINUX);
        doTest(getLogs(), toolchain.getScanner(), getRef());
    }

    private void doTest(File logFile, ScannerDescriptor scanner, PrintStream ref) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        String line;
        int lineCnt = 0;
        while ((line = reader.readLine()) != null) {
            ++lineCnt;
            Pattern pattern;
            List<String> match;

            pattern = Pattern.compile(scanner.getChangeDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " changeDirectory { dir: " + match.get(0) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getMakeAllInDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " makeAllInDirectory { dir: " + match.get(0) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getEnterDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " enterDirectory { depth: " + getInt(match.get(0)) + "; dir: " + match.get(1) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getLeaveDirectoryPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " leaveDirectory { depth: " + getInt(match.get(0)) + "; dir: " + match.get(1) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getStackHeaderPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " stackHeader { file: " + match.get(0) + " }");
                continue;
            }

            pattern = Pattern.compile(scanner.getStackNextPattern());
            if ((match = match(pattern, line)) != null) {
                ref.println(lineCnt + " stackNext { file: " + match.get(0) + " }");
                continue;
            }

            for (ScannerPattern scannerPattern : scanner.getPatterns()) {
                pattern = Pattern.compile(scannerPattern.getPattern());
                if ((match = match(pattern, line)) != null) {
                    ref.println(lineCnt + " " + scannerPattern.getSeverity() + " { file: " + match.get(0) + "; line: " + getInt(match.get(1)) + " }");
                    break;
                }
            }
        }
        compareReferenceFiles();
    }

    private List<String> match(Pattern pattern, String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < m.groupCount(); ++i) {
                list.add(m.group(i + 1));
            }
            return list;
        } else {
            return null;
        }
    }

    private int getInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private File getLogs() {
        String fullClassName = this.getClass().getName();
        String logFileName = fullClassName.replace('.', '/') + '/' + getName() + ".dat";
        return new File(getDataDir(), logFileName);
    }
}
