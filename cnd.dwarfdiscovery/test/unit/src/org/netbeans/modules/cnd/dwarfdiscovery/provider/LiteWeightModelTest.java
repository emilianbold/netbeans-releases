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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.DwarfRenderer;
import org.netbeans.modules.cnd.dwarfdump.DwarfRenderer.Declaration;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 *
 * @author Alexander Simon
 */
public class LiteWeightModelTest extends NbTestCase {

    public LiteWeightModelTest() {
        super("LiteWeightModelTest");
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    //public void testMySQL(){
    //    LWM("/export/home/as204739/Projects/sfbay/mysql-5.1.30/sql/mysqld");
    //}

    public void testQuoteCpu(){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/cpu.gentoo.4.3.o";
        DwarfRenderer dwarfRenderer = LWM(objFileName);
        dwarfRenderer.dumpModel(System.out);
        Map<String, Map<String, Declaration>> map = dwarfRenderer.getLWM();
///export/home/av202691/NetBeansProjects/Quote_1/cpu.cc
//        Cpu::ComputeSupportMetric() (DW_TAG_subprogram) :17
//        Cpu::Cpu(int, int, int) (DW_TAG_subprogram) :6
//        Cpu::GetCategory() (DW_TAG_subprogram) const char :46
//        Cpu::GetType() (DW_TAG_subprogram) const char :33
        Map<String, Declaration> cpuC = getFilename(map, "/cpu.cc");
        assertNotNull(cpuC);
///export/home/av202691/NetBeansProjects/Quote_1/cpu.h
//        Cpu (DW_TAG_class_type) :35
//        Cpu::Cpu(const Cpu&) (DW_TAG_subprogram) :35
//        Cpu::CpuArch (DW_TAG_enumeration_type) :38
//        Cpu::CpuArch::INTEL (DW_TAG_enumerator) :38
//        Cpu::CpuArch::OPTERON (DW_TAG_enumerator) :38
//        Cpu::CpuArch::SPARC (DW_TAG_enumerator) :38
//        Cpu::CpuType (DW_TAG_enumeration_type) :37
//        Cpu::CpuType::HIGH (DW_TAG_enumerator) :37
//        Cpu::CpuType::MEDIUM (DW_TAG_enumerator) :37
//        Cpu::~Cpu() (DW_TAG_subprogram) :35
//        Module (DW_TAG_inheritance) :35
        Map<String, Declaration> cpuH = getFilename(map, "/cpu.h");
        assertNotNull(cpuH);
    }

    private Map<String, Declaration> getFilename(Map<String, Map<String, Declaration>> map, String fileName) {
        for(Map.Entry<String, Map<String, Declaration>> entry : map.entrySet()) {
            if (entry.getKey().endsWith(fileName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private DwarfRenderer LWM(String objFileName){
        //long time = System.currentTimeMillis();
        DwarfRenderer dwarfRenderer = new DwarfRenderer();
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            Iterator<CompilationUnit> iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnit cu = iterator.next();
                if (cu != null) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang)
                            || LANG.DW_LANG_C89.toString().equals(lang)
                            || LANG.DW_LANG_C99.toString().equals(lang)) {
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                    } else if (LANG.DW_LANG_Fortran77.toString().equals(lang) ||
                           LANG.DW_LANG_Fortran90.toString().equals(lang) ||
                           LANG.DW_LANG_Fortran95.toString().equals(lang)) {
                    } else {
                        continue;
                    }
                    dwarfRenderer.process(cu);
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
        } catch (WrongFileFormatException ex) {
        } catch (IOException ex) {
        } catch (Exception ex) {
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        //System.out.println("Analyzing time "+(System.currentTimeMillis()-time));
        return dwarfRenderer;
    }
}
