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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import org.netbeans.modules.cnd.dwarfdump.section.FileEntry;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfStatementList {
    public long total_length;
    public int version;
    public long prologue_length;
    public int minimum_instruction_length;
    public int default_is_stmt;
    public int line_base;
    public int line_range;
    public int opcode_base;
    public long[] standard_opcode_lengths;
    public ArrayList<String> includeDirs = new ArrayList<String>();
    public ArrayList<FileEntry> fileEntries = new ArrayList<FileEntry>();    
    
    private long offset;
    

    /** Creates a new instance of DwarfStatementList */
    public DwarfStatementList(long offset) {
        this.offset = offset;
    }

    public ArrayList<String> getIncludeDirectories() {
        return includeDirs;
    }
    
    public ArrayList<FileEntry> getFileEntries() {
        return fileEntries;
    }
    
    public void dump(PrintStream out) {
        out.printf("  %-28s %s\n", "Length:", total_length);
        out.printf("  %-28s %s\n", "DWARF Version:", version);
        out.printf("  %-28s %s\n", "Prologue Length:", prologue_length);
        out.printf("  %-28s %s\n", "Minimum Instruction Length:", minimum_instruction_length);
        out.printf("  %-28s %s\n", "Initial value of 'is_stmt':", default_is_stmt);
        out.printf("  %-28s %s\n", "Line Base:", line_base);
        out.printf("  %-28s %s\n", "Line Range:", line_range);
        out.printf("  %-28s %s\n", "Opcode Base:", opcode_base);
        
        out.println("\n Opcodes:");
        
        for (int i = 0; i < standard_opcode_lengths.length; i++) {
            out.printf("  Opcode %d has %d args\n", i + 1, standard_opcode_lengths[i]);
        }
        
        out.println("\n The Directory Table:");
        
        for (String includeDir : includeDirs) {
            out.println("  " + includeDir);
        }
        
        out.println("\n The File Name Table:");
        out.println("Entry\tDir\tTime\tSize\tName");

        int idx = 0;
        for (FileEntry fileEntry : fileEntries) {
            out.printf("  %d\t%d\t%d\t%d\t%s\n", ++idx, fileEntry.dirIndex, fileEntry.modifiedTime, fileEntry.fileSize, fileEntry.fileName);
        }
    }
    
    public int getDirectoryIndex(String dirname) {
        int idx = 0;
        
        if (dirname == null) {
            return 0;
        }
                
        for (String dir : includeDirs) {
            idx++;
            if (dir.equals(dirname)) {
                return idx;
            }
        }
        
        return 0;
    }

    public int getFileEntryIdx(String fileName) {
        File file = new File(fileName);
        String dirname = file.getParent();
        String fname = file.getName();
        return getFileEntryIdx(getDirectoryIndex(dirname), fname);
    }

    private int getFileEntryIdx(int dirIdx, String fname) {
        int idx = 0;
        
        for (FileEntry fileEntry : fileEntries) {
            idx++;
            if (fileEntry.dirIndex == dirIdx && fileEntry.fileName.equals(fname)) { 
                return idx;
            }
        }
        
        return 0;
    }
    
    public String getFilePath(int idx) {
        FileEntry fileEntry = fileEntries.get(idx - 1);
        
        if (fileEntry == null) {
            return null;
        }

        String result;
        
        if (fileEntry.dirIndex == 0) {
            result = "." + File.separator + fileEntry.fileName;
        } else {
            result = includeDirs.get((int)fileEntry.dirIndex - 1) + File.separator + fileEntry.fileName;
        }
        
        return result;
    }

    public ArrayList<String> getPathsForFile(String fname) {
        ArrayList<String> result = new ArrayList<String>();
        
        for (FileEntry fileEntry : fileEntries) {
            if (fileEntry.fileName.equals(fname)) {
                String dir = (fileEntry.dirIndex == 0) ? "." : includeDirs.get(fileEntry.dirIndex - 1);
                result.add(dir);
            }
        }
        
        return result;
    }
}
