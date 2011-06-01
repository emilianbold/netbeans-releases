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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gizmo.addr2line;

import org.netbeans.modules.cnd.gizmo.addr2line.dwarf2line.Dwarf2NameFinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class FindNameTest extends NbTestCase {

    public FindNameTest() {
        super("FindNameTest");
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testWelcomeGNU_Sparc() {
        baseTest(0x10, "mutex_threadfunc", "welcome_3", true, 22);
    }

    public void testFftImageTransformer() {
        baseTest(0x381, "FastFourierTransform::Transform", "fftimagetransformer", true, 84);
    }

    public void testPkgConfig() {
        baseTest(0x2a, "g_hash_table_new", "testglib", true, 80);
    }

    public void testPkgConfig1() {
        baseTest(0x71, "g_tree_new", "testglib", true, 153);
    }

    public void testPkgConfig2() {
        baseTest(0x0, "g_free", "testglib", true, 384);
    }

    public void testBuddhabrot() {
        baseTest(0xef, "main", "buddhabrot", true, 50);
    }

    public void testFractal0() {
        baseTest(0x10f, "main", "fractal", true, 207);
    }

    public void testFractal1() {
        baseTest(0x602, "Mandelbrot", "fractal", true, 160);
    }

    public void testFractal2() {
        baseTest(0x463, "Mandelbrot", "fractal", true, 143);
    }

    public void testFractal3() {
        baseTest(0x2b9, "Mandelbrot", "fractal", true, 131);
    }

    public void testFractal4() {
        baseTest(0x16, "complex::operator+", "fractal", true, 55);
    }

    public void testFractal5() {
        baseTest(0xd, "complex::operator+", "fractal", true, 55);
    }

    public void testProfilingdemo0() {
        baseTest(0x100, "main", "profilingdemo", true, 60);
    }

    public void testProfilingdemo1() {
        baseTest(0x93, "work_run_getmem", "profilingdemo", true, 107);
    }

    public void testProfilingdemo2() {
        baseTest(0x36, "threadfunc", "profilingdemo", false, 92);
    }

    private DwarfEntry findFunction(String function, List<DwarfEntry> decls) throws IOException {
        for (DwarfEntry entry : decls) {
            if (entry.getKind() == TAG.DW_TAG_subprogram || entry.getKind() == TAG.DW_TAG_member) {
                String name = entry.getName();
                //System.err.println("Function:"+entry.getQualifiedName());
                if (name.equals(function) || entry.getQualifiedName().equals(function)) {
                    if (entry.getLowAddress() != 0) {
                        return entry;
                    }
                }
             } else if (entry.getKind() == TAG.DW_TAG_class_type){
                 DwarfEntry res = findFunction(function, entry.getMembers());
                 if (res != null) {
                     return res;
                 }
             }
        }
        return null;
    }

    private void baseTest(long shift, String function, String executable, boolean full, int etalonLine) {
        if (Utilities.isWindows()) {
            return; // this is for Unixes only (lineinfo.bash uses dbx)
        }
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
            serviceInfo.put(ServiceInfoDataStorage.EXECUTION_ENV_KEY, ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal()));
            fileInfo = provider.getSourceFileInfo(function, -1, shift, serviceInfo);
            Dwarf dwarf = new Dwarf(executable);
            try {
                Iterator<CompilationUnit> iterator = dwarf.iteratorCompilationUnits();
                loop:while(iterator.hasNext()) {
                    CompilationUnit unit = iterator.next();
                    //System.err.println("Unit:"+unit.getSourceFileFullName());
                    DwarfEntry entry = findFunction(function, unit.getDeclarations(false));
                    if (entry != null) {
                        base = entry.getLowAddress();
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
                            if (candidate == null && prevOffset <= target && target < n.startOffset) {
                                candidate = prev;
                                //break;
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
        if (fileInfo != null) {
            System.err.println("Dwarf Provider:\t" + fileInfo.getFileName() + ":" + fileInfo.getLine());
        }
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN){
            Dwarf2NameFinder source = getDwarfSource(executable);
            source.lookup(base + shift);
            System.err.println("Dwarf Finder:\t" + source.getSourceFile() + ":" + source.getLineNumber());
            //Stop support Dwarf Finder
            //assertEquals(number.line, source.getLineNumber());
        }
        assertNotNull(fileInfo);
        assertNotNull(number);
        assertNotNull(candidate);
        assertEquals(etalonLine, number.line);
        assertEquals(number.line, candidate.line);
        String file1 = number.file.replace('\\', '/');
        if (file1.indexOf('/') >= 0) {
            file1 = file1.substring(file1.lastIndexOf('/')+1);
        }
        String file2 = fileInfo.getFileName().replace('\\', '/');
        if (file2.indexOf('/') >= 0) {
            file2 = file2.substring(file2.lastIndexOf('/')+1);
        }
        if (file2.equals(file1)) {
            assertEquals(number.line, fileInfo.getLine());
        } else {
            assertFalse(full);
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
