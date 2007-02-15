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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.elf.ElfHeader;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
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
    private boolean isCoffFormat;
    private ElfHeader elfHeader = null;
    private ProgramHeaderTable programHeaderTable;
    private SectionHeader[] sectionHeadersTable;
    private ElfSection[] sections = null;
    private HashMap<String, Integer> sectionsMap = new HashMap<String, Integer>();
    private StringTableSection stringTableSection = null;
    private long shiftIvArchive = 0;
    
    public ElfReader(String fname) throws FileNotFoundException, IOException {
        super(fname);
        readHeader();
        readProgramHeaderTable();
        readSectionHeaderTable();
        
        sections = new ElfSection[sectionHeadersTable.length];
        
        // Before reading all sections need to read ElfStringTable section.
        int elfStringTableIdx = elfHeader.getELFStringTableSectionIndex();
        if (!isCoffFormat) {
            stringTableSection = new StringTableSection(this, elfStringTableIdx);
        }
        sections[elfStringTableIdx] = stringTableSection;
        
        // Initialize Name-To-Idx map
        for (int i = 1; i < sections.length; i++) {
            sectionsMap.put(getSectionName(i), i);
        }
    }
    
    public String getSectionName(int sectionIdx) {
        if (!isCoffFormat) {
            if (stringTableSection == null) {
                return ".shstrtab"; // NOI18N
            }
        
            long nameOffset = sectionHeadersTable[sectionIdx].sh_name;
            return stringTableSection.getString(nameOffset);
        } else {
            return sectionHeadersTable[sectionIdx].getSectionName();
        }
    }
    
    public void readHeader() throws WrongFileFormatException, IOException {
        elfHeader = new ElfHeader();
        
        byte[] bytes = new byte[16];
        read(bytes);
        if (isElfMagic(bytes)) {
            readElfHeader(bytes);
            return;
        } else if (isCoffMagic(bytes)) {
            readCoffHeader(0);
            return;
        } else if (isExeMagic(bytes)) {
            readPeHeader(true);
            return;
        } else if (isPeMagic(bytes)) {
            readPeHeader(false);
            return;
        } else if (isArchiveMagic(bytes)) {
            skipFirstHeader();
            List<Long> offsets = getElfTable();
            if (offsets.size()==0) {
                throw new WrongFileFormatException("Not an ELF file"); // NOI18N
            }
            shiftIvArchive = offsets.get(0).longValue();
            seek(shiftIvArchive);
            read(bytes);
            if (isElfMagic(bytes)) {
                readElfHeader(bytes);
            } else if (isCoffMagic(bytes)) {
                readCoffHeader(shiftIvArchive);
            }
            return;
        }
        throw new WrongFileFormatException("Not an ELF/PE/COFF file"); // NOI18N
    }
    
    private void readElfHeader( byte[] bytes) throws IOException{
        elfHeader.elfClass = bytes[4];
        elfHeader.elfData  = bytes[5];
        elfHeader.elfVersion = bytes[6];
        elfHeader.elfOs  = bytes[7];
        elfHeader.elfAbi = bytes[8];
        
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        
        elfHeader.e_type      = readShort();
        elfHeader.e_machine   = readShort();
        elfHeader.e_version   = readInt();
        elfHeader.e_entry     = read3264()+shiftIvArchive;
        elfHeader.e_phoff     = read3264()+shiftIvArchive;
        elfHeader.e_shoff     = read3264()+shiftIvArchive;
        elfHeader.e_flags     = readInt();
        elfHeader.e_ehsize    = readShort();
        elfHeader.e_phentsize = readShort();
        elfHeader.e_phnum     = readShort();
        elfHeader.e_shentsize = readShort();
        elfHeader.e_shnum     = readShort();
        elfHeader.e_shstrndx  = readShort();
    }
    
    private boolean isExeMagic(byte[] bytes){
        return bytes[0] == 'M' && bytes[1] == 'Z';
    }

    private boolean isPeMagic(byte[] bytes){
        return bytes[0] == 'P' && bytes[1] == 'E' && bytes[2] == 0 && bytes[3] == 0;
    }

    private boolean isCoffMagic(byte[] bytes){
        return bytes[0] == 0x4c && bytes[1] == 0x01;
    }
    
    private boolean isElfMagic(byte[] bytes){
        return bytes[0] == 0x7f && bytes[1] == 'E' && bytes[2] == 'L' && bytes[3] == 'F';
    }
    
    private boolean isArchiveMagic(byte[] bytes){
        return bytes[0] == '!' && bytes[1] == '<' && bytes[2] == 'a' && bytes[3] == 'r' &&
                bytes[4] == 'c' && bytes[5] == 'h' && bytes[6] == '>' && bytes[7] == '\n';
    }
    
    private void readPeHeader(boolean isExe) throws IOException{
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        int peOffset = 0;
        if (isExe) {
            seek(0x3c);
            peOffset = readInt();
            seek(peOffset);
            byte[] bytes = new byte[4];
            read(bytes);
            if (!isPeMagic(bytes)) {
                throw new WrongFileFormatException("Not an ELF/PE/COFF file"); // NOI18N
            }
        }
        // skip PE magic
        readCoffHeader(peOffset+4);
    }
    
    private void readCoffHeader(long shift) throws IOException{
        isCoffFormat = true;
        // Skip magic
        seek(2+shift);
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        elfHeader.e_shnum = readShort();
        //skip time stump
        readInt();
        // read string table
        int symbolTableOffset = readInt();
        int symbolTableEntries = readInt();
        int stringTableOffset = symbolTableOffset+symbolTableEntries*18;
        int stringTableLength = (int)length() - stringTableOffset;
        long pointer = getFilePointer();
        seek(stringTableOffset);
        byte[] strings = new byte[stringTableLength];
        read(strings);
        stringTableSection = new StringTableSection(this, strings);
        seek(pointer);
        // 
        int optionalHeaderSize = readShort();
        // flags
        int flags = readShort();
        if (optionalHeaderSize > 0) {
            skipBytes(optionalHeaderSize);
        }
        elfHeader.e_shoff = getFilePointer();
    }
    
    private String getName(byte[] stringtable, int offset){
        StringBuffer str = new StringBuffer();
        for (int i = offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }
        return str.toString();
    }
    
    private void skipFirstHeader() throws IOException{
        byte[] next = new byte[52];
        read(next);
        int length = 0;
        for (int i = 0; i < 10; i++){
            byte c = next[i+40];
            if (c == ' ' ){
                break;
            }
            length*=10;
            length+=(c-'0');
        }
        // Skip first header
        skipBytes(length);
    }
    
    private List<Long> getElfTable() throws IOException{
        byte[] next = new byte[60];
        ArrayList<Long> offsets= new ArrayList<Long>();
        while(true) {
            if (getFilePointer()+60 >= length()){
                break;
            }
            read(next);
            int length = 0;
            for (int i = 0; i < 10; i++){
                byte c = next[i+48];
                if (c == ' '){
                    break;
                }
                length*=10;
                length+=(c-'0');
            }
            //System.out.println(new String(next, 0, 16));
            if (next[0] == '/') {
                // skip;
                skipBytes(length);
                continue;
            } else if (next[0] == '\n') {
                break;
            }
            long pointer = getFilePointer();
            byte[] bytes = new byte[4];
            read(bytes);
            if (isElfMagic(bytes)) {
                offsets.add(new Long(pointer));
            }
            skipBytes(length-4);
        }
        return offsets;
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
                if (isCoffFormat) {
                    sectionHeadersTable[i] = readCoffSectionHeader();
                } else {
                    sectionHeadersTable[i] = readSectionHeader();
                }
            }
        }
    }
    
    private SectionHeader readSectionHeader() throws IOException {
        SectionHeader h = new SectionHeader();
        
        h.sh_name      = readInt();
        h.sh_type      = readInt();
        h.sh_flags     = read3264();
        h.sh_addr      = read3264();
        h.sh_offset    = read3264()+shiftIvArchive;
        h.sh_size      = read3264();
        h.sh_link      = readInt();
        h.sh_info      = readInt();
        h.sh_addralign = read3264();
        h.sh_entsize   = read3264();
        
        return h;
    }
    
    private SectionHeader readCoffSectionHeader() throws IOException {
        SectionHeader h = new SectionHeader();
        
        byte[] bytes = new byte[8];
        read(bytes);
        String name = null;
        if (bytes[0] == '/'){
            int length = 0;
            for (int j = 1; j < 8; j++){
                byte c = bytes[j];
                if (c < '0' ){
                    break;
                }
                length*=10;
                length+=(c-'0');
            }
            name = stringTableSection.getString(length);
        } else {
            name = getName(bytes,0);
        }
        h.name = name;
        //System.out.println("Section: "+name);
        int phisicalAddres = readInt();
        int virtualAddres = readInt();
        h.sh_size = readInt();
        h.sh_offset = readInt();
        int relocationOffset = readInt();
        int lineNumberOffset = readInt();
        int mumberRelocations = readShort();
        int mumberLineNumbers = readShort();
        h.sh_flags = readInt();
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
