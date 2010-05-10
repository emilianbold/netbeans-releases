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
 * DwarfMacroInfoSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author ak119685
 */
public class DwarfMacroInfoSection extends ElfSection {
    private final HashMap<Long, DwarfMacinfoTable> macinfoTables = new HashMap<Long, DwarfMacinfoTable>();
    
    public DwarfMacroInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    public DwarfMacinfoTable getMacinfoTable(long offset) {
        Long lOffset = Long.valueOf(offset);
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
        if (baseOnly) {
            if (type.equals(MACINFO.DW_MACINFO_start_file)) {
                long lineNum = reader.readUnsignedLEB128();
                if (lineNum == 0) {
                    long fileIdx = reader.readUnsignedLEB128();
                } else {
                    reader.seek(header.getSectionOffset() + offset);
                }
                type = MACINFO.get(reader.readByte());
            }
        }
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

    public List<Integer> getCommandIncudedFiles(DwarfMacinfoTable table, long offset) throws IOException{
        List<Integer> res = new ArrayList<Integer>();
        long currPos = reader.getFilePointer();
        reader.seek(header.getSectionOffset() + offset);
        int level = 0;
        int lineNum;
        int  fileIdx;
        loop:while (true) {
            MACINFO type = MACINFO.get(reader.readByte());
            if (type == null) {
                break;
            }
            switch (type) {
                case DW_MACINFO_start_file:
                    level++;
                    lineNum = reader.readUnsignedLEB128();
                    fileIdx = reader.readUnsignedLEB128();
                    if (level == 1) {
                        if (lineNum == 0) {
                            res.add(fileIdx);
                        } else {
                            break loop;
                        }
                    }
                    break;
                case DW_MACINFO_end_file:
                    level--;
                    break;
                case DW_MACINFO_vendor_ext:
                    reader.readUnsignedLEB128();
                    reader.readString();
                    break;
                case DW_MACINFO_define:
                case DW_MACINFO_undef:
                    lineNum = reader.readUnsignedLEB128();
                    reader.readString();
            }
        }
        return res;
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        
        for (DwarfMacinfoTable macinfoTable : macinfoTables.values()) {
            macinfoTable.dump(out);
        }
    }    

    @Override
    public String toString() {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out);
        return st.toString();
    }
}
