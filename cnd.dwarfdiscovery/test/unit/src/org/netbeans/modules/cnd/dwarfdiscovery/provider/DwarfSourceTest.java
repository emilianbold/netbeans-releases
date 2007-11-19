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

import java.util.Iterator;
import junit.framework.TestCase;

/**
 *
 * @author Alexander Simon
 */
public class DwarfSourceTest extends TestCase {

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine() {
        String line = "/set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.";
        String expResult = "Macros:\nHELLO=75\nPaths:\ndist";
        String result = processLine(DwarfSource.scanCommandLine(line).iterator());
        if (!expResult.equals(result)) {
            assertFalse("Parsing line:"+line+"\nExpected:\n"+expResult+"\nFound:\n"+result, true);
        }
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine2() {
        String line = "/opt/SUNWspro/bin/cc -xarch=amd64 -Ui386 -U__i386 -xO3 ../../intel/amd64/ml/amd64.il "+
                "../../i86pc/ml/amd64.il -D_ASM_INLINES -Xa -xspace -Wu,-xmodel=kernel -Wu,-save_args -v "+
                "-xildoff -g -xc99=%all -W0,-noglobal -g3 -gdwarf-2 -g3 -gdwarf-2 -errtags=yes -errwarn=%all "+
                "-W0,-xglobalstatic -xstrconst -DDIS_MEM -D_KERNEL -D_SYSCALL32 -D_SYSCALL32_IMPL -D_ELF64 "+
                "-I../../i86pc -I/export/opensolaris/testws77/usr/src/common -I../../intel -Y I,../../common "+
                "-c -o debug64/cpupm.o ../../i86pc/os/cpupm.c";
        String expResult = "Macros:\n_ASM_INLINES\nDIS_MEM\n_KERNEL\n_SYSCALL32\n_SYSCALL32_IMPL\n_ELF64\n"+
                           "Paths:\n../../i86pc\n/export/opensolaris/testws77/usr/src/common\n../../intel\n../../common";
        String result = processLine(DwarfSource.scanCommandLine(line).iterator());
        if (!expResult.equals(result)) {
            assertFalse("Parsing line:"+line+"\nExpected:\n"+expResult+"\nFound:\n"+result, true);
        }
    }
    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine3() {
        String line = "/opt/SUNWspro/bin/cc -xO3 -xarch=amd64 -Ui386 -U__i386 -Xa -xildoff -errtags=yes "+
                "-errwarn=%all -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED "+
                "-erroff=E_UNRECOGNIZED_PRAGMA_IGNORED -xc99=%all -D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1 "+
                "-W0,-xglobalstatic -v -xstrconst -DTEXT_DOMAIN=\"SUNW_OST_OSCMD\" -D_TS_ERRNO "+
                "-I/export/opensolaris/testws77/proto/root_i386/usr/include/ast "+
                "-DKSHELL -DSHOPT_BRACEPAT -DSHOPT_CMDLIB_BLTIN=0 -DSH_CMDLIB_DIR='\"/usr/ast/bin\"' "+
                "-DSHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\" -DSHOPT_DYNAMIC -DSHOPT_ESH -DSHOPT_FILESCAN "+
                "-DSHOPT_HISTEXPAND -DSHOPT_KIA -DSHOPT_MULTIBYTE -DSHOPT_NAMESPACE -DSHOPT_OPTIMIZE "+
                "-DSHOPT_PFSH -DSHOPT_RAWONLY -DSHOPT_SUID_EXEC -DSHOPT_SYSRC -DSHOPT_VSH -D_BLD_shell "+
                "-D_PACKAGE_ast -DERROR_CONTEXT_T=Error_context_t "+
                "-DUSAGE_LICENSE='\"[-author?David Korn <dgk@research.att.com>]\" "+
                "\"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" "+
                "\"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" "+
                "\"[--catalog?libshell]\"' "+
                "-M/export/opensolaris/testws77/usr/src/common/mapfiles/common/map.noexstk "+
                "-M/export/opensolaris/testws77/usr/src/common/mapfiles/i386/map.pagealign "+
                "-M/export/opensolaris/testws77/usr/src/common/mapfiles/i386/map.noexdata pmain.o "+
                "-o ksh93 -L/export/opensolaris/testws77/proto/root_i386/lib/amd64 "+
                "-L/export/opensolaris/testws77/proto/root_i386/usr/lib/amd64 -lshell";
        String expResult = "Macros:\n_XOPEN_SOURCE=600\n__EXTENSIONS__=1\nTEXT_DOMAIN=SUNW_OST_OSCMD\n"+
                "_TS_ERRNO\nKSHELL\nSHOPT_BRACEPAT\nSHOPT_CMDLIB_BLTIN=0\nSH_CMDLIB_DIR=\"/usr/ast/bin\"\n"+
                "SHOPT_CMDLIB_HDR=solaris_cmdlist.h\nSHOPT_DYNAMIC\nSHOPT_ESH\nSHOPT_FILESCAN\n"+
                "SHOPT_HISTEXPAND\nSHOPT_KIA\nSHOPT_MULTIBYTE\nSHOPT_NAMESPACE\n"+
                "SHOPT_OPTIMIZE\nSHOPT_PFSH\nSHOPT_RAWONLY\nSHOPT_SUID_EXEC\nSHOPT_SYSRC\n"+
                "SHOPT_VSH\n_BLD_shell\n_PACKAGE_ast\nERROR_CONTEXT_T=Error_context_t\n"+
                "USAGE_LICENSE=\"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"\n"+
                "Paths:\n/export/opensolaris/testws77/proto/root_i386/usr/include/ast";
        String result = processLine(DwarfSource.scanCommandLine(line).iterator());
        if (!expResult.equals(result)) {
            assertFalse("Parsing line:"+line+"\nExpected:\n"+expResult+"\nFound:\n"+result, true);
        }
    }

    private String processLine(Iterator<String> res) {
        StringBuilder macros = new StringBuilder();
        StringBuilder paths = new StringBuilder();
        while (res.hasNext()) {
            String option = res.next();
            if (option.startsWith("-D")){ // NOI18N
                String macro = option.substring(2);
                int i = macro.indexOf('=');
                if (i>0){
                    String value = macro.substring(i+1).trim();
                    if (value.length() >= 2 &&
                       (value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'' || // NOI18N
                        value.charAt(0) == '"' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                        value = value.substring(1,value.length()-1);
                    }
                    macros.append("\n"+macro.substring(0,i)+"="+value);
                } else {
                    macros.append("\n"+macro);
                }
            } else if (option.startsWith("-I")){ // NOI18N
                String path = option.substring(2);
                if (path.length()==0 && res.hasNext()){
                    path = res.next();
                }
                paths.append("\n"+path);
            } else if (option.startsWith("-Y")){ // NOI18N
                String defaultSearchPath = option.substring(2);
                if (defaultSearchPath.length()==0 && res.hasNext()){
                    defaultSearchPath = res.next();
                }
                if (defaultSearchPath.startsWith("I,")){ // NOI18N
                    defaultSearchPath = defaultSearchPath.substring(2);
                    paths.append("\n"+defaultSearchPath);
                }
            }
        }
        return "Macros:"+macros.toString()+"\nPaths:"+paths.toString();
    }
}
