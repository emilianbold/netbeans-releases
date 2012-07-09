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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.LogReader.CommandLineSource;

/**
 *
 * @author sg155630
 */
public class LogReaderTest extends TestCase {

    public void testWrongLibtoolCompilerInvocation() {
        testCompilerInvocation(ItemProperties.LanguageKind.Unknown, "/bin/sh ./libtool --tag=CXX --mode=compile /export/home/gcc/gccobj/gcc/xgcc ../../../libjava/gnu/gcj/natCore.cc", 1);
    }

    public void testLibtoolCCompilerInvocation() {
        testCompilerInvocation(ItemProperties.LanguageKind.C, "/bin/sh ./libtool --mode=compile /export/home/gcc/gccobj/gcc/xgcc -shared-libgcc -B/export/home/gcc/gccobj/gcc/ ../../../libjava/gnu/gcj/natCore.c", 1);
    }

    public void testLibtoolCppCompilerInvocation() {
        testCompilerInvocation(ItemProperties.LanguageKind.CPP, "./libtool --tag=CXX --mode=compile /usr/bin/g++ -shared -B/usr/lib/gcc/ filename.cc", 1);
    }

    public void testCppCompilerInvocation() {
        testCompilerInvocation(ItemProperties.LanguageKind.CPP, "/grid/common/pkgs/gcc/v4.4.0/bin/gcc -c -fpic -DVERBOSE  -pthread -fcheck-new -Wno-deprecated -m32 -g  -DBEDB_SUPPORT -D_XOPEN_SOURCE_EXTENDED=1 -DLINUX2 -I. -I..  -I/vobs/ua/include -I/vobs/rcc/include -I/grid/cva/test_ius/ius.10.2.b6/tools/inca/include -I/grid/cva/test_ius/ius.10.2.b6/tools/include -I/vobs/ua/Debug/include -I/vobs/ua/include -I/vobs/sys/include/x86-lx2-32 -I/grid/common/pkgs/purifyplus/v7.0.1/releases/purify.i386_linux2.7.0.1 ../coGuiApp.C", 1);
    }

    public void testCppCompilerInvocation2() {
        testCompilerInvocation(ItemProperties.LanguageKind.C, "/grid/common/pkgs/gcc/v4.4.0/bin/g++ -c -x c -fpic -DVERBOSE  -pthread -fcheck-new -Wno-deprecated -m32 -g  -DBEDB_SUPPORT -D_XOPEN_SOURCE_EXTENDED=1 -DLINUX2 -I. -I..  -I/vobs/ua/include -I/vobs/rcc/include -I/grid/cva/test_ius/ius.10.2.b6/tools/inca/include -I/grid/cva/test_ius/ius.10.2.b6/tools/include -I/vobs/ua/Debug/include -I/vobs/ua/include -I/vobs/sys/include/x86-lx2-32 -I/grid/common/pkgs/purifyplus/v7.0.1/releases/purify.i386_linux2.7.0.1 ../coGuiApp.C", 1);
    }

    public void testCppCompilerInvocation3() {
        testCompilerInvocation(ItemProperties.LanguageKind.C, "/grid/common/pkgs/gcc/v4.4.0/bin/gcc -c -fpic -DVERBOSE  -pthread -fcheck-new -Wno-deprecated -m32 -g  -DBEDB_SUPPORT -D_XOPEN_SOURCE_EXTENDED=1 -DLINUX2 -I. -I..  -I/vobs/ua/include -I/vobs/rcc/include -I/grid/cva/test_ius/ius.10.2.b6/tools/inca/include -I/grid/cva/test_ius/ius.10.2.b6/tools/include -I/vobs/ua/Debug/include -I/vobs/ua/include -I/vobs/sys/include/x86-lx2-32 -I/grid/common/pkgs/purifyplus/v7.0.1/releases/purify.i386_linux2.7.0.1 ../coGuiApp.c", 1);
    }

    public void testCppCompilerInvocation4() {
        testCompilerInvocation(ItemProperties.LanguageKind.CPP, "/grid/common/pkgs/gcc/v4.4.0/bin/gcc -c -x c++ -fpic -DVERBOSE  -pthread -fcheck-new -Wno-deprecated -m32 -g  -DBEDB_SUPPORT -D_XOPEN_SOURCE_EXTENDED=1 -DLINUX2 -I. -I..  -I/vobs/ua/include -I/vobs/rcc/include -I/grid/cva/test_ius/ius.10.2.b6/tools/inca/include -I/grid/cva/test_ius/ius.10.2.b6/tools/include -I/vobs/ua/Debug/include -I/vobs/ua/include -I/vobs/sys/include/x86-lx2-32 -I/grid/common/pkgs/purifyplus/v7.0.1/releases/purify.i386_linux2.7.0.1 ../coGuiApp.c", 1);
    }

    public void testMultySources() {
        testCompilerInvocation(ItemProperties.LanguageKind.CPP, "c++ -g3 -gdwarf-2 -o container_1 container_1a.cc container_1b.cc", 2);
    }
    
    public void testArtifatC() {
        testLanguageArtifact("c++", "gcc -x c++ -g3 -gdwarf-2 -o qq qq.cc");
        testLanguageArtifact("c99", "cc -g3 -gdwarf-2 -xc99 -o qq qq.cc");
        testLanguageArtifact("c99", "gcc -g3 -gdwarf-2 -std=c99 -o qq qq.cc");
        testLanguageArtifact("c89", "gcc -g3 -gdwarf-2 -std=c89 -o qq qq.cc");
    }

    public void testArtifatCpp() {
        testLanguageArtifact("c", "g++ -x c -g3 -gdwarf-2 -o qq qq.cc");
        testLanguageArtifact("c++11", "g++ -g3 -std=c++0x -gdwarf-2 -o qq qq.cc");
        testLanguageArtifact("c++11", "g++ -g3 -std=c++11 -gdwarf-2 -o qq qq.cc");
        testLanguageArtifact("c++11", "g++ -g3 -std=gnu++0x -gdwarf-2 -o qq qq.cc");
        testLanguageArtifact("c++11", "g++ -g3 -std=gnu++11 -gdwarf-2 -o qq qq.cc");
    }

