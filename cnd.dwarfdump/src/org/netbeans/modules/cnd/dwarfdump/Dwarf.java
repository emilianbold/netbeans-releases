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

package org.netbeans.modules.cnd.dwarfdump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;

/**
 *
 * @author ak119685
 */
public class Dwarf {
    private DwarfReader dwarfReader;
    private boolean isArchive;
    private List<MemberHeader> offsets;
    private FileMagic magic;
    private String fileName;
    
    public Dwarf(String objFileName) throws FileNotFoundException, WrongFileFormatException, IOException {
        fileName = objFileName;
        magic = new FileMagic(objFileName);
        if (magic.getMagic() == Magic.Arch){
            // support archives
            skipFirstHeader(magic.getReader());
            offsets = getObjectTable(magic.getReader());
            if (offsets.size()==0) {
                throw new WrongFileFormatException("Not an ELF file"); // NOI18N
            }
            isArchive = true;
        } else {
            isArchive = false;
            dwarfReader = new DwarfReader(objFileName, magic.getReader(), magic.getMagic(), 0, magic.getReader().length());
        }
    }
    
    private void skipFirstHeader(RandomAccessFile reader) throws IOException{
        reader.seek(8);
        byte[] next = new byte[60];
        reader.readFully(next);
        int length = 0;
        for (int i = 0; i < 10; i++){
            byte c = next[i+48];
            if (c == ' ' ){
                break;
            }
            length*=10;
            length+=(c-'0');
        }
        // Skip first header
        reader.skipBytes(length);
    }
    
    private List<MemberHeader> getObjectTable(RandomAccessFile reader) throws IOException{
        byte[] next = new byte[60];
        ArrayList<MemberHeader> offsets= new ArrayList<MemberHeader>();
        while(true) {
            if (reader.getFilePointer()+60 >= reader.length()){
                break;
            }
            reader.readFully(next);
            int length = readNumber(next, 48);
            int nameLength = 0;
            //System.out.println(new String(next, 0, 16));
            if (next[0] == '/' && next[1] == '/') {
                // skip long name section;
                reader.skipBytes(length);
                continue;
            } else if (next[0] == '#' && next[1] == '1' && next[2] == '/') {
                nameLength = readNumber(next, 3);
                reader.skipBytes(nameLength);
            } else if (next[0] == '\n') {
                break;
            }
            long pointer = reader.getFilePointer();
            byte[] bytes = new byte[8];
            reader.readFully(bytes);
            if (FileMagic.isElfMagic(bytes) || FileMagic.isCoffMagic(bytes) || FileMagic.isMachoMagic(bytes)) {
                offsets.add(new MemberHeader(pointer,length));
            }
            reader.skipBytes(length-8-nameLength);
            if (length % 2 == 1){
                reader.skipBytes(1);
            }
        }
        return offsets;
    }

    private int readNumber(final byte[] next, int shift) {
        int length = 0;
        for (int i = 0; i < 10; i++){
            byte c = next[i+shift];
            if (c == ' '){
                break;
            }
            length*=10;
            length+=(c-'0');
        }
        return length;
    }
    
    public void dispose(){
        if (magic != null) {
            try {
                magic.getReader().close();
            } catch (IOException ex) {
                //ex.printStackTrace();
            }
            magic = null;
        }
    }
    
    public CompilationUnit getCompilationUnit(String srcFileFullName) {
        for (CompilationUnit unit : getCompilationUnits()) {
            // TODO: remove hack
            
            String srcFileName = srcFileFullName.substring(srcFileFullName.lastIndexOf(File.separatorChar));
            String unitFileName = unit.getSourceFileFullName();
            unitFileName = unitFileName.substring(unitFileName.lastIndexOf(File.separatorChar));
            
            if (unitFileName.equals(srcFileName)) {
                //if (unit.getSourceFileFullName().equals(srcFileFullName)) {
                return unit;
            }
        }
        
        return null;
    }
    
    public ElfSection getSection(String sectionName) {
	return dwarfReader.getSection(sectionName);
    }
    
    public String getFileName() {
	return fileName;
    }
    
    public List<CompilationUnit> getCompilationUnits() {
        if (isArchive) {
            return getArchiveCompilationUnits(magic.getReader());
        } else {
            return getFileCompilationUnits();
        }
    }
    
    private List<CompilationUnit> getFileCompilationUnits() {
        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)dwarfReader.getSection(SECTIONS.DEBUG_INFO);
        List<CompilationUnit> result = null;
        try {
            if (debugInfo != null) {
                result = debugInfo.getCompilationUnits();
            } else {
                result = new LinkedList<CompilationUnit>();
            }
        } catch (IOException ex) {
            System.err.println("Exception in file: " + dwarfReader.getFileName());
            ex.printStackTrace();
        }
        
        return result;
    }
    
    private List<CompilationUnit> getArchiveCompilationUnits(RandomAccessFile reader) {
        List<CompilationUnit> result = new LinkedList<CompilationUnit>();
        for(MemberHeader member : offsets){
            try {
                long shiftIvArchive = member.getOffset();
                int length = member.getLength();
                reader.seek(shiftIvArchive);
                byte[] bytes = new byte[8];
                reader.readFully(bytes);
                if (FileMagic.isElfMagic(bytes)) {
                    dwarfReader = new DwarfReader(fileName, reader, Magic.Elf, shiftIvArchive, length);
                } else if (FileMagic.isCoffMagic(bytes)) {
                    dwarfReader = new DwarfReader(fileName, reader, Magic.Coff, shiftIvArchive, length);
                } else if (FileMagic.isMachoMagic(bytes)) {
                    dwarfReader = new DwarfReader(fileName, reader, Magic.Macho, shiftIvArchive, length);
                }
                result.addAll(getFileCompilationUnits());
            } catch (IOException ex) {
                System.err.println("Exception in file: " + dwarfReader.getFileName());
                ex.printStackTrace();
            }
        }
        return result;
    }
}
