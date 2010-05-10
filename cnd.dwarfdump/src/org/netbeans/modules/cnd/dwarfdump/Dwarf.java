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

package org.netbeans.modules.cnd.dwarfdump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.trace.TraceDwarf;

/**
 *
 * @author ak119685
 */
public class Dwarf {
    private DwarfReader dwarfReader;
    private List<MemberHeader> offsets;
    private FileMagic magic;
    private String fileName;
    private List<FileMagic> toDispose = new ArrayList<FileMagic>();
    
    private enum Mode {
        Normal, Archive, MachoLOF
    };
    private final Mode mode;
    
    public Dwarf(String objFileName) throws IOException {
        if (TraceDwarf.TRACED) {
            System.out.println("\n**** Dwarfing " + objFileName + "\n"); //NOI18N
        }
        fileName = objFileName;
        try {
            magic = new FileMagic(objFileName);
            if (magic.getMagic() == Magic.Arch){
                // support archives
                skipFirstHeader(magic.getReader());
                offsets = getObjectTable(magic.getReader());
                if (offsets.isEmpty()) {
                    throw new WrongFileFormatException("Not an ELF file"); // NOI18N
                }
                mode = Mode.Archive;
            } else {
                dwarfReader = new DwarfReader(objFileName, magic.getReader(), magic.getMagic(), 0, magic.getReader().length());
                if (dwarfReader.getLinkedObjectFiles().size() > 0) {
                    // Mach-O left dwarf info in linked object files
                    mode = Mode.MachoLOF;
                } else {
                    mode = Mode.Normal;
                }

            }
        } catch (IOException ex) {
            dispose();
            throw ex;
        }
    }
    
    public final void dispose(){
        if (magic != null) {
            magic.dispose();
            magic = null;
        }
        for(FileMagic file : toDispose){
            file.dispose();
        }
        toDispose.clear();
    }
    
    public ElfSection getSection(String sectionName) {
	return dwarfReader.getSection(sectionName);
    }
    
    public String getFileName() {
	return fileName;
    }

    public Iterator<CompilationUnit> iteratorCompilationUnits() throws IOException {
        if (mode == Mode.Archive) {
            return new ArchiveIterator(magic.getReader());
        } else if (mode == Mode.Normal) {
            return iteratorFileCompilationUnits();
        } else {// mode = Mode.MachoLOF
            return new MacArchiveIterator();
        }
    }
    
    public List<CompilationUnit> getCompilationUnits() throws IOException {
        if (mode == Mode.Archive) {
            return getArchiveCompilationUnits(magic.getReader());
        } else if (mode == Mode.Normal) {
            return getFileCompilationUnits();
        } else {// mode = Mode.MachoLOF
            return getMachoLOFCompilationUnits();
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
        ArrayList<MemberHeader> offsetsList= new ArrayList<MemberHeader>();
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
                offsetsList.add(new MemberHeader(pointer,length));
            }
            reader.skipBytes(length-8-nameLength);
            if (length % 2 == 1){
                reader.skipBytes(1);
            }
        }
        return offsetsList;
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

    private List<CompilationUnit> getFileCompilationUnits() throws IOException {
        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)dwarfReader.getSection(SECTIONS.DEBUG_INFO);
        List<CompilationUnit> result = null;
        if (debugInfo != null) {
            result = debugInfo.getCompilationUnits();
        } else {
            result = new LinkedList<CompilationUnit>();
        }
        return result;
    }

    private Iterator<CompilationUnit> iteratorFileCompilationUnits() throws IOException {
        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)dwarfReader.getSection(SECTIONS.DEBUG_INFO);
        Iterator<CompilationUnit> result = null;
        if (debugInfo != null) {
            result = debugInfo.iteratorCompilationUnits();
        } else {
            result = new LinkedList<CompilationUnit>().iterator();
        }
        return result;
    }

    private List<CompilationUnit> getMachoLOFCompilationUnits() throws IOException {
        List<CompilationUnit> result = new LinkedList<CompilationUnit>();
        for (String string : dwarfReader.getLinkedObjectFiles()) {
            Dwarf gimli = new Dwarf(string);
            result.addAll(gimli.getCompilationUnits());
            toDispose.add(gimli.magic);
        }
        return result;
    }

    private List<CompilationUnit> getArchiveCompilationUnits(RandomAccessFile reader) throws IOException {
        List<CompilationUnit> result = new LinkedList<CompilationUnit>();
        for (MemberHeader member : offsets) {
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
        }
        return result;
    }

    private class MacArchiveIterator implements Iterator<CompilationUnit>{
        private List<String> objectFiles;
        private int archiveIndex = 0;
        private Iterator<CompilationUnit> currentList;
        private Dwarf currentDwarf;

        public MacArchiveIterator() throws IOException{
            objectFiles = dwarfReader.getLinkedObjectFiles();
            advanceArchive();
        }

        @Override
        public boolean hasNext() {
            if (currentList == null) {
                return false;
            }
            if (currentList.hasNext()) {
                return true;
            }
            try {
                advanceArchive();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
            return currentList != null;
        }

        @Override
        public CompilationUnit next() {
            return currentList.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advanceArchive() throws IOException {
            while (true) {
                if (currentDwarf != null) {
                    toDispose.remove(currentDwarf.magic);
                    currentDwarf.magic.dispose();
                    currentDwarf = null;
                }
                if (archiveIndex < objectFiles.size()) {
                    String member = objectFiles.get(archiveIndex);
                    archiveIndex++;
                    currentDwarf = new Dwarf(member);
                    toDispose.add(currentDwarf.magic);
                    currentList = currentDwarf.iteratorCompilationUnits();
                    if (!currentList.hasNext()) {
                        continue;
                    }
                } else {
                    currentList = null;
                    return;
                }
            }
        }
    }

    private class ArchiveIterator implements Iterator<CompilationUnit>{
        private int archiveIndex = 0;
        private RandomAccessFile reader;
        private Iterator<CompilationUnit> currentIterator;

        public ArchiveIterator(RandomAccessFile reader) throws IOException {
            this.reader = reader;
            advanceArchive();
        }

        @Override
        public boolean hasNext() {
            if (currentIterator == null) {
                return false;
            }
            if (currentIterator.hasNext()) {
                return true;
            }
            try {
                advanceArchive();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
            return currentIterator != null;
        }

        @Override
        public CompilationUnit next() {
            return currentIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advanceArchive() throws IOException {
            while (true) {
                if (archiveIndex < offsets.size()) {
                    MemberHeader member = offsets.get(archiveIndex);
                    archiveIndex++;
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
                    currentIterator = iteratorFileCompilationUnits();
                    if (!currentIterator.hasNext()) {
                        continue;
                    }
                } else {
                    currentIterator = null;
                    return;
                }
            }
        }
    }
}
