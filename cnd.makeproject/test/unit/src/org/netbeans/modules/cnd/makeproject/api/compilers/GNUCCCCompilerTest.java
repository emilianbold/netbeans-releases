/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler.Pair;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author Alexander Simon
 */
public class GNUCCCCompilerTest {

    private static final boolean TRACE = false;

    public GNUCCCCompilerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testPatternCpp() {
        String s = "c++";
        s = s.replace("+", "\\+");
        s = ".*\\.(" + s + ")$"; //NOI18N;
        Pattern pattern = Pattern.compile(s);
        assert(pattern.matcher("file.c++").find());
        assert(!pattern.matcher("file.cpp").find());
    }

    @Test
    public void testParseCompilerOutputGcc() {
        //System.setProperty("os.name", "SunOS");
        String s =
                "{2} bash-3.00#g++ -x c++ -E -v tmp.cpp\n" +
                "Reading specs from /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/specs\n" +
                "Configured with: /builds/sfw10-gate/usr/src/cmd/gcc/gcc-3.4.3/configure --prefix=/usr/sfw --with-as=/usr/sfw/bin/gas --with-gnu-as --with-ld=/usr/ccs/bin/ld --without-gnu-ld --enable-languages=c,c++ --enable-shared\n" +
                "Thread model: posix\n" +
                "gcc version 3.4.3 (csl-sol210-3_4-branch+sol_rpath)\n" +
                " /usr/sfw/libexec/gcc/i386-pc-solaris2.10/3.4.3/cc1plus -E -quiet -v tmp.cpp\n" +
                "ignoring nonexistent directory \"/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../i386-pc-solaris2.10/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/i386-pc-solaris2.10\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/backward\n" +
                " /usr/local/include\n" +
                " /usr/sfw/include\n" +
                " /usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include\n" +
                " /usr/include\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"tmp.cpp\"\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of GCC on Solaris");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("GNU", Platform.PLATFORM_SOLARIS_INTEL);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, Tool.CCCompiler, "GNU", "GNU", "/usr/sfw/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("/usr/include");
        golden.add("/usr/local/include");
        golden.add("/usr/sfw/include");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/backward");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/i386-pc-solaris2.10");
        golden.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include");
        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    @Test
    public void testParseCompilerOutputMac() {
        //System.setProperty("os.name", "Darwin");
        String s =
                "jorge@macbook: $ gcc -E -v -x c++ /dev/null\n" +
                "Using built-in specs.\n" +
                "Target: i686-apple-darwin9\n" +
                "Configured with: /var/tmp/gcc/gcc-5465~16/src/configure --disable-checking -enable-werror --prefix=/usr --mandir=/share/man --enable-languages=c,objc,c++,obj-c++ --program-transform-name=/^[cg][^.-]*$/s/$/-4.0/ --with-gxx-include-dir=/include/c++/4.0.0 --with-slibdir=/usr/lib --build=i686-apple-darwin9 --with-arch=apple --with-tune=generic --host=i686-apple-darwin9 --target=i686-apple-darwin9\n" +
                "Thread model: posix\n" +
                "gcc version 4.0.1 (Apple Inc. build 5465)\n" +
                "/usr/libexec/gcc/i686-apple-darwin9/4.0.1/cc1plus -E -quiet -v -D__DYNAMIC__ /dev/null -fPIC -mmacosx-version-min=10.5.6 -mtune=generic -march=apple -D__private_extern__=extern\n" +
                "ignoring nonexistent directory \"/usr/lib/gcc/i686-apple-darwin9/4.0.1/../../../../i686-apple-darwin9/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                " /usr/include/c++/4.0.0\n" +
                " /usr/include/c++/4.0.0/i686-apple-darwin9\n" +
                " /usr/include/c++/4.0.0/backward\n" +
                " /usr/local/include\n" +
                " /usr/lib/gcc/i686-apple-darwin9/4.0.1/include\n" +
                " /usr/include\n" +
                " /System/Library/Frameworks (framework directory)\n" +
                " /Library/Frameworks (framework directory)\n" +
                "End of search list.\n" +
                "# 1 \"/dev/null\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"/dev/null\"\n";

        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of GNU on Mac");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("GNU", Platform.PLATFORM_MACOSX);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, Tool.CCCompiler, "GNU", "GNU", "/usr/sfw/bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();

        golden.add("/Library/Frameworks");
        golden.add("/System/Library/Frameworks");
        golden.add("/usr/include");
        golden.add("/usr/include/c++/4.0.0");
        golden.add("/usr/include/c++/4.0.0/backward");
        golden.add("/usr/include/c++/4.0.0/i686-apple-darwin9");
        golden.add("/usr/lib/gcc/i686-apple-darwin9/4.0.1/include");
        golden.add("/usr/local/include");

        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    @Test
    public void testParseCompilerOutputMinGW1() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "C:\\MinGW\\bin>g++.exe -x c++ -E -v tmp.cpp\n" +
                "Reading specs from C:/MinGW/lib/gcc/mingw32/3.4.5/specs\n" +
                "Configured with: ../gcc-3.4.5/configure --with-gcc --with-gnu-ld --with-gnu-as --host=mingw32 --target=mingw32 --prefix=/mingw --enable-threads --disable-nls --enable-languages=c,c++,f77,ada,objc,java --disable-win32-registry --disable-shared --enable-sjlj-exceptions --enable-libgcj --disable-java-awt --without-x --enable-java-gc=boehm --disable-libgcj-debug--enable-interpreter --enable-hash-synchronization --enable-libstdcxx-debug\n" +
                "Thread model: win32\n" +
                "gcc version 3.4.5 (mingw special)\n" +
                "cc1plus -E -quiet -v -iprefix C:\\MinGW\\bin\\../lib/gcc/mingw32/3.4.5/ tmp.cpp\n" +
                "ignoring nonexistent directory \"C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../mingw32/include\"\n" +
                "ignoring nonexistent directory \"/mingw/mingw32/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/mingw32\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/backward\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include\n" +
                "C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/include\n" +
                "/mingw/include/c++/3.4.5\n" +
                "/mingw/include/c++/3.4.5/mingw32\n" +
                "/mingw/include/c++/3.4.5/backward\n" +
                "/mingw/include\n" +
                "/mingw/include\n" +
                "/mingw/lib/gcc/mingw32/3.4.5/include\n" +
                "/mingw/include\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"tmp.cpp\"\n";

        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of MinGW on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("MinGW", Platform.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, Tool.CCCompiler, "MinGW", "MinGW", "C:\\MinGW\\bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/backward");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/../../../../include/c++/3.4.5/mingw32");
        golden.add("C:/MinGW/bin/../lib/gcc/mingw32/3.4.5/include");
        golden.add("C:/MinGW/include");
        golden.add("C:/MinGW/include/c++/3.4.5");
        golden.add("C:/MinGW/include/c++/3.4.5/backward");
        golden.add("C:/MinGW/include/c++/3.4.5/mingw32");
        golden.add("C:/MinGW/lib/gcc/mingw32/3.4.5/include");

        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    @Test
    public void testParseCompilerOutputMinGW2() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "D:\\tec\\MinGW\\bin>g++.exe -x c++ -E -v tmp.cpp\n" +
                "Using built-in specs.\n" +
                "Target: mingw32\n" +
                "Configured with: ../gcc-4.3.2/cnfigure --prefix=/mingw --build=mingw32 --enable-languages=c,ada,c++,fortran,objc,obj-c++ --with-bugurl=http://www.tdragon.net/recentgcc/bugs.php --disable-nls --disable-win32-registry --enable-libgomp --disable-werror --enable-threads --disable-symvers --enable-cxx-flags='-fno-function-sections -fno-data-sections' --enable-fully-dynamic-string --enable-version-specific-runtime-libs --enable-sjlj-exceptions --with-pkgversion='4.3.2-tdm-1 for MinGW'\n" +
                "Thread model: win32\n" +
                "gcc version 4.3.2 (4.3.2-tdm-1 for MinGW)\n" +
                "COLLECT_GCC_OPTIONS='-E' '-v' '-mtune=i386'\n" +
                "d:/tec/mingw/bin/../libexec/gcc/mingw32/4.3.2/cc1plus.exe -E -quiet -v -iprefix d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/ tmp.cpp -mtune=i386\n" +
                "ignoring nonexistent directory \"d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/../../../../mingw32/include\"\n" +
                "ignoring nonexistent directory \"d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/../../../../mingw32/include\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/mingw32\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/backward\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/../../../../include\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include\n" +
                "d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include-fixed\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/mingw32\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/backward\n" +
                "/mingw/lib/gcc/mingw32/../../../include\n" +
                "d:/tec/mingw/lib/gcc/../../include\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include\n" +
                "d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include-fixed\n" +
                "/mingw/include\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command-line>\"\n" +
                "# 1 \"tmp.cpp\"\n" +
                "COMPILER_PATH=d:/tec/mingw/bin/../libexec/gcc/mingw32/4.3.2/;d:/tec/mingw/bin/../libexec/gcc/;d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/../../../../mingw32/bin/\n" +
                "LIBRARY_PATH=d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/;d:/tec/mingw/bin/../lib/gcc/;d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/../../../../mingw32/lib/;d:/tec/mingw/bin/../lib/gcc/mingw32/4.3.2/../../../;/mingw/lib/\n" +
                "COLLECT_GCC_OPTIONS='-E' '-v' '-mtune=i386'\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of TDM MinGW on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("MinGW_TDM", Platform.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, Tool.CCCompiler, "MinGW_TDM", "MinGW_TDM", "D:\\tec\\mingw\\bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("D:/tec/mingw/include");
        golden.add("D:/tec/mingw/lib/gcc/mingw32/../../../include");
        golden.add("d:/tec/mingw/lib/gcc/../../include");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include-fixed");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/backward");
        golden.add("d:/tec/mingw/lib/gcc/../../lib/gcc/mingw32/4.3.2/include/c++/mingw32");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/../../../../include");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include-fixed");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/backward");
        golden.add("d:\\tec\\mingw\\bin\\../lib/gcc/mingw32/4.3.2/include/c++/mingw32");

        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    @Test
    public void testParseCompilerOutputCygwin() {
        //System.setProperty("os.name", "Windows Vista");
        String s =
                "$ g++.exe -x c++ -E -v tmp.cpp\n" +
                "Reading specs from /usr/lib/gcc/i686-pc-cygwin/3.4.4/specs\n" +
                "Configured with: /usr/build/package/orig/test.respin/gcc-3.4.4-3/configure --verbose --prefix=/usr --exec-prefix=/usr --sysconfdir=/etc --libdir=/usr/lib --libexecdir=/usr/lib --mandir=/usr/share/man --infodir=/usr/share/info --enable-languages=c,ada,c++,d,f77,pascal,java,objc --enable-nls --without-included-gettext --enable-version-specific-runtime-libs --without-x --enable-libgcj --disable-java-awt --with-system-zlib --enable-interpreter --disable-libgcj-debug --enable-threads=posix --enable-java-gc=boehm --disable-win32-registry --enable-sjlj-exceptions --enable-hash-synchronization --enable-libstdcxx-debug\n" +
                "Thread model: posix\n" +
                "gcc version 3.4.4 (cygming special, gdc 0.12, using dmd 0.125)\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/cc1plus.exe -E -quiet -v -D__CYGWIN32__ -D__CYGWIN__ -Dunix -D__unix__ -D__unix -idirafter /usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api -idirafter /usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../i686-pc-cygwin/lib/../../include/w32api tmp.cpp -mtune=pentiumpro\n" +
                "ignoring nonexistent directory \"/usr/local/include\"\n" +
                "ignoring nonexistent directory \"/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../i686-pc-cygwin/include\"\n" +
                "ignoring duplicate directory \"/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../i686-pc-cygwin/lib/../../include/w32api\"\n" +
                "#include \"...\" search starts here:\n" +
                "#include <...> search starts here:\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/include\n" +
                "/usr/include\n" +
                "/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api\n" +
                "End of search list.\n" +
                "# 1 \"tmp.cpp\"\n" +
                "# 1 \"<built-in>\"\n" +
                "# 1 \"<command line>\"\n" +
                "# 1 \"tmp.cpp\"\n";
        BufferedReader buf = new BufferedReader(new StringReader(s));
        if (TRACE) {
            System.out.println("Parse Compiler Output of Cygwin on Windows");
        }
        CompilerFlavor flavor = CompilerFlavor.toFlavor("Cygwin", Platform.PLATFORM_WINDOWS);
        MyGNUCCCompiler instance = new MyGNUCCCompiler(ExecutionEnvironmentFactory.getLocal(), flavor, Tool.CCCompiler, "Cygwin", "Cygwin", "C:\\cygwin\\bin");
        instance.parseCompilerOutput(buf, instance.pair);
        List<String> out = instance.pair.systemIncludeDirectoriesList;
        Collections.<String>sort(out);
        List<String> golden = new ArrayList<String>();
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward");
        golden.add("C:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin");
        golden.add("C:/cygwin/usr/include");
        golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api");
        golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include");
        golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++");
        golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward");
        golden.add("C:/cygwin/usr/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin");
        StringBuilder result = new StringBuilder();
        for (String i : out) {
            result.append(i);
            result.append("\n");
        }
        if (TRACE) {
            System.out.println(result);
        }
        assert (golden.equals(out));
    }

    private static final class MyGNUCCCompiler  extends GNUCCCompiler {
        Pair pair = new Pair();
        protected MyGNUCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
            super(env, flavor, kind, name, displayName, path);
        }
        @Override
        protected String normalizePath(String path) {
            return path;
        }
    }
}