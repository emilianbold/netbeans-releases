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

package org.netbeans.modules.cnd.toolchain.compilers;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author alsimon
 */
public class DiscoverFlagsTest {
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
    public void testStudioOrdinaryFlag1() {
        String s = "-fopenmp                      Equivalent to -xopenmp=parallel";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-fopenmp", res.get(0));
    }

    @Test
    public void testStudioOrdinaryFlag2() {
        String s = "-fsimple[=<n>]                Select floating-point optimization preferences <n>";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-fsimple", res.get(0));
    }

    @Test
    public void testStudioAlternativeFlag1() {
        String s = "-fns[={yes|no}]               Select non-standard floating point mode";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-fns", res.get(0));
        Assert.assertEquals("-fns=yes", res.get(1));
        Assert.assertEquals("-fns=no", res.get(2));
    }

    @Test
    public void testStudioAlternativeFlag2() {
        String s = "-d{n|y}                       Dynamic [-dy] or static [-dn] option to linker";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(2, res.size());
        Assert.assertEquals("-dn", res.get(0));
        Assert.assertEquals("-dy", res.get(1));
    }

    @Test
    public void testStudioAlternativeFlag3() {
        String s = "-B[static|dynamic]            Specify dynamic or static binding";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-B", res.get(0));
        Assert.assertEquals("-Bstatic", res.get(1));
        Assert.assertEquals("-Bdynamic", res.get(2));
    }

    @Test
    public void testStudioAlternativeFlag4() {
        String s = "-xmaxopt=[off,1,2,3,4,5]      Maximum optimization level allowed on #pragma opt";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(7, res.size());
        Assert.assertEquals("-xmaxopt=", res.get(0));
        Assert.assertEquals("-xmaxopt=off", res.get(1));
        Assert.assertEquals("-xmaxopt=1", res.get(2));
        Assert.assertEquals("-xmaxopt=2", res.get(3));
        Assert.assertEquals("-xmaxopt=3", res.get(4));
        Assert.assertEquals("-xmaxopt=4", res.get(5));
        Assert.assertEquals("-xmaxopt=5", res.get(6));
    }

    @Test
    public void testStudioAlternativeFlag5() {
        String s = "-filt[=<a>[,<a>]]             Control the filtering of both linker and compiler error messages;";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-filt", res.get(0));
    }
    
    @Test
    public void testStudioAlternativeFlag6() {
        String s = "-std=<a>                      Specify the c++ standard ; <a>={c++03|c++0x|c++11}";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("-std=c++03", res.get(0));
        Assert.assertEquals("-std=c++0x", res.get(1));
        Assert.assertEquals("-std=c++11", res.get(2));
    }

    @Test
    public void testStudioAlternativeFlag7() {
        String s = "-xipo[=<n>]                   Enable optimization and inlining across source files; <n>={0|1|2}";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("-xipo", res.get(0));
        Assert.assertEquals("-xipo=0", res.get(1));
        Assert.assertEquals("-xipo=1", res.get(2));
        Assert.assertEquals("-xipo=2", res.get(3));
    }

    @Test
    public void testStudioAlternativeFlag8() {
        String s = "-xport64[=<a>]                Enable extra checking for code ported from 32-bit to 64-bit platforms;\n" +
            "                              <a>={no|implicit|full}";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("-xport64", res.get(0));
        Assert.assertEquals("-xport64=no", res.get(1));
        Assert.assertEquals("-xport64=implicit", res.get(2));
        Assert.assertEquals("-xport64=full", res.get(3));
    }

    @Test
    public void testStudioAlternativeFlag9() {
        String s = "-O<n>                         Same as -xO<n>";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(6, res.size());
        Assert.assertEquals("-O0", res.get(0));
        Assert.assertEquals("-O1", res.get(1));
        Assert.assertEquals("-O2", res.get(2));
        Assert.assertEquals("-O3", res.get(3));
        Assert.assertEquals("-O4", res.get(4));
        Assert.assertEquals("-O5", res.get(5));
    }

