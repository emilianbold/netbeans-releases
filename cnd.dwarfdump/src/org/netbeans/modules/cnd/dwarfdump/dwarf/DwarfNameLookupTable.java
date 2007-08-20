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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ak119685
 */
public class DwarfNameLookupTable {
    public long unit_length;
    public int  version;
    public long debug_info_offset;
    public long debug_info_length;

    public HashMap<Long, String> entries = new HashMap<Long, String>();
    
    public void dump(PrintStream out) {
        out.println("\nPublic names:\n"); // NOI18N
        out.printf("  %-40s %s\n", "Length:", unit_length); // NOI18N
        out.printf("  %-40s %s\n", "Version:", version); // NOI18N
        out.printf("  %-40s %s\n", "Offset into .debug_info section:", debug_info_offset); // NOI18N
        out.printf("  %-40s %s\n", "Size of area in .debug_info section:", debug_info_length); // NOI18N
        out.println("\n    Offset      Name"); // NOI18N
        
        
        for (Map.Entry<Long, String> entry : entries.entrySet()) {
            long offset = entry.getKey();
            out.printf("%d (0x%x)\t%s\n", offset, offset, entry.getValue()); // NOI18N
        }
    }
    
    public void addEntry(long offset, String name) {
        entries.put(offset, name);
    }
    
    public String getName(long offset) {
        return entries.get(offset);
    }
    
}
