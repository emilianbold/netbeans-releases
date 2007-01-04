/*
 * DwarfMacroInfoSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author ak119685
 */
public class DwarfMacroInfoSection extends ElfSection {
    HashMap<Long, DwarfMacinfoTable> macinfoTables = new HashMap<Long, DwarfMacinfoTable>();
    
    public DwarfMacroInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    public DwarfMacinfoTable getMacinfoTable(long offset) {
        Long lOffset = new Long(offset);
        DwarfMacinfoTable table = macinfoTables.get(lOffset);
        
        if (table == null) {
            try {
                table = readMacinfoTable(offset);
                macinfoTables.put(lOffset, table);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return table;
    }
    
    private DwarfMacinfoTable readMacinfoTable(long offset) throws IOException {
        long currPos = reader.getFilePointer();
        
        reader.seek(header.getSectionOffset() + offset);
        
        DwarfMacinfoTable macroTable = new DwarfMacinfoTable(offset);
        
        MACINFO type = MACINFO.get(reader.readByte());
        Stack<Integer> fileIndeces = new Stack<Integer>();
        int fileIdx = -1;
        fileIndeces.push(fileIdx);
        fileIndeces.push(fileIdx);
        
        while(type != null) {
            DwarfMacinfoEntry entry = new DwarfMacinfoEntry(type);
            if (type.equals(MACINFO.DW_MACINFO_define) || type.equals(MACINFO.DW_MACINFO_undef)) {
                entry.lineNum = reader.readUnsignedLEB128();
                entry.definition = reader.readString();
                entry.fileIdx = fileIdx;
            } else if (type.equals(MACINFO.DW_MACINFO_start_file)) {
                entry.lineNum = reader.readUnsignedLEB128();
                entry.fileIdx = reader.readUnsignedLEB128();
                fileIdx = entry.fileIdx;
                fileIndeces.push(fileIdx);
            } else if (type.equals(MACINFO.DW_MACINFO_end_file)) {
                fileIndeces.pop();
                fileIdx = fileIndeces.peek();
            } else if (type.equals(MACINFO.DW_MACINFO_vendor_ext)) {
                // Just skip...
                reader.readUnsignedLEB128();
                reader.readString();
            }
            
            macroTable.addEntry(entry);
            type = MACINFO.get(reader.readByte());
        }
        
        return macroTable;
    }
    
}
