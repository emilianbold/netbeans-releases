/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * DwarfLineInfostmt_list.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LNE;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LNS;

/**
 *
 * @author ak119685
 */
public class DwarfLineInfoSection extends ElfSection {
    private HashMap<Long, DwarfStatementList> statementLists = new HashMap<Long, DwarfStatementList>();
    
    public DwarfLineInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    public DwarfStatementList getStatementList(long offset) {
        Long lOffset = Long.valueOf(offset);
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

        reader.seek(currPos + stmt_list.prologue_length + 10);
        
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

    @Override
    public String toString() {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out);
        return st.toString();
    }

    public LineNumber getLineNumber(long shift, long target) throws IOException{
        long currPos = reader.getFilePointer();
        try {
            DwarfStatementList statementList = getStatementList(shift);
            reader.seek(header.getSectionOffset() + shift + statementList.prologue_length + 10);
            return interpret(target, statementList, shift);
        } finally {
            reader.seek(currPos);
        }
    }

    public Set<LineNumber> getLineNumbers(long shift) throws IOException{
        long currPos = reader.getFilePointer();
        try {
            DwarfStatementList statementList = getStatementList(shift);
            reader.seek(header.getSectionOffset() + shift + statementList.prologue_length + 10);
            return interpret(statementList, shift);
        } finally {
            reader.seek(currPos);
        }
    }

