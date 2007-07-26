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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.RandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.FileMagic;
import org.netbeans.modules.cnd.dwarfdump.Magic;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.elf.ElfHeader;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.elf.ProgramHeaderTable;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author ak119685
 */
public class ElfReader extends ByteStreamReader {
    private boolean isCoffFormat;
    private boolean isMachoFormat;
    private ElfHeader elfHeader = null;
    private ProgramHeaderTable programHeaderTable;
    private SectionHeader[] sectionHeadersTable;
    private ElfSection[] sections = null;
    private HashMap<String, Integer> sectionsMap = new HashMap<String, Integer>();
    private StringTableSection stringTableSection = null;
    private long shiftIvArchive = 0;
    private long lengthIvArchive = 0;
    
    public ElfReader(String fname, RandomAccessFile reader, Magic magic, long shift, long length) throws IOException {
        super(fname, reader);
        shiftIvArchive = shift;
        lengthIvArchive = length;
        readHeader(magic);
        readProgramHeaderTable();
        readSectionHeaderTable();
        
        sections = new ElfSection[sectionHeadersTable.length];
        
        // Before reading all sections need to read ElfStringTable section.
        int elfStringTableIdx = elfHeader.getELFStringTableSectionIndex();
        if (!isCoffFormat && !isMachoFormat) {
            stringTableSection = new StringTableSection(this, elfStringTableIdx);
        }
        sections[elfStringTableIdx] = stringTableSection;
        
        // Initialize Name-To-Idx map
        
        for (int i = 0; i < sections.length; i++) {
            sectionsMap.put(getSectionName(i), i);
        }
    }
    
    public String getSectionName(int sectionIdx) {
        if (!isCoffFormat && !isMachoFormat) {
            if (stringTableSection == null) {
                return ".shstrtab"; // NOI18N
            }
            
            long nameOffset = sectionHeadersTable[sectionIdx].sh_name;
            return stringTableSection.getString(nameOffset);
        } else {
            return sectionHeadersTable[sectionIdx].getSectionName();
        }
    }
    
    public void readHeader(Magic magic) throws WrongFileFormatException, IOException {
        elfHeader = new ElfHeader();
        seek(shiftIvArchive);
        byte[] bytes = new byte[16];
        read(bytes);
        switch (magic) {
            case Elf:
                readElfHeader(bytes);
                return;
            case Coff:
                readCoffHeader(shiftIvArchive);
                return;
            case Exe:
                readPeHeader(true);
                return;
            case Pe:
                readPeHeader(false);
                return;
            case Macho:
                readMachoHeader();
                return;
        }
        throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
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
            if (!FileMagic.isPeMagic(bytes)) {
                throw new WrongFileFormatException("Not an PE/COFF file"); // NOI18N
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
        long symbolTableOffset = shiftIvArchive+readInt();
        int symbolTableEntries = readInt();
        long stringTableOffset = symbolTableOffset+symbolTableEntries*18;
        int stringTableLength = (int)(shiftIvArchive + lengthIvArchive - stringTableOffset);
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
    
    private void readMachoHeader() throws IOException{
        isMachoFormat = true;
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        seek(shiftIvArchive);
        boolean is64 = readByte() == (byte)0xcf;
        seek(shiftIvArchive+16);
        int ncmds = readInt();
        int sizeOfCmds = readInt();
        int flags = readInt();
        if (is64){
            skipBytes(4);
        }
        List<SectionHeader> headers = new ArrayList<SectionHeader>();
        for (int j = 0; j < ncmds; j++){
            int cmd = readInt();
            int cmdSize = readInt();
            if (cmd == 1 || cmd == 25) { //LC_SEGMENT LC_SEGMENT64
                skipBytes(16);
                if (is64) {
                    long vmAddr = readLong();
                    long vmSize = readLong();
                    long fileOff = readLong();
                    long fileSize = readLong();
                } else {
                    int vmAddr = readInt();
                    int vmSize = readInt();
                    int fileOff = readInt();
                    int fileSize = readInt();
                }
                int vmMaxPort = readInt();
                int vmInitPort = readInt();
                int nSects = readInt();
                int cmdFlags = readInt();
                for (int i = 0; i < nSects; i++){
                    SectionHeader h = readMachoSection(is64);
                    if (h != null){
                        headers.add(h);
                    }
                }
            } else if (cmd == 2){ //LC_SYMTAB
                int symOffset = readInt();
                int nsyms = readInt();
                long strOffset = readInt()+shiftIvArchive;
                int strSize = readInt();
                // read string table
                long pointer = getFilePointer();
                seek(strOffset);
                byte[] strings = new byte[strSize];
                read(strings);
                stringTableSection = new StringTableSection(this, strings);
                seek(pointer);
            } else {
                skipBytes(cmdSize - 8);
            }
        }
        if (headers.size()==0 || stringTableSection == null){
            throw new WrongFileFormatException("Dwarf section not found in Mach-O file"); // NOI18N
        }
        sectionHeadersTable = new SectionHeader[headers.size()];
        for(int i = 0; i < headers.size(); i++){
            sectionHeadersTable[i] = headers.get(i);
        }
        elfHeader.e_shstrndx = (short)(headers.size()-1);
    }
    
    
    private SectionHeader readMachoSection(boolean is64) throws IOException {
        byte[] sectName = new byte[16];
        read(sectName);
        String section = getName(sectName, 0);
        byte[] segName = new byte[16];
        read(segName);
        String segment = getName(segName, 0);
        long addr;
        long size;
        if (is64) {
            addr = readLong();
            size = readLong();
        } else {
            addr = readInt();
            size = readInt();
        }
        long offset = readInt()+shiftIvArchive;
        int align = readInt();
        int reloff = readInt();
        int nreloc = readInt();
        int segFlags = readInt();
        readInt();
        readInt();
        if (is64) {
            readInt();
        }
        if ("__DWARF".equals(segment)){// NOI18N
            SectionHeader h = new SectionHeader();
            if (section.startsWith("__debug")){// NOI18N
                // convert to elf standard
                section = "."+section.substring(2);// NOI18N
            }
            h.name = section;
            h.sh_size = size;
            h.sh_offset = offset;
            h.sh_flags = segFlags;
            return h;
        }
        return null;
    }
    
    
    private String getName(byte[] stringtable, int offset){
        StringBuilder str = new StringBuilder();
        for (int i = offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }
        return str.toString();
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
        h.sh_offset = shiftIvArchive + readInt();
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
