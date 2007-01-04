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

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfMacinfoTable {
    private long offset;
    ArrayList<DwarfMacinfoEntry> table = new ArrayList<DwarfMacinfoEntry>();
    
    public DwarfMacinfoTable(long offset) {
        this.offset = offset;
    }
    
    public void addEntry(DwarfMacinfoEntry entry) {
        table.add(entry);
    }
    
    public ArrayList<DwarfMacinfoEntry> getCommandLineMarcos() {
        return getMacros(-1);
    }
    
    public ArrayList<String> getCommandLineDefines() {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<DwarfMacinfoEntry> macros = getCommandLineMarcos();
        
        for (DwarfMacinfoEntry macro : macros) {
            result.add(macro.definition.replaceFirst(" ", "="));
        }
        
        return result;
    }

    public ArrayList<DwarfMacinfoEntry> getMacros() {
        return table;
    }
    
    public ArrayList<DwarfMacinfoEntry> getMacros(int fileIdx) {
        ArrayList<DwarfMacinfoEntry> result = new ArrayList<DwarfMacinfoEntry>();
        
        for (DwarfMacinfoEntry entry : table) {
            if (entry.fileIdx == fileIdx && (entry.type.equals(MACINFO.DW_MACINFO_define) || entry.type.equals(MACINFO.DW_MACINFO_undef))) {
                result.add(entry);
            }
        }
        
        return result;
    }
    
    public void dump(PrintStream out) {
        for (DwarfMacinfoEntry entry : table) {
            entry.dump(out);
        }
    }    
    
}
