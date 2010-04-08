/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * DwarfDebugInfoSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;

/**
 *
 * @author ak119685
 */
public class DwarfDebugInfoSection extends ElfSection {
    private List<CompilationUnit> compilationUnits;
    //DwarfRelaDebugInfoSection rela;
    
    public DwarfDebugInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
        /*rela = (DwarfRelaDebugInfoSection)*/ reader.getSection(SECTIONS.RELA_DEBUG_INFO);
    }

    public DwarfDebugInfoSection(ElfReader reader, int sectionIdx, SectionHeader header, String sectionName) {
        super(reader, sectionIdx, header, sectionName);
    }
    
    public CompilationUnit getCompilationUnit(long unit_offset) throws IOException {
        for (CompilationUnit unit : getCompilationUnits()) {
            if (unit.unit_offset == unit_offset) {
                return unit;
            }
        }
        return null;
    }

    public Iterator<CompilationUnit> iteratorCompilationUnits() throws IOException {
        if (compilationUnits != null) {
            return compilationUnits.iterator();
        }
        return new UnitIterator();
    }
    
    public List<CompilationUnit> getCompilationUnits() throws IOException {
        if (compilationUnits != null) {
            return compilationUnits;
        }
        compilationUnits = new ArrayList<CompilationUnit>();
        int cuOffset = 0;
        while (cuOffset != header.sh_size) {
            ((DwarfReader)reader).seek(header.getSectionOffset() + cuOffset);
            if (reader.readDWlen()==0) {
                break;
            }
            CompilationUnit unit = new CompilationUnit((DwarfReader)reader, header.getSectionOffset(), cuOffset);
            compilationUnits.add(unit);
            cuOffset += unit.getUnitTotalLength();
        }
        return compilationUnits;
    }
    
    @Override
    public void dump(PrintStream out) {
        try {
            for (CompilationUnit unit : getCompilationUnits()) {
                unit.dump(out);
    }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out);
        return st.toString();
    }

    private class UnitIterator implements Iterator<CompilationUnit> {
        private int cuOffset = 0;
        private CompilationUnit unit;

        public UnitIterator() throws IOException {
            advance();
        }

        @Override
        public boolean hasNext() {
            return unit != null;
        }

        @Override
        public CompilationUnit next() {
            CompilationUnit res = unit;
            try {
                advance();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advance() throws IOException {
            unit = null;
            if (cuOffset != header.sh_size) {
                ((DwarfReader) reader).seek(header.getSectionOffset() + cuOffset);
                if (reader.readDWlen() == 0) {
                    return;
                }
                unit = new CompilationUnit((DwarfReader) reader, header.getSectionOffset(), cuOffset);
                cuOffset += unit.getUnitTotalLength();
            }
        }
    }
}
