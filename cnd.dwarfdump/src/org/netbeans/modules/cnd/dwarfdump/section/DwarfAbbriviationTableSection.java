/*
 * DwarfAbbriviationTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTableEntry;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author ak119685
 */
public class DwarfAbbriviationTableSection extends ElfSection {
    HashMap<Long, DwarfAbbriviationTable> tables = new HashMap<Long, DwarfAbbriviationTable>();
    
    /** Creates a new instance of DwarfAbbriviationTableSection */
    public DwarfAbbriviationTableSection(ElfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }

    public void dump(PrintStream out) {
        super.dump(out);
        
        for (DwarfAbbriviationTable table : tables.values()) {
            table.dump(out);
        }
    }

    public DwarfAbbriviationTable getAbbriviationTable(long offset) {
        Long lOffset = new Long(offset);
        DwarfAbbriviationTable table = tables.get(lOffset);
        
        if (table == null) {
            try {
                table = readTable(offset);
                tables.put(lOffset, table);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return table;
    }

    private DwarfAbbriviationTable readTable(long offset) throws IOException {
        long currPos = reader.getFilePointer();
        
        reader.seek(header.getSectionOffset() + offset);
        
        long idx = -1;
        ArrayList<DwarfAbbriviationTableEntry> entries = new ArrayList<DwarfAbbriviationTableEntry>();
        DwarfAbbriviationTable table = new DwarfAbbriviationTable(offset);
        
        while (idx != 0) {
            idx = reader.readUnsignedLEB128();
            
            if (idx == 0) {
                break;
            }
            
            long aTag = reader.readUnsignedLEB128();
            boolean hasChildren = reader.readBoolean();
            
            DwarfAbbriviationTableEntry entry = new DwarfAbbriviationTableEntry(idx, aTag, hasChildren);
            
            int name = -1;
            int form = -1;
            
            while (name != 0 && form != 0) {
                name = reader.readUnsignedLEB128();
                form = reader.readUnsignedLEB128();
                entry.addAttribute(name, form);
            }
            
            entries.add(entry);
        }
        
        table.setEntries(entries);
        
        reader.seek(currPos);
        return table;
    }
    
}
