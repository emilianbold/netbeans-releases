/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.dwarfdump.reader;

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.elf.ElfHeader;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.elf.ProgramHeaderTable;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

/**
 *
 * @author ak119685
 */
public class ElfReader extends ByteStreamReader {
    private ElfHeader elfHeader = null;
    private ProgramHeaderTable programHeaderTable;
    private SectionHeader[] sectionHeadersTable;
    private ElfSection[] sections = null;
    private HashMap<String, Integer> sectionsMap = new HashMap<String, Integer>();
    private StringTableSection stringTableSection = null;
    
    public ElfReader(String fname) throws FileNotFoundException, IOException {
        super(fname);
        readElfHeader();
        readProgramHeaderTable();
        readSectionHeaderTable();
        
        sections = new ElfSection[sectionHeadersTable.length];
        
        // Before reading all sections need to read ElfStringTable section.
        int elfStringTableIdx = elfHeader.getELFStringTableSectionIndex();
        stringTableSection = new StringTableSection(this, elfStringTableIdx);
        sections[elfStringTableIdx] = stringTableSection;
        
        // Initialize Name-To-Idx map
        
        for (int i = 1; i < sections.length; i++) {
            sectionsMap.put(getSectionName(i), i);
        }
    }
    
    public String getSectionName(int sectionIdx) {
        if (stringTableSection == null) {
            return ".shstrtab";
        }
        
        int nameOffset = sectionHeadersTable[sectionIdx].sh_name;
        return stringTableSection.getString(nameOffset);
    }
    
    public void readElfHeader() throws IOException {
        elfHeader = new ElfHeader();
        
        byte[] bytes = new byte[16];
        read(bytes);
        
        if (bytes[0] != 0x7f || bytes[1] != 'E' || bytes[2] != 'L' || bytes[3] != 'F') {
            throw new IOException("Not an ELF file");
        }
        
        elfHeader.elfClass = bytes[4];
        elfHeader.elfData  = bytes[5];
        elfHeader.elfVersion = bytes[6];
        elfHeader.elfOs  = bytes[7];
        elfHeader.elfAbi = bytes[8];
        
        if ((elfHeader.elfClass != ElfConstants.ELFCLASS32 && elfHeader.elfClass != ElfConstants.ELFCLASS64) ||
                (elfHeader.elfData != ElfConstants.ELFDATA2LSB && elfHeader.elfData != ElfConstants.ELFDATA2MSB)) {
            throw new IOException("unknown ELF format");
        }
        
        setDataEncoding(elfHeader.elfData);
        
        elfHeader.e_type = readShort();
        elfHeader.e_machine = readShort();
        elfHeader.e_version = readInt();
        
        if (elfHeader.is32Bit()) {
            elfHeader.e_entry = readInt();
            elfHeader.e_phoff = readInt();
            elfHeader.e_shoff = readInt();
        } else {
            elfHeader.e_entry = readLong();
            elfHeader.e_phoff = readLong();
            elfHeader.e_shoff = readLong();
        }
        
        elfHeader.e_flags = readInt();
        elfHeader.e_ehsize = readShort();
        elfHeader.e_phentsize = readShort();
        elfHeader.e_phnum = readShort();
        elfHeader.e_shentsize = readShort();
        elfHeader.e_shnum = readShort();
        elfHeader.e_shstrndx = readShort();
        
        setFileClass(elfHeader.elfClass);
    }
    
    private void readProgramHeaderTable() {
        // TODO: Add code
    }
    
    private void readSectionHeaderTable() throws IOException {
        long sectionHeaderOffset = elfHeader.getSectionHeaderOffset();
        
        if (sectionHeaderOffset > 0) {
            seek(sectionHeaderOffset);
            int sectionsNum = elfHeader.getNumberOfSectionHeaders();
            
            sectionHeadersTable = new SectionHeader[sectionsNum];
            
            for (int i = 0; i < sectionsNum; i++) {
                sectionHeadersTable[i] = readSectionHeader();
            }
        }
    }
    
    private SectionHeader readSectionHeader() throws IOException {
        SectionHeader h = new SectionHeader();
        
        h.sh_name = readInt();
        h.sh_type = readInt();
        h.sh_flags = readInt();
        
        if (getFileClass() == ElfConstants.ELFCLASS32) {
            h.sh_addr = readInt();
            h.sh_offset = readInt();
            h.sh_size = readInt();
        } else {
            h.sh_addr = readLong();
            h.sh_offset = readLong();
            h.sh_size = readLong();
        }
        
        h.sh_link = readInt();
        h.sh_info = readInt();
        
        if (getFileClass() == ElfConstants.ELFCLASS32) {
            h.sh_addralign = readInt();
            h.sh_entsize = readInt();
        } else {
            h.sh_addralign = readLong();
            h.sh_entsize = readLong();
        }
        
        return h;
    }
    
    StringTableSection readStringTableSection(String sectionName) throws IOException {
        Integer sectionIdx = sectionsMap.get(sectionName);
        return (sectionIdx == null) ? null : readStringTableSection(sectionIdx);
    }
    
    StringTableSection readStringTableSection(int sectionIdx) throws IOException {
        sections[sectionIdx] = new StringTableSection(this, sectionIdx);
        return (StringTableSection)sections[sectionIdx];
    }
    
    public ElfSection getSection(String sectionName) {
        Integer sectionIdx = sectionsMap.get(sectionName);
        
        if (sectionIdx == null) {
            return null;
        }
        
        if (sections[sectionIdx] == null) {
            sections[sectionIdx] = initSection(sectionIdx, sectionName);
        }
        
        return sections[sectionIdx];
    }
    
    ElfSection initSection(Integer sectionIdx, String sectionName) {
        return null;
    }
    
    public SectionHeader getSectionHeader(int sectionIdx) {
        return sectionHeadersTable[sectionIdx];
    }
}
