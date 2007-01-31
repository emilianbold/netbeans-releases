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
import java.io.PrintStream;
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
            table = new DwarfMacinfoTable(this, offset);
            macinfoTables.put(lOffset, table);
        }
        
        return table;
    }
    
    // Fills the table
    // Returns how many bytes have been read.
    
    public long readMacinfoTable(DwarfMacinfoTable table, long offset, boolean baseOnly) throws IOException {
        long currPos = reader.getFilePointer();
        
        reader.seek(header.getSectionOffset() + offset);
        
        MACINFO type = MACINFO.get(reader.readByte());
        Stack<Integer> fileIndeces = new Stack<Integer>();
        int fileIdx = -1;
        
        while(type != null && (!baseOnly || (baseOnly && fileIdx == -1))) {
            DwarfMacinfoEntry entry = new DwarfMacinfoEntry(type);
            if (type.equals(MACINFO.DW_MACINFO_define) || type.equals(MACINFO.DW_MACINFO_undef)) {
                entry.lineNum = reader.readUnsignedLEB128();
                entry.definition = reader.readString();
                entry.fileIdx = fileIdx;
            } else if (type.equals(MACINFO.DW_MACINFO_start_file)) {
                if (baseOnly) {
                    break;
                }
                
                entry.lineNum = reader.readUnsignedLEB128();
                entry.fileIdx = reader.readUnsignedLEB128();
                fileIndeces.push(fileIdx);
                fileIdx = entry.fileIdx;
            } else if (type.equals(MACINFO.DW_MACINFO_end_file)) {
                /*
                 * Stack COULD be empty. This happens when readMacinfoTable() is
                 * invoked twice - first time for base definitions only and the 
                 * second one for others. In this case on the second invokation 
                 * at the end we will get DW_MACINFO_end_file for file with idx 
                 * -1 (base).
                 */
                if (!fileIndeces.empty()) {
                    fileIdx = fileIndeces.pop();
                }
            } else if (type.equals(MACINFO.DW_MACINFO_vendor_ext)) {
                // Just skip...
                reader.readUnsignedLEB128();
                reader.readString();
            }
            
            table.addEntry(entry);
            type = MACINFO.get(reader.readByte());
        }
        
        long readBytes = reader.getFilePointer() - (header.getSectionOffset() + offset + 1);
        reader.seek(currPos);

        return readBytes;
    }
    
    public void dump(PrintStream out) {
        super.dump(out);
        
        for (DwarfMacinfoTable macinfoTable : macinfoTables.values()) {
            macinfoTable.dump(out);
        }
    }    
    
}
