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

package org.netbeans.modules.cnd.gizmo.addr2line;

import org.netbeans.modules.cnd.gizmo.addr2line.dwarf2line.Dwarf2NameFinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;
import org.netbeans.modules.cnd.gizmo.DwarfSourceInfoProvider;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class FindNameTest extends NbTestCase {

    public FindNameTest() {
        super("FindNameTest");
    }

    public void testFftImageTransformer() {
        baseTest(0x381, "void FastFourierTransform::Transform", "fftimagetransformer", false);
    }

    public void testPkgConfig() {
        baseTest(0x2a, "g_hash_table_new", "testglib", true);
    }

    public void testPkgConfig1() {
        baseTest(0x71, "g_tree_new", "testglib", true);
    }

    public void testPkgConfig2() {
        baseTest(0x0, "g_free", "testglib", true);
    }

    public void testBuddhabrot() {
        baseTest(0xef, "main", "buddhabrot", true);
    }

    public void testFractal0() {
        baseTest(0x10f, "main", "fractal", true);
    }

    public void testFractal1() {
        baseTest(0x602, "Mandelbrot", "fractal", true);
    }

    public void testFractal2() {
        baseTest(0x463, "Mandelbrot", "fractal", true);
    }

    public void testFractal3() {
        baseTest(0x2b9, "Mandelbrot", "fractal", true);
    }

    public void testFractal4() {
        baseTest(0x16, "complex::operator+", "fractal", true);
    }

    public void testFractal5() {
        baseTest(0xd, "complex::operator+", "fractal", true);
    }

    public void testProfilingdemo0() {
        baseTest(0x100, "main", "profilingdemo", true);
    }

    public void testProfilingdemo1() {
        baseTest(0x93, "work_run_getmem", "profilingdemo", true);
    }

    public void testProfilingdemo2() {
        baseTest(0x36, "threadfunc", "profilingdemo", true);
    }

    private void baseTest(long shift, String function, String executable, boolean full) {
        System.err.println("\nSearch for "+function+"0x"+Long.toHexString(shift)+" in "+executable);
        executable = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/"+executable);
        String script = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/lineinfo.bash");
        EtalonLineNumber etalon = getEtalonLineNumber(script);
        String line = etalon.lineNumber(executable, function+"+0x"+Long.toHexString(shift));
        System.err.println("Gdb result:\t"+line);
        long base = 0;
        LineNumber number = null;
        LineNumber candidate = null;
        SourceFileInfo fileInfo = null;
        try {
            DwarfSourceInfoProvider provider = new DwarfSourceInfoProvider();
            Map<String, String> serviceInfo = new HashMap<String, String>();
            serviceInfo.put(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE, executable);
            fileInfo = provider.fileName(function, -1, shift, serviceInfo);
            if (full) {
                Dwarf dwarf = new Dwarf(executable);
                try {
                    loop:for (CompilationUnit unit : dwarf.getCompilationUnits()){
                        //System.err.println("Unit:"+unit.getSourceFileFullName());
                        for (DwarfEntry entry : unit.getDeclarations(false)){
                            if (entry.getKind() == TAG.DW_TAG_subprogram){
                                String name = entry.getName();
                                //System.err.println("Function:"+entry.getQualifiedName());
                                if (name.equals(function) || entry.getQualifiedName().equals(function)) {
                                    base = entry.getLowAddress();
                                    if (base == 0) {
                                        continue;
                                    }
                                    //System.err.println(""+entry);
                                    Set<LineNumber> numbers = unit.getLineNumbers();
                                    TreeSet<LineNumber> sorted = new TreeSet<LineNumber>(numbers);
                                    //for(LineNumber l : sorted) {
                                    //    System.err.println(""+l);
                                    //}
                                    long target = base + shift;
                                    System.err.println("base   address: 0x"+Long.toHexString(base));
                                    System.err.println("target address: 0x"+Long.toHexString(target));
                                    LineNumber prev = null;
                                    long prevOffset = Long.MAX_VALUE;
                                    for (LineNumber n : sorted) {
                                        if (n.startOffset <= target) {
                                            if (target < n.endOffset) {
                                                candidate = n;
                                                break;
                                            }
                                        }
                                        if (prevOffset <= target && target < n.startOffset) {
                                            candidate = prev;
                                            break;
                                        }
                                        prevOffset = n.endOffset;
                                        prev = n;
                                    }
                                    System.err.println("Dwarf Map:\t" + candidate);
                                    number = unit.getLineNumber(target);
                                    //unit.getSourceFileFullName();
                                    System.err.println("Dwarf Proces:\t" + number);
                                    if (number != null) {
                                        break loop;
                                    }
                                }
                            } else if (entry.getKind() == TAG.DW_TAG_class_type){
                                //System.err.println(""+entry);
                            }
                        }
                    }
                } finally {
                    dwarf.dispose();
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WrongFileFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (fileInfo != null) {
            System.err.println("Dwarf Provider:\t" + fileInfo.getFileName() + ":" + fileInfo.getLine());
        }
        if (full) {
            Dwarf2NameFinder source = getDwarfSource(executable);
            source.lookup(base + shift);
            System.err.println("Dwarf Finder:\t" + source.getSourceFile() + ":" + source.getLineNumber());
            assertNotNull(fileInfo);
            assertNotNull(number);
            assertEquals(number.line, source.getLineNumber());
            assertNotNull(candidate);
            assertEquals(number.line, candidate.line);
        } else {
            assertNotNull(fileInfo);
        }
        //if (line.indexOf(", line ")>0) {
        //    assertTrue(line.indexOf(" "+number.line+" ")>=0);
        //}
    }

    private Dwarf2NameFinder getDwarfSource(String resource){
        return new Dwarf2NameFinder(resource);
    }

    private EtalonLineNumber getEtalonLineNumber(String resource){
        return new EtalonLineNumber(resource);
    }

    private String getResource(String resource) {
        File dataDir = getDataDir();
        return dataDir.getAbsolutePath() + resource;
    }

}
