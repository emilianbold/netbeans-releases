/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;

/**
 *
 * @author Alexander Simon
 */
public class DwarfRelaDebugInfoSection extends ElfSection {
    private final Map<Long, Long> table = new HashMap<Long,Long>();
    private int abbrTableIndex = -1;
    private final Map<Long, Long> abbrTable = new HashMap<Long,Long>();

    public DwarfRelaDebugInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
        try {
            read();
        } catch (IOException ex) {
            Logger.getLogger(DwarfRelaDebugInfoSection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Long getAddend(long offset){
        return table.get(offset);
    }

    public Long getAbbrAddend(long offset){
        return abbrTable.get(offset);
    }

    @Override
    public final DwarfArangesSection read() throws IOException {
        long sectionStart = header.getSectionOffset();
        long sectionEnd = header.getSectionSize() + sectionStart;
        reader.seek(sectionStart);
        int i = 0;
        //System.out.println("N\tr_offset\tr_info\tr_addend");
        while(reader.getFilePointer()<sectionEnd) {
            long r_offset = reader.read3264();
            long r_info = reader.read3264();
            long r_addend = reader.read3264();
            //System.out.println(""+i+"\t"+r_offset+"\t0x"+Long.toHexString(r_info)+"\t"+r_addend);
            if (abbrTableIndex >=0 && getR_SYM(r_info) == abbrTableIndex) {
                abbrTable.put(r_offset, r_addend);
            } else {
                table.put(r_offset, r_addend);
            }
            if (r_offset == 6 && r_addend == 0) {
                // TODO:
                // This is ugly fix for Bug 199737 - Wrong parsing of dwarf *.o files
                // Right fix should read .symtab section and getR_SYM(r_info) should point to .debug_abbrev in .symtab
                abbrTableIndex = getR_SYM(r_info);
            }
            i++;
        }
        return null;
    }
    
    private int getR_SYM(long r_info) {
        if (reader.is64Bit()) {
            return (int)(r_info>>32);
        } else {
            return (int)(r_info>>8);
        }
    }

//    Linux:
//    #define R_X86_64_NONE           0       /* No reloc */
//    #define R_X86_64_64             1       /* Direct 64 bit  */
//    #define R_X86_64_PC32           2       /* PC relative 32 bit signed */
//    #define R_X86_64_GOT32          3       /* 32 bit GOT entry */
//    #define R_X86_64_PLT32          4       /* 32 bit PLT address */
//    #define R_X86_64_COPY           5       /* Copy symbol at runtime */
//    #define R_X86_64_GLOB_DAT       6       /* Create GOT entry */
//    #define R_X86_64_JUMP_SLOT      7       /* Create PLT entry */
//    #define R_X86_64_RELATIVE       8       /* Adjust by program base */
//    #define R_X86_64_GOTPCREL       9       /* 32 bit signed pc relative
//                                               offset to GOT */
//    #define R_X86_64_32             10      /* Direct 32 bit zero extended */
//    #define R_X86_64_32S            11      /* Direct 32 bit sign extended */
//    #define R_X86_64_16             12      /* Direct 16 bit zero extended */
//    #define R_X86_64_PC16           13      /* 16 bit sign extended pc relative */
//    #define R_X86_64_8              14      /* Direct 8 bit sign extended  */
//    #define R_X86_64_PC8            15      /* 8 bit sign extended pc relative */
//
//    #define R_X86_64_NUM            16
    private int getR_TYPE(long r_info) {
        return (int)(r_info & 0xFFL);
    }
}