    /**
    make:
        cc  -DAA=3   -DAA1='3' -DAA2="3" -DBB=\"3\" "-DBB1='3'" '-DBB2="3"'  -DBB3=\'3\' "-DBB4=\"3\"" -DBB5='"3"' -c qq.c
    build log:
        cc  -DAA=3   -DAA1='3' -DAA2="3" -DBB=\"3\" "-DBB1='3'" '-DBB2="3"'  -DBB3=\'3\' "-DBB4=\"3\"" -DBB5='"3"' -c qq.c
    compile line:
        cc  -DAA='3' -DAA1='3' -DAA2='3' -DBB='"3"'  -DBB1=''3'' -DBB2='"3"' -DBB3=''3''  -DBB4='"3"'  -DBB5='"3"' -c qq.c
    exec log:
        cc  -DAA=3   -DAA1=3   -DAA2=3   -DBB="3"    -DBB1='3'   -DBB2="3"   -DBB3='3'    -DBB4="3"    -DBB5="3"   -c qq.c
    dwarf:
        cc    AA=3     AA1=3     AA2=3     BB="3"      BB1='3'     BB2="3"     BB3='3'      BB4="3"      BB5="3"

     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testProcessLine() {
        String build   = "cc  -DAA=3   -DAA1='3' -DAA2=\"3\" -DBB=\\\"3\\\" \"-DBB1='3'\" '-DBB2=\"3\"'  -DBB3=\\\'3\\\' \"-DBB4=\\\"3\\\"\" -DBB5='\"3\"' -c qq.c";
        String compile = "cc  -DAA='3' -DAA1='3' -DAA2='3' -DBB='\"3\"'  -DBB1=''3'' -DBB2='\"3\"' -DBB3=''3''  -DBB4='\"3\"'  -DBB5='\"3\"' -c qq.c";
        String exec    = "cc  -DAA=3   -DAA1=3   -DAA2=3   -DBB=\"3\"    -DBB1='3'   -DBB2=\"3\"   -DBB3='3'    -DBB4=\"3\"    -DBB5=\"3\" -c qq.c";
        String expResult =
                      "Source:qq.c\n"+
                      "Macros:\n"+
                      "AA=3\n"+
                      "AA1=3\n"+
                      "AA2=3\n"+
                      "BB=\"3\"\n"+
                      "BB1='3'\n"+
                      "BB2=\"3\"\n"+
                      "BB3='3'\n"+
                      "BB4=\"3\"\n"+
                      "BB5=\"3\"\n"+
                      "Paths:";
        String result = processLine(build, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(build, expResult, result);
        result = processLine(compile, DiscoveryUtils.LogOrigin.DwarfCompileLine);
        assertDocumentText(compile, expResult, result);
        result = processLine(exec, DiscoveryUtils.LogOrigin.ExecLog);
        assertDocumentText(exec, expResult, result);
    }
   
    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testLinuxCommandLine() {
        String line = "gcc -Wp,-MD,kernel/.bounds.s.d  -nostdinc -isystem /usr/lib/gcc/x86_64-pc-linux-gnu/4.3.2/include -D__KERNEL__ " +
                      "-Iinclude  -I/export/home/av202691/NetBeansProjects/linux-2.6.28-gentoo-r5/arch/x86/include " +
                      "-include include/linux/autoconf.h -Wall -Wundef -Wstrict-prototypes -Wno-trigraphs " +
                      "-fno-strict-aliasing -fno-common -Werror-implicit-function-declaration -O2 " +
                      "-m64 -march=core2 -mno-red-zone -mcmodel=kernel -funit-at-a-time -maccumulate-outgoing-args " +
                      "-DCONFIG_AS_CFI=1 -DCONFIG_AS_CFI_SIGNAL_FRAME=1 -pipe -Wno-sign-compare -fno-asynchronous-unwind-tables " +
                      "-mno-sse -mno-mmx -mno-sse2 -mno-3dnow -Iarch/x86/include/asm/mach-default -fno-stack-protector -fomit-frame-pointer " +
                      "-Wdeclaration-after-statement -Wno-pointer-sign -fwrapv " +
                      "-D\"KBUILD_STR(s)=#s\" -D\"KBUILD_BASENAME=KBUILD_STR(bounds)\"  -D\"KBUILD_MODNAME=KBUILD_STR(bounds)\"  -fverbose-asm -S -o kernel/bounds.s kernel/bounds.c";
          String expResult =
                      "Source:kernel/bounds.c\n"+
                      "Macros:\n"+
                      "CONFIG_AS_CFI=1\n"+
                      "CONFIG_AS_CFI_SIGNAL_FRAME=1\n"+
                      "KBUILD_BASENAME=KBUILD_STR(bounds)\n"+
                      "KBUILD_MODNAME=KBUILD_STR(bounds)\n"+
                      "KBUILD_STR(s)=#s\n"+
                      "__KERNEL__\n"+
                      "Paths:\n"+
                      "/usr/lib/gcc/x86_64-pc-linux-gnu/4.3.2/include\n"+
                      "include\n"+
                      "/export/home/av202691/NetBeansProjects/linux-2.6.28-gentoo-r5/arch/x86/include\n"+
                      "include/linux/autoconf.h\n"+
                      "arch/x86/include/asm/mach-default";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
    }
    
    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testChromCommandLine() {
        String line = "gcc -DNSS_ENABLE_ZLIB \"-DSHLIB_PREFIX=\\\"lib\\\"\" "+
                      "\"-DSHLIB_SUFFIX=\\\"so\\\"\" \"-DSHLIB_VERSION=\\\"3\\\"\" "+
                      "\"-DSOFTOKEN_SHLIB_VERSION=\\\"3\\\"\" "+
                      "-DUSE_UTIL_DIRECTLY -c -o out/Release/obj.target/ssl/net/third_party/nss/ssl/sslcon.o net/third_party/nss/ssl/sslcon.c";
          String expResult =
                      "Source:net/third_party/nss/ssl/sslcon.c\n"+
                      "Macros:\n"+
                      "NSS_ENABLE_ZLIB\n"+
                      "SHLIB_PREFIX=\"lib\"\n"+
                      "SHLIB_SUFFIX=\"so\"\n"+
                      "SHLIB_VERSION=\"3\"\n"+
                      "SOFTOKEN_SHLIB_VERSION=\"3\"\n"+
                      "USE_UTIL_DIRECTLY\n"+
                      "Paths:";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
    }


    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testFirefoxCommandLine() {
        String line = "c++ -o nsDependentString.o -c -I../../../dist/include/system_wrappers "+
                      "-include /mozilla-1.9.1/config/gcc_hidden.h "+
                      "-DMOZILLA_INTERNAL_API -DOSTYPE=\\\"Linux2.6\\\" -DOSARCH=Linux -D_IMPL_NS_COM  "+
                      "-I/mozilla-1.9.1/xpcom/string/src -I. "+
                      "-I../../../dist/include/xpcom -I../../../dist/include   -I../../../dist/include/string "+
                      "-I/mozilla-1.9.1/ff-dbg/dist/include/nspr       "+
                      "-fPIC   -fno-rtti -fno-exceptions -Wall -Wpointer-arith -Woverloaded-virtual -Wsynth "+
                      "-Wno-ctor-dtor-privacy -Wno-non-virtual-dtor -Wcast-align -Wno-invalid-offsetof "+
                      "-Wno-long-long -pedantic -g3 -gdwarf-2 -fno-strict-aliasing -fshort-wchar -pthread "+
                      "-pipe  -DDEBUG -D_DEBUG -DDEBUG_av202691 -DTRACING -g -fno-inline   -DMOZILLA_CLIENT "+
                      "-include ../../../mozilla-config.h -Wp,-MD,.deps/nsDependentString.pp "+
                      "/mozilla-1.9.1/xpcom/string/src/nsDependentString.cpp";
        String expResult =
                      "Source:/mozilla-1.9.1/xpcom/string/src/nsDependentString.cpp\n"+
                      "Macros:\n"+
                      "DEBUG\n"+
                      "DEBUG_av202691\n"+
                      "MOZILLA_CLIENT\n"+
                      "MOZILLA_INTERNAL_API\n"+
                      "OSARCH=Linux\n"+
                      "OSTYPE=\"Linux2.6\"\n"+
                      "TRACING\n"+
                      "_DEBUG\n"+
                      "_IMPL_NS_COM\n"+
                      "Paths:\n"+
                      "../../../dist/include/system_wrappers\n"+
                      "/mozilla-1.9.1/config/gcc_hidden.h\n"+
                      "/mozilla-1.9.1/xpcom/string/src\n"+
                      ".\n"+
                      "../../../dist/include/xpcom\n"+
                      "../../../dist/include\n"+
                      "../../../dist/include/string\n"+
                      "/mozilla-1.9.1/ff-dbg/dist/include/nspr\n"+
                      "../../../mozilla-config.h";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
    }

    public void testCygdrive() {
        String CYGWIN_PATH = ":/cygwin"; // NOI18N
        String path = "D:/cygwin_dir/lib/gcc/i686-pc-cygwin/3.4.4/include";
        String cygwinPath = null;

       int i = path.toLowerCase().indexOf(CYGWIN_PATH);
       if (i > 0) {
           if (cygwinPath == null) {
               cygwinPath = "" + Character.toUpperCase(path.charAt(0)) + CYGWIN_PATH; // NOI18N
               for(i = i + CYGWIN_PATH.length();i < path.length();i++){
                   char c = path.charAt(i);
                   if (c == '/'){
                       break;
                   }
                   cygwinPath+=""+c;
               }
           }
       }
       assertEquals(cygwinPath, "D:/cygwin_dir");
    }

    public void testCygdrive2() {
        //String res =CompilerSetManager.getCygwinBase();
        String res ="D:\\cygwin_dir\\bin";
        res = res.substring(0, res.length() - 4).replace("\\", "/"); // NOI18N
        if (res != null && res.endsWith("/")){
            res = res.substring(0,res.length()-1);
        }
       assertEquals(res, "D:/cygwin_dir");
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine() {
        String line = "/set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.";
        String expResult = "Source:main.cc\nMacros:\nHELLO=75\nPaths:\ndist";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
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
        String expResult = "Source:../../i86pc/os/cpupm.c\n" +
                "Macros:\n" +
                "DIS_MEM\n" +
                "_ASM_INLINES\n" +
                "_ELF64\n" +
                "_KERNEL\n" +
                "_SYSCALL32\n" +
                "_SYSCALL32_IMPL\n" +
                "Undefs:\n" +
                "i386\n" +
                "__i386\n" +
                "Paths:\n" +
                "../../i86pc\n" +
                "/export/opensolaris/testws77/usr/src/common\n" +
                "../../intel" +
                "\n../../common";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
        assertDocumentText(line, expResult, result);
    }

    
    /**
     * Test of undef
     */
    public void testUndef() {
        String line = "gcc -E -v -DA -UA file.cc 2>/dev/null";
        String expResult = "Source:file.cc\n" +
                "Macros:\n" +
                "Paths:";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
        assertDocumentText(line, expResult, result);
    }
    