    @SuppressWarnings("fallthrough")
    private Set<LineNumber> interpret(DwarfStatementList section, long shift) throws IOException {
        long address = 0;
        long base_address = 0;
        long prev_base_address = 0;
        String define_file = null;
        int fileno = 0;
        int lineno = 1;
        int prev_fileno = 0;
        int prev_lineno = 1;
        final int const_pc_add = 245 / section.line_range;
        int lineNumber = -1;
        String sourceFile = null;
        Set<LineNumber> result = new HashSet<LineNumber>();

        interpret:
        while (reader.getFilePointer() < header.getSectionOffset() + shift + section.total_length) {

            int opcode = reader.readByte() & 0xFF;

            if (opcode < section.opcode_base) {
                switch (LNS.get(opcode)) {
                    case DW_LNS_extended_op: {
                        long insn_len = reader.readSignedLEB128();
                        opcode = reader.readByte();

                        switch (LNE.get(opcode)) {
                            case DW_LNE_end_sequence:
                                lineNumber = prev_lineno;
                                sourceFile = ((prev_fileno >= 0 && prev_fileno + 1 < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                                result.add(new LineNumber(sourceFile, lineNumber, prev_base_address, address));
                                prev_lineno = lineno = 1;
                                prev_fileno = fileno = 0;
                                base_address = address = 0;
                                break;

                            case DW_LNE_set_address:
                                prev_base_address = base_address;
                                base_address = reader.readByte() & 0xFF;
                                base_address |= (reader.readByte() & 0xFFL) << 8;
                                base_address |= (reader.readByte() & 0xFFL) << 16;
                                base_address |= (reader.readByte() & 0xFFL) << 24;
                                address = base_address;
                                if (prev_base_address == 0) {
                                    prev_base_address = base_address;
                                }
                                break;

                            case DW_LNE_define_file:
                                define_file = reader.readString();
                                reader.readUnsignedLEB128();
                                reader.readUnsignedLEB128();
                                reader.readUnsignedLEB128();
                                break;

                            default:
                                reader.seek(reader.getFilePointer() + insn_len);
                                break;
                        }
                        // fallthrough is legitimate (program author said)
                    }
                    case DW_LNS_copy:
                        lineNumber = prev_lineno;
                        sourceFile = ((prev_fileno >= 0 && prev_fileno + 1 < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                        result.add(new LineNumber(sourceFile, lineNumber, prev_base_address, address));
                        prev_lineno = lineno;
                        prev_fileno = fileno;
                        break;

                    case DW_LNS_advance_pc:
                        {
                            long amt = reader.readUnsignedLEB128();
                            address += amt * section.minimum_instruction_length;
                        }
                        break;

                    case DW_LNS_advance_line:
                        {
                            long amt = reader.readSignedLEB128();
                            prev_lineno = lineno;
                            lineno += (int) amt;
                        }
                        break;

                    case DW_LNS_set_file:
                        prev_fileno = fileno;
                        fileno = (reader.readUnsignedLEB128() - 1);
                        break;

                    case DW_LNS_set_column:
                        reader.readUnsignedLEB128();
                        break;

                    case DW_LNS_negate_stmt:
                        break;

                    case DW_LNS_set_basic_block:
                        break;

                    case DW_LNS_const_add_pc:
                        address += const_pc_add;
                        break;

                    case DW_LNS_fixed_advance_pc:
                        {
                            int amt = reader.readShort() & 0xFFFF;
                            address += amt;
                        }
                        break;
                }
            } else {
                int adj = (opcode & 0xFF) - section.opcode_base;
                int addr_adv = adj / section.line_range;
                int line_adv = section.line_base + (adj % section.line_range);
                long new_addr = address + addr_adv;
                int new_line = lineno + line_adv;
                sourceFile = ((prev_fileno >= 0 && prev_fileno + 1 < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                result.add(new LineNumber(sourceFile, lineno, prev_base_address, new_addr));

                prev_lineno = lineno;
                prev_fileno = fileno;
                lineno = new_line;
                address = new_addr;
            }
        }
        return result;
    }

    @SuppressWarnings("fallthrough")
    private LineNumber interpret(long target, DwarfStatementList section, long shift) throws IOException {
        long address = 0;
        long base_address = 0;
        long prev_base_address = 0;
        String define_file = null;
        int fileno = 0;
        int lineno = 1;
        int prev_fileno = 0;
        int prev_lineno = 1;
        final int const_pc_add = 245 / section.line_range;
        int lineNumber = -1;
        String sourceFile = null;

        interpret:
        while (reader.getFilePointer() < header.getSectionOffset() + shift + section.total_length) {

            int opcode = reader.readByte() & 0xFF;
            if (opcode < section.opcode_base) {
                switch (LNS.get(opcode)) {
                    case DW_LNS_extended_op: {
                        long insn_len = reader.readSignedLEB128();
                        opcode = reader.readByte();

                        switch (LNE.get(opcode)) {
                            case DW_LNE_end_sequence:
                                if (prev_base_address <= target && address > target) {
                                    lineNumber = prev_lineno;
                                    sourceFile = ((prev_fileno >= 0 && prev_fileno + 1 < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                                    return new LineNumber(sourceFile, lineNumber, prev_base_address, address);
                                }
                                prev_lineno = lineno = 1;
                                prev_fileno = fileno = 0;
                                base_address = address = 0;
                                break;

                            case DW_LNE_set_address:
                                prev_base_address = base_address;
                                base_address = reader.readByte() & 0xFF;
                                base_address |= (reader.readByte() & 0xFFL) << 8;
                                base_address |= (reader.readByte() & 0xFFL) << 16;
                                base_address |= (reader.readByte() & 0xFFL) << 24;
                                address = base_address;
                                if (prev_base_address == 0) {
                                    prev_base_address = base_address;
                                }
                                break;

                            case DW_LNE_define_file:
                                define_file = reader.readString();
                                reader.readUnsignedLEB128();
                                reader.readUnsignedLEB128();
                                reader.readUnsignedLEB128();
                                break;

                            default:
                                reader.seek(reader.getFilePointer() + insn_len);
                                break;
                        }
                        // fallthrough is legitimate (program author said)
                    }
                    case DW_LNS_copy:
                        if (prev_base_address <= target && address > target) {
                            lineNumber = prev_lineno;
                            sourceFile = ((prev_fileno >= 0 && prev_fileno + 1 < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                            return new LineNumber(sourceFile, lineNumber, prev_base_address, address);
                        }
                        prev_lineno = lineno;
                        prev_fileno = fileno;
                        break;

                    case DW_LNS_advance_pc:
                        {
                            long amt = reader.readUnsignedLEB128();
                            address += amt * section.minimum_instruction_length;
                        }
                        break;

                    case DW_LNS_advance_line:
                        {
                            long amt = reader.readSignedLEB128();
                            prev_lineno = lineno;
                            lineno += (int) amt;
                        }
                        break;

                    case DW_LNS_set_file:
                        prev_fileno = fileno;
                        fileno = (reader.readUnsignedLEB128() - 1);
                        break;

                    case DW_LNS_set_column:
                        reader.readUnsignedLEB128();
                        break;

                    case DW_LNS_negate_stmt:
                        break;

                    case DW_LNS_set_basic_block:
                        break;

                    case DW_LNS_const_add_pc:
                        address += const_pc_add;
                        break;

                    case DW_LNS_fixed_advance_pc:
                        {
                            int amt = reader.readShort() & 0xFFFF;
                            address += amt;
                        }
                        break;
                }
            } else {
                int adj = (opcode & 0xFF) - section.opcode_base;
                int addr_adv = adj / section.line_range;
                int line_adv = section.line_base + (adj % section.line_range);
                long new_addr = address + addr_adv;
                int new_line = lineno + line_adv;
                if (prev_base_address <= target && new_addr >= target) {
                    lineNumber = new_addr == target ? new_line : lineno;
                    sourceFile = ((prev_fileno >= 0 && prev_fileno + 1 < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                    return new LineNumber(sourceFile, lineNumber, prev_base_address, new_addr);
                }

                prev_lineno = lineno;
                prev_fileno = fileno;
                lineno = new_line;
                address = new_addr;
            }
        }
        return null;
    }

    public static final class LineNumber implements Comparable<LineNumber> {
        public String file;
        public int line;
        public long startOffset;
        public long endOffset;
        private LineNumber(String file, int line, long startOffset, long endOffset){
            this.file = file;
            this.line = line;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LineNumber) {
                LineNumber other = (LineNumber) obj;
                return file.equals(other.file) && line == other.line;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 67 * hash + this.line;
            return hash;
        }

        @Override
        public String toString() {
            return file+":"+line+"\t(0x"+Long.toHexString(startOffset)+"-0x"+Long.toHexString(endOffset)+")"; // NOI18N
        }

        public int compareTo(LineNumber o) {
            int res = file.compareTo(o.file);
            if (res == 0) {
                res = line - o.line;
            }
            return res;
        }
    }
}

