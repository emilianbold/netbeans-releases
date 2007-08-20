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
import java.io.PrintStream;
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
        
        stmt_list.total_length = reader.readDWlen();
        stmt_list.version = reader.readShort();
        stmt_list.prologue_length = reader.read3264();
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

    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        for (DwarfStatementList statementList : statementLists.values()) {
            statementList.dump(out);
        }
    }    
}

