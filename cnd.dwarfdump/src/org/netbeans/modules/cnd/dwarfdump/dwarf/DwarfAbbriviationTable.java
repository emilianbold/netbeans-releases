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

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfAbbriviationTable {
    private ArrayList<DwarfAbbriviationTableEntry> entries = null;
    private int numOfEntries = 0;
    private long offset;

    public DwarfAbbriviationTable(long offset) {
        this.offset = offset;
    }
    
    public void setEntries(ArrayList<DwarfAbbriviationTableEntry> entries) {
        this.entries = entries;
        this.numOfEntries = entries.size();
    }
    
    public DwarfAbbriviationTableEntry getEntry(long idx) {
        return (idx > 0 && idx <= numOfEntries) ? entries.get((int)idx - 1) : null;
    }
    
    public int size() {
        return numOfEntries;
    }
    
    public void dump(PrintStream out) {
        out.println("  Number TAG"); // NOI18N
        
        for (DwarfAbbriviationTableEntry entry : entries) {
            entry.dump(out);
        }
    }
}
