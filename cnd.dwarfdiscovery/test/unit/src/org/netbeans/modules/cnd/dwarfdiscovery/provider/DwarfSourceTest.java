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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.TestCase;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;

/**
 *
 * @author Alexander Simon
 */
public class DwarfSourceTest extends TestCase {

    /**
     * Assert whether the document available through {@link #getDocument()}
     * has a content equal to <code>expectedText</code>.
     */
    protected void assertDocumentText(String line, String expResult, String result) {
        if (expResult.equals(result)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Parsing line:");
        sb.append(line);
        sb.append("\nExpected:\n");
        sb.append(expResult);
        sb.append("\nFound:\n");
        sb.append(result);
        int startLine = 1;
        for (int i = 0; i < result.length() && i < expResult.length(); i++) {
            if (expResult.charAt(i) == '\n') {
                startLine++;
            }
            if (expResult.charAt(i) != result.charAt(i)) {
                sb.append("Diff starts in line " + startLine + "\n");
                String context = expResult.substring(i);
                if (context.length() > 40) {
                    context = context.substring(0, 40);
                }
                sb.append("Expected " + context + "\n");
                context = result.substring(i);
                if (context.length() > 40) {
                    context = context.substring(0, 40);
                }
                sb.append("Found " + context + "\n");
                break;
            }
        }
        assertFalse(sb.toString(), true);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine() {
        String line = "/set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.";
        String expResult = "Macros:\nHELLO=75\nPaths:\ndist";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine2() {
        String line = "/opt/SUNWspro/bin/cc -xarch=amd64 -Ui386 -U__i386 -xO3 ../../intel/amd64/ml/amd64.il " +
                "../../i86pc/ml/amd64.il -D_ASM_INLINES -Xa -xspace -Wu,-xmodel=kernel -Wu,-save_args -v " +
                "-xildoff -g -xc99=%all -W0,-noglobal -g3 -gdwarf-2 -g3 -gdwarf-2 -errtags=yes -errwarn=%all " +
                "-W0,-xglobalstatic -xstrconst -DDIS_MEM -D_KERNEL -D_SYSCALL32 -D_SYSCALL32_IMPL -D_ELF64 " +
                "-I../../i86pc -I/export/opensolaris/testws77/usr/src/common -I../../intel -Y I,../../common " +
                "-c -o debug64/cpupm.o ../../i86pc/os/cpupm.c";
        String expResult = "Macros:\n" +
                "DIS_MEM\n" +
                "_ASM_INLINES\n" +
                "_ELF64\n" +
                "_KERNEL\n" +
                "_SYSCALL32\n" +
                "_SYSCALL32_IMPL\n" +
                "Paths:\n" +
                "../../i86pc\n" +
                "/export/opensolaris/testws77/usr/src/common\n" +
                "../../intel" +
                "\n../../common";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine3() {
        String line =
                "+ /opt/SUNWspro/bin/cc -xO3 -xarch=amd64 -Ui386 -U__i386 -K pic -Xa -xildoff -errtags=yes " +
                "-errwarn=%all -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED " +
                "-erroff=E_UNRECOGNIZED_PRAGMA_IGNORED -xc99=%all -D_XOPEN_SOURCE=600 " +
                "-D__EXTENSIONS__=1 -W0,-xglobalstatic -v -xstrconst -g -xc99=%all " +
                "-D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1 -W0,-noglobal -xdebugformat=stabs " +
                "-DTEXT_DOMAIN=\"SUNW_OST_OSLIB\" -D_TS_ERRNO -Isrc/cmd/ksh93 " +
                "-I../common/include -I/export/home/thp/opensolaris/proto/root_i386/usr/include/ast " +
                "-DKSHELL -DSHOPT_BRACEPAT -DSHOPT_CMDLIB_BLTIN=0 -DSH_CMDLIB_DIR=\"/usr/ast/bin\" " +
                "-DSHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\" -DSHOPT_DYNAMIC -DSHOPT_ESH " +
                "-DSHOPT_FILESCAN -DSHOPT_HISTEXPAND -DSHOPT_KIA -DSHOPT_MULTIBYTE " +
                "-DSHOPT_NAMESPACE -DSHOPT_OPTIMIZE -DSHOPT_PFSH -DSHOPT_RAWONLY " +
                "-DSHOPT_SUID_EXEC -DSHOPT_SYSRC -DSHOPT_VSH -D_BLD_shell " +
                "-D_PACKAGE_ast -DERROR_CONTEXT_T=Error_context_t " +
                "-DUSAGE_LICENSE= \"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\" " +
                "-DPIC -D_REENTRANT -c -o pics/data/builtins.o ../common/data/builtins.c";
        String expResult =
                "Macros:\n" +
                "ERROR_CONTEXT_T=Error_context_t\n" +
                "KSHELL\n" +
                "PIC\n" +
                "SHOPT_BRACEPAT\n" +
                "SHOPT_CMDLIB_BLTIN=0\n" +
                "SHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\"\n" +
                "SHOPT_DYNAMIC\n" +
                "SHOPT_ESH\n" +
                "SHOPT_FILESCAN\n" +
                "SHOPT_HISTEXPAND\n" +
                "SHOPT_KIA\n" +
                "SHOPT_MULTIBYTE\n" +
                "SHOPT_NAMESPACE\n" +
                "SHOPT_OPTIMIZE\n" +
                "SHOPT_PFSH\n" +
                "SHOPT_RAWONLY\n" +
                "SHOPT_SUID_EXEC\n" +
                "SHOPT_SYSRC\n" +
                "SHOPT_VSH\n" +
                "SH_CMDLIB_DIR=\"/usr/ast/bin\"\n" +
                "TEXT_DOMAIN=\"SUNW_OST_OSLIB\"\n" +
                "USAGE_LICENSE=\"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"\n" +
                "_BLD_shell\n" +
                "_PACKAGE_ast\n" +
                "_REENTRANT\n" +
                "_TS_ERRNO\n" +
                "_XOPEN_SOURCE=600\n" +
                "__EXTENSIONS__=1\n" +
                "Paths:\n" +
                "src/cmd/ksh93\n" +
                "../common/include\n" +
                "/export/home/thp/opensolaris/proto/root_i386/usr/include/ast";

        String result = processLine(line, false);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine4() {
        String line = "/opt/onbld/bin/i386/cw -_cc -xO3 -xarch=amd64 -Ui386 -U__i386 -K pic  -Xa  " +
                "-xildoff -errtags=yes -errwarn=%all -erroff=E_EMPTY_TRANSLATION_UNIT " +
                "-erroff=E_STATEMENT_NOT_REACHED -erroff=E_UNRECOGNIZED_PRAGMA_IGNORED -xc99=%all " +
                "-D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1    -W0,-xglobalstatic -v  -xstrconst -g " +
                "-xc99=%all -D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1 -W0,-noglobal -_gcc=-fno-dwarf2-indirect-strings " +
                "-xdebugformat=stabs -DTEXT_DOMAIN=\"SUNW_OST_OSLIB\" -D_TS_ERRNO  " +
                "-Isrc/cmd/ksh93  -I../common/include  -I/export/home/thp/opensolaris/proto/root_i386/usr/include/ast  " +
                "-DKSHELL  -DSHOPT_BRACEPAT  -DSHOPT_CMDLIB_BLTIN=0  '-DSH_CMDLIB_DIR=\"/usr/ast/bin\"'  " +
                "'-DSHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\"'  -DSHOPT_DYNAMIC  -DSHOPT_ESH  -DSHOPT_FILESCAN  " +
                "-DSHOPT_HISTEXPAND  -DSHOPT_KIA -DSHOPT_MULTIBYTE  -DSHOPT_NAMESPACE  -DSHOPT_OPTIMIZE  " +
                "-DSHOPT_PFSH  -DSHOPT_RAWONLY  -DSHOPT_SUID_EXEC  -DSHOPT_SYSRC  -DSHOPT_VSH  -D_BLD_shell  " +
                "-D_PACKAGE_ast  -DERROR_CONTEXT_T=Error_context_t  " +
                "'-DUSAGE_LICENSE= \"[-author?David Korn <dgk@research.att.com>]\" " +
                "\"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" " +
                "\"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"' " +
                "-DPIC -D_REENTRANT -c -o pics/data/builtins.o ../common/data/builtins.c";
        String expResult =
                "Macros:\n" +
                "ERROR_CONTEXT_T=Error_context_t\n" +
                "KSHELL\n" +
                "PIC\n" +
                "SHOPT_BRACEPAT\n" +
                "SHOPT_CMDLIB_BLTIN=0\n" +
                "SHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\"\n" +
                "SHOPT_DYNAMIC\n" +
                "SHOPT_ESH\n" +
                "SHOPT_FILESCAN\n" +
                "SHOPT_HISTEXPAND\n" +
                "SHOPT_KIA\n" +
                "SHOPT_MULTIBYTE\n" +
                "SHOPT_NAMESPACE\n" +
                "SHOPT_OPTIMIZE\n" +
                "SHOPT_PFSH\n" +
                "SHOPT_RAWONLY\n" +
                "SHOPT_SUID_EXEC\n" +
                "SHOPT_SYSRC\n" +
                "SHOPT_VSH\n" +
                "SH_CMDLIB_DIR=\"/usr/ast/bin\"\n" +
                "TEXT_DOMAIN=\"SUNW_OST_OSLIB\"\n" +
                "USAGE_LICENSE=\"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"\n" +
                "_BLD_shell\n" +
                "_PACKAGE_ast\n" +
                "_REENTRANT\n" +
                "_TS_ERRNO\n" +
                "_XOPEN_SOURCE=600\n" +
                "__EXTENSIONS__=1\n" +
                "Paths:\n" +
                "src/cmd/ksh93\n" +
                "../common/include\n" +
                "/export/home/thp/opensolaris/proto/root_i386/usr/include/ast";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testGccLine() {
        String line = "/bin/sh ./libtool --tag=CXX --mode=compile /export/home/gcc/gccobj/gcc/xgcc -shared-libgcc -B/export/home/gcc/gccobj/gcc/ -nostdinc++ -L/export/home/gcc/gccobj/i386-pc-solaris2.11/libstdc++-v3/src -L/export/home/gcc/gccobj/i386-pc-solaris2.11/libstdc++-v3/src/.libs -B/usr/local/i386-pc-solaris2.11/bin/ -B/usr/local/i386-pc-solaris2.11/lib/ -isystem /usr/local/i386-pc-solaris2.11/include -isystem /usr/local/i386-pc-solaris2.11/sys-include -DHAVE_CONFIG_H -I. -I../../../libjava -I./include -I./gcj -I../../../libjava -Iinclude -I../../../libjava/include -I/export/home/gcc/boehm-gc/include  -DGC_SOLARIS_THREADS=1 -DGC_SOLARIS_PTHREADS=1 -DSOLARIS25_PROC_VDB_BUG_FIXED=1 -DSILENT=1 -DNO_SIGNALS=1 -DALL_INTERIOR_POINTERS=1 -DJAVA_FINALIZATION=1 -DGC_GCJ_SUPPORT=1 -DATOMIC_UNCOLLECTABLE=1   -I../../../libjava/libltdl -I../../../libjava/libltdl  -I../../../libjava/.././libjava/../gcc -I../../../libjava/../zlib -I../../../libjava/../libffi/include -I../libffi/include  -O2 -g3 -gdwarf-2 -fno-rtti -fnon-call-exceptions  -fdollars-in-identifiers -Wswitch-enum -ffloat-store  -I/usr/openwin/include -W -Wall -D_GNU_SOURCE -DPREFIX=\"\\\"/usr/local\\\"\" -DLIBDIR=\"\\\"/usr/local/lib\\\"\" -DBOOT_CLASS_PATH=\"\\\"/usr/local/share/java/libgcj-3.4.3.jar\\\"\" -g3 -gdwarf-2 -MD -MT gnu/gcj/natCore.lo -MF gnu/gcj/natCore.pp -c -o gnu/gcj/natCore.lo ../../../libjava/gnu/gcj/natCore.cc";
        String expResult =
                "Macros:\n" +
                "ALL_INTERIOR_POINTERS=1\n" +
                "ATOMIC_UNCOLLECTABLE=1\n" +
                "BOOT_CLASS_PATH=\"\\\"/usr/local/share/java/libgcj-3.4.3.jar\\\"\"\n" +
                "GC_GCJ_SUPPORT=1\n" +
                "GC_SOLARIS_PTHREADS=1\n" +
                "GC_SOLARIS_THREADS=1\n" +
                "HAVE_CONFIG_H\n" +
                "JAVA_FINALIZATION=1\n" +
                "LIBDIR=\"\\\"/usr/local/lib\\\"\"\n" +
                "NO_SIGNALS=1\n" +
                "PREFIX=\"\\\"/usr/local\\\"\"\n" +
                "SILENT=1\n" +
                "SOLARIS25_PROC_VDB_BUG_FIXED=1\n" +
                "_GNU_SOURCE\n" +
                "Paths:\n" +
                "/usr/local/i386-pc-solaris2.11/include\n" +
                "/usr/local/i386-pc-solaris2.11/sys-include\n" +
                ".\n" +
                "../../../libjava\n" +
                "./include\n" +
                "./gcj\n" +
                "../../../libjava\n" +
                "include\n" +
                "../../../libjava/include\n" +
                "/export/home/gcc/boehm-gc/include\n" +
                "../../../libjava/libltdl\n" +
                "../../../libjava/libltdl\n" +
                "../../../libjava/.././libjava/../gcc\n" +
                "../../../libjava/../zlib\n" +
                "../../../libjava/../libffi/include\n" +
                "../libffi/include\n" +
                "/usr/openwin/include";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    private String processLine(String line, boolean isScriptOutput) {
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new TreeMap<String, String>();
        String what = DiscoveryUtils.gatherCompilerLine(line, isScriptOutput, userIncludes, userMacros,null);
        StringBuilder res = new StringBuilder();
        res.append("Macros:");
        for (Map.Entry<String, String> entry : userMacros.entrySet()) {
            res.append("\n");
            res.append(entry.getKey());
            if (entry.getValue() != null) {
                res.append("=");
                res.append(entry.getValue());
            }
        }
        res.append("\nPaths:");
        for (String path : userIncludes) {
            res.append("\n");
            res.append(path);
        }
        return res.toString();
    }
}
