/*
 * DwarfNameLookupTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

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