    /**
     * Test of undef
     */
    public void testUndef2() {
        String line = "gcc -E -v -DA -UA -DA file.cc 2>/dev/null";
        String expResult = "Source:file.cc\n" +
                "Macros:\n" +
                "A\n" +
                "Paths:";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
        assertDocumentText(line, expResult, result);
    }
    
    /**
     * Test of undef
     */
    public void testUndef3() {
        String line = "gcc -E -v -UA -DA file.cc 2>/dev/null";
        String expResult = "Source:file.cc\n" +
                "Macros:\n" +
                "A\n" +
                "Paths:";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of undef
     */
    public void testUndef4() {
        String line = "gcc -E -v -UA -UA -DA file.cc 2>/dev/null";
        String expResult = "Source:file.cc\n" +
                "Macros:\n" +
                "A\n" +
                "Paths:";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of undef
     */
    public void testUndef5() {
        String line = "gcc -E -v -DA -DA -UA -UA file.cc 2>/dev/null";
        String expResult = "Source:file.cc\n" +
                "Macros:\n" +
                "Undefs:\n" +
                "A\n" +
                "Paths:";
        String result = processLine(line, DiscoveryUtils.LogOrigin.DwarfCompileLine);
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
                "-xdebugformat=stabs -DTEXT_DOMAIN=\\\"SUNW_OST_OSLIB\\\" -D_TS_ERRNO  " +
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
        String expResult = "Source:../common/data/builtins.c\n" +
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
                "Undefs:\n" +
                "i386\n" +
                "__i386\n" +
                "Paths:\n" +
                "src/cmd/ksh93\n" +
                "../common/include\n" +
                "/export/home/thp/opensolaris/proto/root_i386/usr/include/ast";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testGccLine() {
        String line = "/bin/sh ./libtool --tag=CXX --mode=compile /export/home/gcc/gccobj/gcc/xgcc -shared-libgcc -B/export/home/gcc/gccobj/gcc/ -nostdinc++ -L/export/home/gcc/gccobj/i386-pc-solaris2.11/libstdc++-v3/src -L/export/home/gcc/gccobj/i386-pc-solaris2.11/libstdc++-v3/src/.libs -B/usr/local/i386-pc-solaris2.11/bin/ -B/usr/local/i386-pc-solaris2.11/lib/ -isystem /usr/local/i386-pc-solaris2.11/include -isystem /usr/local/i386-pc-solaris2.11/sys-include -DHAVE_CONFIG_H -I. -I../../../libjava -I./include -I./gcj -I../../../libjava -Iinclude -I../../../libjava/include -I/export/home/gcc/boehm-gc/include  -DGC_SOLARIS_THREADS=1 -DGC_SOLARIS_PTHREADS=1 -DSOLARIS25_PROC_VDB_BUG_FIXED=1 -DSILENT=1 -DNO_SIGNALS=1 -DALL_INTERIOR_POINTERS=1 -DJAVA_FINALIZATION=1 -DGC_GCJ_SUPPORT=1 -DATOMIC_UNCOLLECTABLE=1   -I../../../libjava/libltdl -I../../../libjava/libltdl  -I../../../libjava/.././libjava/../gcc -I../../../libjava/../zlib -I../../../libjava/../libffi/include -I../libffi/include  -O2 -g3 -gdwarf-2 -fno-rtti -fnon-call-exceptions  -fdollars-in-identifiers -Wswitch-enum -ffloat-store  -I/usr/openwin/include -W -Wall -D_GNU_SOURCE -DPREFIX=\"\\\"/usr/local\\\"\" -DLIBDIR=\"\\\"/usr/local/lib\\\"\" -DBOOT_CLASS_PATH=\"\\\"/usr/local/share/java/libgcj-3.4.3.jar\\\"\" -g3 -gdwarf-2 -MD -MT gnu/gcj/natCore.lo -MF gnu/gcj/natCore.pp -c -o gnu/gcj/natCore.lo ../../../libjava/gnu/gcj/natCore.cc";
        String expResult ="Source:../../../libjava/gnu/gcj/natCore.cc\n" +
                "Macros:\n" +
                "ALL_INTERIOR_POINTERS=1\n" +
                "ATOMIC_UNCOLLECTABLE=1\n" +
                "BOOT_CLASS_PATH=\"/usr/local/share/java/libgcj-3.4.3.jar\"\n" +
                "GC_GCJ_SUPPORT=1\n" +
                "GC_SOLARIS_PTHREADS=1\n" +
                "GC_SOLARIS_THREADS=1\n" +
                "HAVE_CONFIG_H\n" +
                "JAVA_FINALIZATION=1\n" +
                "LIBDIR=\"/usr/local/lib\"\n" +
                "NO_SIGNALS=1\n" +
                "PREFIX=\"/usr/local\"\n" +
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
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
    }

    //gcc -DDEFAULT_BASEDIR=\"/usr/local\" -DDATADIR="\"/usr/local/var\"" -DDEFAULT_CHARSET_HOME="\"/usr/local\"" -DSHAREDIR="\"/usr/local/share/mysql\"" -DDEFAULT_HOME_ENV=MYSQL_HOME -DDEFAULT_GROUP_SUFFIX_ENV=MYSQL_GROUP_SUFFIX -DDEFAULT_SYSCONFDIR="\"/usr/local/etc\"" -DHAVE_CONFIG_H -I. -I../include -I../include -I../include -I.    -O3    -DHAVE_RWLOCK_T -DUNIV_SOLARIS -MT my_init.o -MD -MP -MF .deps/my_init.Tpo -c -o my_init.o my_init.c
    public void testGccLine2() {
        String line = "gcc -DDEFAULT_BASEDIR=\\\"/usr/local\\\" -DDATADIR=\"\\\"/usr/local/var\\\"\" -DDEFAULT_CHARSET_HOME=\"\\\"/usr/local\\\"\" -DSHAREDIR=\"\\\"/usr/local/share/mysql\\\"\" -DDEFAULT_HOME_ENV=MYSQL_HOME -DDEFAULT_GROUP_SUFFIX_ENV=MYSQL_GROUP_SUFFIX -DDEFAULT_SYSCONFDIR=\"\\\"/usr/local/etc\\\"\" -DHAVE_CONFIG_H -I. -I../include   -O3    -DHAVE_RWLOCK_T -DUNIV_SOLARIS -MT my_init.o -MD -MP -MF .deps/my_init.Tpo -c -o my_init.o my_init.c";
        String expResult =
                "Source:my_init.c\n" +
                "Macros:\n" +
                "DATADIR=\"/usr/local/var\"\n" +
                "DEFAULT_BASEDIR=\"/usr/local\"\n" +
                "DEFAULT_CHARSET_HOME=\"/usr/local\"\n" +
                "DEFAULT_GROUP_SUFFIX_ENV=MYSQL_GROUP_SUFFIX\n" +
                "DEFAULT_HOME_ENV=MYSQL_HOME\n" +
                "DEFAULT_SYSCONFDIR=\"/usr/local/etc\"\n" +
                "HAVE_CONFIG_H\n" +
                "HAVE_RWLOCK_T\n" +
                "SHAREDIR=\"/usr/local/share/mysql\"\n" +
                "UNIV_SOLARIS\n" +
                "Paths:\n" +
                ".\n" +
                "../include";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
    }

    public void testMSVCCompilerInvocation() {
        String line = "cl -Zi -Od -MDd -I/ws/cheetah/output/win32_mvm_debug/javacall/inc "+
                "/Zm1000 -DENABLE_CDC=0 -DENABLE_MIDP_MALLOC=1 -DENABLE_IMAGE_CACHE=1 "+
                "-DENABLE_ICON_CACHE=1 -DENABLE_I3_TEST=0    -DENABLE_NUTS_FRAMEWORK=0    "+
                "-DENABLE_NETWORK_INDICATOR=1 -DENABLE_MULTIPLE_ISOLATES=1 -DENABLE_MULTIPLE_DISPLAYS=0 "+
                "-DENABLE_JAVA_DEBUGGER=1 -DENABLE_NATIVE_APP_MANAGER=0 -DENABLE_NAMS_TEST_SERVICE=0 "+
                "-DENABLE_NATIVE_INSTALLER=0 -DENABLE_NATIVE_SUITE_STORAGE=0 -DENABLE_NATIVE_RMS=0 "+
                "-DENABLE_NATIVE_PTI=0 -DENABLE_MESSAGE_STRINGS=0 -DENABLE_CLDC_11=1 -DENABLE_VM_PROFILES=0 "+
                "-DENABLE_MONET=0 -DENABLE_SERVER_SOCKET=1 -DENABLE_JPEG=0 -DENABLE_DIRECT_DRAW=0 "+
                "-DENABLE_FILE_SYSTEM=1 -DENABLE_ON_DEVICE_DEBUG=1 -DENABLE_WTK_DEBUG=0 -DENABLE_AMS_FOLDERS=0 "+
                "-DENABLE_OCSP=0 -DENABLE_DYNAMIC_COMPONENTS=0 -DPROJECT_NAME='\"Sun Java Wireless Client\"' "+
                "-DAZZERT=1 -DENABLE_DEBUG=1 -DENABLE_CONTROL_ARGS_FROM_JAD=0 -DRELEASE='\"ap160621:11.28.08-19:33\"' "+
                "-DIMPL_VERSION='\"\"' -DFULL_VERSION='\"ap160621:11.28.08-19:33\"' -DROMIZING "+
                "-I/ws/cheetah/output/win32_mvm_debug/javacall/inc -DWIN32 -D_WINDOWS -D_DEBUG "+
                "-DAZZERT /W3 /nologo  -DHARDWARE_LITTLE_ENDIAN=1 -DHOST_LITTLE_ENDIAN=1 "+
                "/D ROMIZING -DJVM_RELEASE_VERSION='1.1' -DJVM_BUILD_VERSION='internal' "+
                "-DJVM_NAME='phoneME Feature VM' /MDd /Zi /Od  -DREQUIRES_JVMCONFIG_H=1 "+
                "-DENABLE_JSR_135=1 -DENABLE_MEDIA_RECORD                                       "+
                "-I/ws/cheetah/midp/src/protocol/socket/include       "+
                "-I/ws/cheetah/output/win32_mvm_debug/cldc/javacall_i386_vc/dist/include "+
                "-I/ws/cheetah/output/win32_mvm_debug/midp -I/ws/cheetah/output/win32_mvm_debug/pcsl/javacall_i386/inc "+
                "-I/ws/cheetah/abstractions/src/share/include -I/ws/cheetah/abstractions/src/cldc_application/native/include "+
                "-I/ws/cheetah/abstractions/src/cldc_application/native/javacall "+
                "-I/ws/cheetah/output/win32_mvm_debug/midp/generated  -I/ws/cheetah/midp/src/configuration/properties_port/include "+
                "-I/ws/cheetah/midp/src/core/suspend_resume/sr_main/include -I/ws/cheetah/midp/src/core/suspend_resume/sr_vm/include "+
                "-I/ws/cheetah/midp/src/core/suspend_resume/sr_port/include  -I/ws/cheetah/midp/src/core/crc32/include "+
                "-I/ws/cheetah/midp/src/core/jarutil/include -I/ws/cheetah/midp/src/core/global_status/include  "+
                "-I/ws/cheetah/midp/src/core/kni_util/include -I/ws/cheetah/midp/src/core/libc_ext/include "+
                "-I/ws/cheetah/midp/src/core/log/javacall/include -I/ws/cheetah/midp/src/core/native_thread/include "+
                "-I/ws/cheetah/midp/src/core/native_thread/stubs/include  -I/ws/cheetah/midp/src/core/resource_manager/include "+
                "-I/ws/cheetah/midp/src/core/timer_queue/include -I/ws/cheetah/midp/src/core/timer_queue/reference/include "+
                "-I/ws/cheetah/midp/src/core/timezone/include -I/ws/cheetah/midp/src/core/vm_services/include "+
                "-I/ws/cheetah/midp/src/core/memory/include -I/ws/cheetah/midp/src/core/storage/include "+
                "-I/ws/cheetah/midp/src/core/string/include -I/ws/cheetah/midp/src/events/eventqueue/include "+
                "-I/ws/cheetah/midp/src/events/eventqueue_port/include -I/ws/cheetah/midp/src/events/eventsystem/include "+
                "-I/ws/cheetah/midp/src/events/mastermode_port/include  -I/ws/cheetah/midp/src/ams/ams_base/include "+
                "-I/ws/cheetah/midp/src/ams/ams_base_cldc/include -I/ws/cheetah/midp/src/ams/platform_request/include "+
                "-I/ws/cheetah/midp/src/ams/suitestore/internal_api/include -I/ws/cheetah/midp/src/ams/suitestore/internal_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/common_api/include -I/ws/cheetah/midp/src/ams/suitestore/common_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/task_manager_api/include -I/ws/cheetah/midp/src/ams/suitestore/task_manager_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/installer_api/include -I/ws/cheetah/midp/src/ams/suitestore/installer_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/recordstore_api/include -I/ws/cheetah/midp/src/ams/suitestore/recordstore_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/secure_api/include -I/ws/cheetah/midp/src/ams/suitestore/secure_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/appmanager_ui_resources/include -I/ws/cheetah/midp/src/ams/example/ams_common/include "+
                "-I/ws/cheetah/midp/src/ams/example/ams_common_port/include -I/ws/cheetah/midp/src/ams/example/jams/include "+
                "-I/ws/cheetah/midp/src/ams/example/jams_port/javacall/native -I/ws/cheetah/midp/src/ams/example/ams_params/include "+
                "-I/ws/cheetah/midp/src/ams/example/ams_common/include -I/ws/cheetah/midp/src/ams/example/ams_common/include "+
                "-I/ws/cheetah/midp/src/ams/example/ams_common_port/include -I/ws/cheetah/midp/src/ams/example/javacall_common/include "+
                "-I/ws/cheetah/midp/src/ams/example/jams_port/include      -I/ws/cheetah/midp/src/push/push_server/include "+
                "-I/ws/cheetah/midp/src/push/push_timer/include -I/ws/cheetah/midp/src/push/push_timer/javacall/include "+
                "-I/ws/cheetah/midp/src/i18n/i18n_main/include -I/ws/cheetah/midp/src/i18n/i18n_port/include "+
                "-I/ws/cheetah/midp/src/highlevelui/annunciator/include -I/ws/cheetah/midp/src/highlevelui/keymap/include  "+
                "-I/ws/cheetah/midp/src/highlevelui/lcdlf/include -I/ws/cheetah/midp/src/highlevelui/lcdlf/lfjava/include "+
                "-I/ws/cheetah/midp/src/highlevelui/lfjport/include  -I/ws/cheetah/midp/src/highlevelui/nim_port/include "+
                "-I/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_app_common/include "+
                "-I/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_mode_port/include  "+
                "-I/ws/cheetah/midp/src/lowlevelui/graphics_api/include -I/ws/cheetah/midp/src/lowlevelui/putpixel_port/include "+
                "-I/ws/cheetah/midp/src/lowlevelui/graphics/include -I/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/include "+
                "-I/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/native -I/ws/cheetah/midp/src/lowlevelui/image_api/include "+
                "-I/ws/cheetah/midp/src/lowlevelui/image/include  -I/ws/cheetah/midp/src/lowlevelui/image_decode/reference/native "+
                "-I/ws/cheetah/midp/src/lowlevelui/image_decode/include    -I/ws/cheetah/midp/src/rms/record_store/include "+
                "-I/ws/cheetah/midp/src/rms/record_store/file_based/native -I/ws/cheetah/midp/src/security/crypto/include "+
                "-I/ws/cheetah/midp/src/security/file_digest/include -I/ws/cheetah/midp/src/protocol/gcf/include "+
                "-I/ws/cheetah/midp/src/protocol/file/include    -I/ws/cheetah/midp/src/protocol/serial_port/include "+
                "-I/ws/cheetah/midp/src/protocol/socket/include    -I/ws/cheetah/midp/src/protocol/udp/include  "+
                "-I/ws/cheetah/midp/src/links/include     -I/ws/cheetah/jsr135/src/cldc_application/native/common "+
                "-I/ws/cheetah/jsr135/src/share/components/direct-player/native  -c "+
                "-Fo/ws/cheetah/output/win32_mvm_debug/midp/obj_g/i386/socketProtocol.o "+
                "`echo  /ws/cheetah/midp/src/protocol/socket/reference/native/socketProtocol.c | xargs -n1 cygpath -w` 	"+
                "> /ws/cheetah/output/win32_mvm_debug/midp/makelog.out 2>&1; status=$?; cat /ws/cheetah/output/win32_mvm_debug/midp/makelog.out | "+
                "tee -a /ws/cheetah/output/win32_mvm_debug/midp/make.out; if [ $status -ne 0 ]; then false; else true; fi";
        String expResult ="Source:/ws/cheetah/midp/src/protocol/socket/reference/native/socketProtocol.c\n"+
                "Macros:\n"+
                "AZZERT\n"+
                "ENABLE_AMS_FOLDERS=0\n"+
                "ENABLE_CDC=0\n"+
                "ENABLE_CLDC_11=1\n"+
                "ENABLE_CONTROL_ARGS_FROM_JAD=0\n"+
                "ENABLE_DEBUG=1\n"+
                "ENABLE_DIRECT_DRAW=0\n"+
                "ENABLE_DYNAMIC_COMPONENTS=0\n"+
                "ENABLE_FILE_SYSTEM=1\n"+
                "ENABLE_I3_TEST=0\n"+
                "ENABLE_ICON_CACHE=1\n"+
                "ENABLE_IMAGE_CACHE=1\n"+
                "ENABLE_JAVA_DEBUGGER=1\n"+
                "ENABLE_JPEG=0\n"+
                "ENABLE_JSR_135=1\n"+
                "ENABLE_MEDIA_RECORD\n"+
                "ENABLE_MESSAGE_STRINGS=0\n"+
                "ENABLE_MIDP_MALLOC=1\n"+
                "ENABLE_MONET=0\n"+
                "ENABLE_MULTIPLE_DISPLAYS=0\n"+
                "ENABLE_MULTIPLE_ISOLATES=1\n"+
                "ENABLE_NAMS_TEST_SERVICE=0\n"+
                "ENABLE_NATIVE_APP_MANAGER=0\n"+
                "ENABLE_NATIVE_INSTALLER=0\n"+
                "ENABLE_NATIVE_PTI=0\n"+
                "ENABLE_NATIVE_RMS=0\n"+
                "ENABLE_NATIVE_SUITE_STORAGE=0\n"+
                "ENABLE_NETWORK_INDICATOR=1\n"+
                "ENABLE_NUTS_FRAMEWORK=0\n"+
                "ENABLE_OCSP=0\n"+
                "ENABLE_ON_DEVICE_DEBUG=1\n"+
                "ENABLE_SERVER_SOCKET=1\n"+
                "ENABLE_VM_PROFILES=0\n"+
                "ENABLE_WTK_DEBUG=0\n"+
                "FULL_VERSION=\"ap160621:11.28.08-19:33\"\n"+
                "HARDWARE_LITTLE_ENDIAN=1\n"+
                "HOST_LITTLE_ENDIAN=1\n"+
                "IMPL_VERSION=\"\"\n"+
                "JVM_BUILD_VERSION=internal\n"+
                "JVM_NAME=phoneME Feature VM\n"+
                "JVM_RELEASE_VERSION=1.1\n"+
                "PROJECT_NAME=\"Sun Java Wireless Client\"\n"+
                "RELEASE=\"ap160621:11.28.08-19:33\"\n"+
                "REQUIRES_JVMCONFIG_H=1\n"+
                "ROMIZING\n"+
                "WIN32\n"+
                "_DEBUG\n"+
                "_WINDOWS\n"+
                "Paths:\n"+
                "/ws/cheetah/output/win32_mvm_debug/javacall/inc\n"+
                "/ws/cheetah/output/win32_mvm_debug/javacall/inc\n"+
                "/ws/cheetah/midp/src/protocol/socket/include\n"+
                "/ws/cheetah/output/win32_mvm_debug/cldc/javacall_i386_vc/dist/include\n"+
                "/ws/cheetah/output/win32_mvm_debug/midp\n"+
                "/ws/cheetah/output/win32_mvm_debug/pcsl/javacall_i386/inc\n"+
                "/ws/cheetah/abstractions/src/share/include\n"+
                "/ws/cheetah/abstractions/src/cldc_application/native/include\n"+
                "/ws/cheetah/abstractions/src/cldc_application/native/javacall\n"+
                "/ws/cheetah/output/win32_mvm_debug/midp/generated\n"+
                "/ws/cheetah/midp/src/configuration/properties_port/include\n"+
                "/ws/cheetah/midp/src/core/suspend_resume/sr_main/include\n"+
                "/ws/cheetah/midp/src/core/suspend_resume/sr_vm/include\n"+
                "/ws/cheetah/midp/src/core/suspend_resume/sr_port/include\n"+
                "/ws/cheetah/midp/src/core/crc32/include\n"+
                "/ws/cheetah/midp/src/core/jarutil/include\n"+
                "/ws/cheetah/midp/src/core/global_status/include\n"+
                "/ws/cheetah/midp/src/core/kni_util/include\n"+
                "/ws/cheetah/midp/src/core/libc_ext/include\n"+
                "/ws/cheetah/midp/src/core/log/javacall/include\n"+
                "/ws/cheetah/midp/src/core/native_thread/include\n"+
                "/ws/cheetah/midp/src/core/native_thread/stubs/include\n"+
                "/ws/cheetah/midp/src/core/resource_manager/include\n"+
                "/ws/cheetah/midp/src/core/timer_queue/include\n"+
                "/ws/cheetah/midp/src/core/timer_queue/reference/include\n"+
                "/ws/cheetah/midp/src/core/timezone/include\n"+
                "/ws/cheetah/midp/src/core/vm_services/include\n"+
                "/ws/cheetah/midp/src/core/memory/include\n"+
                "/ws/cheetah/midp/src/core/storage/include\n"+
                "/ws/cheetah/midp/src/core/string/include\n"+
                "/ws/cheetah/midp/src/events/eventqueue/include\n"+
                "/ws/cheetah/midp/src/events/eventqueue_port/include\n"+
                "/ws/cheetah/midp/src/events/eventsystem/include\n"+
                "/ws/cheetah/midp/src/events/mastermode_port/include\n"+
                "/ws/cheetah/midp/src/ams/ams_base/include\n"+
                "/ws/cheetah/midp/src/ams/ams_base_cldc/include\n"+
                "/ws/cheetah/midp/src/ams/platform_request/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/internal_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/internal_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/common_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/common_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/task_manager_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/task_manager_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/installer_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/installer_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/recordstore_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/recordstore_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/secure_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/secure_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/appmanager_ui_resources/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common_port/include\n"+
                "/ws/cheetah/midp/src/ams/example/jams/include\n"+
                "/ws/cheetah/midp/src/ams/example/jams_port/javacall/native\n"+
                "/ws/cheetah/midp/src/ams/example/ams_params/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common_port/include\n"+
                "/ws/cheetah/midp/src/ams/example/javacall_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/jams_port/include\n"+
                "/ws/cheetah/midp/src/push/push_server/include\n"+
                "/ws/cheetah/midp/src/push/push_timer/include\n"+
                "/ws/cheetah/midp/src/push/push_timer/javacall/include\n"+
                "/ws/cheetah/midp/src/i18n/i18n_main/include\n"+
                "/ws/cheetah/midp/src/i18n/i18n_port/include\n"+
                "/ws/cheetah/midp/src/highlevelui/annunciator/include\n"+
                "/ws/cheetah/midp/src/highlevelui/keymap/include\n"+
                "/ws/cheetah/midp/src/highlevelui/lcdlf/include\n"+
                "/ws/cheetah/midp/src/highlevelui/lcdlf/lfjava/include\n"+
                "/ws/cheetah/midp/src/highlevelui/lfjport/include\n"+
                "/ws/cheetah/midp/src/highlevelui/nim_port/include\n"+
                "/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_app_common/include\n"+
                "/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_mode_port/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics_api/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/putpixel_port/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/native\n"+
                "/ws/cheetah/midp/src/lowlevelui/image_api/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/image/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/image_decode/reference/native\n"+
                "/ws/cheetah/midp/src/lowlevelui/image_decode/include\n"+
                "/ws/cheetah/midp/src/rms/record_store/include\n"+
                "/ws/cheetah/midp/src/rms/record_store/file_based/native\n"+
                "/ws/cheetah/midp/src/security/crypto/include\n"+
                "/ws/cheetah/midp/src/security/file_digest/include\n"+
                "/ws/cheetah/midp/src/protocol/gcf/include\n"+
                "/ws/cheetah/midp/src/protocol/file/include\n"+
                "/ws/cheetah/midp/src/protocol/serial_port/include\n"+
                "/ws/cheetah/midp/src/protocol/socket/include\n"+
                "/ws/cheetah/midp/src/protocol/udp/include\n"+
                "/ws/cheetah/midp/src/links/include\n"+
                "/ws/cheetah/jsr135/src/cldc_application/native/common\n"+
                "/ws/cheetah/jsr135/src/share/components/direct-player/native";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertDocumentText(line, expResult, result);
        LogReader.LineInfo li = LogReader.testCompilerInvocation(line);
        assert li.compilerType == LogReader.CompilerType.CPP;
    }

    public void testIcpcInvocation() {
        String line = "usr/local/packages/icc_remote/12.0.5.225_fixbug13889838/bin/icpc xsolmod.cpp -c -o xsolmod.o  -O2  -DOCCI_NO_WSTRING=1 -fPIC -cxxlib -std=c89 "+
                "-fno-omit-frame-pointer -mp1 -fp_port -mP2OPT_convert_opt=F  -fno-strict-aliasing -sox=profile -sox=inline "+
                "  -no-global-hoist -mGLOB_preemption_model=3  -hotpatch  -wd191 -wd175 -wd188 -wd810 -we127 -we1345 -we1338 -wd279 "+
                "-wd186 -wd1572 -wd589 -wd11505 -we592 -Qoption,cpp,--treat_func_as_string_literal -mPGOPTI_func_group -mPGOPTI_conv_icall_pgosf=FALSE "+
                "-vec-report0 -std=c89 -fno-omit-frame-pointer -mp1 -fp_port -mP2OPT_convert_opt=F  -fno-strict-aliasing -sox=profile -sox=inline   "+
                "-no-global-hoist -mGLOB_preemption_model=3  -hotpatch  -wd191 -wd175 -wd188 -wd810 -we127 -we1345 -we1338 -wd279 -wd186 -wd1572 "+
                "-wd589 -wd11505 -we592 -Qoption,cpp,--treat_func_as_string_literal -mPGOPTI_func_group -mPGOPTI_conv_icall_pgosf=FALSE "+
                "-vec-report0 -DXSMODNAME=xsolapi -DXSolapi -Wall -Wcheck -w2 -Wunused-function -we55 -we140 -we266 -we117 -we167 -we1418 "+
                "-wd981 -wd869 -wd174 -wd111 -wd593 -wd177 -wd1684 -wd193 -wd2415 -wd2545 -wd2259 -wd2557 -DTRUSTED_OLAPI -wd1476 -wd1505  "+
                "-I/ade/b/1226108341/oracle/rdbms/src/hdir -I/ade/b/1226108341/oracle/rdbms/public -I/ade/b/1226108341/oracle/rdbms/include "+
                "-I/ade/b/1226108341/oracle/rdbms/src/port/generic -I/ade/b/1226108341/oracle/oraolap/src/include -I/ade/b/1226108341/oracle/oraolap/src/xsolapi "+
                "-I/ade/b/1226108341/oracle/oraolap/public -Iport/server -Iport/generic   -I/ade/b/1226108341/oracle/oracore/include "+
                "-I/ade/b/1226108341/oracle/oracore/public -I/ade/b/1226108341/oracle/oracore/port/include -I/ade/b/1226108341/oracle/xdk/include "+
                "-I/ade/b/1226108341/oracle/xdk/public -I/ade/b/1226108341/oracle/ldap/public/sslinc -I/ade/b/1226108341/oracle/ldap/include/sslinc "+
                "-I/ade/b/1226108341/oracle/ldap/include/cryptoinc -I/ade/b/1226108341/oracle/network/public -I/ade/b/1226108341/oracle/network/include "+
                "-I/ade/b/1226108341/oracle/plsql/public -I/ade/b/1226108341/oracle/plsql/include -I/ade/b/1226108341/oracle/javavm/include "+
                "-I/ade/b/1226108341/oracle/has/include -I/ade/b/1226108341/oracle/opsm/include -I/ade/b/1226108341/oracle/ldap/public "+
                "-I/ade/b/1226108341/oracle/ldap/include -I/ade/b/1226108341/oracle/nlsrtl/include -I/ade/b/1226108341/oracle/oss/include   "+
                "-DLINUX -DORAX86_64 -D_GNU_SOURCE -D_LARGEFILE64_SOURCE=1 -D_LARGEFILE_SOURCE=1 -DSLTS_ENABLE -DSLMXMX_ENABLE -D_REENTRANT "+
                "-DNS_THREADS -DLONG_IS_64 -DSS_64BIT_SERVER -DLDAP_CM -DBNRMAJVSN=12 -DBNRMINVSN=1 -DBNRMIDTVSN=0 -DBNRPMAJVSN=0 -DBNRPMINVSN=2 "+
                "-DBNRMAJVSN_STR=\\\"12\\\" -DBNRMAJVSNLETTER_STR=\\\"12c\\\" -DBNRMINVSN_STR=\\\"1\\\" -DBNRMIDTVSN_STR=\\\"0\\\" -DBNRPMAJVSN_STR=\\\"0\\\" "+
                "DBNRPMINVSN_STR=\\\"2\\\" -DBNRVERSION_STR=\\\"12.1.0.0.2\\\" -DBNRSTATUS_STR=\\\"Beta\\\" -DBNRSTATUS_MAC=BNRBETA -DBNRCURRYEAR=2012 "+
                "-DBNRCURRYEAR_STR=\\\"2012\\\" -DPLSQLNCG_SUPPORTED=1  -DNTEV_USE_POLL -DNTEV_USE_QUEUE -DNTEV_USE_GENERIC -DNTEV_USE_EPOLL";
        String result = processLine(line, DiscoveryUtils.LogOrigin.BuildLog);
        assertTrue(result.startsWith("Source:xsolmod.cpp"));
        //assertDocumentText(line, expResult, result);
        LogReader.LineInfo li = LogReader.testCompilerInvocation(line);
        assertEquals(li.compilerType, LogReader.CompilerType.CPP);
    }

    private String processLine(String line, DiscoveryUtils.LogOrigin isScriptOutput) {
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new TreeMap<String, String>();
        List<String> undefs = new ArrayList<String>();
        List<String> languageArtifacts = new ArrayList<String>();
        line = LogReader.trimBackApostropheCalls(line, null);
        Pattern pattern = Pattern.compile(";|\\|\\||&&"); // ;, ||, && //NOI18N
        String[] cmds = pattern.split(line);
        String what = DiscoveryUtils.gatherCompilerLine(cmds[0], isScriptOutput, userIncludes, userMacros, undefs, null, languageArtifacts, null, false).get(0);
        StringBuilder res = new StringBuilder();
        res.append("Source:").append(what).append("\n");
        res.append("Macros:");
        for (Map.Entry<String, String> entry : userMacros.entrySet()) {
            res.append("\n");
            res.append(entry.getKey());
            if (entry.getValue() != null) {
                res.append("=");
                res.append(entry.getValue());
            }
        }
        if (!undefs.isEmpty()) {
            res.append("\nUndefs:");
            for (String undef : undefs) {
                res.append("\n");
                res.append(undef);
            }
        }
        res.append("\nPaths:");
        for (String path : userIncludes) {
            res.append("\n");
            res.append(path);
        }
        return res.toString();
    }

    /**
     * Assert whether the document available through {@link #getDocument()}
     * has a content equal to <code>expectedText</code>.
     */
    private void assertDocumentText(String line, String expResult, String result) {
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
                sb.append("Diff starts in line ").append(startLine).append("\n");
                String context = expResult.substring(i);
                if (context.length() > 40) {
                    context = context.substring(0, 40);
                }
                sb.append("Expected ").append(context).append("\n");
                context = result.substring(i);
                if (context.length() > 40) {
                    context = context.substring(0, 40);
                }
                sb.append("Found ").append(context).append("\n");
                break;
            }
        }
        assertFalse(sb.toString(), true);
    }

    private void testCompilerInvocation(ItemProperties.LanguageKind ct, String line, int size) {
        LogReader.LineInfo li = LogReader.testCompilerInvocation(line);
        if (ct == ItemProperties.LanguageKind.Unknown) {
            assertEquals(li.getLanguage(), ct);
            return;
        }
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new HashMap<String, String>();
        List<String> undefs = new ArrayList<String>();
        List<String> languageArtifacts = new ArrayList<String>();
        List<String> sourcesList = DiscoveryUtils.gatherCompilerLine(line, DiscoveryUtils.LogOrigin.BuildLog, userIncludes, userMacros, undefs, null, languageArtifacts, null, false);
        assertTrue(sourcesList.size() == size);
        for(String what :sourcesList) {
            CommandLineSource cs = new CommandLineSource(li, languageArtifacts, "/", what, userIncludes, userMacros, undefs, null);
            assertEquals(cs.getLanguageKind(), ct);
        }
    }

    private void testLanguageArtifact(String artifact, String line) {
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new HashMap<String, String>();
        List<String> languageArtifacts = new ArrayList<String>();
        DiscoveryUtils.gatherCompilerLine(line, DiscoveryUtils.LogOrigin.BuildLog, userIncludes, userMacros, null, null, languageArtifacts, null, false);
        assert languageArtifacts.contains(artifact);
    }
}
