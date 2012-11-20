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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.dwarfdiscovery.litemodel.DwarfRenderer;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.litemodel.api.Declaration;

/**
 *
 * @author Alexander Simon
 */
public class LiteWeightModelTest extends NbTestCase {
    private static final int FULL = 0;
    private static final int TOP_LEVEL_DECLARATIONS = 1;
    private static final int TOP_LEVEL_DECLARATIONS_IN_COMILATION_UNIT = 2;

    public LiteWeightModelTest() {
        super("LiteWeightModelTest");
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testQuoteCpu(){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/cpu.gentoo.4.3.o";
        DwarfRenderer dwarfRenderer = LWM(objFileName, TOP_LEVEL_DECLARATIONS);
        //dwarfRenderer.dumpModel(System.out);
        Map<String, Map<String, Declaration>> map = dwarfRenderer.getLWM();
        Map<String, Declaration> cpuC = getFilename(map, "/cpu.cc");
        assertNotNull(cpuC);
        Map<String, Declaration> cpuH = getFilename(map, "/cpu.h");
        assertNotNull(cpuH);
    }

    public void testQuoteCpuOnly(){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/cpu.gentoo.4.3.o";
        DwarfRenderer dwarfRenderer = LWM(objFileName, TOP_LEVEL_DECLARATIONS_IN_COMILATION_UNIT);
        //dwarfRenderer.dumpModel(System.out);
        Map<String, Map<String, Declaration>> map = dwarfRenderer.getLWM();
        Map<String, Declaration> cpuC = getFilename(map, "/cpu.cc");
        assertNotNull(cpuC);
        Map<String, Declaration> cpuH = getFilename(map, "/cpu.h");
        assertNull(cpuH);
    }

    public void testQuoteQuoteFullSun(){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/quote.solaris.o";
        DwarfRenderer dwarfRenderer = LWM(objFileName, FULL);
        //dwarfRenderer.dumpModel(System.out);
        Set<String> templates = new HashSet<String>();
        Map<String, Map<String, Declaration>> map = dwarfRenderer.getLWM();
        for(Map.Entry<String, Map<String, Declaration>> entry : map.entrySet()) {
            String templetePrefix = null;
            String currentInstantiation = null;
            for(Map.Entry<String, Declaration> declEntry: entry.getValue().entrySet()) {
                String fqn = declEntry.getKey();
                Declaration decl = declEntry.getValue();
                if (decl.getKind() == Declaration.Kind.SUN_class_template) {
                    templetePrefix= fqn;
                    continue;
                }
                if (templetePrefix != null && fqn.startsWith(templetePrefix)) {
                    String instantiation = extractTemplateParameters(fqn);
                    if (instantiation != null) {
                        if (currentInstantiation == null || !currentInstantiation.equals(instantiation)) {
                            currentInstantiation = instantiation;
                            templates.add(instantiation);
                            //System.out.println(instantiation);
                        }
                    }
                }
            }
        }
        assertTrue(templates.contains("std::vector<Module*,std::allocator<Module*> >"));
        assertTrue(templates.contains("std::list<Customer,std::allocator<Customer> >"));
    }

    public void testQuoteQuoteFullGnu(){
        File dataDir = getDataDir();
        String objFileName = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/dwarfdiscovery/provider/quote.gnu.o";
        DwarfRenderer dwarfRenderer = LWM(objFileName, FULL);
        //dwarfRenderer.dumpModel(System.out);
        Set<String> templates = new HashSet<String>();
        Map<String, Map<String, Declaration>> map = dwarfRenderer.getLWM();
        for(Map.Entry<String, Map<String, Declaration>> entry : map.entrySet()) {
            String currentInstantiation = null;
            for(Map.Entry<String, Declaration> declEntry: entry.getValue().entrySet()) {
                String fqn = declEntry.getKey();
                Declaration decl = declEntry.getValue();
                if (decl.getKind() == Declaration.Kind.structure_type ||
                    decl.getKind() == Declaration.Kind.class_type) {
                    String instantiation = extractTemplateParameters(fqn);
                    if (instantiation != null) {
                        if (currentInstantiation == null || !currentInstantiation.equals(instantiation)) {
                            currentInstantiation = instantiation;
                            templates.add(instantiation);
                            //System.out.println(instantiation);
                        }
                    }
                }
            }
        }
        assertTrue(templates.contains("std::vector<Module*,std::allocator<Module*> >"));
        assertTrue(templates.contains("std::list<Customer,std::allocator<Customer> >"));
    }

    private String extractTemplateParameters(String fqn){
        int level = 0;
        for(int i = 0; i < fqn.length(); i++) {
            char c = fqn.charAt(i);
            if (c == '<') {
                level++;
            } else if (c == '>') {
                level--;
                if (level == 0) {
                    return fqn.substring(0,i+1);
                }
            }
        }
        return null;
    }

    private Map<String, Declaration> getFilename(Map<String, Map<String, Declaration>> map, String fileName) {
        for(Map.Entry<String, Map<String, Declaration>> entry : map.entrySet()) {
            if (entry.getKey().endsWith(fileName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private DwarfRenderer LWM(String objFileName, int limit){
        //long time = System.currentTimeMillis();
        DwarfRenderer dwarfRenderer = null;
        switch (limit) {
            case FULL:
                dwarfRenderer = DwarfRenderer.createFullRenderer();
                break;
            case TOP_LEVEL_DECLARATIONS:
                dwarfRenderer = DwarfRenderer.createTopLevelDeclarationsRenderer();
                break;
            case TOP_LEVEL_DECLARATIONS_IN_COMILATION_UNIT:
                dwarfRenderer = DwarfRenderer.createTopLevelDeclarationsCompilationUnitsRenderer();
                break;
        }
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            CompilationUnitIterator iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnitInterface cu = iterator.next();
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
            ex.printStackTrace(System.err);
        } catch (WrongFileFormatException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        //System.out.println("Analyzing time "+(System.currentTimeMillis()-time));
        return dwarfRenderer;
    }
}
