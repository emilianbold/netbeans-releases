/*
 * DwarfNameLookupTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
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

        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)reader.getSection(".debug_info");


        for (int i = 0; i < debugInfo.getCompilationUnitsNumber(); i++) {
            DwarfNameLookupTable table = new DwarfNameLookupTable();
            
            table.unit_length = reader.readInt();
            table.version = reader.readShort();
            table.debug_info_offset = reader.readInt();
            table.debug_info_length = reader.readInt();
            
            CompilationUnit cu = debugInfo.getCompilationUnit(table.debug_info_offset);
            
            for (;;) {
                int entryOffset = reader.readInt();
                
                if (entryOffset == 0) {
                    break;
                }
                
                String name = reader.readString();
                table.entries.add(new DwarfNameLookupEntry(entryOffset, name));
                
                cu.getEntry(entryOffset).setQualifiedName(name);
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