    @Test
    public void testStudioIgnoredFlag1() {
        String s = "-Xlinker <arg>                Pass <arg> to linker";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testStudioIgnoredFlag2() {
        String s = "-ftrap=<t>                    Select floating-point trapping mode in effect at startup";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, false);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testGccOrdinaryFlag1() {
        String s = "  -fsched2-use-traces         Does nothing.  Preserved for backward\n" +
            "                              compatibility.";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("-fsched2-use-traces", res.get(0));
    }

    @Test
    public void testGccAlternativeFlag1() {
        String s = "  -fexcess-precision=[fast|standard] Specify handling of excess floating-point\n" +
            "                              precision";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(2, res.size());
        Assert.assertEquals("-fexcess-precision=fast", res.get(0));
        Assert.assertEquals("-fexcess-precision=standard", res.get(1));
    }

    @Test
    public void testGccAlternativeFlag2() {
        String s = "  -finit-logical=<true|false> Initialize local logical variables";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(2, res.size());
        Assert.assertEquals("-finit-logical=true", res.get(0));
        Assert.assertEquals("-finit-logical=false", res.get(1));
    }

    @Test
    public void testGccAlternativeFlag3() {
        String s = "  -O<number>                  Set optimization level to <number>";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(6, res.size());
        Assert.assertEquals("-O0", res.get(0));
        Assert.assertEquals("-O1", res.get(1));
        Assert.assertEquals("-O2", res.get(2));
        Assert.assertEquals("-O3", res.get(3));
        Assert.assertEquals("-O4", res.get(4));
        Assert.assertEquals("-O5", res.get(5));
    }

    @Test
    public void testGccAlternativeFlag4() {
        String s = "  -mtune=CPU              optimize for CPU, CPU is one of:\n" +
            "                           generic32, generic64, i8086, i186, i286, i386, i486,\n" +
            "                           i586, i686, pentium, pentiumpro, pentiumii,\n" +
            "                           pentiumiii, pentium4, prescott, nocona, core, core2,\n" +
            "                           corei7, l1om, k6, k6_2, athlon, opteron, k8,\n" +
            "                           amdfam10, bdver1";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(27, res.size());
        Assert.assertEquals("-mtune=generic32", res.get(0));
        Assert.assertEquals("-mtune=bdver1", res.get(26));
    }

    @Test
    public void testGccAlternativeFlag5() {
        String s = "  -march=CPU[,+EXTENSION...]\n" +
            "                          generate code for CPU and EXTENSION, CPU is one of:\n" +
            "                           generic32, generic64, i386, i486, i586, i686,\n" +
            "                           pentium, pentiumpro, pentiumii, pentiumiii, pentium4,\n" +
            "                           prescott, nocona, core, core2, corei7, l1om, k6,\n" +
            "                           k6_2, athlon, opteron, k8, amdfam10, bdver1\n" +
            "                          EXTENSION is combination of:\n" +
            "                           8087, 287, 387, no87, mmx, nommx, sse, sse2, sse3,\n" +
            "                           ssse3, sse4.1, sse4.2, sse4, nosse, avx, noavx, vmx,\n" +
            "                           smx, xsave, xsaveopt, aes, pclmul, fsgsbase, rdrnd,\n" +
            "                           f16c, fma, fma4, xop, lwp, movbe, ept, clflush, nop,\n" +
            "                           syscall, rdtscp, 3dnow, 3dnowa, padlock, svme, sse4a,\n" +
            "                           abm";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(24, res.size());
        Assert.assertEquals("-march=generic32", res.get(0));
        Assert.assertEquals("-march=bdver1", res.get(23));
    }

    @Test
    public void testGccIgnoredFlag1() {
        String s = "  -fsched-stalled-insns-dep=<number> Set dependence distance checking in\n" +
            "                              premature scheduling of queued insns";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testGccIgnoredFlag2() {
        String s = "  -idirafter <dir>            Add <dir> to the end of the system include path";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testGccIgnoredFlag3() {
        String s = "  --divide                do not treat `/' as a comment character";
        List<String> res = new ArrayList<String>();
        CCCCompiler.discoverFlags(s, res, true);
        Assert.assertEquals(0, res.size());
    }
}
