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
 * DwarfNameLookupTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfNameLookupTableSection extends ElfSection {
    private ArrayList<DwarfNameLookupTable> tables = null;
    
    public DwarfNameLookupTableSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }

    public ArrayList<DwarfNameLookupTable> getNameLookupTables() {
        if (tables == null) {
            try {
                tables = readNameLookupTables();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return tables;
    }

    private ArrayList<DwarfNameLookupTable> readNameLookupTables() throws IOException {
        ArrayList<DwarfNameLookupTable> result = new ArrayList<DwarfNameLookupTable>();
        
        long currPos = reader.getFilePointer();
        reader.seek(header.getSectionOffset());

        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)reader.getSection(SECTIONS.DEBUG_INFO);

        long bytesToRead = header.getSectionSize();
        
        while (bytesToRead > 0) {
            DwarfNameLookupTable table = new DwarfNameLookupTable();
            
            table.unit_length = reader.readDWlen();
            bytesToRead -= table.unit_length + 4; // 4 is a size of unit_length itself

            table.version = reader.readShort();
            table.debug_info_offset = reader.read3264();
            table.debug_info_length = reader.read3264();
            
            CompilationUnit cu = debugInfo.getCompilationUnit(table.debug_info_offset);
            
            if (cu == null) {
		continue;
	    }
	    
            for (;;) {
                long entryOffset = reader.read3264();
                
                if (entryOffset == 0) {
                    break;
                }
                
                table.addEntry(entryOffset, reader.readString());
            }

            result.add(table);
        }
        
        reader.seek(currPos);
        
        return result;
    }
    
    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        
        for (DwarfNameLookupTable table : tables) {
            table.dump(out);
        }
    }

    @Override
    public String toString() {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out);
        return st.toString();
    }

    public DwarfNameLookupTable getNameLookupTableFor(long info_offset) {
        for (DwarfNameLookupTable table : getNameLookupTables()) {
            if (table.debug_info_offset == info_offset) {
                return table;
            }
        }
        
        return null;
    }
    
}
