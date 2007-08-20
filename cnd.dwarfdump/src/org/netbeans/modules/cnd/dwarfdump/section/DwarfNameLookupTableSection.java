/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DwarfNameLookupTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

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
    ArrayList<DwarfNameLookupTable> tables = null;
    
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

    public DwarfNameLookupTable getNameLookupTableFor(long info_offset) {
        for (DwarfNameLookupTable table : getNameLookupTables()) {
            if (table.debug_info_offset == info_offset) {
                return table;
            }
        }
        
        return null;
    }
    
}
