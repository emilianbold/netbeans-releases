/*
 * DwarfLineInfostmt_list.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author ak119685
 */
public class DwarfLineInfoSection extends ElfSection {
    HashMap<Long, DwarfStatementList> statementLists = new HashMap<Long, DwarfStatementList>();
    
    public DwarfLineInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    public DwarfStatementList getStatementList(long offset) {
        Long lOffset = new Long(offset);
        DwarfStatementList statementList = statementLists.get(lOffset);
        
        if (statementList == null) {
            try {
                statementList = readStatementList(offset);
                statementLists.put(lOffset, statementList);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return statementList;
    }
    
    private DwarfStatementList readStatementList(long offset) throws IOException {
        long currPos = reader.getFilePointer();
        
        reader.seek(header.getSectionOffset() + offset);
        
        DwarfStatementList stmt_list = new DwarfStatementList(offset);
        
        stmt_list.total_length = reader.readInt();
        stmt_list.version = reader.readShort();
        stmt_list.prologue_length = reader.readInt();
        stmt_list.minimum_instruction_length = reader.readByte();
        stmt_list.default_is_stmt = reader.readByte();
        stmt_list.line_base = reader.readByte();
        stmt_list.line_range = reader.readByte();
        stmt_list.opcode_base = reader.readByte();
        
        stmt_list.standard_opcode_lengths = new long[stmt_list.opcode_base - 1];
        
        for (int i = 0; i < stmt_list.opcode_base - 1; i++) {
            stmt_list.standard_opcode_lengths[i] = reader.readUnsignedLEB128();
        }
        
        String dirname = reader.readString();
        
        while (dirname.length() > 0) {
            stmt_list.includeDirs.add(dirname);
            dirname = reader.readString();
        }
        
        String fname = reader.readString();
        
        while(fname.length() > 0) {
            stmt_list.fileEntries.add(new FileEntry(fname, reader.readUnsignedLEB128(), reader.readUnsignedLEB128(), reader.readUnsignedLEB128()));
            fname = reader.readString();
        }
        
        reader.seek(currPos);
        
        //TODO: add code...
        return stmt_list;
        
    }
    
    
}
