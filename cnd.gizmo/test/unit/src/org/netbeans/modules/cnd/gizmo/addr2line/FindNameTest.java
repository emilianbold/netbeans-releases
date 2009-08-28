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
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class FindNameTest extends NbTestCase {

    public FindNameTest() {
        super("FindNameTest");
    }

    public void testFractalFractal0() {
        baseTest(0x10f, "main", "fractal");
    }

    public void testFractalFractal1() {
        baseTest(0x602, "Mandelbrot", "fractal");
    }

    public void testFractalFractal2() {
        baseTest(0x463, "Mandelbrot", "fractal");
    }

    public void testFractalFractal3() {
        baseTest(0x2b9, "Mandelbrot", "fractal");
    }

    public void testFractalFractal4() {
        baseTest(0x16, "complex::operator+", "fractal");
    }

    public void testFractalFractal5() {
        baseTest(0xd, "complex::operator+", "fractal");
    }

    public void testProfilingdemo0() {
        baseTest(0x100, "main", "profilingdemo");
    }

    public void testProfilingdemo1() {
        baseTest(0x93, "work_run_getmem", "profilingdemo");
    }

    public void testProfilingdemo2() {
        baseTest(0x36, "threadfunc", "profilingdemo");
    }

    private void baseTest(long shift, String function, String executable) {
        System.err.println("\nSearch for "+function+"0x"+Long.toHexString(shift)+" in "+executable);
        executable = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/"+executable);
        String script = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/lineinfo.bash");
        EtalonLineNumber etalon = getEtalonLineNumber(script);
        String line = etalon.lineNumber(executable, function+"+0x"+Long.toHexString(shift));
        System.err.println("Gdb result:\t"+line);
        long base = 0;
        LineNumber number = null;
        try {
            Dwarf dwarf = new Dwarf(executable);
            try {
                loop:for (CompilationUnit unit : dwarf.getCompilationUnits()){
                    for (DwarfEntry entry : unit.getDeclarations()){
                        if (entry.getKind() == TAG.DW_TAG_subprogram){
                            String name = entry.getName();
                            if (name.equals(function) || entry.getQualifiedName().equals(function)) {
                                //System.err.println(""+entry);
                                Set<LineNumber> numbers = unit.getLineNumbers();
                                TreeSet<LineNumber> sorted = new TreeSet<LineNumber>(numbers);
                                //for(LineNumber l : sorted) {
                                //    System.err.println(""+l);
                                //}
                                base = entry.getLowAddress();
                                long target = base + shift;
                                //System.err.println("base   address: 0x"+Long.toHexString(base));
                                //System.err.println("target address: 0x"+Long.toHexString(target));
                                LineNumber candidate = null;
                                for (LineNumber n : sorted) {
                                    if (n.startOffset < target && target <= n.endOffset) {
                                        candidate = n;
                                        break;
                                    }
                                }
                                System.err.println("Dwarf Map:\t" + candidate);
                                number = unit.getLineNumber(base + shift);
                                //unit.getSourceFileFullName();
                                System.err.println("Dwarf Proces:\t" + number);
                                break loop;
                            }
                        }
                    }
                }
            } finally {
                dwarf.dispose();
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WrongFileFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Dwarf2NameFinder source = getDwarfSource(executable);
        source.lookup(base + shift);
        System.err.println("Dwarf Finder:\t" + source.getSourceFile() + ":" + source.getLineNumber());
        assertNotNull(number);
        assertEquals(number.line, source.getLineNumber());
        //assertTrue(line.indexOf(""+number.line)>=0);
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
