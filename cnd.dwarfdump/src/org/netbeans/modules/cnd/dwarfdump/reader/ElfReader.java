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

package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.RandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.FileMagic;
import org.netbeans.modules.cnd.dwarfdump.Magic;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.elf.ElfHeader;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.trace.TraceDwarf;

/**
 *
 * @author ak119685
 */
public class ElfReader extends ByteStreamReader {
    private boolean isCoffFormat;
    private boolean isMachoFormat;
    private ElfHeader elfHeader = null;
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
        if (!readHeader(magic)) {
            return;
        }
        readProgramHeaderTable();
        readSectionHeaderTable();
        
        sections = new ElfSection[sectionHeadersTable.length];
        
        if (!isCoffFormat) {
            // Before reading all sections need to read ElfStringTable section.
            int elfStringTableIdx = elfHeader.getELFStringTableSectionIndex();
            stringTableSection = new StringTableSection(this, elfStringTableIdx);
            sections[elfStringTableIdx] = stringTableSection;
        }
        
        // Initialize Name-To-Idx map
        for (int i = 0; i < sections.length; i++) {
            sectionsMap.put(getSectionName(i), i);
        }
        if (isCoffFormat) {
            // string table already read
            Integer idx = sectionsMap.get(SECTIONS.DEBUG_STR);
            if (idx != null) {
                sections[idx] = stringTableSection;
            }
        }
    }
    
    public final String getSectionName(int sectionIdx) {
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
    
    public final boolean readHeader(Magic magic) throws WrongFileFormatException, IOException {
        elfHeader = new ElfHeader();
        seek(shiftIvArchive);
        byte[] bytes = new byte[16];
        read(bytes);
        switch (magic) {
            case Elf:
                readElfHeader(bytes);
                return true;
            case Coff:
                readCoffHeader(shiftIvArchive);
                return true;
            case Exe:
                readPeHeader(true);
                return true;
            case Pe:
                readPeHeader(false);
                return true;
            case Macho:
                return readMachoHeader();
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
        //
        int optionalHeaderSize = readShort();
        // flags
        /*int flags =*/ readShort();
        if (optionalHeaderSize > 0) {
            skipBytes(optionalHeaderSize);
        }
        elfHeader.e_shoff = getFilePointer();
        // read string table
        long pointer = getFilePointer();
        seek(stringTableOffset);
        byte[] strings = new byte[stringTableLength];
        read(strings);
        stringTableSection = new StringTableSection(this, strings);
        seek(pointer);
    }
    
    private boolean readMachoHeader() throws IOException{
        isMachoFormat = true;
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        seek(shiftIvArchive);
        boolean is64 = readByte() == (byte)0xcf;
        seek(shiftIvArchive+16);
        int ncmds = readInt();
        /*int sizeOfCmds =*/ readInt();
        /*int flags =*/ readInt();
        if (is64){
            skipBytes(4);
        }
        List<SectionHeader> headers = new ArrayList<SectionHeader>();
        for (int j = 0; j < ncmds; j++){
            int cmd = readInt();
            int cmdSize = readInt();
            if (TraceDwarf.TRACED) {
                System.out.println("Load command: " + LoadCommand.valueOf(cmd) + " (" + cmd + ")"); //NOI18N
            }
            if (LoadCommand.LC_SEGMENT.is(cmd) || LoadCommand.LC_SEGMENT_64.is(cmd) ) { //LC_SEGMENT LC_SEGMENT64
                skipBytes(16);
                if (is64) {
                    /*long vmAddr =*/ readLong();
                    /*long vmSize =*/ readLong();
                    /*long fileOff =*/ readLong();
                    /*long fileSize =*/ readLong();
                } else {
                    /*int vmAddr =*/ readInt();
                    /*int vmSize =*/ readInt();
                    /*int fileOff =*/ readInt();
                    /*int fileSize =*/ readInt();
                }
                /*int vmMaxPort =*/ readInt();
                /*int vmInitPort =*/ readInt();
                int nSects = readInt();
                /*int cmdFlags =*/ readInt();
                for (int i = 0; i < nSects; i++){
                    SectionHeader h = readMachoSection(is64);
                    if (h != null){
                        headers.add(h);
                    }
                }
            } else if (LoadCommand.LC_SYMTAB.is(cmd)){ //LC_SYMTAB
                /*int symOffset =*/ readInt();
                /*int nsyms =*/ readInt();
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
        if (TraceDwarf.TRACED && stringTableSection!=null ) {
            stringTableSection.dump(System.out);
        }
        if (headers.isEmpty() || stringTableSection == null){
            if (isThereAnyLinkedObjectFiles(stringTableSection)) {
                // we got situation when Mac's linker put dwarf information not in the executable file
                // but just put links to object files instead
                return false;
            }
            throw new WrongFileFormatException("Dwarf section not found in Mach-O file"); // NOI18N
        }
        // clear string section, another string section will be read late
        stringTableSection = null;
        sectionHeadersTable = new SectionHeader[headers.size()];
        for(int i = 0; i < headers.size(); i++){
            sectionHeadersTable[i] = headers.get(i);
        }
        elfHeader.e_shstrndx = (short)(headers.size()-1);
        return true;
    }
    
    private boolean isThereAnyLinkedObjectFiles(StringTableSection stringTableSection) {
        if (stringTableSection == null) {
            return false;
        }
        int offset = 1;
        while (offset < stringTableSection.getStringTable().length) {
            String string = stringTableSection.getString(offset);
            // XXX: find out how gdb determines object files link
            // but this way would work 90% of times
            if (string.length() > 2 && ".o".equals(string.substring(string.length()-2))) { //NOI18N
                linkedObjectFiles.add(string);
            }
            offset += string.length() + 1;
        }

        return linkedObjectFiles.size() > 0;
    }
    
    private List<String> linkedObjectFiles = new ArrayList<String>();
    
    public List<String> getLinkedObjectFiles() {
        return linkedObjectFiles;
    }
    
    private SectionHeader readMachoSection(boolean is64) throws IOException {
        byte[] sectName = new byte[16];
        read(sectName);
        String section = getName(sectName, 0);
        byte[] segName = new byte[16];
        read(segName);
        String segment = getName(segName, 0);
        long size;
        if (is64) {
            /*long addr =*/ readLong();
            size = readLong();
        } else {
            /*long addr =*/ readInt();
            size = readInt();
        }
        long offset = readInt()+shiftIvArchive;
        /*int align =*/ readInt();
        /*int reloff =*/ readInt();
        /*int nreloc =*/ readInt();
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
        } else if (TraceDwarf.TRACED) {
            System.out.println("Segment,Section: " + segment + "," + section); //NOI18N
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
        /*int phisicalAddres =*/ readInt();
        /*int virtualAddres =*/ readInt();
        h.sh_size = readInt();
        h.sh_offset = shiftIvArchive + readInt();
        /*int relocationOffset =*/ readInt();
        /*int lineNumberOffset =*/ readInt();
        /*int mumberRelocations =*/ readShort();
        /*int mumberLineNumbers =*/ readShort();
        h.sh_flags = readInt();
        return h;
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

enum LoadCommand {
    UNKNOWN, LC_SEGMENT, LC_SYMTAB, LC_SYMSEG, LC_THREAD, LC_UNIXTHREAD, LC_LOADFVMLIB, LC_IDFVMLIB, LC_IDENT, LC_FVMFILE, LC_PREPAGE, LC_DYSYMTAB, LC_LOAD_DYLIB, LC_ID_DYLIB, LC_LOAD_DYLINKER, LC_ID_DYLINKER, LC_PREBOUND_DYLIB, LC_ROUTINES, LC_SUB_FRAMEWORK, LC_SUB_UMBRELLA, LC_SUB_CLIENT, LC_SUB_LIBRARY, LC_TWOLEVEL_HINTS, LC_PREBIND_CKSUM, LC_LOAD_WEAK_DYLIB, LC_SEGMENT_64, LC_ROUTINES_64, LC_UUID, UNDEFINED, LC_CODE_SIGNATURE;

    public boolean is(int value) {
        return ordinal() == value;
    }
    
    public static LoadCommand valueOf(int k) {
        for (LoadCommand command : values()) {
            if (command.is(k)) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
