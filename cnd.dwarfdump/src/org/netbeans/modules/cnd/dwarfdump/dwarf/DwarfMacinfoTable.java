/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import java.io.PrintStream;
import java.util.ArrayList;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;

/**
 *
 * @author ak119685
 */
public class DwarfMacinfoTable {
    private long baseSourceTableOffset = -1;
    private long fileSourceTableOffset = -1;
    private DwarfMacroInfoSection section;
    ArrayList<DwarfMacinfoEntry> baseSourceTable = new ArrayList<DwarfMacinfoEntry>();
    ArrayList<DwarfMacinfoEntry> fileSourceTable = new ArrayList<DwarfMacinfoEntry>();
    private boolean baseSourceTableRead;
    private boolean fileSourceTableRead;
    
    public DwarfMacinfoTable(DwarfMacroInfoSection section, long offset) {
        this.section = section;
        this.baseSourceTableRead = false;
        this.fileSourceTableRead = false;
        baseSourceTableOffset = fileSourceTableOffset = offset;
    }
    
    public void addEntry(DwarfMacinfoEntry entry) {
        if (entry.fileIdx == -1) {
            baseSourceTable.add(entry);
        } else {
            fileSourceTable.add(entry);
        }
    }
    
    private ArrayList<DwarfMacinfoEntry> getBaseSourceTable() {
        
        if (baseSourceTableRead) {
            return baseSourceTable;
        }
        
        try {
            readBaseSourceTable();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return baseSourceTable;
    }
    
    private ArrayList<DwarfMacinfoEntry> getFileSourceTable() {
        if (fileSourceTableRead) {
            return fileSourceTable;
        }
        
        try {
            readFileSourceTable();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return fileSourceTable;
    }
    
    private void readBaseSourceTable() throws IOException {
        long length = section.readMacinfoTable(this, baseSourceTableOffset, true);
        fileSourceTableOffset = baseSourceTableOffset + length;
        baseSourceTableRead = true;
    }
    
    private void readFileSourceTable() throws IOException {
        long length = section.readMacinfoTable(this, fileSourceTableOffset, false);
        fileSourceTableRead = true;
    }
    
    public ArrayList<DwarfMacinfoEntry> getCommandLineMarcos() {
        ArrayList<DwarfMacinfoEntry> entries = getBaseSourceTable();
        int size = entries.size();
        
        if (size == 0 || entries.get(0).lineNum == 0) {
            return entries;
        }
        
        ArrayList<DwarfMacinfoEntry> result = new ArrayList<DwarfMacinfoEntry>();
        int idx = 0;
        int currLine = entries.get(0).lineNum;
        int prevLine = -1;
        
        // Skip non-command-line entries... 
        
        while (currLine > prevLine && idx < size) {
            prevLine = currLine;
            if (idx == size -1){
                return result;
            }
            currLine = entries.get(++idx).lineNum;
        }
                
        DwarfMacinfoEntry entry = entries.get(idx);

        do {
            result.add(entry);
            currLine = entry.lineNum;
            idx++;
        } while (idx < size && (entry = entries.get(idx)).lineNum - currLine == 1);
        
        return result;
    }
    
    public ArrayList<String> getCommandLineDefines() {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<DwarfMacinfoEntry> macros = getCommandLineMarcos();
        
        for (DwarfMacinfoEntry macro : macros) {
            result.add(macro.definition.replaceFirst(" ", "=")); // NOI18N
        }
        
        return result;
    }
    
    public ArrayList<DwarfMacinfoEntry> getMacros(int fileIdx) {
        ArrayList<DwarfMacinfoEntry> result = new ArrayList<DwarfMacinfoEntry>();
        
        for (DwarfMacinfoEntry entry : getFileSourceTable()) {
            if (entry.fileIdx == fileIdx && (entry.type.equals(MACINFO.DW_MACINFO_define) || entry.type.equals(MACINFO.DW_MACINFO_undef))) {
                result.add(entry);
            }
        }
        
        return result;
    }
        
    public void dump(PrintStream out) {
        out.printf("\nMACRO Table (offset = %d [0x%08x]):\n\n", baseSourceTableOffset, baseSourceTableOffset); // NOI18N
        
        for (DwarfMacinfoEntry entry : getBaseSourceTable()) {
            entry.dump(out);
        }
        
        for (DwarfMacinfoEntry entry : getFileSourceTable()) {
            entry.dump(out);
        }
    }
    
}
